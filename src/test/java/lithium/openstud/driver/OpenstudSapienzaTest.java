package lithium.openstud.driver;

import lithium.openstud.driver.core.Openstud;
import lithium.openstud.driver.core.OpenstudBuilder;
import lithium.openstud.driver.core.OpenstudValidator;
import lithium.openstud.driver.core.models.*;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import lithium.openstud.driver.exceptions.OpenstudUserNotEnabledException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class OpenstudSapienzaTest
{

    private static boolean setUpIsDone = false;
    private static boolean invalidCredentials = false;
    private static Openstud os;

    @Before
    public void setUp() throws OpenstudConnectionException, OpenstudUserNotEnabledException, OpenstudInvalidResponseException {
        if (setUpIsDone || invalidCredentials) {
            return;
        }
        os = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(System.getenv("OPENSTUD_TESTID")).build();
        try {
            os.login();
        } catch (OpenstudInvalidCredentialsException e) {
            e.printStackTrace();
            invalidCredentials = true;
        }
        setUpIsDone = true;
    }

    @Test
    public void testPasswordValidation() throws OpenstudInvalidCredentialsException {
        String id = "12345678";
        String malformed_pwd = "No_Numbers!";
        Openstud os = new OpenstudBuilder().setPassword(malformed_pwd).setStudentID(id).build();
        assertFalse(OpenstudValidator.validate(os));
        malformed_pwd = "NoSp3ci4l";
        os = new OpenstudBuilder().setPassword(malformed_pwd).setStudentID(id).build();
        assertFalse(OpenstudValidator.validate(os));
        malformed_pwd = "2 Spaces !";
        os = new OpenstudBuilder().setPassword(malformed_pwd).setStudentID(id).build();
        assertFalse(OpenstudValidator.validate(os));
        malformed_pwd = "Long_Password_17!";
        os = new OpenstudBuilder().setPassword(malformed_pwd).setStudentID(id).build();
        assertFalse(OpenstudValidator.validate(os));
        String correct_pwd = "#8_Chars";
        os = new OpenstudBuilder().setPassword(correct_pwd).setStudentID(id).build();
        assertTrue(OpenstudValidator.validate(os));
        correct_pwd = "[.!-=?#@]1aA";
        os = new OpenstudBuilder().setPassword(correct_pwd).setStudentID(id).build();
        assertTrue(OpenstudValidator.validate(os));
    }

    @Test
    public void testUserIDValidation() throws OpenstudInvalidCredentialsException {
        Openstud os = new OpenstudBuilder().setPassword("Perfect_psw1").build();
        assertFalse(OpenstudValidator.validate(os));
    }

    @Test
    public void testLogin() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        assertTrue( os.isReady() );
    }

    @Test
    public void testGetSecurityQuestion() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException {
        Openstud os = new OpenstudBuilder().setStudentID(System.getenv("OPENSTUD_TESTID")).build();
        assertNotNull(os.getSecurityQuestion());
    }

    @Test
    public void testGetCalendar() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        Student st = os.getInfoStudent();
        os.getCalendarEvents(st);
    }

    @Test
    public void testGetIsee() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        Isee res=os.getCurrentIsee();
        assertTrue(res!=null && res.isValid());
    }

    @Test
    public void testGetIseeHistory() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        List<Isee> res=os.getIseeHistory();
        assertTrue(res!=null && res.size()!=0);
    }

    @Test
    public void testGetInfoStudent() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Student st=os.getInfoStudent();
        assertTrue(st!=null && st.getStudentID()!=null);
    }

    @Test
    public void testGetExamsDoable() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<ExamDoable> list=os.getExamsDoable();
        assertNotNull(list);
    }

    @Test
    public void testGetTimetable() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<ExamDoable> list=os.getExamsDoable();
        Map<String, List<Lesson>> map = os.getTimetable(list);
        assertNotNull(map);
    }


    @Test
    public void testGetExamsPassed() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<ExamDone> list=os.getExamsDone();
        assertNotNull(list);
    }

    @Test
    public void testGetActiveReservations() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<ExamReservation> list=os.getActiveReservations();
        assertNotNull(list);
    }

    @Test
    public void testClassroomInfos() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<Classroom> list=os.getClassRoom("San pietro", true);
        assertNotNull(list);
    }


    @Test
    public void testGetAvailableReservations() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<ExamDoable> list=os.getExamsDoable();
        Student st=os.getInfoStudent();
        if(list.size()>=1) {
            List<ExamReservation> ret=os.getAvailableReservations(list.get(0),st);
            assertNotNull(ret);
        }
        assertTrue(true);
    }

    @Test
    public void testGetPdf() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<ExamReservation> list=os.getActiveReservations();
        if(list.size()>=1) {
            byte[] pdf = os.getPdf(list.get(0));
            assertNotNull(pdf);
        }
        assertTrue(true);
    }

    @Test
    public void testGetPaidTaxes() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<Tax> list=os.getPaidTaxes();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testGetNewsEnglish() throws OpenstudInvalidResponseException, OpenstudConnectionException {
        List<News> list=os.getNews("en", true, null, 0, null, null);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testGetNewsItalian() throws OpenstudInvalidResponseException, OpenstudConnectionException {
        List<News> list=os.getNews("it", true, null, 0, null, null);
        assertNotNull(list);
        int limit = 5;
        list=os.getNews("it", true, limit, null, null, null);
        assertFalse(list.isEmpty());
        assertTrue(list.size() <= limit);
    }


    @Test
    public void testGetNewsEventsEnglish() throws OpenstudInvalidResponseException, OpenstudConnectionException {
        List<Event> list=os.getNewsletterEvents();
        assertNotNull(list);
    }

    @Test
    public void testGetUnpaidTaxes() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        List<Tax> list=os.getUnpaidTaxes();
        assertNotNull(list);
    }

    @Test
    public void testGetSurvey() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        assertNotNull(os.getCourseSurvey("82WQLAN9"));
    }

    @Test
    public void testGetCertificatePDF() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException {
        Student student = os.getInfoStudent();
        List<Career> careers = os.getCareersChoicesForCertificate(student, CertificateType.DEGREE_WITH_THESIS_ENG);
        assertNotNull(careers);
        assertNotNull(os.getCertificatePDF(student,careers.get(0), CertificateType.DEGREE_WITH_THESIS_ENG));
    }

    @Test
    public void testGetPhoto() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException {
        Student student = os.getInfoStudent();
        os.getStudentPhoto(student);
    }

    @Test
    public void testGetCard() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException {
        Student student = os.getInfoStudent();
        StudentCard card = os.getStudentCard(student, false);
    }
}
