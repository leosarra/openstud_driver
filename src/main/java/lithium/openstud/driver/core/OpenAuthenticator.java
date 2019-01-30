package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.*;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Level;

public class OpenAuthenticator implements Authenticator
{
    private Openstud os;
    OpenAuthenticator(Openstud openstud)
    {
        this.os = openstud;
    }

    @Override
    public String getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        int count = 0;
        if (os.getStudentID() == null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while (true) {
            try {
                return _getSecurityQuestion();
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private String _getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("matricola", String.valueOf(os.getStudentID())).build();
            Request req = new Request.Builder().url(os.getEndpointAPI() + "/pwd/recuperaDomanda/matricola").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            if (body.contains("the page you are looking for is currently unavailable")) throw new OpenstudInvalidResponseException("InfoStud is in maintenance").setMaintenanceType();
            if (body.contains("Matricola Errata")) throw new OpenstudInvalidCredentialsException("Invalid studentID");
            if (body.contains("Impossibile recuperare la password per email")) return null;
            os.log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (response.isNull("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid.");
            return response.getString("risultato");
        } catch (IOException e) {
            os.log(Level.SEVERE, e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e);
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    @Override
    public int recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        int count = 0;
        if (os.getStudentID() == null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while (true) {
            try {
                return _recoverPassword(answer);
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance()) throw e;
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private int _recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("matricola", String.valueOf(os.getStudentID())).add("risposta", answer).build();
            Request req = new Request.Builder().url(os.getEndpointAPI() + "/pwd/recupera/matricola").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            if (body.contains("the page you are looking for is currently unavailable")) throw new OpenstudInvalidResponseException("InfoStud is in maintenance").setMaintenanceType();
            if (body.contains("Matricola Errata")) throw new OpenstudInvalidCredentialsException("Invalid studentID");
            if (body.contains("Impossibile recuperare la password per email")) return -1;
            os.log(Level.INFO, body);
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
            os.log(Level.SEVERE, e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e);
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
        return 0;
    }

    @Override
    public void resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        int count = 0;
        if (os.getStudentID() == null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while (true) {
            try {
                _resetPassword(new_password);
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private void _resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("oldPwd", os.getStudentPassword())
                    .add("newPwd", new_password)
                    .add("confermaPwd", new_password)   //TODO confirmation should be passed as second param?
                    .build();

            Request req = new Request.Builder().url(os.getEndpointAPI() + "/pwd/" + os.getStudentID() + "/reset").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            os.log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (response.isNull("codiceErrore") || response.isNull("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid.");

            String error_code = response.getString("codiceErrore");
            boolean result = response.getBoolean("risultato");
            if(error_code.equals("000") && result) {
                os.setStudentPassword(new_password); // TODO check this
                return;
            }

            throw new OpenstudInvalidCredentialsException("Answer is not correct");

        } catch (IOException e) {
            os.log(Level.SEVERE, e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e);
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    public int recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        int count=0;
        if (os.getStudentID()==null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while(true){
            try {
                return _recoverPasswordWithEmail(email, answer);
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE,e);
                    throw e;
                }
            }
        }
    }

    private int _recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("matricola",String.valueOf(os.getStudentID())).add("email", email).add("risposta",answer).build();
            Request req = new Request.Builder().url(os.getEndpointAPI() + "/pwd/recupera/matricola").header("Accept","application/json")
                    .header("Content-EventType","application/x-www-form-urlencoded").post(formBody).build();
            Response resp = os.getClient().newCall(req).execute();
            if(resp.body()==null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            if (body.contains("Matricola Errata")) throw new OpenstudInvalidCredentialsException("Invalid studentID");
            if (body.contains("Impossibile recuperare la password per email")) return -1;
            os.log(Level.INFO, body);
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
            os.log(Level.SEVERE,e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e){
            OpenstudInvalidResponseException invalidResponse= new OpenstudInvalidResponseException(e);
            os.log(Level.SEVERE,invalidResponse);
            throw invalidResponse;
        }
        return 0;
    }


    public void login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        int count = 0;
        if (os.getStudentPassword()== null || os.getStudentPassword().isEmpty())
            throw new OpenstudInvalidCredentialsException("Password can't be left empty");
        if (os.getStudentID()== null) throw new OpenstudInvalidResponseException("StudentID can't be left empty");
        while (true) {
            try {
                _login();
                break;
            } catch (OpenstudInvalidResponseException e) {
                if (++count == os.getMaxTries()) {
                    os.log(Level.SEVERE, e);
                    throw e;
                }
            }
        }
    }

    private synchronized void _login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        try {
            if (!StringUtils.isNumeric(os.getStudentID())) throw new OpenstudInvalidCredentialsException("Student ID is not valid");
            RequestBody formBody = new FormBody.Builder()
                    .add("key", os.getKey()).add("matricola", String.valueOf(os.getStudentID())).add("stringaAutenticazione", os.getStudentPassword()).build();
            Request req = new Request.Builder().url(os.getEndpointAPI()+ "/autenticazione").header("Accept", "application/json")
                    .header("Content-EventType", "application/x-www-form-urlencoded").post(formBody).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            if (body.contains("the page you are looking for is currently unavailable")) throw new OpenstudInvalidResponseException("InfoStud is in maintenance").setMaintenanceType();
            os.log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (body.contains("Matricola Errata"))
                throw new OpenstudInvalidCredentialsException("Student ID is not valid");
            else if (!response.has("output"))
                throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            os.setToken(response.getString("output"));
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
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
        os.setReady(true);
    }
}
