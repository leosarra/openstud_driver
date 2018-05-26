package lithium.openstud.driver;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class OpenstudAuthTest
{
    @Test
    public void testLogin() throws OpenstudInvalidUserException, OpenstudEndpointNotReadyException, OpenstudInvalidPasswordException, OpenstudConnectionException {
        Openstud osb = new OpenstudBuilder().setPassword(System.getenv("OPENSTUD_TESTPWD")).setStudentID(Integer.parseInt(System.getenv("OPENSTUD_TESTID"))).build();
        osb.login();
        assertTrue( osb.isReady() );
    }
}
