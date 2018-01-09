package bgu.spl181.net.api.protocols;

import bgu.spl181.net.api.users.User;
import bgu.spl181.net.data.DataBase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

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

        user = ((DataBase) dataBase).getUser(username, password);
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

        User registeredUser = new User(username, password, country, new LinkedList<>(), 0, User.NORMAL);
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
        if (trySignout(message)) {
            acknowledge("signout succeeded");
        }
        else {
            reportError("signout failed");
        }
    }
    private boolean trySignout(String message) {
        if (message.split(" ").length != 1) {
            return false;
        }

        if (!userIsLoggedIn()) {
            return false;
        }

        logUserOut();
        return true;
    }

    /**
     * message format: REQUEST <name> [parameters,...]
     *
     * @param message the input to the server
     */
    @Override
    protected void request(String message) {
        String requestName = getRequestName(message);


        Map<String, Consumer<String>> normalServiceRequestCommands = new HashMap<>();

        normalServiceRequestCommands.put("balance", this::requestBalance);
        normalServiceRequestCommands.put("info", this::requestInfo);
        normalServiceRequestCommands.put("rent", this::requestRent);
        normalServiceRequestCommands.put("return", this::requestReturn);


        Map<String, Consumer<String>> adminRequestCommands = new HashMap<>();

        adminRequestCommands.put("addmovie", this::requestAddMovie);
        adminRequestCommands.put("remmovie", this::requestRemMovie);
        adminRequestCommands.put("changeprice", this::requestChangePrice);


        if (!userIsLoggedIn()) {
            reportError("request " + requestName + " failed");
            return;
        }

        if (normalServiceRequestCommands.keySet().contains(requestName)) {
            normalServiceRequestCommands.get(requestName).accept(message);
            return;
        }

        if (user.getType() != user.ADMIN) {
            reportError("request " + requestName + " failed");
            return;
        }

        if (adminRequestCommands.keySet().contains(requestName)) {
            adminRequestCommands.get(requestName).accept(message);
            return;
        }

        reportError("request " + requestName + " failed");
    }

    /**
     * message format: REQUEST info "[movie name]"
     *
     * @param message the input to the server
     */
    private void requestInfo(String message) {

    }

    /**
     * message format: REQUEST balance <info/add> [parameters,...]
     *
     * @param message the input to the server
     */
    private void requestBalance(String message) {
        Map<String, Consumer<String>> balanceRequestCommands = new HashMap<>();

        balanceRequestCommands.put("info", this::requestBalanceInfo);
        balanceRequestCommands.put("add", this::requestBalanceAdd);

        String balanceRequestType = getBalanceRequestType(message);

        if (balanceRequestCommands.keySet().contains(balanceRequestType)) {
            balanceRequestCommands.get(balanceRequestType).accept(message);
        }
        else {
            reportError("request balance failed");
        }
    }

    /**
     * message format: REQUEST balance add <amount>
     *
     * @param message the input to the server
     */
    private void requestBalanceAdd(String message) {
        if (canRequestBalanceAdd(message)) {
            int amount = getAmountOfBalanceAddRequest(message);
            acknowledge("balance " + user.getBalance() + " added " + amount);
        }
        else {
            reportError("request balance failed");
        }
    }

    private boolean canRequestBalanceAdd(String message) {
        if (message.split(" ").length != 4) {
            return false;
        }

        try {
            Integer.parseInt(message.split(" ")[3]);
        }
        catch (Exception e) {
            return false;
        }
    }

    private int getAmountOfBalanceAddRequest(String message) {
        return Integer.parseInt(message.split(" ")[3]);
    }

    /**
     * message format: REQUEST balance info
     *
     * @param message the input to the server
     */
    private void requestBalanceInfo(String message) {
        if (canRequestBalanceInfo(message)) {
            acknowledge("balance " + user.getBalance());
        }
        else {
            reportError("request balance failed");
        }
    }
    private boolean canRequestBalanceInfo(String message) {
        if (message.split(" ").length != 3) {
            return false;
        }

        if (!userIsLoggedIn()) {
            return false;
        }

        return true;
    }

    private String getBalanceRequestType(String message) {
        String[] dividedBySpaces = message.split(" ");

        if (dividedBySpaces.length < 3) {
            return "";
        }

        return dividedBySpaces[2];
    }

    /**
     * message format: REQUEST <name> [parameters,...]
     *
     * @param message the input to the server
     */
    private String getRequestName(String message) {
        String[] dividedBySpaces = message.split(" ");

        if (dividedBySpaces.length < 2) {
            return "";
        }

        return dividedBySpaces[1];
    }
}
