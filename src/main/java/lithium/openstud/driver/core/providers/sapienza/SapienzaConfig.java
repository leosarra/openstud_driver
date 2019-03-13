package lithium.openstud.driver.core.providers.sapienza;
import java.util.HashMap;
import java.util.Map;
import lithium.openstud.driver.core.OpenstudHelper;
import lithium.openstud.driver.core.internals.ProviderConfig;

public class SapienzaConfig implements ProviderConfig {
    private final boolean AUTH_ENABLED = true;
    private final boolean BIO_ENABLED = true;
    private final boolean CLASSROOM_ENABLED = true;
    private final boolean EXAM_ENABLED = true;
    private final boolean NEWS_ENABLED = true;
    private final boolean TAX_ENABLED = true;
    private final boolean REFRESH_ENABLED = true;
    private final Map<String,String> CUSTOM_KEY_MAP = new HashMap<>();
    @Override
    public String getEndpointAPI(OpenstudHelper.Mode mode) {
        if (mode == OpenstudHelper.Mode.MOBILE) return "https://www.studenti.uniroma1.it/phxdroidws";
        else return "https://www.studenti.uniroma1.it/phoenixws";
    }

    @Override
    public String getEndpointTimetable(OpenstudHelper.Mode mode) {
        return "https://gomp.sapienzaapps.it";
    }

    public boolean isAuthEnabled() {
        return AUTH_ENABLED;
    }

    public boolean isClassroomEnabled() {
        return CLASSROOM_ENABLED;
    }

    public boolean isExamEnabled() {
        return EXAM_ENABLED;
    }

    public boolean isNewsEnabled() {
        return NEWS_ENABLED;
    }

    public boolean isTaxEnabled() {
        return TAX_ENABLED;
    }

    public boolean isBioEnabled() {
        return BIO_ENABLED;
    }

    public boolean isRefreshEnabled() {
        return REFRESH_ENABLED;
    }

    @Override
    public String getKey(OpenstudHelper.Mode mode) {
        if (mode == OpenstudHelper.Mode.WEB) return "1nf0r1cc1";
        else return "r4g4zz3tt1";
    }

    public synchronized String getKey(String key){
        return CUSTOM_KEY_MAP.getOrDefault(key, null);
    }

    public synchronized void setKeys(Map<String,String> customKeys){
        CUSTOM_KEY_MAP.putAll(customKeys);
    }
}
