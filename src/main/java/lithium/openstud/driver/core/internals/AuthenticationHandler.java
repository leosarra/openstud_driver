package lithium.openstud.driver.core.internals;

import lithium.openstud.driver.exceptions.*;

public interface AuthenticationHandler
{
    String getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    boolean recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException;

    void resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    boolean recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException,
            OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException;

    void login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException,
            OpenstudInvalidResponseException, OpenstudUserNotEnabledException;

    void refreshToken() throws OpenstudRefreshException, OpenstudInvalidResponseException;

    }
