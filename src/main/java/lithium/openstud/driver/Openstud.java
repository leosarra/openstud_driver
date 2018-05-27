package lithium.openstud.driver;

import io.github.openunirest.http.HttpResponse;
import io.github.openunirest.http.JsonNode;
import io.github.openunirest.http.Unirest;
import io.github.openunirest.http.exceptions.UnirestException;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudEndpointNotReadyException;
import lithium.openstud.driver.exceptions.OpenstudInvalidPasswordException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Openstud {
    private int maxTries;
    private String endpointAPI;
    private int connectionTimeout;
    private int socketTimeout;
    private String token;
    private String studentPassword;
    private int studentID;
    private boolean isReady;

    protected Openstud(String webEndpoint, int studentID, String studentPassword, int retryCounter, int connectionTimeout, int socketTimeout) {
        this.maxTries=retryCounter;
        this.endpointAPI=webEndpoint;
        this.connectionTimeout=connectionTimeout;
        this.socketTimeout=socketTimeout;
        this.studentID=studentID;
        this.studentPassword=studentPassword;
    }

    private synchronized void setToken(String token){
        this.token=token;
    }

    public boolean isReady(){
        return isReady;
    }

    private int refreshToken(){
        try {
            Unirest.setTimeouts(connectionTimeout,socketTimeout);
            HttpResponse<JsonNode> jsonResponse = Unirest.post(endpointAPI+"/autenticazione").header("Accept","application/json")
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .field("key","r4g4zz3tt1").field("matricola",studentID).field("stringaAutenticazione",studentPassword).asJson();
            JSONObject response = new JSONObject(jsonResponse.getBody());
            if (!response.has("object")) return -1;
            response=response.getJSONObject("object");
            if (!response.has("output")) return -1;
            setToken(response.getString("output"));
            if (response.has("esito")) {
                switch (response.getJSONObject("esito").getInt("flagEsito")) {
                    case -4:
                        return -1;
                    case -1:
                        return -1;
                    default:
                        return 0;
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void login() throws OpenstudEndpointNotReadyException, OpenstudInvalidPasswordException, OpenstudConnectionException, OpenstudInvalidResponseException {
        int count=0;
        while(true){
            try {
                _login();
                break;
            } catch (OpenstudEndpointNotReadyException |OpenstudConnectionException e) {
                if (++count == maxTries) throw e;
            }
        }
    }

    private void _login() throws OpenstudInvalidPasswordException, OpenstudEndpointNotReadyException, OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Unirest.setTimeouts(connectionTimeout,socketTimeout);
            HttpResponse<JsonNode> jsonResponse = Unirest.post(endpointAPI+"/autenticazione").header("Accept","application/json")
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .field("key","r4g4zz3tt1").field("matricola",studentID).field("stringaAutenticazione",studentPassword).asJson();
            JSONObject response = new JSONObject(jsonResponse.getBody());
            if (!response.has("object")) throw new OpenstudInvalidResponseException("Infostud response is not valid");
            response=response.getJSONObject("object");
            if (!response.has("output")) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            setToken(response.getString("output"));
            if (response.has("esito")) {
                switch (response.getJSONObject("esito").getInt("flagEsito")) {
                    case -4:
                        throw new OpenstudInvalidResponseException("User is not enabled to use Infostud service.");
                    case -1:
                        throw new OpenstudInvalidPasswordException("Password not valid");
                    case 0:
                        break;
                    default:
                        throw new OpenstudEndpointNotReadyException("Infostud is not working as expected");
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new OpenstudConnectionException("Unirest library can't process login, check internet connection.");
        }
        isReady=true;
    }

    public Isee getIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (!isReady()) return null;
        int count=0;
        Isee isee;
        while(true){
            try {
                isee=_getIsee();
                break;
            } catch (OpenstudConnectionException|OpenstudInvalidResponseException e) {
                if (++count == maxTries) throw e;
                if (refreshToken()==-1) throw e;
            }
        }
        return isee;
    }

    private Isee _getIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(endpointAPI+"/contabilita/"+studentID+"/isee?ingresso="+token).asJson();
            JSONObject response = new JSONObject(jsonResponse.getBody());
            if (!response.has("object")) throw new OpenstudInvalidResponseException("Infostud response is not valid");
            response=response.getJSONObject("object");
            if(!response.has("risultato")) throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response=response.getJSONObject("risultato");
            Isee res = new Isee();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            for(String element : response.keySet()) {
                switch (element) {
                    case "value":
                        res.setValue(response.getDouble("value"));
                        break;
                    case "protocollo":
                        String protocol = response.getString("protocollo");
                        if (protocol == null || protocol.isEmpty()) return null;
                        res.setProtocol(response.getString("protocollo"));
                        break;
                    case "modificabile":
                        res.setEditable(response.getInt("modificabile")==1);
                        break;
                    case "dataOperazione":
                        String dateOperation = response.getString("dataOperazione");
                        if (!(dateOperation == null || dateOperation.isEmpty())) {
                            try {
                                res.setDateOperation(formatter.parse(response.getString("dataOperazione")));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "data":
                        String dateDeclaration = response.getString("data");
                        if (!(dateDeclaration == null || dateDeclaration.isEmpty())) {
                            try {
                                res.setDateDeclaration(formatter.parse(response.getString("data")));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
            return  res;
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new OpenstudConnectionException("Unirest library can't get isee, check internet connection.");
        }
    }


    public Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (!isReady()) return null;
        int count=0;
        Student st;
        while(true){
            try {
                st=_getInfoStudent();
                break;
            } catch (OpenstudConnectionException|OpenstudInvalidResponseException e) {
                if (++count == maxTries) throw e;
                if (refreshToken()==-1) throw e;
            }
        }
        return st;
    }

    private Student _getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(endpointAPI+"/studente/"+studentID+"?ingresso="+token).asJson();
            JSONObject response = new JSONObject(jsonResponse.getBody());
            if (!response.has("object")) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            response=response.getJSONObject("object");
            if(!response.has("ritorno")) throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response=response.getJSONObject("ritorno");
            Student st = new Student();
            st.setStudentID(studentID);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            for(String element : response.keySet()) {
                switch (element) {
                    case "codiceFiscale":
                        st.setCF(response.getString("codiceFiscale"));
                        break;
                    case "cognome":
                        st.setLastName(response.getString("cognome"));
                        break;
                    case "nome":
                        st.setFirstName(response.getString("nome"));
                        break;
                    case "dataDiNascita":
                        String dateBirth = response.getString("dataDiNascita");
                        if (!(dateBirth == null || dateBirth.isEmpty())) {
                            try {
                                st.setBirthDate(formatter.parse(response.getString("dataDiNascita")));
                            } catch (ParseException e) {
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
                        st.setCfu(response.getInt("creditiTotali"));
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
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new OpenstudConnectionException("Unirest library can't get isee, check internet connection.");
        }
    }
}
