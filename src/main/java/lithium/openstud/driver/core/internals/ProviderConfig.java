package lithium.openstud.driver.core.internals;

import lithium.openstud.driver.core.OpenstudHelper;
import java.util.Map;

public interface ProviderConfig {
    String getEndpointAPI(OpenstudHelper.Mode mode);

    String getEndpointTimetable(OpenstudHelper.Mode mode);

    boolean isAuthEnabled();

    boolean isClassroomEnabled();

    boolean isExamEnabled();

    boolean isNewsEnabled();

    boolean isTaxEnabled();

    boolean isBioEnabled();

    boolean isRefreshEnabled();

    String getKey(String key);

    void  addKeys(Map<String, String> customKeys);
    
    String getKey(OpenstudHelper.Mode mode);

}
