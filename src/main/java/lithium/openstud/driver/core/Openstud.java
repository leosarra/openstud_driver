package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.*;
import okhttp3.*;
import org.apache.commons.lang3.tuple.Pair;
import org.threeten.bp.LocalDate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Openstud implements Authenticator, Personal, NewsHandler, TaxHandler, ClassroomHandler, ExamHandler {
    private int maxTries;
    private String endpointAPI;
    private String endpointTimetable;
    private String endpointNews;
    private volatile String token;
    private String studentPassword;
    private String studentID;
    private boolean isReady;
    private Logger logger;
    private OkHttpClient client;
    private String key;
    private int waitTimeClassroomRequest;
    private int limitSearch;

    private OpenAuthenticator authenticator;
    private OpenPersonal personal;
    private OpenNewsHandler newsHandler;
    private OpenTaxHandler taxHandler;
    private OpenClassroomHandler classroomHandler;
    private OpenExamHandler examHandler;

    public Openstud() {
        super();
    }

    Openstud(OpenstudHelper.Mode mode, String webEndpoint, String endpointTimetable, String newsEndpoint, String studentID, String studentPassword,
             Logger logger, int retryCounter, int connectionTimeout,
             int readTimeout, int writeTimeout, boolean readyState, int waitTimeClassroomRequest, int limitSearch) {
        this.maxTries = retryCounter;
        this.endpointAPI = webEndpoint;
        this.studentID = studentID;
        this.endpointTimetable = endpointTimetable;
        this.endpointNews = newsEndpoint;
        this.studentPassword = studentPassword;
        this.logger = logger;
        this.isReady = readyState;
        this.waitTimeClassroomRequest = waitTimeClassroomRequest;
        this.limitSearch = limitSearch;
        if (mode == OpenstudHelper.Mode.WEB) key = "1nf0r1cc1";
        else key = "r4g4zz3tt1";
        client = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS).retryOnConnectionFailure(true)
                .build();
        authenticator = new OpenAuthenticator(this);
        personal = new OpenPersonal(this);
        newsHandler = new OpenNewsHandler(this);
        taxHandler = new OpenTaxHandler(this);
        classroomHandler = new OpenClassroomHandler(this);
        examHandler = new OpenExamHandler(this);
    }

    String getPassword() {
        return studentPassword;
    }

    String getStudentID(){
        return studentID;
    }

    String getEndpointAPI()
    {
        return endpointAPI;
    }

    String getEndpointTimetable()
    {
        return endpointTimetable;
    }

    public String getEndpointNews()
    {
        return endpointNews;
    }

    String getStudentPassword()
    {
        return studentPassword;
    }

    Logger getLogger()
    {
        return logger;
    }

    OkHttpClient getClient()
    {
        return client;
    }

    String getKey()
    {
        return key;
    }

    int getWaitTimeClassroomRequest()
    {
        return waitTimeClassroomRequest;
    }

    int getLimitSearch()
    {
        return limitSearch;
    }

    void setStudentPassword(String password)
    {
        studentPassword = password;
    }

    void setReady(boolean isReady)
    {
        this.isReady = isReady;
    }

    int getMaxTries(){
        return maxTries;
    }

    void setToken(String token) {
        this.token = token;
    }

    synchronized String getToken() {
        return this.token;
    }

    void log(Level lvl, String str) {
        if (logger != null) logger.log(lvl, str);
    }

    void log(Level lvl, Object obj) {
        if (logger != null) logger.log(lvl, obj.toString());
    }

    public boolean isReady() {
        return isReady;
    }

    @Override
    public void refreshToken() throws OpenstudRefreshException, OpenstudInvalidResponseException
    {
        authenticator.refreshToken();
    }

    @Override
    public void login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        authenticator.login();
    }

    @Override
    public String getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        return authenticator.getSecurityQuestion();
    }

    @Override
    public int recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        return authenticator.recoverPassword(answer);
    }

    @Override
    public void resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        authenticator.resetPassword(new_password);
    }

    @Override
    public int recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        return authenticator.recoverPasswordWithEmail(email, answer);
    }

    @Override
    public Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        return personal.getInfoStudent();
    }

    @Override
    public List<News> getNews(String locale, boolean withDescription, Integer limit, Integer page, Integer maxPage,
                              String query) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        return newsHandler.getNews(locale, withDescription, limit, page, maxPage, query);
    }

    @Override
    public List<Event> getNewsletterEvents() throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        return newsHandler.getNewsletterEvents();
    }

    @Override
    public List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return taxHandler.getUnpaidTaxes();
    }

    @Override
    public List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return taxHandler.getPaidTaxes();
    }

    @Override
    public Isee getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return taxHandler.getCurrentIsee();
    }

    @Override
    public List<Isee> getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return taxHandler.getIseeHistory();
    }

    @Override
    public List<Classroom> getClassRoom(String query, boolean withTimetable) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        return classroomHandler.getClassRoom(query, withTimetable);
    }

    @Override
    public List<Lesson> getClassroomTimetable(Classroom room, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException
    {
        return classroomHandler.getClassroomTimetable(room, date);
    }

    @Override
    public List<Lesson> getClassroomTimetable(int id, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException
    {
        return classroomHandler.getClassroomTimetable(id, date);
    }

    @Override
    public Map<String, List<Lesson>> getTimetable(List<ExamDoable> exams) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        return classroomHandler.getTimetable(exams);
    }

    @Override
    public List<ExamDoable> getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return examHandler.getExamsDoable();
    }

    @Override
    public List<ExamDone> getExamsDone() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return examHandler.getExamsDone();
    }

    @Override
    public List<ExamReservation> getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return examHandler.getActiveReservations();
    }

    @Override
    public List<ExamReservation> getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return examHandler.getAvailableReservations(exam, student);
    }

    @Override
    public Pair<Integer, String> insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException
    {
        return examHandler.insertReservation(res);
    }

    @Override
    public int deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException
    {
        return examHandler.deleteReservation(res);
    }

    @Override
    public byte[] getPdf(ExamReservation reservation) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return examHandler.getPdf(reservation);
    }

    @Override
    public List<Event> getCalendarEvents(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        return examHandler.getCalendarEvents(student);
    }
}