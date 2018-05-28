package lithium.openstud.driver.core;

import java.util.logging.Logger;

public class OpenstudBuilder {
    private int retryCounter=3;
    private String webEndpoint="https://www.studenti.uniroma1.it/phxdroidws";
    private int connectionTimeout=5000;
    private int socketTimeout=60000;
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

    public OpenstudBuilder setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    public OpenstudBuilder setSocketTimeout(int socketTimeout){
        this.socketTimeout=socketTimeout;
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
        return new Openstud(webEndpoint,studentID, password, logger, retryCounter,connectionTimeout, socketTimeout);
    }
}
