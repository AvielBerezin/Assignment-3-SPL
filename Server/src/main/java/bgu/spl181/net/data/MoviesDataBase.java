package bgu.spl181.net.data;

import bgu.spl181.net.api.users.Movie;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by avielber on 1/11/18.
 */
public class MoviesDataBase extends AbstractDataBase<Movie> {
    Integer maxId = null;

    @Override
    protected String getJsonPath() {
        return "./DataBase/Movies.json";
    }

    @Override
    protected String getDataSpecification() {
        return "movies";
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
    public Movie add(Movie objectData) {
        Movie added = super.add(objectData);

        if (added != null) {
            maxId = Math.max(maxId, added.getId());
        }

        return added;
    }
}
