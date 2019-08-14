package lithium.openstud.driver.core;

import lithium.openstud.driver.exceptions.OpenstudInvalidCredentialsException;

public class OpenstudValidator {

    public static boolean validatePassword(Openstud os) {
        return validatePassword(os, os.getStudentPassword());
    }

    public static boolean validatePassword(Openstud os, String password) {
        OpenstudHelper.Provider provider = os.getProvider();
        String nice_path;
        if (provider == OpenstudHelper.Provider.SAPIENZA) {
            nice_path = "^" +   // start
                    "(?=.*[0-9])" +   // at least one digit
                    "(?=.*[a-z])" +   // at least one lower case letter
                    "(?=.*[A-Z])" +   // at least one upper case letter
                    "(?=.*[\\[\\]*?.@#$%^&!=_-])" +  // =.][#?!@$%^&*_-
                    "(?=\\S+$)" + // no spaces
                    ".{8,16}" + // length in [8, 16]
                    "$";   // end
        } else {
            throw new IllegalArgumentException("Password validation not supported for this provider");
        }
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
