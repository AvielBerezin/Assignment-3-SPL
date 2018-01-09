package bgu.spl181.net.api.users;

import bgu.spl181.net.data.JsonParsable;
import bgu.spl181.net.data.JsonParser;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by avielber on 1/9/18.
 *
 * A class representing a user
 */
public abstract class User implements JsonParsable {

    public User(String username, String password, String country, List<Movie> movies, int balance) {
        this.username = username;
        this.password = password;
        this.country = country;
        this.movies = movies;
        this.balance = balance;
    }

    private final String username;
    private final String password;
    private final String country;
    private final List<Movie> movies;
    private final int balance;

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


    protected abstract String getType();
}
