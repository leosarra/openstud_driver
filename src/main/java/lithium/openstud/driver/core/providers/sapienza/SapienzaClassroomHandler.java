package lithium.openstud.driver.core.providers.sapienza;

import lithium.openstud.driver.core.Openstud;
import lithium.openstud.driver.core.OpenstudHelper;
import lithium.openstud.driver.core.internals.ClassroomHandler;
import lithium.openstud.driver.core.models.Classroom;
import lithium.openstud.driver.core.models.ExamDoable;
import lithium.openstud.driver.core.models.Lesson;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SapienzaClassroomHandler implements ClassroomHandler {
    private Openstud os;

    public SapienzaClassroomHandler(Openstud os) {
        this.os = os;
    }

    @Override
    public List<Classroom> getClassRoom(String query, boolean withTimetable) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        if (!os.isReady()) return null;
        int count = 0;
        while (true) {
            try {
                return _getClassroom(query, withTimetable);
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) throw e;
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private List<Classroom> _getClassroom(String query, boolean withTimetable) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        List<Classroom> ret = new LinkedList<>();
        try {
            Request req = new Request.Builder().url(String.format("%s/classroom/search?q=%s", os.getEndpointTimetable(), query.replace(" ", "%20"))).build();
            String body = handleRequest(req);
            JSONArray array = new JSONArray(body);
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime zonedTime = now.atOffset(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.of("+1")).toLocalDateTime();
            for (int i = 0; i < array.length(); i++) {
                if (i == os.getLimitSearch()) break;
                JSONObject object = array.getJSONObject(i);
                Classroom classroom = parseClassroom(object);
                if (withTimetable) {
                    List<Lesson> classLessons = getClassroomTimetable(classroom.getInternalId(), LocalDate.now());
                    for (Lesson lesson : classLessons) {
                        if (lesson.getStart().isBefore(zonedTime) && lesson.getEnd().isAfter(zonedTime))
                            classroom.setLessonNow(lesson);
                        else if (lesson.getStart().isAfter(zonedTime)) {
                            classroom.setNextLesson(lesson);
                            break;
                        }
                    }
                    classroom.setTodayLessons(classLessons);
                    Thread.sleep(os.getWaitTimeClassroomRequest());
                }
                ret.add(classroom);
            }
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private String handleRequest(Request req) throws IOException, OpenstudInvalidResponseException {
        Response resp = os.getClient().newCall(req).execute();
        if (resp.body() == null) throw new OpenstudInvalidResponseException("GOMP answer is not valid");
        String body = resp.body().string();
        resp.close();
        if (body.contains("maximum request limit"))
            throw new OpenstudInvalidResponseException("Request rate limit reached").setRateLimitType();
        os.log(Level.INFO, body);
        return body;
    }

    private Classroom parseClassroom(JSONObject object) {
        Classroom classroom = new Classroom();
        for (String info : object.keySet()) {
            if (object.isNull(info)) continue;
            switch (info) {
                case "roominternalid":
                    classroom.setInternalId(object.getInt(info));
                    break;
                case "fullname":
                    classroom.setFullName(object.getString(info));
                    break;
                case "name":
                    classroom.setName(object.getString(info));
                    break;
                case "site":
                    classroom.setWhere(object.getString(info));
                    break;
                case "lat":
                    classroom.setLatitude(object.getDouble(info));
                    break;
                case "lng":
                    classroom.setLongitude(object.getDouble(info));
                    break;
                case "occupied":
                    classroom.setOccupied(object.getBoolean(info));
                    break;
                case "willbeoccupied":
                    classroom.setWillBeOccupied(object.getBoolean(info));
                    break;
                case "weight":
                    classroom.setWeight(object.getInt(info));
                    break;
                default:
                    break;
            }
        }
        return classroom;
    }

    @Override
    public List<Lesson> getClassroomTimetable(Classroom room, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (room == null) return new LinkedList<>();
        return getClassroomTimetable(room.getInternalId(), date);
    }

    @Override
    public List<Lesson> getClassroomTimetable(int id, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (!os.isReady()) return null;
        int count = 0;
        while (true) {
            try {
                return _getClassroomTimetable(id, date);
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) throw e;
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private List<Lesson> _getClassroomTimetable(int id, LocalDate date) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        List<Lesson> ret = new LinkedList<>();
        try {
            Request req = new Request.Builder().url(String.format("%s/events/%s/%s/%s/%s", os.getEndpointTimetable(), date.getYear(), date.getMonthValue(), date.getDayOfMonth(), id)).build();
            String body = handleRequest(req);
            JSONArray array = new JSONArray(body);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                ret.add(SapienzaHelper.extractLesson(object, formatter, false));
            }
            return OpenstudHelper.sortLessonsByStartDate(ret, true);

        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    @Override
    public Map<String, List<Lesson>> getTimetable(List<ExamDoable> exams) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        if (!os.isReady()) return null;
        int count = 0;
        Map<String, List<Lesson>> ret;
        while (true) {
            try {
                ret = _getTimetable(exams);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) throw e;
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
        return ret;
    }

    private Map<String, List<Lesson>> _getTimetable(List<ExamDoable> exams) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        Map<String, List<Lesson>> ret = new HashMap<>();
        if (exams.isEmpty()) return ret;
        try {
            StringBuilder builderExams = new StringBuilder();
            boolean first = true;
            for (ExamDoable exam : exams) {
                if (!first)
                    builderExams.append(",");
                first = false;
                builderExams.append(exam.getExamCode());
            }
            String codes = builderExams.toString();
            Request req = new Request.Builder().url(String.format("%s/lectures/%s", os.getEndpointTimetable(), builderExams.toString())).build();
            String body = handleRequest(req);
            JSONObject response = new JSONObject(body);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            for (String examCode : response.keySet()) {
                if (!codes.contains(examCode)) continue;
                JSONArray array = response.getJSONArray(examCode);
                LinkedList<Lesson> lessons = new LinkedList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    lessons.add(SapienzaHelper.extractLesson(object, formatter, false));
                }
                ret.put(examCode, lessons);
            }
            return ret;

        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }
}
