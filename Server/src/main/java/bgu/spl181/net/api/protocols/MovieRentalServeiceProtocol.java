package bgu.spl181.net.api.protocols;

import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.api.users.Movie;
import bgu.spl181.net.api.users.User;
import bgu.spl181.net.data.DataBase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by avielber on 1/8/18.
 */
public class MovieRentalServeiceProtocol extends UserServeiceTextBasedProtocol {

    private User user = null;

    public MovieRentalServeiceProtocol(DataBase dataBase) {
        super(dataBase);
    }

    private boolean userIsLoggedIn() {
        return user != null;
    }
    private void logUserOut() {
        dataBase.users.free(user);
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

        if (userIsLoggedIn()) {
            return false;
        }

        if (!dataBase.users.isFree(user = dataBase.users.get(User.equalsByUsername(username).and(User.equalsByPassword(password))))) {
            return false;
        }


        dataBase.users.takeHostage(user);
        connections.enableBroadcast(connectionId);
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

        if (divideBySpaces[3].equals("country=")) {
            return false;
        }

        String username = divideBySpaces[1];
        String password = divideBySpaces[2];

        if (dataBase.users.exists(User.equalsByUsername(username))) {
            return false;
        }

        User registeredUser = new User(username, password, country, new LinkedList<>(), 0, User.NORMAL);
        return dataBase.users.add(registeredUser) != null;
    }

    /**
     * message format: SIGNOUT
     *
     * @param message the input to the server
     */
    @Override
    protected void signout(String message) {
        if (trySignout(message)) {
            connections.disableBroadcast(connectionId);
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

        if (!user.getType().equals(User.ADMIN)) {
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
     * message format: REQUEST changeprice <”movie name”> <price>
     *
     * @param message the input to the server
     */
    private void requestChangePrice(String message) {
        int amountOfQuotationMarks = count(message, "\"");

        if (amountOfQuotationMarks != 2) {
            reportError("request changeprice failed");
            return;
        }

        String[] divideByQuotationMarks = message.split("\"");

//        String REQUEST_changeprice_ = divideByQuotationMarks[0]; // not used
        String movieName = divideByQuotationMarks[1];
        String _price_ = divideByQuotationMarks[2];

        if (!_price_inCorrectForm(_price_)) {
            reportError("request addmovie failed");
            return;
        }
        int price = getPriceFrom_price_(_price_);

        Movie movie = dataBase.movies.get(Movie.equalsByName(movieName));
        if (movie == null) {
            reportError("request addmovie failed");
            return;
        }

        if (price <= 0) {
            reportError("request addmovie failed");
            return;
        }


        movie = new Movie(movie.getId(), movieName,
                price,
                movie.getBannedCountries(),
                movie.getAvailableAmount(), movie.getTotalAmount());

        dataBase.movies.update(movie, movie.equalsById());
        acknowledge("addmovie \"" + movieName + "\" success");
        broadcast("movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + movie.getPrice());
    }

    private boolean _price_inCorrectForm(String _price_) {
        int amountOfSpaces = count(_price_, " ");
        if (amountOfSpaces != 2) {
            return false;
        }

        String[] dividedBySpaces = _price_.split(" ");
        if (!dividedBySpaces[0].equals("")) {
            return false;
        }
        if (!dividedBySpaces[2].equals("")) {
            return false;
        }

        try {
            Integer.parseInt(dividedBySpaces[1]);
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }

    private int getPriceFrom_price_(String _price_) {
        String[] dividedBySpaces = _price_.split(" ");
        return Integer.parseInt(dividedBySpaces[1]);
    }

    /**
     * message format: REQUEST remmovie <”movie name”>
     *
     * @param message the input to the server
     */
    private void requestRemMovie(String message) {
        int amountOfQuotationMarks = count(message, "\"");

        if (amountOfQuotationMarks != 2) {
            reportError("request remmovie failed");
            return;
        }

        String[] divideByQuotationMarks = message.split("\"");

//        String REQUEST_remmovie_ = divideByQuotationMarks[0]; // not used
        String movieName = divideByQuotationMarks[1];

        if (movieName.equals("")) {
            reportError("request remmovie failed");
            return;
        }

        if (!divideByQuotationMarks[2].equals("")) {
            reportError("request remmovie failed");
            return;
        }

        if (!dataBase.movies.exists(Movie.equalsByName(movieName))) {
            reportError("request remmovie failed");
            return;
        }


        dataBase.movies.delete(Movie.equalsByName(movieName));
        acknowledge("remmovie \"" + movieName + "\" success");
        broadcast("movie \"" + movieName + "\" removed");
    }

    /**
     * message format: REQUEST addmovie <”movie name”> <amount> <price> [“banned country”,…]
     *
     * @param message the input to the server
     */
    private void requestAddMovie(String message) {
        int amountOfQuotationMarks = count(message, "\"");

        if (amountOfQuotationMarks % 2 != 0) {
            reportError("request addmovie failed");
            return;
        }

        if (amountOfQuotationMarks == 0) {
            reportError("request addmovie failed");
            return;
        }

        String[] divideByQuotationMarks = message.split("\"");

//        String REQUEST_addmovie_ = divideByQuotationMarks[0]; // not used
        String movieName = divideByQuotationMarks[1];
        String _amount_price_ = divideByQuotationMarks[2];

        if (!_amount_price_inCorrectForm(_amount_price_)) {
            reportError("request addmovie failed");
            return;
        }
        int amount = getAmountFrom_amount_price_(_amount_price_);
        int price = getPriceFrom_amount_price_(_amount_price_);

        if (!bannedCountriesAreInCorrectForm(divideByQuotationMarks)) {
            reportError("request addmovie failed");
            return;
        }
        List<String> bannedCountries = getBannedCountriesFromRequestAddmovieSplittedString(divideByQuotationMarks);

        if (dataBase.movies.exists(Movie.equalsByName(movieName))) {
            reportError("request addmovie failed");
            return;
        }

        if (price <= 0) {
            reportError("request addmovie failed");
            return;
        }

        if (amount <= 0) {
            reportError("request addmovie failed");
            return;
        }


        Movie movie = new Movie(dataBase.movies.newId(),
                movieName, price, bannedCountries, amount, amount);

        if (dataBase.movies.add(movie) == null) {
            reportError("request addmovie failed");
            return;
        }

        acknowledge("addmovie \"" + movieName + "\" success");
        broadcast("movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + movie.getPrice());
    }

    private boolean _amount_price_inCorrectForm(String _amount_price_) {
        int amountOfSpaces = count(_amount_price_, " ");

        if (amountOfSpaces != 3 & amountOfSpaces != 2) {
            return false;
        }

        String[] dividedBySpaces = _amount_price_.split(" ");
        try {
            Integer.parseInt(dividedBySpaces[1]);
            Integer.parseInt(dividedBySpaces[2]);
        }
        catch (Exception e) {
            return false;
        }

        if (!dividedBySpaces[0].equals("")) {
            return false;
        }

        if (amountOfSpaces == 3 & dividedBySpaces[3].length() != 0) {
            return false;
        }

        return true;
    }

    private int getAmountFrom_amount_price_(String _amount_price_) {
        String[] dividedBySpaces = _amount_price_.split(" ");
        return Integer.parseInt(dividedBySpaces[1]);
    }

    private int getPriceFrom_amount_price_(String _amount_price_) {
        String[] dividedBySpaces = _amount_price_.split(" ");
        return Integer.parseInt(dividedBySpaces[2]);
    }

    private boolean bannedCountriesAreInCorrectForm(String[] divideByQuotationMarks) {
        for (int i = 3; i < divideByQuotationMarks.length; i += 2) {
            if (divideByQuotationMarks.length == 0) {
                return false;
            }
        }

        for (int i = 4; i < divideByQuotationMarks.length; i += 2) {
            if (divideByQuotationMarks[i].trim().length() != 0) {
                return false;
            }
        }

        return true;
    }

    private List<String> getBannedCountriesFromRequestAddmovieSplittedString(String[] divideByQuotationMarks) {
        List<String> bannedCountries = new LinkedList<>();

        for (int i = 3; i < divideByQuotationMarks.length; i += 2) {
            bannedCountries.add(divideByQuotationMarks[i]);
        }

        return bannedCountries;
    }

    /**
     * message format: REQUEST return <"movie name">
     *
     * @param message the input to the server
     */
    private void requestReturn(String message) {
        // TODO what to do if two users are returning or buying the same movie simultaneously
        // TODO so that both cause a broadcast of the movie new state
        // TODO how do I keep the latest broadcast correct?
        // TODO how to solve that issue without damaging the concurrency of two users buying and/or returning different unrelated movies???


        int amountOfQuotationMarks = count(message, "\"");
        if (amountOfQuotationMarks != 2) {
            reportError("request return failed");
            return;
        }


        String[] dividedByQuotationMarks = message.split("\"");
        String movieName = dividedByQuotationMarks[1];

        Movie movie = dataBase.movies.get(Movie.equalsByName(movieName));


        if (movie == null) {
            reportError("request return failed");
            return;
        }

        // that's fine because comparison is based on movie id
        // so if movies objects are not updated, nothing bad happened
        // the movie will still be recognized in there
        if (!user.getMovies().contains(movie)) {
            reportError("request return failed");
            return;
        }

        if (!user.getMovies().remove(movie)) {
            reportError("request return failed");
            return;
        }

        movie = new Movie(movie.getId(), movie.getName(),
                movie.getPrice(),
                movie.getBannedCountries(),
                movie.getAvailableAmount() + 1, movie.getTotalAmount());


        dataBase.users.update(user, user.equalsByUsername());
        dataBase.movies.update(movie, movie.equalsById());

        acknowledge("return \"" + movieName + "\" success");
        broadcast("movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + movie.getPrice());
    }

    /**
     * message format: REQUEST rent <"movie name">
     *
     * @param message the input to the server
     */
    private void requestRent(String message) {
        // TODO what to do if two users(processes) are buying the same last movie at the same time
        // TODO or if a user is buying a movie and admin simultaneously decreasing the movie total amount
        // TODO or something like that...
        // TODO solution: synchronize???
        // TODO how to solve that issue without damaging the concurrency of two users buying and/or returning and/or updating different unrelated movies???


        int amountOfQuotationMarks = count(message, "\"");
        if (amountOfQuotationMarks != 2) {
            reportError("request rent failed");
            return;
        }

        String[] dividedByQuotationMarks = message.split("\"");
        String movieName = dividedByQuotationMarks[1];

        Movie movie = dataBase.movies.get(Movie.equalsByName(movieName));

        if (movie == null) {
            reportError("request rent failed");
            return;
        }

        if (movie.getAvailableAmount() < 1) {
            reportError("request rent failed");
            return;
        }

        if (movie.getBannedCountries().contains(user.getCountry())) {
            reportError("request rent failed");
            return;
        }

        // that's fine because comparison is based on movie id
        // so if movies objects are not updated, nothing bad happened
        // the movie will still be recognized in there
        if (user.getMovies().contains(movie)) {
            reportError("request rent failed");
            return;
        }

        if (movie.getPrice() > user.getBalance()) {
            reportError("request rent failed");
            return;
        }

        movie = new Movie(movie.getId(), movie.getName(),
                movie.getPrice(),
                movie.getBannedCountries(),
                movie.getAvailableAmount() - 1, movie.getTotalAmount());

        user = new User(user.getUsername(), user.getPassword(),
                user.getCountry(),
                user.getMovies(),
                user.getBalance() - movie.getPrice(),
                user.getType());

        dataBase.movies.update(movie, movie.equalsById());
        dataBase.users.update(user, user.equalsByUsername());

        acknowledge("rent \"" + movieName + "\" success");
        broadcast("movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + movie.getPrice());
    }

    /**
     * message format: REQUEST info "[movie name]"
     *
     * @param message the input to the server
     */
    private void requestInfo(String message) {
        int amountOfQuotationMarks = count(message, "\"");
        if (amountOfQuotationMarks == 2) {
            requestInfoOfMovie(message);
        }
        else if (amountOfQuotationMarks == 0) {
            requestInfoOFAll(message);
        }
        else {
            reportError("request info failed");
        }
    }

    /**
     * message format: REQUEST info
     *
     * @param message the input to the server
     */
    private void requestInfoOFAll(String message) {
        String[] dividedBySpaces = message.trim().split(" ");
        if (dividedBySpaces.length != 2 &
                dividedBySpaces.length != 3) {
            reportError("request info failed");
            return;
        }

        if (dividedBySpaces.length == 3 &&
                dividedBySpaces[2].length() != 0) {
            reportError("request info failed");
            return;
        }

        List<String> moviesNames = dataBase.movies.getAllNames();
        acknowledge("info" + acknowledgeTextForListOfReachStrings(moviesNames));
    }

    /**
     * message format: REQUEST info "movie name"
     *
     * @param message the input to the server
     */
    private void requestInfoOfMovie(String message) {
        String[] divideByQuotationMarks = message.split("\"");

        if (divideByQuotationMarks.length != 2 &
                divideByQuotationMarks.length != 3) {
            reportError("request info failed");
            return;
        }

        if (divideByQuotationMarks.length == 3 &&
                divideByQuotationMarks[2].trim().length() != 0) {
            reportError("request info failed");
            return;
        }

        String movieName = divideByQuotationMarks[1];

        Movie movie = dataBase.movies.get(Movie.equalsByName(movieName));

        if (movie == null) {
            reportError("request info failed");
            return;
        }


        List<String> bannedCountries = movie.getBannedCountries();
        acknowledge("info \"" + movie.getName() + "\" " +
                movie.getAvailableAmount() + " " + // TODO do i send the number of available copies or the number of total copies?
                movie.getPrice() +
                acknowledgeTextForListOfReachStrings(bannedCountries));
    }
    private String acknowledgeTextForListOfReachStrings(List<String> reachStrings) {
        String result = "";

        for (String reachString : reachStrings) {
            result += " \"" + reachString + "\"";
        }

        return result;
    }

    private int count(String string, String counted) {
        int count = 0;
        int start = 0;
        while ((start = string.indexOf(counted, start)) != -1) {
            count++;
            start++;
        }
        return count;
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
            return true;
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
