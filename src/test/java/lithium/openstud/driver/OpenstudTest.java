package lithium.openstud.driver;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class OpenstudTest
{
    @Test
    public void testLogin() throws OpenstudEndpointNotReadyException, OpenstudInvalidPasswordException, OpenstudConnectionException, OpenstudUnexpectedServerResponseException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        assertTrue( osb.isReady() );
    }

    @Test
    public void testGetIsee() throws OpenstudEndpointNotReadyException, OpenstudInvalidPasswordException, OpenstudConnectionException, OpenstudInvalidSetupException, OpenstudUnexpectedServerResponseException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        Isee res=osb.getIsee();
        assertTrue(res!=null && res.isValid());
    }

    @Test
    public void testGetInfoStudent() throws OpenstudEndpointNotReadyException, OpenstudUnexpectedServerResponseException, OpenstudInvalidPasswordException, OpenstudConnectionException, OpenstudInvalidSetupException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        Student st=osb.getInfoStudent();
        assertTrue(st!=null && st.getStudentID()!=0);
    }
}
