package lithium.openstud.driver.core.internals;

import lithium.openstud.driver.core.models.Isee;
import lithium.openstud.driver.core.models.Tax;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;

import java.util.List;

public interface TaxHandler {
    List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    byte[] getPaymentSlip(Tax unpaidTax) throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    Isee getCurrentIsee() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    List<Isee> getIseeHistory() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;


}
