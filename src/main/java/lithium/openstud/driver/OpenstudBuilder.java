package lithium.openstud.driver;

public class OpenstudBuilder {
    private int retryCounter=3;
    private String webEndpoint="https://www.studenti.uniroma1.it/phxdroidws";
    private int connectionTimeout=5000;
    private int socketTimeout=60000;
    private int studentID;
    private String password;

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

    public Openstud build(){
        return new Openstud(webEndpoint,studentID, password,retryCounter,connectionTimeout, socketTimeout);
    }
}
