package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;

import java.util.List;

public interface TaxHandler
{
    List<Tax> getUnpaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    List<Tax> getPaidTaxes() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;


    }
