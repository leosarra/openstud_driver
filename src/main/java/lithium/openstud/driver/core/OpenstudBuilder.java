package lithium.openstud.driver.core;

import java.util.logging.Logger;

public class OpenstudBuilder {
    private int retryCounter=3;
    private String webEndpoint="https://www.studenti.uniroma1.it/phxdroidws";
    private int connectTimeout=10;
    private int writeTimeout=10;
    private int readTimeout=30;
    private int studentID = -1;
    private String password;
    private Logger logger;

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
    public OpenstudBuilder setReadTimeout(int readTimeout){
        this.readTimeout=readTimeout;
        return this;
    }
    public OpenstudBuilder setWriteTimeout(int writeTimeout){
        this.writeTimeout=writeTimeout;
        return this;
    }
    public OpenstudBuilder setStudentID(int id){
        this.studentID=id;
        return this;
    }
    public OpenstudBuilder setPassword(String password){
        this.password=password;
        return this;
    }

    public OpenstudBuilder setLogger(Logger logger){
        this.logger=logger;
        return this;
    }
    public Openstud build(){
        return new Openstud(webEndpoint,studentID, password, logger, retryCounter,connectTimeout, readTimeout, writeTimeout);
    }
}