package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.*;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Openstud implements Authenticator, Personal, NewsHandler, TaxHandler, ClassroomHandler {
    private int maxTries;
    private String endpointAPI;
    private String endpointTimetable;
    private String endpointNews;
    private volatile String token;
    private String studentPassword;
    private String studentID;
    private boolean isReady;
    private Logger logger;
    private OkHttpClient client;
    private String key;
    private int waitTimeClassroomRequest;
    private int limitSearch;

    private OpenAuthenticator authenticator;
    private OpenPersonal personal;
    private OpenNewsHandler newsHandler;
    private OpenTaxHandler taxHandler;
    private OpenClassroomHandler classroomHandler;

    public Openstud() {
        super();
    }

    Openstud(OpenstudHelper.Mode mode, String webEndpoint, String endpointTimetable, String newsEndpoint, String studentID, String studentPassword,
             Logger logger, int retryCounter, int connectionTimeout,
             int readTimeout, int writeTimeout, boolean readyState, int waitTimeClassroomRequest, int limitSearch) {
        this.maxTries = retryCounter;
        this.endpointAPI = webEndpoint;
        this.studentID = studentID;
        this.endpointTimetable = endpointTimetable;
        this.endpointNews = newsEndpoint;
        this.studentPassword = studentPassword;
        this.logger = logger;
        this.isReady = readyState;
        this.waitTimeClassroomRequest = waitTimeClassroomRequest;
        this.limitSearch = limitSearch;
        if (mode == OpenstudHelper.Mode.WEB) key = "1nf0r1cc1";
        else key = "r4g4zz3tt1";
        client = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS).retryOnConnectionFailure(true)
                .build();
        authenticator = new OpenAuthenticator(this);
        personal = new OpenPersonal(this);
        newsHandler = new OpenNewsHandler(this);
        taxHandler = new OpenTaxHandler(this);
        classroomHandler = new OpenClassroomHandler(this);
    }

    String getPassword() {
        return studentPassword;
    }

    String getStudentID(){
        return studentID;
    }

    String getEndpointAPI()
    {
        return endpointAPI;
    }

    String getEndpointTimetable()
    {
        return endpointTimetable;
    }

    public String getEndpointNews()
    {
        return endpointNews;
    }

    String getStudentPassword()
    {
        return studentPassword;
    }

    Logger getLogger()
    {
        return logger;
    }

    OkHttpClient getClient()
    {
        return client;
    }

    String getKey()
    {
        return key;
    }

    int getWaitTimeClassroomRequest()
    {
        return waitTimeClassroomRequest;
    }

    int getLimitSearch()
    {
        return limitSearch;
    }

    void setStudentPassword(String password)
    {
        studentPassword = password;
    }

    void setReady(boolean isReady)
    {
        this.isReady = isReady;
    }

    int getMaxTries(){
        return maxTries;
    }

    void setToken(String token) {
        this.token = token;
    }

    synchronized String getToken() {
        return this.token;
    }

    void log(Level lvl, String str) {
        if (logger != null) logger.log(lvl, str);
    }

    void log(Level lvl, Object obj) {
        if (logger != null) logger.log(lvl, obj.toString());
    }

    public boolean isReady() {
        return isReady;
    }

    synchronized void refreshToken() throws OpenstudRefreshException, OpenstudInvalidResponseException {
        try {
            if (!StringUtils.isNumeric(studentID)) throw new OpenstudRefreshException("Student ID is not valid");
            RequestBody formBody = new FormBody.Builder()
                    .add("key", key).add("matricola", String.valueOf(studentID)).add("stringaAutenticazione", studentPassword).build();
            Request req = new Request.Builder().url(endpointAPI + "/autenticazione").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) return;
            String body = resp.body().string();
            if (body.contains("the page you are looking for is currently unavailable")) throw new OpenstudInvalidResponseException("InfoStud is in maintenance").setMaintenanceType();
            JSONObject response = new JSONObject(body);
            if (!response.has("output") || response.getString("output").isEmpty()) return;
            setToken(response.getString("output"));
            if (response.has("esito")) {
                switch (response.getJSONObject("esito").getInt("flagEsito")) {
                    case -4:
                        throw new OpenstudRefreshException("Invalid credentials when refreshing token");
                    case -2:
                        throw new OpenstudRefreshException("Password expired").setPasswordExpiredType();
                    case -1:
                        throw new OpenstudRefreshException("Invalid credentials when refreshing token");
                    case 0:
                        break;
                    default:
                        throw new OpenstudInvalidResponseException("Infostud is not working as intended");
                }
            }
        } catch (IOException | JSONException e) {
            log(Level.SEVERE, e);
            e.printStackTrace();
        }
    }

    @Override
    public void login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        authenticator.login();
    }

    @Override
    public String getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        return authenticator.getSecurityQuestion();
    }

    @Override
    public int recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        return authenticator.recoverPassword(answer);
    }

    @Override
    public void resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        authenticator.resetPassword(new_password);
    }

    @Override
    public int recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        return authenticator.recoverPasswordWithEmail(email, answer);
    }

    @Override
    public Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        return personal.getInfoStudent();
    }

    public List<Event> getCalendarEvents(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                List<ExamDoable> exams = _getExamsDoable();
                List<ExamReservation> reservations = _getActiveReservations();
                List<ExamReservation> avaiableReservations = new LinkedList<>();
                for (ExamDoable exam : exams) {
                   avaiableReservations.addAll(_getAvailableReservations(exam, student));
                }
                return OpenstudHelper.generateEvents(reservations, avaiableReservations);
            }catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    if (e.isMaintenance()) throw e;
                    log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
    }

    public List<ExamDoable> getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        List<ExamDoable> exams;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                exams = _getExamsDoable();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    if (e.isMaintenance()) throw e;
                    log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return exams;
    }

    private List<ExamDoable> _getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/studente/" + studentID + "/insegnamentisostenibili?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
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
                    }
                }
                list.add(exam);
            }
            return list;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    public List<ExamDone> getExamsDone() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        List<ExamDone> exams;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                exams = _getExamsDone();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    if (e.isMaintenance()) throw e;
                    log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return OpenstudHelper.sortExamByDate(exams, false);
    }

    private List<ExamDone> _getExamsDone() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/studente/" + studentID + "/esami?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
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
                    }
                }
                list.add(exam);
            }
            return list;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    public List<ExamReservation> getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        List<ExamReservation> reservations;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                reservations = _getActiveReservations();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    if (e.isMaintenance()) throw e;
                    log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return reservations;
    }

    private List<ExamReservation> _getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/studente/" + studentID + "/prenotazioni?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            if (!response.has("appelli") || response.isNull("appelli")) throw new OpenstudInvalidResponseException("Infostud response is not valid. Maybe the server is not working");
            JSONArray array = response.getJSONArray("appelli");
            return OpenstudHelper.sortReservationByDate(OpenstudHelper.extractReservations(array),true);
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }


    public List<ExamReservation> getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        List<ExamReservation> reservations;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                reservations = _getAvailableReservations(exam, student);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    if (e.isMaintenance()) throw e;
                    log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return reservations;
    }

    private List<ExamReservation> _getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/appello/ricerca?ingresso=" + getToken() + "&tipoRicerca=" + 4 + "&criterio=" + exam.getModuleCode() +
                    "&codiceCorso=" + exam.getCourseCode() + "&annoAccaAuto=" + student.getAcademicYearCourse()).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            if (!response.has("appelli") || response.isNull("appelli")) return new LinkedList<>();
            JSONArray array = response.getJSONArray("appelli");
            return OpenstudHelper.extractReservations(array);
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    public Pair<Integer, String> insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        Pair<Integer, String> pr;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                pr = _insertReservation(res);
                if (pr == null) {
                    if (!(++count == maxTries)) continue;
                }
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return pr;
    }

    private ImmutablePair<Integer, String> _insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            RequestBody reqbody = RequestBody.create(null, new byte[]{});
            Request req = new Request.Builder().url(endpointAPI + "/prenotazione/" + res.getReportID() + "/" + res.getSessionID()
                    + "/" + res.getCourseCode() + "?ingresso=" + getToken()).post(reqbody).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
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
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    public int deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException {
        if (!isReady() || res.getReservationNumber() == -1) return -1;
        int count = 0;
        int ret;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                ret = _deleteReservation(res);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return ret;
    }

    private int _deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/prenotazione/" + res.getReportID() + "/" + res.getSessionID()
                    + "/" + studentID + "/" + res.getReservationNumber() + "?ingresso=" + getToken()).delete().build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            int flag = -1;
            if (response.has("esito")) {
                if (response.getJSONObject("esito").has("flagEsito")) {
                    flag = response.getJSONObject("esito").getInt("flagEsito");
                }
            } else throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            return flag;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    public byte[] getPdf(ExamReservation reservation) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady() || reservation == null) return null;
        int count = 0;
        byte[] pdf;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                pdf = _getPdf(reservation);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            } catch (OpenstudRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE, invalidCredentials);
                throw invalidCredentials;
            }
        }
        return pdf;
    }

    private byte[] _getPdf(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/prenotazione/" + res.getReportID() + "/" + res.getSessionID() + "/"
                    + studentID + "/pdf?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultato") || response.isNull("risultato"))
                throw new OpenstudInvalidResponseException("Infostud answer is not valid, maybe the token is no longer valid");
            response = response.getJSONObject("risultato");
            if (!response.has("byte") || response.isNull("byte"))
                throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            JSONArray byteArray = response.getJSONArray("byte");
            byte[] pdf = new byte[byteArray.length()];
            for (int i = 0; i < byteArray.length(); i++) pdf[i] = (byte) byteArray.getInt(i);
            log(Level.INFO, "Found PDF made of " + pdf.length + " bytes \n");
            return pdf;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    @Override
    public List<News> getNews(String locale, boolean withDescription, Integer limit, Integer page, Integer maxPage,
                              String query) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        return newsHandler.getNews(locale, withDescription, limit, page, maxPage, query);
    }

    @Override
    public List<Event> getNewsletterEvents() throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        return newsHandler.getNewsletterEvents();
    }

    @Override
    public List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return taxHandler.getUnpaidTaxes();
    }

    @Override
    public List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return taxHandler.getPaidTaxes();
    }

    @Override
    public Isee getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return taxHandler.getCurrentIsee();
    }

    @Override
    public List<Isee> getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return taxHandler.getIseeHistory();
    }

    @Override
    public List<Classroom> getClassRoom(String query, boolean withTimetable) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        return classroomHandler.getClassRoom(query, withTimetable);
    }

    @Override
    public List<Lesson> getClassroomTimetable(Classroom room, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException
    {
        return classroomHandler.getClassroomTimetable(room, date);
    }

    @Override
    public List<Lesson> getClassroomTimetable(int id, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException
    {
        return classroomHandler.getClassroomTimetable(id, date);
    }

    @Override
    public Map<String, List<Lesson>> getTimetable(List<ExamDoable> exams) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        return classroomHandler.getTimetable(exams);
    }
}