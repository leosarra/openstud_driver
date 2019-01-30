package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.OpenstudConnectionException;
import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import lithium.openstud.driver.exceptions.OpenstudInvalidResponseException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface ExamHandler
{
    List<ExamDoable> getExamsDoable() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    List<ExamDone> getExamsDone() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    List<ExamReservation> getActiveReservations() throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    List<ExamReservation> getAvailableReservations(ExamDoable exam, Student student) throws OpenstudConnectionException,
            OpenstudInvalidResponseException, OpenstudInvalidCredentialsException;

    Pair<Integer, String> insertReservation(ExamReservation res) throws OpenstudInvalidResponseException,
            OpenstudConnectionException, OpenstudInvalidCredentialsException;

    int deleteReservation(ExamReservation res) throws OpenstudInvalidResponseException, OpenstudConnectionException,
            OpenstudInvalidCredentialsException;

    byte[] getPdf(ExamReservation reservation) throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

    List<Event> getCalendarEvents(Student student) throws OpenstudConnectionException, OpenstudInvalidResponseException,
            OpenstudInvalidCredentialsException;

}
