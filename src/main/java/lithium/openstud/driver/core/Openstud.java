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

import java.io.IOException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Openstud {
    private final int maxTries;
    private final String endpointAPI;
    private volatile String token;
    private String studentPassword;
    private final int studentID;
    private boolean isReady;
    private final Logger logger;
    private final OkHttpClient client;
    Openstud(String webEndpoint, int studentID, String studentPassword, Logger logger, int retryCounter, int connectionTimeout, int readTimeout, int writeTimeout) {
        this.maxTries=retryCounter;
        this.endpointAPI=webEndpoint;
        this.studentID=studentID;
        this.studentPassword=studentPassword;
        this.logger=logger;
        client = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS).retryOnConnectionFailure(true)
                .build();
    }

    private void setToken(String token){
        this.token=token;
    }

    private String getToken(){
        return this.token;
    }

    private void log(Level lvl, String str){
        if (logger!=null) logger.log(lvl,str);
    }

    private void log(Level lvl, Object obj){
        if (logger!=null) logger.log(lvl,obj.toString());
    }

    public boolean isReady(){
        return isReady;
    }

    private synchronized void refreshToken() throws OpenstudInvalidRefreshException, OpenstudInvalidResponseException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("key","r4g4zz3tt1").add("matricola",String.valueOf(studentID)).add("stringaAutenticazione",studentPassword).build();
            Request req = new Request.Builder().url(endpointAPI+"/autenticazione").header("Accept","application/json")
                    .header("Content-Type","application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) return;
            String body = resp.body().string();
            JSONObject response = new JSONObject(body);
            if (!response.has("output") || response.getString("output").isEmpty()) return;
            setToken(response.getString("output"));
            if (response.has("esito")) {
                switch (response.getJSONObject("esito").getInt("flagEsito")) {
                    case -4:
                        throw new OpenstudInvalidRefreshException("Invalid credentials when refreshing token");
                    case -1:
                        throw new OpenstudInvalidRefreshException("Invalid credentials when refreshing token");
                    case 0:
                        break;
                    default:
                        throw new OpenstudInvalidResponseException("Infostud is not working as intended");
                }
            }
        } catch (IOException|JSONException e) {
            log(Level.SEVERE,e);
            e.printStackTrace();
        }
    }

    public void login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        int count=0;
        if (studentPassword==null || studentPassword.isEmpty()) throw new OpenstudInvalidCredentialsException("Password can't be left empty");
        if (studentID==-1) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while(true){
            try {
                _login();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            }
        }
    }

    private synchronized void _login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("key","r4g4zz3tt1").add("matricola",String.valueOf(studentID)).add("stringaAutenticazione",studentPassword).build();
            Request req = new Request.Builder().url(endpointAPI+"/autenticazione").header("Accept","application/json")
                    .header("Content-Type","application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (body.contains("Matricola Errata")) throw new OpenstudInvalidCredentialsException("Student ID is not valid");
            else if (!response.has("output")) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            setToken(response.getString("output"));
            if (response.has("esito")) {
                switch (response.getJSONObject("esito").getInt("flagEsito")) {
                    case -4:
                        throw new OpenstudUserNotEnabledException("User is not enabled to use Infostud service.");
                    case -1:
                        throw new OpenstudInvalidCredentialsException("Password not valid");
                    case 0:
                        break;
                    default:
                        throw new OpenstudConnectionException("Infostud is not working as expected");
                }
            }
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
        isReady=true;
    }

    public Isee getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        Isee isee;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                isee=_getCurrentIsee();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
                throw invalidCredentials;
            }
        }
        return isee;
    }

    public List<Isee> getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        List<Isee> history;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                history=_getIseeHistory();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
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
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultatoLista"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("risultatoLista");
            if (!response.has("risultati") || response.isNull("risultati")) return new LinkedList<>();
            JSONArray array  = response.getJSONArray("risultati");
            for (int i=0;i<array.length();i++){
                Isee result = OpenstudHelper.extractIsee(array.getJSONObject(i));
                if (result == null) continue;
                list.add(OpenstudHelper.extractIsee(array.getJSONObject(i)));
            }
            return list;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }

    private Isee _getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/contabilita/" + studentID + "/isee?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("risultato");
            return OpenstudHelper.extractIsee(response);
        } catch (IOException e) {
            log(Level.SEVERE,e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }


    public Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        Student st;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                st=_getInfoStudent();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
                throw invalidCredentials;
            }
        }
        return st;
    }

    private Student _getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI+"/studente/"+studentID+"?ingresso="+getToken()).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO,body);
            JSONObject response = new JSONObject(body);
            if(!response.has("ritorno")) throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response=response.getJSONObject("ritorno");
            Student st = new Student();
            st.setStudentID(studentID);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for(String element : response.keySet()) {
                if(response.isNull(element)) continue;
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
                                st.setBirthDate(LocalDate.parse(response.getString("dataDiNascita"),formatter));
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
            return  st;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }


    public List<ExamDoable> getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        List<ExamDoable> exams;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                exams=_getExamsDoable();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
                throw invalidCredentials;
            }
        }
        return exams;
    }

    private List<ExamDoable> _getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/studente/" + studentID + "/insegnamentisostenibili?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO,body);
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
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }

    public List<ExamPassed> getExamsPassed() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        List<ExamPassed> exams;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                exams=_getExamsPassed();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
                throw invalidCredentials;
            }
        }
        return exams;
    }

    private List<ExamPassed> _getExamsPassed() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/studente/" + studentID + "/esami?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO,body);
            JSONObject response = new JSONObject(body);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            List<ExamPassed> list = new LinkedList<>();
            if (!response.has("esami") || response.isNull("esami")) return list;
            JSONArray array = response.getJSONArray("esami");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ExamPassed exam = new ExamPassed();
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
                            String dateBirth = obj.getString("data");
                            if (!(dateBirth == null || dateBirth.isEmpty())) {
                                try {
                                    exam.setDate(LocalDate.parse(dateBirth,formatter));
                                } catch (DateTimeParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case "annoAcca":
                            exam.setYear(obj.getInt("annoAcca"));
                            break;
                        case "esito":
                            JSONObject esito=obj.getJSONObject("esito");
                            if(esito.has("valoreNominale")) exam.setNominalResult(esito.getString("valoreNominale"));
                            if(esito.has("valoreNonNominale") && !esito.isNull("valoreNonNominale")) exam.setResult(esito.getInt("valoreNonNominale"));
                            break;
                    }
                }
                list.add(exam);
            }
            return list;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }

    public List<ExamReservation> getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        List<ExamReservation> reservations;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                reservations=_getActiveReservations();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
                throw invalidCredentials;
            }
        }
        return reservations;
    }

    private List<ExamReservation> _getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/studente/" + studentID + "/prenotazioni?ingresso=" + getToken()).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO,body);
            JSONObject response = new JSONObject(body);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            if (!response.has("appelli") || response.isNull("appelli")) return null;
            JSONArray array = response.getJSONArray("appelli");
            return OpenstudHelper.extractReservations(array);
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }


    public List<ExamReservation> getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        List<ExamReservation> reservations;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                reservations=_getAvailableReservations(exam, student);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
                throw invalidCredentials;
            }
        }
        return reservations;
    }

    private List<ExamReservation> _getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(endpointAPI + "/appello/ricerca?ingresso=" + getToken()+ "&tipoRicerca="+4+"&criterio="+exam.getModuleCode()+
                    "&codiceCorso="+exam.getCourseCode()+"&annoAccaAuto="+student.getAcademicYearCourse()).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO,body);
            JSONObject response = new JSONObject(body);
            if (!response.has("ritorno")) throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            if (!response.has("appelli") || response.isNull("appelli")) return new LinkedList<>();
            JSONArray array = response.getJSONArray("appelli");
            return OpenstudHelper.extractReservations(array);
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }

    public Pair<Integer,String> insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        Pair<Integer,String> pr;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                pr =_insertReservation(res);
                if(((ImmutablePair<Integer, String>) pr).left==-1 && ((ImmutablePair<Integer, String>) pr).right==null) {
                    if (!(++count == maxTries)) continue;
                }
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
                throw invalidCredentials;
            }
        }
        if(((ImmutablePair<Integer, String>) pr).left==-1 && ((ImmutablePair<Integer, String>) pr).right==null) return null;
        return pr;
    }

    private ImmutablePair<Integer,String> _insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            RequestBody reqbody = RequestBody.create(null, new byte[]{});
            Request req = new Request.Builder().url(endpointAPI + "/prenotazione/" + res.getReportID() + "/" + res.getSessionID()
                    + "/" + res.getCourseCode() + "?ingresso=" + getToken()).post(reqbody).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            String url = null;
            int flag = -1;
            if (response.has("esito")) {
                if (response.getJSONObject("esito").has("flagEsito")) {
                    flag = response.getJSONObject("esito").getInt("flagEsito");
                }
            }
            else throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            if (!response.isNull("url") && response.has("url")) url = response.getString("url");
            return new ImmutablePair<>(flag, url);
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }

    public int deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException {
        if (!isReady() || res.getReservationNumber()==-1) return -1;
        int count=0;
        int ret;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                ret =_deleteReservation(res);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
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
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
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
        }catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }

    public byte[] getPdf(ExamReservation reservation) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady() || reservation==null) return null;
        int count=0;
        byte[] pdf;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                pdf=_getPdf(reservation);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
                throw invalidCredentials;
            }
        }
        return pdf;
    }

    private byte[] _getPdf(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            Request req = new Request.Builder().url(endpointAPI+"/prenotazione/"+res.getReportID()+"/"+res.getSessionID()+"/"
                    +studentID+"/pdf?ingresso="+getToken()).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO,body);
            JSONObject response = new JSONObject(body);
            if(!response.has("risultato") || response.isNull("risultato"))  throw new OpenstudInvalidResponseException("Infostud answer is not valid, maybe the token is no longer valid");
            response=response.getJSONObject("risultato");
            if(!response.has("byte") || response.isNull("byte"))  throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            JSONArray byteArray= response.getJSONArray("byte");
            byte[] pdf = new byte[byteArray.length()];
            for(int i=0;i<byteArray.length();i++) pdf[i] = (byte) byteArray.getInt(i);
            log(Level.INFO,"Found PDF made of "+pdf.length+" bytes \n");
            return  pdf;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }

    public List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        List<Tax> taxes;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                taxes=_getPaidTaxes();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
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
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultatoLista"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            if (response.isNull("risultatoLista"))
                return new LinkedList<>();
            response = response.getJSONObject("risultatoLista");
            if (!response.has("risultati") || response.isNull("risultati")) return new LinkedList<>();
            JSONArray array  = response.getJSONArray("risultati");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i=0;i<array.length();i++){
                JSONObject obj = array.getJSONObject(i);
                Tax tax = new Tax();
                for (String element : obj.keySet()) {
                    switch (element){
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
                                log(Level.SEVERE,e);
                            }
                            break;
                        case "annoAcca":
                            tax.setAcademicYear(obj.getInt("annoAcca"));
                            break;
                        case "dataVers":
                            tax.setPaymentDate(LocalDate.parse(obj.getString("dataVers"),formatter));
                            break;
                    }
                }
                tax.setPaymentDescriptionList(OpenstudHelper.extractPaymentDescriptionList(obj.getJSONArray("causali"),logger));
                list.add(tax);
            }
            return list;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }

    public List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!isReady()) return null;
        int count=0;
        List<Tax> taxes;
        boolean refresh = false;
        while(true){
            try {
                if(refresh) refreshToken();
                refresh = true;
                taxes=_getUnpaidTaxes();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == maxTries) {
                    log(Level.SEVERE,e);
                    throw e;
                }
            } catch (OpenstudInvalidRefreshException e) {
                OpenstudInvalidCredentialsException invalidCredentials = new OpenstudInvalidCredentialsException(e);
                log(Level.SEVERE,invalidCredentials);
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
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultatoLista"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            if (response.isNull("risultatoLista"))
                return new LinkedList<>();
            response = response.getJSONObject("risultatoLista");
            if (!response.has("risultati") || response.isNull("risultati")) return new LinkedList<>();
            JSONArray array  = response.getJSONArray("risultati");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i=0;i<array.length();i++){
                JSONObject obj = array.getJSONObject(i);
                Tax tax = new Tax();
                for (String element : obj.keySet()) {
                    switch (element){
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
                                double value = Double.parseDouble(obj.getString("importoBollettino").replace(",","."));
                                tax.setAmount(value);
                            } catch (NumberFormatException e) {
                                log(Level.SEVERE,e);
                            }
                            break;
                        case "annoAcca":
                            tax.setAcademicYear(obj.getInt("annoAcca"));
                            break;
                        case "scadenza":
                            if(obj.getString("scadenza").equals("")) continue;
                            tax.setExpirationDate(LocalDate.parse(obj.getString("scadenza"),formatter));
                            break;
                    }
                }
                tax.setPaymentDescriptionList(OpenstudHelper.extractPaymentDescriptionList(obj.getJSONArray("causali"),logger));
                list.add(tax);
            }
            return list;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            log(Level.SEVERE,connectionException);
            throw connectionException;
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
    }


}
