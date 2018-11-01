package lithium.openstud.driver.core;

import org.json.JSONArray;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenstudHelper {



    public enum Mode {
        MOBILE, WEB
    }

    private static Logger log;

    public static double computeWeightedAverage(List<ExamDone> list, int laude) {
        double cfu = 0;
        double sum = 0;
        for (ExamDone exam : list) {
            if (exam.isPassed() && exam.getResult() >= 18) {
                int grade = exam.getResult();
                if (grade == 31) grade = laude;
                sum += grade * exam.getCfu();
                cfu += exam.getCfu();
            }
        }
        if (cfu == 0 || sum == 0) return -1;
        return sum / cfu;
    }

    public static double computeArithmeticAverage(List<ExamDone> list, int laude) {
        int num = 0;
        double sum = 0;
        for (ExamDone exam : list) {
            if (exam.isPassed() && exam.getResult() >= 18) {
                int grade = exam.getResult();
                if (grade == 31) grade = laude;
                sum += grade;
                num++;
            }
        }
        if (num == 0 || sum == 0) return -1;
        return sum / num;
    }

    public static int getSumCFU(List<ExamDone> list) {
        int cfu = 0;
        for (ExamDone exam : list) {
            if (exam.isPassed()) {
                cfu += exam.getCfu();
            }
        }
        return cfu;
    }

    public static ExamDone createFakeExamDone(String description, int cfu, int grade) {
        if (cfu <= 0 || grade < 18) return null;
        ExamDone done = new ExamDone();
        done.setCertified(true);
        done.setPassed(true);
        done.setCfu(cfu);
        done.setDate(LocalDate.now());
        done.setDescription(description);
        if (grade >= 31) done.setResult(31);
        else done.setResult(grade);
        return done;
    }

    public static List<Event> generateEventsFromTimetable(Map<String,List<Lesson>> timetable) {
        List<Event> events = new LinkedList<>();
        for (String code : timetable.keySet()) {
            List<Lesson> lessons = timetable.get(code);
            for (Lesson lesson : lessons) {
                Event ev = new Event(lesson.getName(),lesson.getStart(), lesson.getEnd(), EventType.LESSON);
                ev.setTeacher(lesson.getTeacher());
                ev.setWhere(lesson.getWhere());
                events.add(ev);
            }
        }
        return events;
    }

    static List<Event> generateEvents(List<ExamReservation> reservations, List<ExamReservation> avaiableReservations) {
        List<Event> events = new LinkedList<>();
        for (ExamReservation res : reservations) {
            Event ev = new Event(res.getExamSubject(), res.getExamDate().atStartOfDay(), null, EventType.RESERVED);
            ev.setWhere(res.getNote());
            ev.setTeacher(res.getTeacher());
            events.add(ev);
        }
        for (ExamReservation res : avaiableReservations) {
            boolean exist = false;
            for (ExamReservation res_active : reservations) {
                if (res_active.getReportID() == res.getReportID() && res_active.getSessionID() == res.getSessionID()) {
                    exist = true;
                    break;
                }
            }
            if (exist) continue;
            Event event = new Event(res.getExamSubject(), res.getExamDate().atStartOfDay(), null, EventType.DOABLE);
            event.setStartReservations(res.getStartDate());
            event.setEndReservations(res.getEndDate());
            event.setTeacher(res.getTeacher());
            event.setWhere(res.getNote());
            events.add(event);
        }
        return events;
    }

    public static List<ExamDone> sortByDate(List<ExamDone> list, boolean ascending) {
        Collections.sort(list, (o1, o2) -> {
            if (o1.getDate() == null && o2.getDate() == null) return 0;
            if (ascending)
                if (o1.getDate() == null) return 1;
                else if (o2.getDate() == null) return -1;
                else return o1.getDate().compareTo(o2.getDate());
            else {
                if (o1.getDate() == null) return -1;
                else if (o2.getDate() == null) return 1;
                else return o2.getDate().compareTo(o1.getDate());
            }
        });
        return list;
    }

    public static List<Lesson> sortLessonsByStartDate(List<Lesson> list, boolean ascending) {
        Collections.sort(list, (o1, o2) -> {
            if (o1.getStart() == null && o2.getStart()  == null) return 0;
            if (ascending)
                if (o1.getStart()  == null) return 1;
                else if (o2.getStart()  == null) return -1;
                else return o1.getStart().compareTo(o2.getStart());
            else {
                if (o1.getStart() == null) return -1;
                else if (o2.getStart()== null) return 1;
                else return o2.getStart().compareTo(o1.getStart());
            }
        });
        return list;
    }

    public static List<ExamDone> sortByGrade(List<ExamDone> list, boolean ascending) {
        Collections.sort(list, (o1, o2) -> {
            if (ascending)
                return Integer.compare(o1.getResult(), o2.getResult());
            else {
                return Integer.compare(o2.getResult(), o1.getResult());
            }
        });
        return list;
    }

    protected static Lesson extractLesson(JSONObject response, DateTimeFormatter formatter) {
        Lesson lesson = new Lesson();
        for (String lessonInfo : response.keySet()) {
            if (response.isNull(lessonInfo)) continue;
            switch (lessonInfo) {
                case "name":
                    String name = response.getString(lessonInfo);
                    int startIdx = name.indexOf(" ");
                    int endIdx = name.indexOf("Docente:");
                    if (startIdx != -1 && endIdx != -1) {
                        if (name.endsWith(" ")) {
                            name = name.substring(0, name.length() - 1);
                        }
                        lesson.setName(name.substring(startIdx, endIdx).trim());
                    } else lesson.setName(name);
                    int indexTeacher = name.indexOf("Docente:");
                    if (indexTeacher != -1) lesson.setTeacher(name.substring(indexTeacher + "Docente: ".length()));
                    break;
                case "where":
                    lesson.setWhere(response.getString(lessonInfo));
                    break;
                case "start":
                    lesson.setStart(LocalDateTime.parse(response.getString(lessonInfo), formatter));
                    break;
                case "end":
                    lesson.setEnd(LocalDateTime.parse(response.getString(lessonInfo), formatter));
                    break;
            }
        }
        return lesson;
    }

    protected static Isee extractIsee(JSONObject response) {
        Isee res = new Isee();
        for (String element : response.keySet()) {
            switch (element) {
                case "valore":
                    double value = response.getDouble("valore");
                    if (value == -2) return null;
                    res.setValue(response.getDouble("valore"));
                    break;
                case "protocollo":
                    String protocol = response.getString("protocollo");
                    if (protocol == null || protocol.isEmpty()) return null;
                    res.setProtocol(response.getString("protocollo"));
                    break;
                case "modificabile":
                    res.setEditable(response.getInt("modificabile") == 1);
                    break;
                case "dataOperazione":
                    if (response.isNull("dataOperazione")) break;
                    DateTimeFormatter formatterOperation = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String dateOperation = response.getString("dataOperazione");
                    if (!(dateOperation == null || dateOperation.isEmpty())) {
                        try {
                            res.setDateOperation(LocalDate.parse(response.getString("dataOperazione"), formatterOperation));
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "data":
                    DateTimeFormatter formatterDateDeclaration = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    if (response.isNull("data")) return null;
                    String dateDeclaration = response.getString("data");
                    if (!(dateDeclaration == null || dateDeclaration.isEmpty())) {
                        try {
                            res.setDateDeclaration(LocalDate.parse(response.getString("data"), formatterDateDeclaration));
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }

        }
        return res;
    }

    protected static List<PaymentDescription> extractPaymentDescriptionList(JSONArray array, Logger logger) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<PaymentDescription> list = new LinkedList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            PaymentDescription pdes = new PaymentDescription();
            for (String element : obj.keySet()) {
                switch (element) {
                    case "descrizione":
                        pdes.setDescription(obj.getString("descrizione"));
                        break;
                    case "importo":
                        try {
                            Double value = Double.parseDouble(obj.getString("importo"));
                            pdes.setAmount(value);
                        } catch (NumberFormatException e) {
                            logger.log(Level.SEVERE, e.toString());
                        }
                        break;
                    case "annoAccademicoString":
                        pdes.setAcademicYear(obj.getString("annoAccademicoString"));
                        break;
                    case "impoVers":
                        try {
                            Double value = Double.parseDouble(obj.getString("impoVers"));
                            pdes.setAmountPaid(value);
                        } catch (NumberFormatException e) {
                            logger.log(Level.SEVERE, e.toString());
                        }
                        break;
                }
            }
            list.add(pdes);
        }
        return list;
    }

    protected static List<ExamReservation> extractReservations(JSONArray array) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<ExamReservation> list = new LinkedList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            ExamReservation res = new ExamReservation();
            for (String element : obj.keySet()) {
                switch (element) {
                    case "codIdenVerb":
                        res.setReportID(obj.getInt("codIdenVerb"));
                        break;
                    case "canale":
                        res.setChannel(obj.getString("canale"));
                        break;
                    case "codAppe":
                        res.setSessionID(obj.getInt("codAppe"));
                        break;
                    case "codCorsoStud":
                        res.setCourseCode(Integer.parseInt(obj.getString("codCorsoStud")));
                        break;
                    case "descrizione":
                        res.setExamSubject(obj.getString("descrizione"));
                        break;
                    case "descCorsoStud":
                        res.setCourseDescription(obj.getString("descCorsoStud"));
                        break;
                    case "crediti":
                        res.setCfu(obj.getInt("crediti"));
                        break;
                    case "docente":
                        res.setTeacher(obj.getString("docente"));
                        break;
                    case "annoAcca":
                        res.setYearCourse(obj.getString("annoAcca"));
                        break;
                    case "facolta":
                        res.setDepartment(obj.getString("facolta"));
                        break;
                    case "numeroPrenotazione":
                        if (obj.isNull("numeroPrenotazione")) break;
                        res.setReservationNumber(obj.getInt("numeroPrenotazione"));
                        break;
                    case "ssd":
                        if (obj.isNull("ssd")) break;
                        res.setSsd(obj.getString("ssd"));
                        break;
                    case "dataprenotazione":
                        if (obj.isNull("dataprenotazione")) break;
                        String reservationDate = obj.getString("dataprenotazione");
                        if (!(reservationDate == null || reservationDate.isEmpty())) {
                            try {
                                res.setReservationDate(LocalDate.parse(reservationDate, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "note":
                        res.setNote(obj.getString("note"));
                        break;
                    case "dataAppe":
                        String examDate = obj.getString("dataAppe");
                        if (!(examDate == null || examDate.isEmpty())) {
                            try {
                                res.setExamDate(LocalDate.parse(examDate, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "dataInizioPrenotazione":
                        if (obj.isNull("dataInizioPrenotazione")) break;
                        String startDate = obj.getString("dataInizioPrenotazione");
                        if (!(startDate == null || startDate.isEmpty())) {
                            try {
                                res.setStartDate(LocalDate.parse(startDate, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "dataFinePrenotazione":
                        if (obj.isNull("dataFinePrenotazione")) break;
                        String endDate = obj.getString("dataFinePrenotazione");
                        if (!(endDate == null || endDate.isEmpty())) {
                            try {
                                res.setEndDate(LocalDate.parse(endDate, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "SiglaModuloDidattico":
                        if (!obj.isNull("SiglaModuloDidattico")) res.setModule(obj.getString("SiglaModuloDidattico"));
                        break;
                }
            }
            list.add(res);
        }
        return list;
    }

    protected static void setLogger(Logger log) {
        OpenstudHelper.log = log;
    }
}
