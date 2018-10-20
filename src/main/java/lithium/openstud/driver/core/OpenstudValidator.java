package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;

public class OpenstudValidator {

    public static boolean validatePassword(Openstud os) {
        return validatePassword(os.getPassword());
    }

    public static boolean validatePassword(String password) {
        String nice_path = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,16}$";
        return password != null && password.matches(nice_path);
    }

    //to be used when password is forgotten
    public static boolean validateUserID(Openstud os) throws OpenstudInvalidCredentialsException {
        return os.getStudentID() != null;
    }

    public static boolean validate(Openstud os) throws OpenstudInvalidCredentialsException {
        return validatePassword(os) && validateUserID(os);
    }
}
