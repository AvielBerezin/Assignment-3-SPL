package bgu.spl181.net.data;

import bgu.spl181.net.data.movies.Movie;
import bgu.spl181.net.data.movies.Movies;
import bgu.spl181.net.data.users.User;
import bgu.spl181.net.data.users.Users;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by avielber on 1/11/18.
 */
public class MoviesDataBase extends AbstractDataBase<Movie> {
    Integer maxId = null;

    @Override
    protected String getJsonPath() {
        return "/users/studs/bsc/2016/avielber/Documents/git/Assignment-3-SPL/DataBase/Movies.json";//../../../../DataBase/Movies.json";
    }

    public List<String> getAllNames() {
        List<Movie> allMovies = getAll();

        if (allMovies == null) {
            return null;
        }

        return allMovies.stream()
                .map(movie -> movie.getName())
                .collect(Collectors.toList());
    }

    public Integer newId() {
        if (maxId == null) {
            if (updateMaxId() == null) {
                return null;
            }
        }

        return maxId + 1;
    }

    private Integer updateMaxId() {
        return maxId = getAll().stream()
                .map(Movie::getId)
                .max((a, b) -> a - b)
                .orElseGet(null);
    }

    @Override
    protected List<Movie> readAll() {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(getJsonPath())) {
            Movies movies = gson.fromJson(fileReader, Movies.class);
            return movies.getMovies();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    @Override
    protected List<Movie> writeAll() {
        Gson gson = new Gson();

        try (FileWriter fileWriter = new FileWriter(getJsonPath())) {
            Movies movies = new Movies();
            List<Movie> all = getAll();
            movies.setMovies(all);

            gson.toJson(movies, fileWriter);

            return all;
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    @Override
    public Movie add(Movie objectData) {
        Movie added = super.add(objectData);

        if (added != null) {
            maxId = Math.max(maxId, added.getId());
        }

        return added;
    }
}
