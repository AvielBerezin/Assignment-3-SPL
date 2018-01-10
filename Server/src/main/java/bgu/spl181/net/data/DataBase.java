package bgu.spl181.net.data;

import bgu.spl181.net.api.users.Movie;
import bgu.spl181.net.api.users.User;

import java.util.List;

/**
 * Created by avielber on 1/9/18.
 */
public class DataBase {
    public User getUser(String username, String password) {
        return null;
    }

    public boolean userExists(String username) {
        return false;
    }

    public void addUser(User registeredUser) {

    }

    public Movie getMovie(String name) {
        return null;
    }

    public void updateMovie(Movie movie) {

    }

    public boolean movieExists(String name) {
        return false;
    }

    public void deleteMovie(String name) {

    }

    public int highestMovieId() {
        return 0;
    }

    public void addMovie(Movie movie) {

    }

    public void updateUser(User user) {

    }

    public List<String> getAllMoviesNames() {
        return null;
    }
}
