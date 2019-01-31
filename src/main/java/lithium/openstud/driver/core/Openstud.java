package lithium.openstud.driver.core;

import lithium.openstud.driver.core.internals.*;
import lithium.openstud.driver.core.models.*;
import lithium.openstud.driver.core.providers.sapienza.*;
import lithium.openstud.driver.exceptions.*;
import okhttp3.*;
import org.apache.commons.lang3.tuple.Pair;
import org.threeten.bp.LocalDate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Openstud implements AuthenticationHandler, BioHandler, NewsHandler, TaxHandler, ClassroomHandler, ExamHandler
{
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

    private AuthenticationHandler authenticator;
    private BioHandler personal;
    private NewsHandler newsHandler;
    private TaxHandler taxHandler;
    private ClassroomHandler classroomHandler;
    private ExamHandler examHandler;
    private ProviderConfig config;

    public Openstud() {
        super();
    }

    Openstud(OpenstudHelper.Mode mode, String webEndpoint, String endpointTimetable, String newsEndpoint, String studentID, String studentPassword,
             Logger logger, int retryCounter, int connectionTimeout,
             int readTimeout, int writeTimeout, boolean readyState, int waitTimeClassroomRequest, int limitSearch) {
        init();
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
        key = config.getKey(mode);
        client = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS).retryOnConnectionFailure(true)
                .build();
    }

    private void init(){
        authenticator = new SapienzaAuthenticationHandler(this);
        personal = new SapienzaBioHandler(this);
        newsHandler = new SapienzaNewsHandler(this);
        taxHandler = new SapienzaTaxHandler(this);
        classroomHandler = new SapienzaClassroomHandler(this);
        examHandler = new SapienzaExamHandler(this);
        config= new SapienzaConfig();
    }

    String getPassword() {
        return studentPassword;
    }

    public String getStudentID(){
        return studentID;
    }

    public String getEndpointAPI()
    {
        return endpointAPI;
    }

    public String getEndpointTimetable()
    {
        return endpointTimetable;
    }

    public String getEndpointNews()
    {
        return endpointNews;
    }

    public String getStudentPassword()
    {
        return studentPassword;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public OkHttpClient getClient()
    {
        return client;
    }

    public String getKey()
    {
        return key;
    }

    public int getWaitTimeClassroomRequest()
    {
        return waitTimeClassroomRequest;
    }

    public int getLimitSearch()
    {
        return limitSearch;
    }

    public void setStudentPassword(String password)
    {
        studentPassword = password;
    }

    public void setReady(boolean isReady)
    {
        this.isReady = isReady;
    }

    public int getMaxTries(){
        return maxTries;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public synchronized String getToken() {
        return this.token;
    }

    void log(Level lvl, String str) {
        if (logger != null) logger.log(lvl, str);
    }

    public void log(Level lvl, Object obj) {
        if (logger != null) logger.log(lvl, obj.toString());
    }

    public boolean isReady() {
        return isReady;
    }

    @Override
    public void refreshToken() throws OpenstudRefreshException, OpenstudInvalidResponseException
    {
        if (!config.isRefreshEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        authenticator.refreshToken();
    }

    @Override
    public void login() throws OpenstudInvalidCredentialsException, OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudUserNotEnabledException {
        if (!config.isAuthEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        authenticator.login();
    }

    @Override
    public String getSecurityQuestion() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isAuthEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return authenticator.getSecurityQuestion();
    }

    @Override
    public boolean recoverPassword(String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        if (!config.isAuthEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return authenticator.recoverPassword(answer);
    }

    @Override
    public void resetPassword(String new_password) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isAuthEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        authenticator.resetPassword(new_password);
    }

    @Override
    public boolean recoverPasswordWithEmail(String email, String answer) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException, OpenstudInvalidAnswerException {
        if (!config.isAuthEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return authenticator.recoverPasswordWithEmail(email, answer);
    }

    @Override
    public Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isBioEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return personal.getInfoStudent();
    }

    @Override
    public List<News> getNews(String locale, boolean withDescription, Integer limit, Integer page, Integer maxPage,
                              String query) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        if (!config.isNewsEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return newsHandler.getNews(locale, withDescription, limit, page, maxPage, query);
    }

    @Override
    public List<Event> getNewsletterEvents() throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        if (!config.isNewsEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return newsHandler.getNewsletterEvents();
    }

    @Override
    public List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isTaxEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return taxHandler.getUnpaidTaxes();
    }

    @Override
    public List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isTaxEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return taxHandler.getPaidTaxes();
    }

    @Override
    public Isee getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isTaxEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return taxHandler.getCurrentIsee();
    }

    @Override
    public List<Isee> getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isTaxEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return taxHandler.getIseeHistory();
    }

    @Override
    public List<Classroom> getClassRoom(String query, boolean withTimetable) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        if (!config.isClassroomEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return classroomHandler.getClassRoom(query, withTimetable);
    }

    @Override
    public List<Lesson> getClassroomTimetable(Classroom room, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException
    {
        if (!config.isClassroomEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return classroomHandler.getClassroomTimetable(room, date);
    }

    @Override
    public List<Lesson> getClassroomTimetable(int id, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException
    {
        if (!config.isClassroomEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return classroomHandler.getClassroomTimetable(id, date);
    }

    @Override
    public Map<String, List<Lesson>> getTimetable(List<ExamDoable> exams) throws OpenstudInvalidResponseException, OpenstudConnectionException
    {
        if (!config.isClassroomEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return classroomHandler.getTimetable(exams);
    }

    @Override
    public List<ExamDoable> getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getExamsDoable();
    }

    @Override
    public List<ExamDone> getExamsDone() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getExamsDone();
    }

    @Override
    public List<ExamReservation> getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getActiveReservations();
    }

    @Override
    public List<ExamReservation> getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getAvailableReservations(exam, student);
    }

    @Override
    public Pair<Integer, String> insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException
    {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.insertReservation(res);
    }

    @Override
    public int deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException
    {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.deleteReservation(res);
    }

    @Override
    public byte[] getPdf(ExamReservation reservation) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getPdf(reservation);
    }

    @Override
    public List<Event> getCalendarEvents(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException
    {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getCalendarEvents(student);
    }
}