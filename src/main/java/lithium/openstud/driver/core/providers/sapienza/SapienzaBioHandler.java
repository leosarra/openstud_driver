package lithium.openstud.driver.core.providers.sapienza;

import lithium.openstud.driver.core.Openstud;
import lithium.openstud.driver.core.internals.BioHandler;
import lithium.openstud.driver.core.models.Student;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import lithium.openstud.driver.exceptions.OpenstudRefreshException;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.IOException;
import java.util.logging.Level;

public class SapienzaBioHandler implements BioHandler {
    private Openstud os;

    public SapienzaBioHandler(Openstud os) {
        this.os = os;
    }

    @Override
    public Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                return _getInfoStudent();
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
    }

    private Student _getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            JSONObject response = getResponse();
            return SapienzaHelper.extractStudent(os,response);
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

    private JSONObject getResponse() throws IOException, OpenstudInvalidResponseException {
        Request req = new Request.Builder().url(String.format("%s/studente/%s?ingresso=%s",os.getEndpointAPI(), os.getStudentID(), os.getToken())).build();
        Response resp = os.getClient().newCall(req).execute();
        if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
        String body = resp.body().string();
        os.log(Level.INFO, body);
        JSONObject response = new JSONObject(body);
        if (!response.has("ritorno"))
            throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
        return response.getJSONObject("ritorno");
    }
}
