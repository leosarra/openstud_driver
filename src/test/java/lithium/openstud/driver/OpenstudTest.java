package lithium.openstud.driver;

import lithium.openstud.driver.core.*;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import lithium.openstud.driver.exceptions.OpenstudUserNotEnabledException;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class OpenstudTest
{
    @Test
    public void testLogin() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        assertTrue( osb.isReady() );
    }

    @Test
    public void testGetIsee() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        Isee res=osb.getCurrentIsee();
        assertTrue(res!=null && res.isValid());
    }

    @Test
    public void testGetIseeHistory() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        List<Isee> res=osb.getIseeHistory();
        assertTrue(res!=null && res.size()!=0);
    }

    @Test
    public void testGetInfoStudent() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        Student st=osb.getInfoStudent();
        assertTrue(st!=null && st.getStudentID()!=0);
    }

    @Test
    public void testGetExamsDoable() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        List<ExamDoable> list=osb.getExamsDoable();
        assertNotNull(list);
    }

    @Test
    public void testGetExamsPassed() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        List<ExamDone> list=osb.getExamsDone();
        assertNotNull(list);
    }

    @Test
    public void testGetActiveReservations() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        List<ExamReservation> list=osb.getActiveReservations();
        assertNotNull(list);
    }

    @Test
    public void testGetAvailableReservations() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        List<ExamDoable> list=osb.getExamsDoable();
        Student st=osb.getInfoStudent();
        if(list.size()>=1) {
            List<ExamReservation> ret=osb.getAvailableReservations(list.get(0),st);
            assertNotNull(ret);
        }
        assertTrue(true);
    }

    @Test
    public void testGetPdf() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        List<ExamReservation> list=osb.getActiveReservations();
        if(list.size()>=1) {
            byte[] pdf = osb.getPdf(list.get(0));
            assertNotNull(pdf);
        }
        assertTrue(true);
    }

    @Test
    public void testGetPaidTaxes() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        List<Tax> list=osb.getPaidTaxes();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testGetUnpaidTaxes() throws OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudUserNotEnabledException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        List<Tax> list=osb.getUnpaidTaxes();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }
}
