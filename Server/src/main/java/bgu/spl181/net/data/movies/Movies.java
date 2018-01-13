package bgu.spl181.net.data.movies;

import bgu.spl181.net.data.IdataObjects.Container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by avielber on 1/13/18.
 */
public class Movies implements Container<Movie> {

    private List<Movie> movies = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}