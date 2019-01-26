package lithium.openstud.driver.core;


import lithium.openstud.driver.exceptions.*;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Openstud {
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
    }

    private void setToken(String token) {
        this.token = token;
    }

    private synchronized String getToken() {
        return this.token;
    }

    private void log(Level lvl, String str) {
        if (logger != null) logger.log(lvl, str);
    }

    private void log(Level lvl, Object obj) {
        if (logger != null) logger.log(lvl, obj.toString());
    }

    public boolean isReady() {
        return isReady;
    }

    private synchronized void refreshToken() throws OpenstudRefreshException, OpenstudInvalidResponseException {
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

    public String getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        int count = 0;
        if (studentID == null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while (true) {
            try {
                return _getSecurityQuestion();
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private String _getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("matricola", String.valueOf(studentID)).build();
            Request req = new Request.Builder().url(endpointAPI + "/pwd/recuperaDomanda/matricola").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            if (body.contains("the page you are looking for is currently unavailable")) throw new OpenstudInvalidResponseException("InfoStud is in maintenance").setMaintenanceType();
            if (body.contains("Matricola Errata")) throw new OpenstudInvalidCredentialsException("Invalid studentID");
            if (body.contains("Impossibile recuperare la password per email")) return null;
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (response.isNull("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid.");
            return response.getString("risultato");
        } catch (IOException e) {
            log(Level.SEVERE, e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e);
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    public int recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        int count = 0;
        if (studentID == null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while (true) {
            try {
                return _recoverPassword(answer);
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    public void resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        int count = 0;
        if (studentID == null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while (true) {
            try {
                _resetPassword(new_password);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private void _resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("oldPwd", studentPassword)
                    .add("newPwd", new_password)
                    .add("confermaPwd", new_password)   //TODO confirmation should be passed as second param?
                    .build();

            Request req = new Request.Builder().url(endpointAPI + "/pwd/" + studentID + "/reset").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (response.isNull("codiceErrore") || response.isNull("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid.");

            String error_code = response.getString("codiceErrore");
            boolean result = response.getBoolean("risultato");
            if(error_code.equals("000") && result) {
                studentPassword = new_password; // TODO check this
                return;
            }

            throw new OpenstudInvalidCredentialsException("Answer is not correct");

        } catch (IOException e) {
            log(Level.SEVERE, e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e);
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    private int _recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("matricola", String.valueOf(studentID)).add("risposta", answer).build();
            Request req = new Request.Builder().url(endpointAPI + "/pwd/recupera/matricola").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            if (body.contains("the page you are looking for is currently unavailable")) throw new OpenstudInvalidResponseException("InfoStud is in maintenance").setMaintenanceType();
            if (body.contains("Matricola Errata")) throw new OpenstudInvalidCredentialsException("Invalid studentID");
            if (body.contains("Impossibile recuperare la password per email")) return -1;
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (response.isNull("livelloErrore"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid.");
            switch (response.getInt("livelloErrore")) {
                case 3:
                    throw new OpenstudInvalidAnswerException("Answer is not correct");
                case 0:
                    break;
                default:
                    throw new OpenstudInvalidResponseException("Infostud is not working as expected");
            }
        } catch (IOException e) {
            log(Level.SEVERE, e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e);
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
        return 0;
    }

    public int recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        int count=0;
        if (studentID==null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while(true){
            try {
                return _recoverPasswordWithEmail(email, answer);
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            }
        }
    }

    private int _recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("matricola",String.valueOf(studentID)).add("email", email).add("risposta",answer).build();
            Request req = new Request.Builder().url(endpointAPI+"/pwd/recupera/matricola").header("Accept","application/json")
                    .header("Content-EventType","application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            if (body.contains("Matricola Errata")) throw new OpenstudInvalidCredentialsException("Invalid studentID");
            if (body.contains("Impossibile recuperare la password per email")) return -1;
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (response.isNull("livelloErrore"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid.");
            switch (response.getInt("livelloErrore")) {
                case 3:
                    throw new OpenstudInvalidAnswerException("Answer is not correct");
                case 0:
                    break;
                default:
                    throw new OpenstudInvalidResponseException("Infostud is not working as expected");
            }
        } catch (IOException e) {
            log(Level.SEVERE,e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
        return 0;
    }


    public void login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        int count = 0;
        if (studentPassword == null || studentPassword.isEmpty())
            throw new OpenstudInvalidCredentialsException("Password can't be left empty");
        if (studentID == null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while (true) {
            try {
                _login();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private synchronized void _login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        try {
            if (!StringUtils.isNumeric(studentID)) throw new OpenstudInvalidCredentialsException("Student ID is not valid");
            RequestBody formBody = new FormBody.Builder()
                    .add("key", key).add("matricola", String.valueOf(studentID)).add("stringaAutenticazione", studentPassword).build();
            Request req = new Request.Builder().url(endpointAPI + "/autenticazione").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            if (body.contains("the page you are looking for is currently unavailable")) throw new OpenstudInvalidResponseException("InfoStud is in maintenance").setMaintenanceType();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (body.contains("Matricola Errata"))
                throw new OpenstudInvalidCredentialsException("Student ID is not valid");
            else if (!response.has("output"))
                throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            setToken(response.getString("output"));
            if (response.has("esito")) {
                switch (response.getJSONObject("esito").getInt("flagEsito")) {
                    case -4:
                        throw new OpenstudUserNotEnabledException("User is not enabled to use Infostud service.");
                    case -2:
                        throw new OpenstudInvalidCredentialsException("Password expired").setPasswordExpiredType();
                    case -1:
                        throw new OpenstudInvalidCredentialsException("Password not valid");
                    case 0:
                        break;
                    default:
                        throw new OpenstudInvalidResponseException("Infostud is not working as expected");
                }
            }
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
        isReady = true;
    }

    public Isee getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        Isee isee;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                isee = _getCurrentIsee();
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
        return isee;
    }

    public List<Isee> getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        List<Isee> history;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                history = _getIseeHistory();
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
        return history;
    }

    private List<Isee> _getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/contabilita/" + studentID + "/listaIsee?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            List<Isee> list = new LinkedList<>();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultatoLista"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("risultatoLista");
            if (!response.has("risultati") || response.isNull("risultati")) return new LinkedList<>();
            JSONArray array = response.getJSONArray("risultati");
            for (int i = 0; i < array.length(); i++) {
                Isee result = OpenstudHelper.extractIsee(array.getJSONObject(i));
                if (result == null) continue;
                list.add(OpenstudHelper.extractIsee(array.getJSONObject(i)));
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

    private Isee _getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/contabilita/" + studentID + "/isee?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("risultato");
            return OpenstudHelper.extractIsee(response);
        } catch (IOException e) {
            log(Level.SEVERE, e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }


    public Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        Student st;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                st = _getInfoStudent();
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
        return st;
    }

    private Student _getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/studente/" + studentID + "?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            Student st = new Student();
            st.setStudentID(studentID);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (String element : response.keySet()) {
                if (response.isNull(element)) continue;
                switch (element) {
                    case "codiceFiscale":
                        st.setCF(response.getString("codiceFiscale"));
                        break;
                    case "cognome":
                        st.setLastName(StringUtils.capitalize(response.getString("cognome").toLowerCase()));
                        break;
                    case "nome":
                        st.setFirstName(StringUtils.capitalize(response.getString("nome").toLowerCase()));
                        break;
                    case "dataDiNascita":
                        String dateBirth = response.getString("dataDiNascita");
                        if (!(dateBirth == null || dateBirth.isEmpty())) {
                            try {
                                st.setBirthDate(LocalDate.parse(response.getString("dataDiNascita"), formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "comuneDiNasciata":
                        st.setBirthCity(response.getString("comuneDiNasciata"));
                        break;
                    case "luogoDiNascita":
                        st.setBirthPlace(response.getString("luogoDiNascita"));
                        break;
                    case "annoCorso":
                        st.setCourseYear(response.getString("annoCorso"));
                        break;
                    case "primaIscr":
                        st.setFirstEnrollment(response.getString("primaIscr"));
                        break;
                    case "ultIscr":
                        st.setLastEnrollment(response.getString("ultIscr"));
                        break;
                    case "facolta":
                        st.setDepartmentName(response.getString("facolta"));
                        break;
                    case "nomeCorso":
                        st.setCourseName(response.getString("nomeCorso"));
                        break;
                    case "annoAccaAtt":
                        st.setAcademicYear(response.getInt("annoAccaAtt"));
                        break;
                    case "codCorso":
                        st.setCodeCourse(response.getInt("codCorso"));
                        break;
                    case "tipoStudente":
                        st.setTypeStudent(response.getInt("tipoStudente"));
                        break;
                    case "tipoIscrizione":
                        st.setStudentStatus(response.getString("tipoIscrizione"));
                        break;
                    case "isErasmus":
                        st.setErasmus(response.getBoolean("isErasmus"));
                        break;
                    case "nazioneNascita":
                        st.setNation(response.getString("nazioneNascita"));
                        break;
                    case "creditiTotali":
                        String cfu = response.getString("creditiTotali");
                        if (NumberUtils.isDigits(cfu)) st.setCfu(Integer.parseInt(cfu));
                        break;
                    case "indiMailIstituzionale":
                        st.setEmail(response.getString("indiMailIstituzionale"));
                    case "sesso":
                        st.setGender(response.getString("sesso"));
                        break;
                    case "annoAccaCors":
                        st.setAcademicYearCourse(response.getInt("annoAccaCors"));
                        break;
                    case "cittadinanza":
                        st.setCitizenship(response.getString("cittadinanza"));
                        break;
                }
            }
            return st;
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

    public List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        List<Tax> taxes;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                taxes = _getPaidTaxes();
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
        return taxes;
    }

    private List<Tax> _getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/contabilita/" + studentID + "/bollettinipagati?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            List<Tax> list = new LinkedList<>();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultatoLista"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            if (response.isNull("risultatoLista"))
                return new LinkedList<>();
            response = response.getJSONObject("risultatoLista");
            if (!response.has("risultati") || response.isNull("risultati")) return new LinkedList<>();
            JSONArray array = response.getJSONArray("risultati");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Tax tax = new Tax();
                for (String element : obj.keySet()) {
                    switch (element) {
                        case "codiceBollettino":
                            tax.setCode(obj.getString("codiceBollettino"));
                            break;
                        case "corsoDiStudi":
                            tax.setCodeCourse(obj.getString("corsoDiStudi"));
                            break;
                        case "descCorsoDiStudi":
                            tax.setDescriptionCourse(obj.getString("descCorsoDiStudi"));
                            break;
                        case "impoVers":
                            try {
                                double value = Double.parseDouble(obj.getString("impoVers"));
                                tax.setAmount(value);
                            } catch (NumberFormatException e) {
                                log(Level.SEVERE, e);
                            }
                            break;
                        case "annoAcca":
                            tax.setAcademicYear(obj.getInt("annoAcca"));
                            break;
                        case "dataVers":
                            tax.setPaymentDate(LocalDate.parse(obj.getString("dataVers"), formatter));
                            break;
                    }
                }
                tax.setPaymentDescriptionList(OpenstudHelper.extractPaymentDescriptionList(obj.getJSONArray("causali"), logger));
                list.add(tax);
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

    public List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count = 0;
        List<Tax> taxes;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) refreshToken();
                refresh = true;
                taxes = _getUnpaidTaxes();
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
        return taxes;
    }

    private List<Tax> _getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/contabilita/" + studentID + "/bollettininonpagati?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            List<Tax> list = new LinkedList<>();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultatoLista"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            if (response.isNull("risultatoLista"))
                return new LinkedList<>();
            response = response.getJSONObject("risultatoLista");
            if (!response.has("risultati") || response.isNull("risultati")) return new LinkedList<>();
            JSONArray array = response.getJSONArray("risultati");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Tax tax = new Tax();
                for (String element : obj.keySet()) {
                    switch (element) {
                        case "codiceBollettino":
                            tax.setCode(obj.getString("codiceBollettino"));
                            break;
                        case "corsoDiStudi":
                            tax.setCodeCourse(obj.getString("corsoDiStudi"));
                            break;
                        case "descCorsoDiStudi":
                            tax.setDescriptionCourse(obj.getString("descCorsoDiStudi"));
                            break;
                        case "importoBollettino":
                            try {
                                double value = Double.parseDouble(obj.getString("importoBollettino").replace(",", "."));
                                tax.setAmount(value);
                            } catch (NumberFormatException e) {
                                log(Level.SEVERE, e);
                            }
                            break;
                        case "annoAcca":
                            tax.setAcademicYear(obj.getInt("annoAcca"));
                            break;
                        case "scadenza":
                            if (obj.getString("scadenza").equals("")) continue;
                            tax.setExpirationDate(LocalDate.parse(obj.getString("scadenza"), formatter));
                            break;
                    }
                }
                tax.setPaymentDescriptionList(OpenstudHelper.extractPaymentDescriptionList(obj.getJSONArray("causali"), logger));
                list.add(tax);
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


    public List<Classroom> getClassRoom(String query, boolean withTimetable) throws OpenstudInvalidResponseException, OpenstudConnectionException  {
        if (!isReady()) return null;
        int count = 0;
        List<Classroom> ret;
        while (true) {
            try {
                ret = _getClassroom(query, withTimetable);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
        return ret;
    }

    private List<Classroom> _getClassroom(String query, boolean withTimetable) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        List<Classroom> ret = new LinkedList<>();
        try {
            Request req = new Request.Builder().url(endpointTimetable +"classroom/search?q="+ query.replace(" ","%20")).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("GOMP answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONArray array = new JSONArray(body);
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i<array.length(); i++) {
                if(i==limitSearch) break;
                JSONObject object = array.getJSONObject(i);
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
                    }

                }
                if(withTimetable) {
                    List<Lesson> classLessons = getClassroomTimetable(classroom.getInternalId(), LocalDate.now());
                    for(Lesson lesson : classLessons) {
                        if(lesson.getStart().isBefore(now) && lesson.getEnd().isAfter(now)) classroom.setLessonNow(lesson);
                        else if (lesson.getStart().isAfter(now)) {
                            classroom.setNextLesson(lesson);
                            break;
                        }
                    }
                    classroom.setTodayLessons(classLessons);
                    ret.add(classroom);
                    Thread.sleep(waitTimeClassroomRequest);
                }
                ret.add(classroom);
            }
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public List<Lesson> getClassroomTimetable(Classroom room, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (room == null) return new LinkedList<>();
        return getClassroomTimetable(room.getInternalId(), date);
    }

    public List<Lesson> getClassroomTimetable(int id, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (!isReady()) return null;
        int count = 0;
        List<Lesson> ret;
        while (true) {
            try {
                ret = _getClassroomTimetable(id,date);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
        return ret;

    }

    private List<Lesson> _getClassroomTimetable(int id, LocalDate date) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        List<Lesson> ret = new LinkedList<>();
        try {
            Request req = new Request.Builder().url(endpointTimetable +"events/"+date.getYear()+"/"+date.getMonthValue()+"/"+date.getDayOfMonth()+"/"+id).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("GOMP answer is not valid");
            String body = resp.body().string();
            if (body.contains("maximum request limit")) throw new OpenstudInvalidResponseException("Request rate limit reached").setRateLimitType();
            log(Level.INFO, body);
            JSONArray array = new JSONArray(body);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            for (int i = 0; i<array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                ret.add(OpenstudHelper.extractLesson(object, formatter));
            }
            return OpenstudHelper.sortLessonsByStartDate(ret, true);

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

    public Map<String, List<Lesson>> getTimetable(List<ExamDoable> exams) throws OpenstudInvalidResponseException, OpenstudConnectionException  {
        if (!isReady()) return null;
        int count = 0;
        Map<String, List<Lesson>> ret;
        while (true) {
            try {
                ret = _getTimetable(exams);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
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
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (ExamDoable exam : exams) {
                if (!first) {
                    builder.append(",");
                }
                first = false;
                builder.append(exam.getExamCode());
            }
            String codes = builder.toString();
            Request req = new Request.Builder().url(endpointTimetable +"lectures/"+ builder.toString()).build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("GOMP answer is not valid");
            String body = resp.body().string();
            if (body.contains("maximum request limit")) throw new OpenstudInvalidResponseException("Request rate limit reached").setRateLimitType();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            for (String examCode : response.keySet()) {
                if (!codes.contains(examCode)) continue;
                JSONArray array = response.getJSONArray(examCode);
                LinkedList<Lesson> lessons = new LinkedList<>();
                for (int i = 0; i<array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    lessons.add(OpenstudHelper.extractLesson(object, formatter));
                    }
                ret.put(examCode, lessons);
                }
            return ret;

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

    public List<News> getNews(String locale, boolean withDescription, int limit, int page, int maxPage, String query) throws OpenstudInvalidResponseException, OpenstudConnectionException  {
        int count = 0;
        List<News> ret;
        while (true) {
            try {
                ret = _getNews(locale, withDescription, limit, page, maxPage, query);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) throw e;
                if (++count == maxTries) {
                    log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
        return ret;
    }

    private List<News> _getNews(String locale, boolean withDescription, int limit, int page, int maxPage, String query) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        if(locale == null)
            locale = "en";
        try {
            List<News> ret = new LinkedList<>();
            int startPage = 0;
            int endPage = maxPage;
            if(page != -1){
                startPage = page;
                endPage = startPage + 1;
            }
            String website_url = "https://www.uniroma1.it";
            String page_key = "page";
            String query_key = "search_api_views_fulltext";
            boolean shouldStop = false;
            for(int i = startPage; i < endPage && !shouldStop; i++){
                Connection connection = Jsoup.connect(String.format("%s/%s/tutte-le-notizie", website_url, locale))
                        .data(page_key, i+"");
                if(query!= null)
                    connection = connection.data(query_key, query);
                Document doc = connection.get();
                Elements boxes = doc.getElementsByClass("box-news");
                for(Element box : boxes){
                    News news = new News();
                    news.setTitle(box.getElementsByTag("img").attr("title"));
                    // handle empty news
                    if(news.getTitle().isEmpty())
                        continue;
                    news.setLocale(locale);
                    news.setUrl(website_url + box.getElementsByTag("a").attr("href").trim());
                    news.setSmallUrl(box.getElementsByTag("img").attr("src"));
                    ret.add(news);
                    if(limit != -1 && ret.size() >= limit){
                        shouldStop = true;
                        break;
                    }
                }
            }
            for (News news : ret){
                if (!OpenstudHelper.isValidUrl(news.getUrl()))
                    continue;
                Document doc = Jsoup.connect(news.getUrl()).get();
                if(withDescription){
                    Element start = doc.getElementsByAttributeValueEnding("class", "testosommario").first();
                    if (start != null)
                        news.setDescription(start.getElementsByClass("field-item even").first().text());
                }
                Element date = doc.getElementsByClass("date-display-single").first();
                if(date!=null)
                    news.setDate(date.text());
                news.setImageUrl(doc.getElementsByClass("img-responsive").attr("src"));
            }
            return ret;

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


    String getPassword() {
        return studentPassword;
    }

    String getStudentID(){
        return studentID;
    }


}