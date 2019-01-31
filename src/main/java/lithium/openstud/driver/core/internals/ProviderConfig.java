package lithium.openstud.driver.core.internals;

import lithium.openstud.driver.core.OpenstudHelper;

public interface ProviderConfig {

    boolean isAuthEnabled();

    boolean isClassroomEnabled();

    boolean isExamEnabled();

    boolean isNewsEnabled();

    boolean isTaxEnabled();

    boolean isBioEnabled();

    boolean isRefreshEnabled();

    String getKey(OpenstudHelper.Mode mode);

}
