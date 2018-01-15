package bgu.spl181.net.api.protocols;

import bgu.spl181.net.data.DataBase;
import bgu.spl181.net.data.movies.Movie;
import bgu.spl181.net.data.users.PartialMovie;
import bgu.spl181.net.data.users.User;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by avielber on 1/8/18.
 *
 * Movie rental service protocol
 */
public class MRSP extends USTBP {

    private final String textRegex = "[^ \"]+";
    private final String reachTextRegex = "(\"[^\"]+\")";
    private final String naturalNumberRegex = "(\\d+)";
    private final String attributeRegex = reachTextRegex + "|" + textRegex;
    private final String countriesRegex = "( " + reachTextRegex + ")*";


    private User user = null;

    public MRSP(DataBase dataBase) {
        super(dataBase);
    }

    private boolean userIsLoggedIn() {
        return user != null;
    }
    private void logUserOut() {
        dataBase.users.free(user);
        connections.disableBroadcast(connectionId);
        user = null;
        shouldTerminate = true;
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
        if (!message.matches("LOGIN " + textRegex + " " + textRegex)) {
            return false;
        }
        Matcher matcher = Pattern.compile(textRegex)
                .matcher(message);

        if (!matcher.find()) return false;
        // LOGIN

        if (!matcher.find()) return false;
        String username = matcher.group();

        if (!matcher.find()) return false;
        String password = matcher.group();


        if (userIsLoggedIn()) {
            return false;
        }

        Predicate<User> byUsername = aUser -> aUser.getUsername().equals(username);
        Predicate<User> byPassword = aUser -> aUser.getPassword().equals(password);
        user = dataBase.users.get(byUsername.and(byPassword));
        if (!dataBase.users.isFree(user)) {
            user = null;
            return false;
        }


        dataBase.users.takeHostage(user);
        connections.enableBroadcast(connectionId);
        return true;
    }

    /**
     * message format: REGISTER <username> <password> country="<country name>"
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
        if (!message.matches("REGISTER " + textRegex + " " + textRegex + " country=" + reachTextRegex)) {
            return false;
        }

        User registeredUser = generateUser(message);

        if (registeredUser == null) {
            return false;
        }

        Predicate<User> byUsername = aUser -> aUser.getUsername().equals(registeredUser.getUsername());
        if (dataBase.users.exists(byUsername)) {
            return false;
        }


        return dataBase.users.add(registeredUser) != null;
    }

    /**
     * generates user from message
     * assumes form of message: REGISTER <username> <password> country="<country name>"
     * @param message a message from client
     * @return generated user from message
     */
    private User generateUser(String message) {
        Matcher matcher = Pattern.compile(attributeRegex)
                .matcher(message);

        if (!matcher.find()) return null;
        // REGISTER

        if (!matcher.find()) return null;
        String username = matcher.group();

        if (!matcher.find()) return null;
        String password = matcher.group();

        if (!matcher.find()) return null;
        // country=

        if (!matcher.find()) return null;
        String country = matcher.group().replaceAll("\"","");

        User user = new User();

        user.setUsername(username);
        user.setPassword(password);
        user.setCountry(country);
        user.setMovies(new LinkedList<>());
        user.setBalance(0);
        user.setType("normal");

        return user;
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
        if (!message.matches("SIGNOUT")) {
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

        if (!user.getType().equals("admin")) {
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
        if (!message.matches("REQUEST changeprice " + reachTextRegex + " " + naturalNumberRegex)) {
            reportError("request changeprice failed");
            return;
        }

        Matcher nameMatcher = Pattern.compile(reachTextRegex)
                .matcher(message);
        boolean validate = nameMatcher.find(); // for debug
        String name = nameMatcher.group().replaceAll("\"", "");

        Matcher priceMatcher = Pattern.compile(naturalNumberRegex)
                .matcher(message);
        validate = validate & priceMatcher.find();
        Integer price = Integer.parseInt(priceMatcher.group());

        Predicate<Movie> byName = aMovie -> aMovie.getName().equals(name);
        Movie movie = dataBase.movies.get(byName);
        if (movie == null) { // movie not found
            reportError("request addmovie failed");
            return;
        }

        movie.setPrice(price);

        Predicate<Movie> byId = aMovie -> aMovie.getId().equals(movie.getId());
        dataBase.movies.update(movie, byId);
        acknowledge("addmovie \"" + name + "\" success");
        broadcast("movie \"" + name + "\" " + movie.getAvailableAmount() + " " + movie.getPrice());
    }
    /**
     * message format: REQUEST remmovie <”movie name”>
     *
     * @param message the input to the server
     */
    private void requestRemMovie(String message) {
        if (!message.matches("REQUEST remmovie " + reachTextRegex)) {
            reportError("request remmovie failed");
            return;
        }

        Matcher matcher = Pattern.compile(reachTextRegex)
                .matcher(message);
        boolean validate = matcher.find(); // for debug
        String name = matcher.group().replaceAll("\"", "");

        Predicate<Movie> byName = aMovie -> aMovie.getName().equals(name);
        if (!dataBase.movies.exists(byName)) {
            reportError("request remmovie failed");
            return;
        }

        dataBase.movies.delete(byName);
        acknowledge("remmovie \"" + name + "\" success");
        broadcast("movie \"" + name + "\" removed");
    }

    /**
     * message format: REQUEST addmovie <”movie name”> <amount> <price> [“banned country”,…]
     *
     * @param message the input to the server
     */
    private void requestAddMovie(String message) {
        String messagePattern = "REQUEST addmovie " + reachTextRegex + " " + naturalNumberRegex + " " + naturalNumberRegex + countriesRegex;

        if (!message.matches(messagePattern)) {
            reportError("request addmovie failed");
            return;
        }

        Movie movie = generateMovie(message);

        Predicate<Movie> byName = aMovie -> aMovie.getName().equals(movie.getName());
        if (dataBase.movies.exists(byName)) {
            reportError("request addmovie failed");
            return;
        }

        if (dataBase.movies.add(movie) == null) {
            reportError("request addmovie failed");
            return;
        }

        acknowledge("addmovie \"" + movie.getName() + "\" success");
        broadcast("movie \"" + movie.getName() + "\" " + movie.getAvailableAmount() + " " + movie.getPrice());
    }

    /**
     * generate movie from message
     * assuming message in the format:
     * REQUEST addmovie <”movie name”> <amount> <price> [“banned country”,…]
     * @param message a message from client
     * @return a movie generated from message
     */
    private Movie generateMovie(String message) {
        Pattern pattern = Pattern.compile(attributeRegex);
        Matcher matcher = pattern.matcher(message);

        String REQUEST;
        String addmovie;
        String name;
        String amount;
        String price;
        String bannedCountry;

        List<String> bannedCountries = new LinkedList<>();

        boolean validate; // for debug

        validate = matcher.find();
        REQUEST = matcher.group();

        validate = validate && matcher.find();
        addmovie = matcher.group();

        validate = validate && matcher.find();
        name = matcher.group().replaceAll("\"", "");

        validate = validate && matcher.find();
        amount = matcher.group();

        validate = validate && matcher.find();
        price = matcher.group();

        while (matcher.find()) {
            bannedCountry = matcher.group().replaceAll("\"", "");
            bannedCountries.add(bannedCountry);
        }

        Movie movie = new Movie();
        movie.setId(dataBase.movies.newId());
        movie.setName(name);
        movie.setPrice(Integer.parseInt(price));
        movie.setBannedCountries(bannedCountries);
        movie.setAvailableAmount(Integer.parseInt(amount));
        movie.setTotalAmount(Integer.parseInt(amount));

        return movie;
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

        if (!message.matches("REQUEST return " + reachTextRegex)) {
            reportError("request return failed");
            return;
        }


        String[] dividedByQuotationMarks = message.split("\"");
        String movieName = dividedByQuotationMarks[1];

        Predicate<Movie> byName = aMovie -> aMovie.getName().equals(movieName);
        Movie movie = dataBase.movies.get(byName);


        if (movie == null) { // did not find in data base
            reportError("request return failed");
            return;
        }

        Predicate<PartialMovie> byId = aMovie -> movie.getId().equals(aMovie.getId());

        if (user.getMovies().stream()
                .filter(byId)
                .collect(Collectors.toList())
                .isEmpty()) {
            reportError("request return failed");
            return;
        }

        user.setMovies(user.getMovies().stream()
                .filter(byId.negate())
                .collect(Collectors.toList()));

        movie.setAvailableAmount(movie.getAvailableAmount() + 1);

        Predicate<User> byUsername = aUser -> aUser.getUsername().equals(user.getUsername());
        Predicate<Movie> movieById = aMovie -> aMovie.getId().equals(movie.getId());
        dataBase.users.update(user, byUsername);
        dataBase.movies.update(movie, movieById);

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

        if (!message.matches("REQUEST rent " + reachTextRegex)) {
            reportError("request rent failed");
            return;
        }

        String movieName = message.split("\"")[1];

        Predicate<Movie> byName = aMovie -> aMovie.getName().equals(movieName);
        Movie movie = dataBase.movies.get(byName);

        if (movie == null) { // wasn't found in data base
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

        Predicate<PartialMovie> byId = aMovie -> aMovie.getId().equals(movie.getId());
        if(!user.getMovies().stream()
                .filter(byId)
                .collect(Collectors.toList())
                .isEmpty()) {
            reportError("request rent failed");
            return;
        }

        if (movie.getPrice() > user.getBalance()) {
            reportError("request rent failed");
            return;
        }


        movie.setAvailableAmount(movie.getAvailableAmount() - 1);

        user.setBalance(user.getBalance() - movie.getPrice());

        PartialMovie partialMovie = new PartialMovie();
        partialMovie.setId(movie.getId());
        partialMovie.setName(movie.getName());
        user.getMovies().add(partialMovie);

        Predicate<Movie> movieById = aMovie -> aMovie.getId().equals(movie.getId());
        Predicate<User> byUsername = aUser -> aUser.getUsername().equals(user.getUsername());
        dataBase.movies.update(movie, movieById);
        dataBase.users.update(user, byUsername);

        acknowledge("rent \"" + movieName + "\" success");
        broadcast("movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + movie.getPrice());
    }

    /**
     * message format: REQUEST info "[movie name]"
     *
     * @param message the input to the server
     */
    private void requestInfo(String message) {
        if (message.matches("REQUEST info")) {
            requestInfoOFAll(message);
        }
        else if (message.matches("REQUEST info " + reachTextRegex)) {
            requestInfoOfMovie(message);
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
        if (message.matches("REQUEST info")) {
            List<String> moviesNames = dataBase.movies.getAllNames();
            acknowledge("info" + acknowledgeTextForListOfReachStrings(moviesNames));
        }
        else {
            reportError("request info failed");
        }
    }

    /**
     * message format: REQUEST info "movie name"
     *
     * @param message the input to the server
     */
    private void requestInfoOfMovie(String message) {
        if (!message.matches("REQUEST info " + reachTextRegex)) {
            reportError("request info failed");
            return;
        }

        String movieName = message.split("\"")[1];

        Predicate<Movie> byName = aMovie -> aMovie.getName().equals(movieName);
        Movie movie = dataBase.movies.get(byName);

        if (movie == null) { // wasn't found in the data base
            reportError("request info failed");
            return;
        }


        List<String> bannedCountries = movie.getBannedCountries();
        acknowledge("info \"" + movie.getName() + "\" " +
                movie.getAvailableAmount() + " " +
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
        if (message.matches("REQUEST balance add " + naturalNumberRegex)) {
            int amount = getAmountOfBalanceAddRequest(message);
            user.setBalance(user.getBalance() + amount);
            Predicate<User> byUsername = aUser -> aUser.getUsername().equals(user.getUsername());
            dataBase.users.update(user, byUsername);
            acknowledge("balance " + user.getBalance() + " added " + amount);
        }
        else {
            reportError("request balance failed");
        }
    }

    /**
     * gets amount from message
     * assumes message is in format: REQUEST balance add <amount>
     * @param message message from client
     * @return amount from message
     */
    private int getAmountOfBalanceAddRequest(String message) {
        return Integer.parseInt(message.split(" ")[3]);
    }

    /**
     * message format: REQUEST balance info
     *
     * @param message the input to the server
     */
    private void requestBalanceInfo(String message) {
        if (!message.matches("REQUEST balance info")) {
            reportError("request balance failed");
            return;
        }

        if (!userIsLoggedIn()) {
            reportError("request balance failed");
            return;
        }

        acknowledge("balance " + user.getBalance());
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
        Matcher matcher = Pattern.compile(textRegex)
                .matcher(message);

        if (matcher.find() && matcher.find()) {
            return matcher.group();
        }

        return "";
    }
}
