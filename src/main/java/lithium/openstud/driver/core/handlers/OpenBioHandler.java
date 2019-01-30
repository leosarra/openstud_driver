package lithium.openstud.driver.core.handlers;

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

public class OpenBioHandler implements BioHandler
{
    private Openstud os;

    public OpenBioHandler(Openstud os)
    {
        this.os = os;
    }

    @Override
    public Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        Student st;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) os.refreshToken();
                refresh = true;
                st = _getInfoStudent();
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
        return st;
    }

    private Student _getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(os.getEndpointAPI()+ "/studente/" + os.getStudentID()+ "?ingresso=" + os.getToken()).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            os.log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("ritorno"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("ritorno");
            Student st = new Student();
            st.setStudentID(os.getStudentID());
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
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }
}
