package bgu.spl181.net.api.users;

import bgu.spl181.net.data.JsonParsable;
import bgu.spl181.net.data.JsonParser;

import java.util.*;

/**
 * Created by avielber on 1/9/18.
 *
 * A class representing a user
 */
public class User implements JsonParsable {
    public static final String NORMAL = "normal";
    public static final String ADMIN = "admin";

    public User(String username, String password, String country, List<Movie> movies, int balance, String type) {
        this.username = username;
        this.password = password;
        this.country = country;
        this.movies = movies;
        this.balance = balance;

        if (type != NORMAL & type != ADMIN) {
            type = NORMAL;
        }
        this.type = type;
    }

    private final String username;
    private final String password;
    private final String country;
    /**
     *  might not be updated throughout the run of the server because movies are copied and being changed.
     *  but its ok because it is only used to:
     *   - print the id and name of the movies which are not changing.
     *   - check if user has already rent a movie when requesting a rent of that movie.
     *     which happens comparing id's/names and not the entire object.
     */
    private final List<Movie> movies;
    private final int balance;
    private final String type;

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getCountry() {
        return country;
    }
    public List<Movie> getMovies() {
        return movies;
    }
    public int getBalance() {
        return balance;
    }
    public String getType() {
        return type;
    }

    @Override
    public String toJsonString() {
        JsonParser parser = new JsonParser();
        return  "{" +
                parser.toJsonString("username") + ": " +  parser.toJsonString(getUsername()) + "," +
                parser.toJsonString("type") + ": " + parser.toJsonString(getType()) + "," +
                parser.toJsonString("password") + ": " +  parser.toJsonString(getPassword()) + "," +
                parser.toJsonString("country") + ": " +  parser.toJsonString(getCountry()) + "," +
                parser.toJsonString("movies") + ": " + parser.moviesToJsonString(getMovies()) + "," +
                parser.toJsonString("balance") + ": "  + parser.toJsonString(getBalance()) +
                "}";
    }
}
