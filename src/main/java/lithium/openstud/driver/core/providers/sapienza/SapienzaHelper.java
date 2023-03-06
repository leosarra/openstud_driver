package lithium.openstud.driver.core.providers.sapienza;

import com.lithium.kapitalize.Kapitalize;
import com.lithium.kapitalize.languages.EnglishLanguage;
import com.lithium.kapitalize.languages.SpecialRules;
import lithium.openstud.driver.core.Openstud;
import lithium.openstud.driver.core.models.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

class SapienzaHelper {

    static Lesson extractLesson(JSONObject response, DateTimeFormatter formatter, int offset) {
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
                    LocalDateTime startTime = LocalDateTime.parse(response.getString(lessonInfo), formatter);
                    startTime = startTime.plusHours(offset);
                    lesson.setStart(startTime);
                    break;
                case "end":
                    LocalDateTime endTime = LocalDateTime.parse(response.getString(lessonInfo), formatter);
                    endTime = endTime.plusHours(offset);
                    lesson.setEnd(endTime);
                    break;
                default:
                    break;
            }
        }
        return lesson;
    }

    static Isee extractIsee(Openstud os, JSONObject response) {
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
                            os.log(Level.SEVERE, e);
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
                            os.log(Level.SEVERE, e);
                        }
                    }
                    break;
                default:
                    break;
            }

        }
        return res;
    }

    static List<PaymentDescription> extractPaymentDescriptionList(Openstud os, JSONArray array) {
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
                            e.printStackTrace();
                            os.log(Level.SEVERE, e);
                        }
                        break;
                    case "annoAccademicoString":
                        pdes.setAcademicYear(obj.getString("annoAccademicoString"));
                        break;
                    case "impoVers":
                        try {
                            Double valueVers = Double.parseDouble(obj.getString("impoVers"));
                            pdes.setAmountPaid(valueVers);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            os.log(Level.SEVERE, e);
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

    static List<ExamReservation> extractReservations(Openstud os, JSONArray array) {
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
                                os.log(Level.SEVERE, e);
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
                                os.log(Level.SEVERE, e);
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
                                os.log(Level.SEVERE, e);
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
                                os.log(Level.SEVERE, e);
                            }
                        }
                        break;
                    case "SiglaModuloDidattico":
                        if (!obj.isNull("SiglaModuloDidattico")) res.setModule(obj.getString("SiglaModuloDidattico"));
                        break;
                    case "modalitaSvolgimentoList":
                        if (!obj.isNull("modalitaSvolgimentoList")) res.setAttendingModesList(obj.getJSONArray("modalitaSvolgimentoList"));
                        break;
                    default:
                        break;
                }
            }
            list.add(res);
        }
        return list;
    }

    static Student extractStudent(Openstud os, JSONObject response) {
        Student st = new Student();
        st.setStudentID(os.getStudentID());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        EnglishLanguage lang = new EnglishLanguage(SpecialRules.ITALIAN);
        lang.setCapitalizePostNominalsInitials(false);
        Kapitalize kapitalize = new Kapitalize(lang);
        for (String element : response.keySet()) {
            if (response.isNull(element)) continue;
            switch (element) {
                case "codiceFiscale":
                    st.setSocialSecurityNumber(response.getString("codiceFiscale"));
                    break;
                case "cognome":
                    st.setLastName(kapitalize.capitalize(response.getString("cognome")));
                    break;
                case "nome":
                    st.setFirstName(kapitalize.capitalize(response.getString("nome")));
                    break;
                case "dataDiNascita":
                    String dateBirth = response.getString("dataDiNascita");
                    if (!(dateBirth == null || dateBirth.isEmpty())) {
                        try {
                            st.setBirthDate(LocalDate.parse(response.getString("dataDiNascita"), formatter));
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                            os.log(Level.SEVERE, e);
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
                default:
                    break;
            }
        }
        return st;
    }


    static int getCertificateValue(CertificateType certificate) {
        switch (certificate) {
            case REGISTRATION:
                return 1;
            case DEGREE_WITH_EXAMS:
            case DEGREE_WITH_EXAMS_ENG:
                return 7;
            case DEGREE_WITH_THESIS:
            case DEGREE_WITH_THESIS_ENG:
                return 6;
            case DEGREE_WITH_EVALUATION:
            case DEGREE_WITH_EVALUATION_ENG:
                return 9;
            case EXAMS_COMPLETED:
                return 2;
            case DEGREE_FOR_RANSOM:
                return 8;
            default:
                return -1;
        }
    }

}
