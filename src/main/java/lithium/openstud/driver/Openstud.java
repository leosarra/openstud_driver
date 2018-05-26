package lithium.openstud.driver;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

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

    public boolean isReady(){
        return isReady;
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

    public void _login() throws OpenstudInvalidPasswordException, OpenstudInvalidUserException, OpenstudEndpointNotReadyException, OpenstudConnectionException {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(endpointAPI+"/autenticazione").header("Accept","application/json").
                    header("Content-Type","application/x-www-form-urlencoded")
                    .field("key","r4g4zz3tt1").field("matricola",studentID).field("stringaAutenticazione",studentPassword).asJson();
            JSONObject response = new JSONObject(jsonResponse.getBody());
            if (!response.has("object")) throw new OpenstudEndpointNotReadyException("Infostud answer is not valid");
            response=response.getJSONObject("object");
            if (!response.has("output")) throw new OpenstudEndpointNotReadyException("Infostud answer is not valid");
            token = response.getString("output");
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
}
