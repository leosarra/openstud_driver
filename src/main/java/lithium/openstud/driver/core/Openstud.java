package lithium.openstud.driver.core;

import lithium.openstud.driver.core.internals.*;
import lithium.openstud.driver.core.models.*;
import lithium.openstud.driver.core.providers.sapienza.*;
import lithium.openstud.driver.exceptions.*;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.tuple.Pair;
import org.threeten.bp.LocalDate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Openstud implements AuthenticationHandler, BioHandler, NewsHandler, TaxHandler, ClassroomHandler, ExamHandler {
    private int maxTries;
    private String endpointAPI;
    private String endpointTimetable;
    private volatile String token;
    private String studentPassword;
    private String studentID;
    private boolean isReady;
    private Logger logger;
    private OkHttpClient client;
    private String key;
    private int waitTimeClassroomRequest;
    private int limitSearch;
    private OpenstudHelper.Provider provider;
    private AuthenticationHandler authenticator;
    private BioHandler personal;
    private NewsHandler newsHandler;
    private TaxHandler taxHandler;
    private ClassroomHandler classroomHandler;
    private ExamHandler examHandler;
    private ProviderConfig config;
    private OpenstudHelper.Mode mode;

    public Openstud() {
        super();
    }

    Openstud(OpenstudBuilder builder) {
        this.provider = builder.provider;
        this.maxTries = builder.retryCounter;
        this.studentID = builder.studentID;
        this.studentPassword = builder.password;
        this.logger = builder.logger;
        this.isReady = builder.readyState;
        this.waitTimeClassroomRequest = builder.waitTimeClassroomRequest;
        this.limitSearch = builder.limitSearchResults;
        this.mode = builder.mode;
        client = new OkHttpClient.Builder()
                .connectTimeout(builder.connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(builder.writeTimeout, TimeUnit.SECONDS)
                .readTimeout(builder.readTimeout, TimeUnit.SECONDS).retryOnConnectionFailure(true)
                .connectionSpecs(Collections.singletonList(ConnectionSpec.COMPATIBLE_TLS))
                .build();
        init();
        config.addKeys(builder.keyMap);
    }

    private void init() {
        if (provider == null) throw new IllegalArgumentException("Provider can't be left null");
        else if (provider == OpenstudHelper.Provider.SAPIENZA) {
            authenticator = new SapienzaAuthenticationHandler(this);
            personal = new SapienzaBioHandler(this);
            newsHandler = new SapienzaNewsHandler(this);
            taxHandler = new SapienzaTaxHandler(this);
            classroomHandler = new SapienzaClassroomHandler(this);
            examHandler = new SapienzaExamHandler(this);
            config = new SapienzaConfig();
        }
        endpointAPI = config.getEndpointAPI(mode);
        endpointTimetable = config.getEndpointTimetable(mode);
        key = config.getKey(mode);
    }

    String getPassword() {
        return studentPassword;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getEndpointAPI() {
        return endpointAPI;
    }

    public String getEndpointTimetable() {
        return endpointTimetable;
    }

    public String getStudentPassword() {
        return studentPassword;
    }

    public Logger getLogger() {
        return logger;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public String getKey() {
        return key;
    }

    public String getKey(String keyName){
        return config.getKey(keyName);
    }

    public int getWaitTimeClassroomRequest() {
        return waitTimeClassroomRequest;
    }

    public int getLimitSearch() {
        return limitSearch;
    }

    public void setStudentPassword(String password) {
        studentPassword = password;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public int getMaxTries() {
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
    public synchronized void refreshToken() throws OpenstudRefreshException, OpenstudInvalidResponseException {
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
                              String query) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        if (!config.isNewsEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return newsHandler.getNews(locale, withDescription, limit, page, maxPage, query);
    }

    @Override
    public List<Event> getNewsletterEvents() throws OpenstudInvalidResponseException, OpenstudConnectionException {
        if (!config.isNewsEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return newsHandler.getNewsletterEvents();
    }

    @Override
    public List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isTaxEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return taxHandler.getUnpaidTaxes();
    }

    @Override
    public List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isTaxEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return taxHandler.getPaidTaxes();
    }

    @Override
    public Isee getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isTaxEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return taxHandler.getCurrentIsee();
    }

    @Override
    public List<Isee> getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isTaxEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return taxHandler.getIseeHistory();
    }

    @Override
    public List<Classroom> getClassRoom(String query, boolean withTimetable) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        if (!config.isClassroomEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return classroomHandler.getClassRoom(query, withTimetable);
    }

    @Override
    public List<Lesson> getClassroomTimetable(Classroom room, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (!config.isClassroomEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return classroomHandler.getClassroomTimetable(room, date);
    }

    @Override
    public List<Lesson> getClassroomTimetable(int id, LocalDate date) throws OpenstudConnectionException, OpenstudInvalidResponseException {
        if (!config.isClassroomEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return classroomHandler.getClassroomTimetable(id, date);
    }

    @Override
    public Map<String, List<Lesson>> getTimetable(List<ExamDoable> exams) throws OpenstudInvalidResponseException, OpenstudConnectionException {
        if (!config.isClassroomEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return classroomHandler.getTimetable(exams);
    }

    @Override
    public List<ExamDoable> getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getExamsDoable();
    }

    @Override
    public List<ExamDone> getExamsDone() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getExamsDone();
    }

    @Override
    public List<ExamReservation> getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getActiveReservations();
    }

    @Override
    public List<ExamReservation> getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getAvailableReservations(exam, student);
    }

    @Override
    public Pair<Integer, String> insertReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.insertReservation(res);
    }

    @Override
    public int deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException, OpenstudInvalidCredentialsException {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.deleteReservation(res);
    }

    @Override
    public byte[] getPdf(ExamReservation reservation) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getPdf(reservation);
    }

    @Override
    public List<Event> getCalendarEvents(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException {
        if (!config.isExamEnabled()) throw new IllegalStateException("Provider doesn't support this feature");
        return examHandler.getCalendarEvents(student);
    }
}