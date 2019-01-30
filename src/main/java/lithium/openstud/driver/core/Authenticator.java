package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.*;

public interface Authenticator
{
    String getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    int recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException;

    void resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    int recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException,
            OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException;

    void login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException,
            OpenstudInvalidResponseException, OpenstudUserNotEnabledException;
}
