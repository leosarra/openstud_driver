package lithium.openstud.driver;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class Openstud {
    private int maxTries;
    private String endpointAPI;
    private int connectionTimeout;
    private int socketTimeout;
    private String token;
    private String studentPassword;
    private int studentID;
    private boolean isReady;

    public Openstud(String webEndpoint, int studentID, String studentPassword, int retryCounter, int connectionTimeout, int socketTimeout) {
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

    public void login() throws OpenstudEndpointNotReadyException, OpenstudInvalidPasswordException, OpenstudInvalidUserException, OpenstudConnectionException {
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

    private void _login() throws OpenstudInvalidPasswordException, OpenstudInvalidUserException, OpenstudEndpointNotReadyException, OpenstudConnectionException {
        try {
            Unirest.setTimeouts(connectionTimeout,socketTimeout);
            HttpResponse<JsonNode> jsonResponse = Unirest.post(endpointAPI+"/autenticazione").header("Accept","application/json")
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .field("key","r4g4zz3tt1").field("matricola",studentID).field("stringaAutenticazione",studentPassword).asJson();
            JSONObject response = new JSONObject(jsonResponse.getBody());
            if (!response.has("object")) throw new OpenstudEndpointNotReadyException("Infostud answer is not valid");
            response=response.getJSONObject("object");
            if (!response.has("output")) throw new OpenstudEndpointNotReadyException("Infostud answer is not valid");
            setToken(response.getString("output"));
            if (response.has("esito")) {
                switch (response.getJSONObject("esito").getInt("flagEsito")) {
                    case -4:
                        throw new OpenstudInvalidUserException("User is not enabled to use Infostud service.");
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

    public Isee getIsee() throws OpenstudInvalidSetupException, OpenstudEndpointNotReadyException, OpenstudConnectionException {
        if (!isReady()) throw new OpenstudInvalidSetupException("OpenStud is not ready. Remember to call login() first!");
        int count=0;
        Isee isee;
        while(true){
            try {
                isee=_getIsee();
                break;
            } catch (OpenstudConnectionException e) {
                if (++count == maxTries) throw e;
                if (refreshToken()==-1) throw e;
            }
        }
        return isee;
    }

    private Isee _getIsee() throws OpenstudConnectionException, OpenstudEndpointNotReadyException {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(endpointAPI+"/contabilita/"+studentID+"/isee?ingresso="+token).asJson();
            JSONObject response = new JSONObject(jsonResponse.getBody());
            if (!response.has("object")) throw new OpenstudEndpointNotReadyException("Infostud answer is not valid");
            response=response.getJSONObject("object");
            if(!response.has("risultato")) return null;
            response=response.getJSONObject("risultato");
            Isee res = new Isee();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            for(String element : (Set<String>) response.keySet()) {
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
}
