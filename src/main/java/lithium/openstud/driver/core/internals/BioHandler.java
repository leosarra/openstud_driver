package lithium.openstud.driver.core.internals;

import lithium.openstud.driver.core.models.Career;
import lithium.openstud.driver.core.models.CertificateType;
import lithium.openstud.driver.core.models.Student;
import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;

import java.util.List;


public interface BioHandler {
    Student getInfoStudent() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    List<Career> getCareersChoicesForCertificate(Student student, CertificateType certificate) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException;
    byte[] getCertificatePDF(Student student, Career career, CertificateType certificate) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException;
    byte[] getStudentPhoto(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException, OpenstudInvalidCredentialsException;
}