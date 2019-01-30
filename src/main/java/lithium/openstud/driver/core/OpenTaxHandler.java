package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import lithium.openstud.driver.exceptions.OpenstudRefreshException;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class OpenTaxHandler implements TaxHandler
{
    private Openstud os;

    public OpenTaxHandler(Openstud os)
    {
        this.os = os;
    }

    @Override
    public List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!os.isReady()) return null;
        int count = 0;
        List<Tax> taxes;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) os.refreshToken();
                refresh = true;
                taxes = _getPaidTaxes();
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
        return taxes;
    }

    private List<Tax> _getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(os.getEndpointAPI() + "/contabilita/" + os.getStudentID()+
                    "/bollettinipagati?ingresso=" + os.getToken()).build();
            Response resp = os.getClient().newCall(req).execute();
            List<Tax> list = new LinkedList<>();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            os.log(Level.INFO, body);
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
                                os.log(Level.SEVERE, e);
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
                tax.setPaymentDescriptionList(OpenstudHelper.extractPaymentDescriptionList(obj.getJSONArray("causali"), os.getLogger()));
                list.add(tax);
            }
            return list;
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
    public List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        List<Tax> taxes;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) os.refreshToken();
                refresh = true;
                taxes = _getUnpaidTaxes();
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
        return taxes;
    }

    private List<Tax> _getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(os.getEndpointAPI()+ "/contabilita/" + os.getStudentID() +
                    "/bollettininonpagati?ingresso=" + os.getToken()).build();
            Response resp = os.getClient().newCall(req).execute();
            List<Tax> list = new LinkedList<>();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            os.log(Level.INFO, body);
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
                                os.log(Level.SEVERE, e);
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
                tax.setPaymentDescriptionList(OpenstudHelper.extractPaymentDescriptionList(obj.getJSONArray("causali"), os.getLogger()));
                list.add(tax);
            }
            return list;
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

    public Isee getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        Isee isee;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) os.refreshToken();
                refresh = true;
                isee = _getCurrentIsee();
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
        return isee;
    }

    private Isee _getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(os.getEndpointAPI() + "/contabilita/" + os.getStudentID() +
                    "/isee?ingresso=" + os.getToken()).build();
            Response resp = os.getClient().newCall(req).execute();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            os.log(Level.INFO, body);
            JSONObject response = new JSONObject(body);
            if (!response.has("risultato"))
                throw new OpenstudInvalidResponseException("Infostud response is not valid. I guess the token is no longer valid");
            response = response.getJSONObject("risultato");
            return OpenstudHelper.extractIsee(response);
        } catch (IOException e) {
            os.log(Level.SEVERE, e);
            throw new OpenstudConnectionException(e);
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }

    public List<Isee> getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!os.isReady()) return null;
        int count = 0;
        List<Isee> history;
        boolean refresh = false;
        while (true) {
            try {
                if (refresh) os.refreshToken();
                refresh = true;
                history = _getIseeHistory();
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
        return history;
    }

    private List<Isee> _getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException {
        try {
            Request req = new Request.Builder().url(os.getEndpointAPI() + "/contabilita/" + os.getStudentID() +
                    "/listaIsee?ingresso=" + os.getToken()).build();
            Response resp = os.getClient().newCall(req).execute();
            List<Isee> list = new LinkedList<>();
            if (resp.body() == null) throw new OpenstudInvalidResponseException("Infostud answer is not valid");
            String body = resp.body().string();
            os.log(Level.INFO, body);
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
            os.log(Level.SEVERE, connectionException);
            throw connectionException;
        } catch (JSONException e) {
            OpenstudInvalidResponseException invalidResponse = new OpenstudInvalidResponseException(e).setJSONType();
            os.log(Level.SEVERE, invalidResponse);
            throw invalidResponse;
        }
    }
}
