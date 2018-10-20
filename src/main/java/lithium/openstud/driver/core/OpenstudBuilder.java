package lithium.openstud.driver.core;

import java.util.logging.Logger;

public class OpenstudBuilder {
    private int retryCounter = 3;
    private String webEndpoint = "https://www.studenti.uniroma1.it/phxdroidws";
    private int connectTimeout = 10;
    private int writeTimeout = 10;
    private int readTimeout = 30;
    private int studentID = -1;
    private String password;
    private Logger logger;
    private boolean readyState = false;
    private OpenstudHelper.Mode mode = OpenstudHelper.Mode.MOBILE;

    public OpenstudBuilder setRetryCounter(int retryCounter) {
        this.retryCounter = retryCounter;
        return this;
    }

    public OpenstudBuilder setWebEndpoint(String webEndpoint) {
        this.webEndpoint = webEndpoint;
        return this;
    }

    public OpenstudBuilder setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public OpenstudBuilder setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public OpenstudBuilder setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public OpenstudBuilder setStudentID(int id) {
        this.studentID = id;
        return this;
    }

    public OpenstudBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public OpenstudBuilder setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public OpenstudBuilder validatePassword() throws OpenstudInvalidCredentialsException {
        String nice_path = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,16}$";
        if (this.password.matches(nice_path))
            return this;
        throw new OpenstudInvalidCredentialsException("This password is not valid");
    }

    public OpenstudBuilder validateUserID() throws OpenstudInvalidCredentialsException {
        if (this.studentID == -1)
            throw new OpenstudInvalidCredentialsException("UserID cannot be left empty");
        return this;
    }

    public OpenstudBuilder validate() throws OpenstudInvalidCredentialsException {
        return this.validatePassword().validateUserID();
    }

    public OpenstudBuilder forceReadyState() {
        this.readyState = true;
        return this;
    }

    public OpenstudBuilder setMode(OpenstudHelper.Mode mode) {
        this.mode = mode;
        return this;
    }

    public Openstud build() {
        if (mode == OpenstudHelper.Mode.MOBILE) webEndpoint = "https://www.studenti.uniroma1.it/phxdroidws";
        else if (mode == OpenstudHelper.Mode.WEB) webEndpoint = "https://www.studenti.uniroma1.it/phoenixws";
        return new Openstud(webEndpoint, studentID, password, logger, retryCounter, connectTimeout, readTimeout, writeTimeout, readyState, mode);
    }
}
