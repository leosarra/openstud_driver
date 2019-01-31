package lithium.openstud.driver.core.providers.sapienza;

import lithium.openstud.driver.core.models.ExamReservation;
import lithium.openstud.driver.core.models.Isee;
import lithium.openstud.driver.core.models.Lesson;
import lithium.openstud.driver.core.models.PaymentDescription;
import org.json.JSONArray;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SapienzaHelper {

    public static Lesson extractLesson(JSONObject response, DateTimeFormatter formatter) {
        Lesson lesson = new Lesson();
        for (String lessonInfo : response.keySet()) {
            if (response.isNull(lessonInfo)) continue;
            switch (lessonInfo) {
                case "name":
                    String name = response.getString(lessonInfo);
                    int startIdx = name.indexOf(" ");
                    int endIdx = name.indexOf("Docente:");
                    if (startIdx != -1 && endIdx != -1) {
                        if (name.endsWith(" ")) {
                            name = name.substring(0, name.length() - 1);
                        }
                        lesson.setName(name.substring(startIdx, endIdx).trim());
                    } else lesson.setName(name);
                    int indexTeacher = name.indexOf("Docente:");
                    if (indexTeacher != -1) lesson.setTeacher(name.substring(indexTeacher + "Docente: ".length()));
                    break;
                case "where":
                    lesson.setWhere(response.getString(lessonInfo));
                    break;
                case "start":
                    lesson.setStart(LocalDateTime.parse(response.getString(lessonInfo), formatter));
                    break;
                case "end":
                    lesson.setEnd(LocalDateTime.parse(response.getString(lessonInfo), formatter));
                    break;
                default:
                    break;
            }
        }
        return lesson;
    }

    public static Isee extractIsee(JSONObject response) {
        Isee res = new Isee();
        for (String element : response.keySet()) {
            switch (element) {
                case "valore":
                    double value = response.getDouble("valore");
                    if (value == -2) return null;
                    res.setValue(response.getDouble("valore"));
                    break;
                case "protocollo":
                    String protocol = response.getString("protocollo");
                    if (protocol == null || protocol.isEmpty()) return null;
                    res.setProtocol(response.getString("protocollo"));
                    break;
                case "modificabile":
                    res.setEditable(response.getInt("modificabile") == 1);
                    break;
                case "dataOperazione":
                    if (response.isNull("dataOperazione")) break;
                    DateTimeFormatter formatterOperation = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String dateOperation = response.getString("dataOperazione");
                    if (!(dateOperation == null || dateOperation.isEmpty())) {
                        try {
                            res.setDateOperation(LocalDate.parse(response.getString("dataOperazione"), formatterOperation));
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "data":
                    DateTimeFormatter formatterDateDeclaration = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    if (response.isNull("data")) return null;
                    String dateDeclaration = response.getString("data");
                    if (!(dateDeclaration == null || dateDeclaration.isEmpty())) {
                        try {
                            res.setDateDeclaration(LocalDate.parse(response.getString("data"), formatterDateDeclaration));
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }

        }
        return res;
    }

    public static List<PaymentDescription> extractPaymentDescriptionList(JSONArray array, Logger logger) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<PaymentDescription> list = new LinkedList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            PaymentDescription pdes = new PaymentDescription();
            for (String element : obj.keySet()) {
                switch (element) {
                    case "descrizione":
                        pdes.setDescription(obj.getString("descrizione"));
                        break;
                    case "importo":
                        try {
                            Double value = Double.parseDouble(obj.getString("importo"));
                            pdes.setAmount(value);
                        } catch (NumberFormatException e) {
                            logger.log(Level.SEVERE, e.toString());
                        }
                        break;
                    case "annoAccademicoString":
                        pdes.setAcademicYear(obj.getString("annoAccademicoString"));
                        break;
                    case "impoVers":
                        try {
                            Double value = Double.parseDouble(obj.getString("impoVers"));
                            pdes.setAmountPaid(value);
                        } catch (NumberFormatException e) {
                            logger.log(Level.SEVERE, e.toString());
                        }
                        break;
                    default:
                        break;
                }
            }
            list.add(pdes);
        }
        return list;
    }

    public static List<ExamReservation> extractReservations(JSONArray array) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<ExamReservation> list = new LinkedList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            ExamReservation res = new ExamReservation();
            for (String element : obj.keySet()) {
                switch (element) {
                    case "codIdenVerb":
                        res.setReportID(obj.getInt("codIdenVerb"));
                        break;
                    case "canale":
                        res.setChannel(obj.getString("canale"));
                        break;
                    case "codAppe":
                        res.setSessionID(obj.getInt("codAppe"));
                        break;
                    case "codCorsoStud":
                        res.setCourseCode(Integer.parseInt(obj.getString("codCorsoStud")));
                        break;
                    case "descrizione":
                        res.setExamSubject(obj.getString("descrizione"));
                        break;
                    case "descCorsoStud":
                        res.setCourseDescription(obj.getString("descCorsoStud"));
                        break;
                    case "crediti":
                        res.setCfu(obj.getInt("crediti"));
                        break;
                    case "docente":
                        res.setTeacher(obj.getString("docente"));
                        break;
                    case "annoAcca":
                        res.setYearCourse(obj.getString("annoAcca"));
                        break;
                    case "facolta":
                        res.setDepartment(obj.getString("facolta"));
                        break;
                    case "numeroPrenotazione":
                        if (obj.isNull("numeroPrenotazione")) break;
                        res.setReservationNumber(obj.getInt("numeroPrenotazione"));
                        break;
                    case "ssd":
                        if (obj.isNull("ssd")) break;
                        res.setSsd(obj.getString("ssd"));
                        break;
                    case "dataprenotazione":
                        if (obj.isNull("dataprenotazione")) break;
                        String reservationDate = obj.getString("dataprenotazione");
                        if (!(reservationDate == null || reservationDate.isEmpty())) {
                            try {
                                res.setReservationDate(LocalDate.parse(reservationDate, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "note":
                        res.setNote(obj.getString("note"));
                        break;
                    case "dataAppe":
                        String examDate = obj.getString("dataAppe");
                        if (!(examDate == null || examDate.isEmpty())) {
                            try {
                                res.setExamDate(LocalDate.parse(examDate, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "dataInizioPrenotazione":
                        if (obj.isNull("dataInizioPrenotazione")) break;
                        String startDate = obj.getString("dataInizioPrenotazione");
                        if (!(startDate == null || startDate.isEmpty())) {
                            try {
                                res.setStartDate(LocalDate.parse(startDate, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "dataFinePrenotazione":
                        if (obj.isNull("dataFinePrenotazione")) break;
                        String endDate = obj.getString("dataFinePrenotazione");
                        if (!(endDate == null || endDate.isEmpty())) {
                            try {
                                res.setEndDate(LocalDate.parse(endDate, formatter));
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "SiglaModuloDidattico":
                        if (!obj.isNull("SiglaModuloDidattico")) res.setModule(obj.getString("SiglaModuloDidattico"));
                        break;
                    default:
                        break;
                }
            }
            list.add(res);
        }
        return list;
    }

}
