package lithium.openstud.driver.core.providers.sapienza;

import lithium.openstud.driver.core.OpenstudHelper;
import lithium.openstud.driver.core.internals.ProviderConfig;
import lithium.openstud.driver.core.models.CertificateType;

import java.util.HashMap;
import java.util.Map;

public class SapienzaConfig implements ProviderConfig {
    private final boolean AUTH_ENABLED = true;
    private final boolean BIO_ENABLED = true;
    private final boolean CLASSROOM_ENABLED = true;
    private final boolean EXAM_ENABLED = true;
    private final boolean NEWS_ENABLED = true;
    private final boolean TAX_ENABLED = true;
    private final boolean REFRESH_ENABLED = true;
    private final boolean SURVEY_ENABLED = true;
    private final boolean CERT_ENABLED = true;
    private final boolean CAREER_FOR_CERTIFICATE_ENABLED = true;
    private final boolean CARD_ENABLED = true;
    private final boolean PHOTO_ENABLED = true;
    private final Map<String, String> CUSTOM_KEY_MAP = new HashMap<>();
    private final CertificateType[] SUPPORTED_CERTIFICATES = new CertificateType[]{CertificateType.EXAMS_COMPLETED,
            CertificateType.DEGREE_FOR_RANSOM, CertificateType.DEGREE_WITH_EVALUATION, CertificateType.DEGREE_WITH_EVALUATION_ENG,
            CertificateType.DEGREE_WITH_EXAMS, CertificateType.DEGREE_WITH_EXAMS_ENG, CertificateType.REGISTRATION,
            CertificateType.DEGREE_WITH_THESIS, CertificateType.DEGREE_WITH_THESIS_ENG};

    @Override
    public String getEndpointAPI(OpenstudHelper.Mode mode) {
        if (mode == OpenstudHelper.Mode.MOBILE) return "https://www.studenti.uniroma1.it/phxdroidws";
        else return "https://www.studenti.uniroma1.it/phoenixws";
    }

    @Override
    public String getEndpointTimetable(OpenstudHelper.Mode mode) {
        return "https://gomp.sapienzaapps.it";
    }

    @Override
    public String getEmailURL() {
        return "https://mail.google.com/a/studenti.uniroma1.it";
    }

    @Override
    public boolean isAuthEnabled() {
        return AUTH_ENABLED;
    }

    @Override
    public boolean isClassroomEnabled() {
        return CLASSROOM_ENABLED;
    }

    @Override
    public boolean isExamEnabled() {
        return EXAM_ENABLED;
    }

    @Override
    public boolean isNewsEnabled() {
        return NEWS_ENABLED;
    }

    @Override
    public boolean isTaxEnabled() {
        return TAX_ENABLED;
    }

    @Override
    public boolean isBioEnabled() {
        return BIO_ENABLED;
    }

    @Override
    public boolean isCertEnabled() {
        return CERT_ENABLED;
    }

    @Override
    public boolean isSurveyEnabled() {
        return SURVEY_ENABLED;
    }

    @Override
    public boolean isCareerForCertificateEnabled() {
        return CAREER_FOR_CERTIFICATE_ENABLED;
    }

    @Override
    public boolean isStudentCardEnabled() {
        return CARD_ENABLED;
    }

    @Override
    public boolean isStudentPhotoEnabled() {
        return PHOTO_ENABLED;
    }

    @Override
    public boolean isRefreshEnabled() {
        return REFRESH_ENABLED;
    }

    @Override
    public boolean isCertSupported(CertificateType certificate) {
        for (CertificateType cert : SUPPORTED_CERTIFICATES) {
            if (cert == certificate) return true;
        }
        return false;
    }

    @Override
    public String getKey(OpenstudHelper.Mode mode) {
        if (mode == OpenstudHelper.Mode.WEB) return "1nf0r1cc1";
        else return "r4g4zz3tt1";
    }

    public synchronized String getKey(String key) {
        if (CUSTOM_KEY_MAP.containsKey(key)) return CUSTOM_KEY_MAP.get(key);
        else return null;
    }

    public synchronized void addKeys(Map<String, String> customKeys) {
        CUSTOM_KEY_MAP.putAll(customKeys);
    }
}
