package lithium.openstud.driver.core.providers.sapienza;

import lithium.openstud.driver.core.Openstud;
import lithium.openstud.driver.core.OpenstudHelper;
import lithium.openstud.driver.core.internals.ExamHandler;
import lithium.openstud.driver.core.models.*;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import lithium.openstud.driver.exceptions.OpenstudRefreshException;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class SapienzaExamHandler implements ExamHandler {
    private Openstud os;

    public SapienzaExamHandler(Openstud os) {
        this.os = os;
    }

    @Override
    public List<ExamDoable> getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        List<ExamDoable> exams;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                exams = _getExamsDoable();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    if (e.isMaintenance()) throw e;
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return exams;
    }

    private List<ExamDoable> _getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(String.format("%s/studente/%s/insegnamentisostenibili?ingresso=%s", os.getEndpointAPI(), os.getStudentID(), os.getToken())).build();
            JSONObject response = handleRequest(req);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            List<ExamDoable> list = new LinkedList<>();
            if (!response.has("esami") || response.isNull("esami")) return list;
            JSONArray array = response.getJSONArray("esami");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ExamDoable exam = new ExamDoable();
                for (String element : obj.keySet()) {
                    switch (element) {
                        case "codiceInsegnamento":
                            exam.setExamCode(obj.getString("codiceInsegnamento"));
                            break;
                        case "codiceModuloDidattico":
                            exam.setModuleCode(obj.getString("codiceModuloDidattico"));
                            break;
                        case "codiceCorsoInsegnamento":
                            exam.setCourseCode(obj.getString("codiceCorsoInsegnamento"));
                            break;
                        case "cfu":
                            exam.setCfu(obj.getInt("cfu"));
                            break;
                        case "descrizione":
                            exam.setDescription(obj.getString("descrizione"));
                            break;
                        case "ssd":
                            exam.setSsd(obj.getString("ssd"));
                            break;
                        default:
                            break;
                    }
                }
                list.add(exam);
            }
            return list;
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
    public List<ExamDone> getExamsDone() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        List<ExamDone> exams;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                exams = _getExamsDone();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    if (e.isMaintenance()) throw e;
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return OpenstudHelper.sortExamByDate(exams, false);
    }

    private List<ExamDone> _getExamsDone() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(String.format("%s/studente/%s/esami?ingresso=%s", os.getEndpointAPI(), os.getStudentID(), os.getToken())).build();
            JSONObject response = handleRequest(req);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            List<ExamDone> list = new LinkedList<>();
            if (!response.has("esami") || response.isNull("esami")) return list;
            JSONArray array = response.getJSONArray("esami");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ExamDone exam = new ExamDone();
                for (String element : obj.keySet()) {
                    switch (element) {
                        case "codiceInsegnamento":
                            exam.setExamCode(obj.getString("codiceInsegnamento"));
                            break;
                        case "cfu":
                            exam.setCfu(obj.getInt("cfu"));
                            break;
                        case "descrizione":
                            exam.setDescription(obj.getString("descrizione"));
                            break;
                        case "ssd":
                            exam.setSsd(obj.getString("ssd"));
                            break;
                        case "data":
                            if (obj.isNull("data")) break;
                            String dateBirth = obj.getString("data");
                            if (dateBirth.isEmpty()) break;
                            try {
                                exam.setDate(LocalDate.parse(dateBirth, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "certificato":
                            exam.setCertified(obj.getBoolean("certificato"));
                            break;
                        case "superamento":
                            exam.setPassed(obj.getBoolean("superamento"));
                            break;
                        case "annoAcca":
                            exam.setYear(obj.getInt("annoAcca"));
                            break;
                        case "esito":
                            JSONObject esito = obj.getJSONObject("esito");
                            if (esito.has("valoreNominale")) exam.setNominalResult(esito.getString("valoreNominale"));
                            if (esito.has("valoreNonNominale") && !esito.isNull("valoreNonNominale"))
                                exam.setResult(esito.getInt("valoreNonNominale"));
                            break;
                        default:
                            break;
                    }
                }
                list.add(exam);
            }
            return list;
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
    public List<ExamReservation> getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        List<ExamReservation> reservations;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                reservations = _getActiveReservations();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    if (e.isMaintenance()) throw e;
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return reservations;
    }

    private List<ExamReservation> _getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(String.format("%s/studente/%s/prenotazioni?ingresso=%s", os.getEndpointAPI(), os.getStudentID(), os.getToken())).build();
            JSONObject response = handleRequest(req);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            if (!response.has("appelli") || response.isNull("appelli"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. Maybe the server is not working");
            JSONArray array = response.getJSONArray("appelli");
            return OpenstudHelper.sortReservationByDate(SapienzaHelper.extractReservations(os, array), true);
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
    public List<ExamReservation> getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        List<ExamReservation> reservations;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                reservations = _getAvailableReservations(exam, student);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    if (e.isMaintenance()) throw e;
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return reservations;
    }

    private List<ExamReservation> _getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(String.format("%s/appello/ricerca?ingresso=%s&tipoRicerca=%s&criterio=%s&codiceCorso=%s&annoAccaAuto=%s", os.getEndpointAPI(), os.getToken(), 4, exam.getModuleCode(), exam.getCourseCode(), student.getAcademicYearCourse())).build();
            JSONObject response = handleRequest(req);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            if (!response.has("appelli") || response.isNull("appelli")) return new LinkedList<>();
            JSONArray array = response.getJSONArray("appelli");
            return SapienzaHelper.extractReservations(os, array);
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
    public Pair<Integer, String> insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        Pair<Integer, String> pr;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                pr = _insertReservation(res);
                if (pr == null) {
                    if (!(++count == os.getMaxTries())) continue;
                }
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return pr;
    }

    private ImmutablePair<Integer, String> _insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            RequestBody reqbody = RequestBody.create(new byte[]{}, null);
            Request req = new Request.Builder().url(String.format("%s/prenotazione/%s/%s/%s?ingresso=%s", os.getEndpointAPI(), res.getReportID(), res.getSessionID(), res.getCourseCode(), os.getToken())).post(reqbody).build();
            JSONObject response = handleRequest(req);
            String url = null;
            int flag = -1;
            String nota = null;
            if (response.has("esito")) {
                if (response.getJSONObject("esito").has("flagEsito")) {
                    flag = response.getJSONObject("esito").getInt("flagEsito");
                }
                if (response.getJSONObject("esito").has("nota")) {
                    if (!response.getJSONObject("esito").isNull("nota"))
                        nota = response.getJSONObject("esito").getString("nota");
                }
            } else throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            if (!response.isNull("url") && response.has("url")) url = response.getString("url");
            if (url == null && flag != 0 && (nota == null || !nota.contains("già prenotato"))) return null;
            return new ImmutablePair<>(flag, url);
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

    private JSONObject handleRequest(Request req) throws IOException, OpenstudInvalidResponseException {
        Response resp = os.getClient().newCall(req).execute();
        if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
        String body = resp.body().string();
        resp.close();
        os.log(Level.INFO, body);
        return new JSONObject(body);
    }

    @Override
    public int deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException {
        if (!os.isReady() || res.getReservationNumber() == -1) return -1;
        int count = 0;
        int ret;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                ret = _deleteReservation(res);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return ret;
    }

    private int _deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            Request req = new Request.Builder().url(String.format("%s/prenotazione/%s/%s/%s/%s?ingresso=%s", os.getEndpointAPI(), res.getReportID(), res.getSessionID(),
                    os.getStudentID(), res.getReservationNumber(), os.getToken())).delete().build();
            JSONObject response = handleRequest(req);
            int flag = -1;
            if (response.has("esito")) {
                if (response.getJSONObject("esito").has("flagEsito")) {
                    flag = response.getJSONObject("esito").getInt("flagEsito");
                }
            } else throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            return flag;
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
    public byte[] getPdf(ExamReservation reservation) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady() || reservation == null) return null;
        int count = 0;
        byte[] pdf;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                pdf = _getPdf(reservation);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return pdf;
    }

    private byte[] _getPdf(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            Request req = new Request.Builder().url(String.format("%s/prenotazione/%s/%s/%s/pdf?ingresso=%s", os.getEndpointAPI(), res.getReportID(), res.getSessionID(), os.getStudentID(), os.getToken())).build();
            JSONObject response = handleRequest(req);
            if (!response.has("risultato") || response.isNull("risultato"))
                throw new OpenstudInvalidResponseException("Infostud answer is not valid, maybe the token is no longer valid");
            response = response.getJSONObject("risultato");
            if (!response.has("byte") || response.isNull("byte"))
                throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            JSONArray byteArray = response.getJSONArray("byte");
            byte[] pdf = new byte[byteArray.length()];
            for (int i = 0; i < byteArray.length(); i++) pdf[i] = (byte) byteArray.getInt(i);
            os.log(Level.INFO, "Found PDF made of " + pdf.length + " bytes \n");
            return pdf;
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
    public List<Event> getCalendarEvents(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                List<ExamDoable> exams = _getExamsDoable();
                List<ExamReservation> reservations = _getActiveReservations();
                List<ExamReservation> avaiableReservations = new LinkedList<>();
                for (ExamDoable exam : exams) {
                    avaiableReservations.addAll(_getAvailableReservations(exam, student));
                }
                return OpenstudHelper.generateEvents(reservations, avaiableReservations);
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    if (e.isMaintenance()) throw e;
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
    }

    @Override
    public String getCourseSurvey(String surveyCode) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                return _getCourseSurvey(surveyCode);
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    if (e.isMaintenance()) throw e;
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                os.log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
    }

    private String _getCourseSurvey(String surveyCode) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            Request req = new Request.Builder().url(String.format("%s/opis/token/info/%s/%s?ingresso=%s", os.getEndpointAPI(), os.getStudentID(), surveyCode.trim().toUpperCase(), os.getToken())).build();
            JSONObject response = handleRequest(req);
            if (!response.has("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            if (response.isNull("risultato")) return null;
            return response.getString("risultato");
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
