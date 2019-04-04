package lithium.openstud.driver.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class OpenstudBuilder {
    OpenstudHelper.Mode mode = OpenstudHelper.Mode.MOBILE;
    OpenstudHelper.Provider provider = OpenstudHelper.Provider.SAPIENZA;
    int retryCounter = 3;
    int connectTimeout = 10;
    int writeTimeout = 10;
    int readTimeout = 30;
    String studentID;
    String password;
    Logger logger;
    boolean readyState = false;
    int limitSearchResults = 13;
    int waitTimeClassroomRequest = 200;
    Map<String, String> keyMap = new HashMap<>();

    public void setLimitSearchResults(int limitSearchResults) {
        this.limitSearchResults = limitSearchResults;
    }

    public void setClassroomWaitRequest(int millis) {
        if (millis < 0) return;
        this.waitTimeClassroomRequest = millis;
    }

    public OpenstudBuilder setRetryCounter(int retryCounter) {
        this.retryCounter = retryCounter;
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

    public OpenstudBuilder setStudentID(String id) {
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

    public OpenstudBuilder forceReadyState() {
        this.readyState = true;
        return this;
    }

    public OpenstudBuilder setMode(OpenstudHelper.Mode mode) {
        this.mode = mode;
        return this;
    }

    public OpenstudBuilder setProvider(OpenstudHelper.Provider provider) {
        this.provider = provider;
        return this;
    }

    public OpenstudBuilder setKeys(Map<String, String> keyMap) {
        this.keyMap = keyMap;
        return this;
    }

    public Openstud build() {

        return new Openstud(this);
    }
}
