package lithium.openstud.driver.core.providers.sapienza;

import lithium.openstud.driver.core.Openstud;
import lithium.openstud.driver.core.internals.BioHandler;
import lithium.openstud.driver.core.models.Career;
import lithium.openstud.driver.core.models.CertificateType;
import lithium.openstud.driver.core.models.Student;
import lithium.openstud.driver.core.models.StudentCard;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import lithium.openstud.driver.exceptions.OpenstudRefreshException;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
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


    @Override
    public byte[] getCertificatePDF(Student student, Career career, CertificateType certificate) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        byte[] ret;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                ret = _getCertificatePDF(student, career, certificate);
                break;
            } catch (OpenstudInvalidResponseException e) {
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


    private byte[] _getCertificatePDF(Student student, Career career, CertificateType certificate) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        try {
            String lang = "it";
            String teachingCode = "";
            if (certificate == CertificateType.DEGREE_WITH_EXAMS_ENG || certificate == CertificateType.DEGREE_WITH_EVALUATION_ENG || certificate == CertificateType.DEGREE_WITH_THESIS_ENG)
                lang = "en";
            if (career.getTeachingCode() != null) teachingCode = career.getTeachingCode();
            Request req = new Request.Builder().url(String.format("%s/certificati/corsodilaurea/%s/%s/%s?ingresso=%s&codiceDidattica=%s&indiceCarriera=%s", os.getEndpointAPI(), student.getStudentID(), SapienzaHelper.getCertificateValue(certificate), lang, os.getToken(), teachingCode, career.getIndex())).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            resp.close();
            os.log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("risultato");
            if (!response.has("result") || response.isNull("result")) return null;
            response = response.getJSONObject("result");
            if (!response.has("documentServerResultDTO") || response.isNull("documentServerResultDTO")) return null;
            response = response.getJSONObject("documentServerResultDTO");
            if (response.has("pdf_file_http_path") && !response.isNull("pdf_file_http_path")) {
                Request request = new Request.Builder().url(response.getString("pdf_file_http_path")).build();
                Response fileResponse = os.getClient().newCall(request).execute();
                if (!fileResponse.isSuccessful()) {
                    throw new IOException("Failed to download file: " + response);
                }
                if (fileResponse.body() == null) throw new IOException("Error when downloading pdf");
                return fileResponse.body().bytes();
            } else return null;
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
    public List<Career> getCareersChoicesForCertificate(Student student, CertificateType certificate) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        List<Career> ret;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                ret = _getCareersChoicesForCertificate(student, certificate);
                break;
            } catch (OpenstudInvalidResponseException e) {
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

    private List<Career> _getCareersChoicesForCertificate(Student student, CertificateType certificate) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        try {
            Request req = new Request.Builder().url(String.format("%s/certificati/corsodilaurea/%s/listaCarriere?ingresso=%s&codiceTipoCertificato=%s", os.getEndpointAPI(), student.getStudentID(), os.getToken(), SapienzaHelper.getCertificateValue(certificate))).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            resp.close();
            os.log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (response.has("descrizioneErrore") && !response.isNull("descrizioneErrore") && response.getString("descrizioneErrore").toLowerCase().contains("non risultano"))
                return new LinkedList<>();
            if (!response.has("risultatoLista"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("risultatoLista");
            if (response == null) return new LinkedList<>();
            JSONArray array = response.getJSONArray("risultati");
            if (array == null) return new LinkedList<>();
            List<Career> ret = new LinkedList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Career car = new Career();
                car.setIndex(i);
                for (String element : obj.keySet()) {
                    switch (element) {
                        case "codiIscr":
                            car.setRegistrationCode(obj.getString(element));
                            break;
                        case "codiCorsStud":
                            car.setCodeCourse(obj.getString(element));
                            break;
                        case "descCorsStud":
                            car.setDescriptionComplete(obj.getString(element));
                            break;
                        case "descDenoCost":
                            car.setDescription(obj.getString(element));
                            break;
                        case "descStruOrga":
                            car.setOrganization(obj.getString(element));
                            break;
                        case "descTipoTito":
                            car.setType(obj.getString(element));
                            break;
                        case "codiDida":
                            car.setTeachingCode(obj.getString(element));
                            break;
                    }
                }
                ret.add(car);
            }
            return ret;
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


    private Student _getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(String.format("%s/studente/%s?ingresso=%s", os.getEndpointAPI(), os.getStudentID(), os.getToken())).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            resp.close();
            os.log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            return SapienzaHelper.extractStudent(os, response);
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


    public byte[] getStudentPhoto(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady() || student == null) return null;
        int count = 0;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                byte[] ret = _getStudentPhoto(student);
                if (ret != null && ret.length == 0) return null;
                return ret;
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

    private byte[] _getStudentPhoto(Student student) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            Request req = new Request.Builder().url(String.format("%s/cartastudente/%s/foto?ingresso=%s", os.getEndpointAPI(), student.getStudentID(), os.getToken())).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            ResponseBody body = resp.body();
            os.log(Level.INFO, body);
            if (body == null) return null;
            InputStream inputStream = body.byteStream();
            byte[] ret = IOUtils.toByteArray(inputStream);
            inputStream.close();
            return ret;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        }

    }


    public StudentCard getStudentCard(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady() || student == null) return null;
        int count = 0;
        while (true) {
            try {
                if (count > 0) os.refreshToken();
                StudentCard card = _getStudentCard(student);
                if (card != null) {
                    byte[] image = _getStudentPhoto(student);
                    if (image != null && image.length == 0) card.setImage(image);
                }
                return card;
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

    private StudentCard _getStudentCard(Student student) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        try {
            Request req = new Request.Builder().url(String.format("%s/cartastudente/%s/info?ingresso=%s", os.getEndpointAPI(), student.getStudentID(), os.getToken())).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            ResponseBody body = resp.body();
            os.log(Level.INFO, body);
            if (body == null) return null;
            String stringBody = body.string();
            JSONObject response = new JSONObject(stringBody);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            if (!response.has("carte"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            JSONArray array = response.getJSONArray("carte");
            List<StudentCard> cards = new LinkedList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                StudentCard card = new StudentCard();
                for (String element : obj.keySet()) {
                    switch (element) {
                        case "codice":
                            card.setCode(obj.getString(element));
                            break;
                        case "matricola":
                            card.setStudentId(String.valueOf(obj.getInt(element)));
                            break;
                        case "stato":
                            if (obj.getString(element).toLowerCase().equals("attiva")) card.setEnabled(true);
                            break;
                        case "dataRichiesta":
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                            card.setIssueDate(LocalDateTime.parse(obj.getString(element), formatter));
                            break;
                    }
                }
                cards.add(card);
            }
            StudentCard ret = null;
            for (StudentCard card: cards){
                if (card.getCode()!=null && card.isEnabled()) {
                    if (ret == null) ret = card;
                    else {
                        if (ret.getIssueDate()!= null && card.getIssueDate() != null && ret.getIssueDate().isBefore(card.getIssueDate())) {
                            ret = card;
                        }
                    }
                }
            }
            return ret;
        } catch (IOException e) {
            OpenstudConnectionException connectionException = new OpenstudConnectionException(e);
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        }

    }

}
