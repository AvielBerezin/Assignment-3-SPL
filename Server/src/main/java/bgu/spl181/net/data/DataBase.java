package bgu.spl181.net.data;

import bgu.spl181.net.api.users.Movie;
import bgu.spl181.net.api.users.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by avielber on 1/9/18.
 */
public class DataBase {
    private static final String USERS_JSON_PATH = "DataBase/Users.json";
    private static final String MOVIES_JSON_PATH = "DataBase/Movies.json";

    public User getUser(String username, String password) {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(USERS_JSON_PATH)) {
            Map<String, List<User>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<User>>>(){}.getType());

            List<User> allUsers = map.get("users");

            List<User> specificUser = allUsers.stream()
                    .filter(user -> user.getUsername() == username & user.getPassword() == password)
                    .collect(Collectors.toList());

            if (specificUser.size() != 1) {
                return null;
            }

            return specificUser.get(0);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public boolean userExists(String username) {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(USERS_JSON_PATH)) {
            Map<String, List<User>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<User>>>(){}.getType());

            List<User> allUsers = map.get("users");

            List<User> specificUser = allUsers.stream()
                    .filter(user -> user.getUsername() == username)
                    .collect(Collectors.toList());

            return specificUser.size() == 1;
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return false;
    }

    public void addUser(User user) {
        Gson gson = new Gson();

        try (
                FileReader fileReader = new FileReader(USERS_JSON_PATH);
                FileWriter fileWriter = new FileWriter(USERS_JSON_PATH)) {
            Map<String, List<User>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<User>>>(){}.getType());

            map.get("users").add(user);

            gson.toJson(map, fileWriter);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public void updateUser(User user) {
        Gson gson = new Gson();

        try (
                FileReader fileReader = new FileReader(USERS_JSON_PATH);
                FileWriter fileWriter = new FileWriter(USERS_JSON_PATH)) {
            Map<String, List<User>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<User>>>(){}.getType());

            List<User> allUsers = map.get("users");

            allUsers = allUsers.stream()
                    .filter(stillAlive -> stillAlive.getUsername() != user.getUsername())
                    .collect(Collectors.toList());

            allUsers.add(user);

            map.replace("users", allUsers);

            gson.toJson(map, fileWriter);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }





    public Movie getMovie(String name) {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(MOVIES_JSON_PATH)) {
            Map<String, List<Movie>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<Movie>>>(){}.getType());

            List<Movie> allMovies = map.get("movies");

            List<Movie> specificMovie = allMovies.stream()
                    .filter(movie -> movie.getName() == name)
                    .collect(Collectors.toList());

            if (specificMovie.size() != 1) {
                return null;
            }

            return specificMovie.get(0);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public boolean movieExists(String name) {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(MOVIES_JSON_PATH)) {
            Map<String, List<Movie>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<Movie>>>(){}.getType());

            List<Movie> allMovies = map.get("movies");

            List<Movie> specificMovie = allMovies.stream()
                    .filter(user -> user.getName() == name)
                    .collect(Collectors.toList());

            return specificMovie.size() == 1;
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return false;
    }

    public void addMovie(Movie movie) {
        Gson gson = new Gson();

        try (
                FileReader fileReader = new FileReader(MOVIES_JSON_PATH);
                FileWriter fileWriter = new FileWriter(MOVIES_JSON_PATH)) {
            Map<String, List<Movie>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<Movie>>>(){}.getType());

            map.get("movies").add(movie);

            gson.toJson(map, fileWriter);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void deleteMovie(String name) {
        Gson gson = new Gson();

        try (
                FileReader fileReader = new FileReader(MOVIES_JSON_PATH);
                FileWriter fileWriter = new FileWriter(MOVIES_JSON_PATH)) {
            Map<String, List<Movie>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<Movie>>>(){}.getType());

            List<Movie> allMovies = map.get("movies")
                    .stream()
                    .filter(movie -> movie.getName() != name)
                    .collect(Collectors.toList());

            map.replace("movies", allMovies);

            gson.toJson(map, fileWriter);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void updateMovie(Movie movie) {
        deleteMovie(movie.getName());
        addMovie(movie);
    }

    public int highestMovieId() {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(MOVIES_JSON_PATH)) {
            Map<String, List<Movie>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<Movie>>>(){}.getType());

            return map.get("movies").stream()
                    .map(movie -> movie.getId())
                    .max((num1,num2)->num2-num1)
                    .orElse(-1);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return -1;
    }

    public List<String> getAllMoviesNames() {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(MOVIES_JSON_PATH)) {
            Map<String, List<Movie>> map = gson.fromJson(fileReader, new TypeToken<Map<String, List<Movie>>>(){}.getType());

            List<Movie> allMovies = map.get("movies");

            return allMovies.stream()
                    .map(Movie::getName)
                    .collect(Collectors.toList());
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }
}
