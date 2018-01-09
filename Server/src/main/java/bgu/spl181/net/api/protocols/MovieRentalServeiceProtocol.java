package bgu.spl181.net.api.protocols;

import bgu.spl181.net.api.users.NormalUser;
import bgu.spl181.net.api.users.User;
import bgu.spl181.net.data.DataBase;

import java.util.LinkedList;

/**
 * Created by avielber on 1/8/18.
 */
public class MovieRentalServeiceProtocol extends UserServeiceTextBasedProtocol {

    private User user = null;
    private boolean successfulLoginPerformed = false;

    private boolean userIsLoggedIn() {
        return user != null;
    }
    private void logUserOut() {
        user = null;
    }

    /**
     * message format: LOGIN <username> <password>
     *
     * @param message the input to the server
     */
    @Override
    protected void login(String message) {
        if (tryLogin(message)) {
            acknowledge("login succeeded");
        }
        else {
            reportError("login failed");
        }
    }
    private boolean tryLogin(String message) {
        String[] dividedBySpaces = message.split(" ");

        if (dividedBySpaces.length != 3) {
            return false;
        }

        String username = dividedBySpaces[1];
        String password = dividedBySpaces[2];

        if (successfulLoginPerformed | userIsLoggedIn()) {
            return false;
        }

        user = ((DataBase) database).getUser(username, password);
        if (!userIsLoggedIn()) {
            return false;
        }

        successfulLoginPerformed = true;
        return true;
    }

    /**
     * message format: REGISTER <username> <password> country="<country name>"</>
     *
     * @param message the input to the server
     */
    @Override
    protected void register(String message) {
        if (tryRegister(message)) {
            acknowledge("registration succeeded");
        }
        else {
            reportError("registration failed");
        }
    }

    private boolean tryRegister(String message) {
        String[] divideByQuotationMarks = message.split("\"");

        if (!(divideByQuotationMarks.length == 2 |
                (divideByQuotationMarks.length == 3 & divideByQuotationMarks[2].length() == 0))) {
            return false;
        }

        String registrationFirstPart = divideByQuotationMarks[0];
        String country = divideByQuotationMarks[1];

        String[] divideBySpaces = registrationFirstPart.split(" ");


        if (divideBySpaces.length != 4) {
            return false;
        }

        if (divideBySpaces[3] != "country=") {
            return false;
        }

        String username = divideBySpaces[1];
        String password = divideBySpaces[2];

        if (((DataBase) dataBase).userExists(username)) {
            return false;
        }

        User registeredUser = new NormalUser(username, password, country, new LinkedList<>(), 0);
        ((DataBase) dataBase).addUser(registeredUser);
        return true;
    }

    /**
     * message format: SIGNOUT
     *
     * @param message the input to the server
     */
    @Override
    protected void signout(String message) {
        if (message.split(" ").length > 1) {
            reportError("signout failed");
        }
        else {
            if (userIsLoggedIn()) {
                logUserOut();

                acknowledge("signout succeeded");
            } else {
                reportError("signout failed");
            }
        }
    }

    /**
     * message format: REQUEST <name> [parameters,...]
     *
     * @param message the input to the server
     */
    @Override
    protected void request(String message) {
        String[] dividedBySpaces = message.split(" ");

        if (dividedBySpaces.length != 2) {
            reportError("request failed");
        }
        else {

            String requestName = dividedBySpaces[1];

            boolean succeeded = true;
            if (succeeded) {
                acknowledge("request " + requestName + " succeeded"); // not correct
            } else {
                reportError("request " + requestName + " failed");
            }
        }
    }
}
