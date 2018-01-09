package bgu.spl181.net.data;

import bgu.spl181.net.api.users.Movie;

import java.util.Collection;
import java.util.List;

/**
 * Created by avielber on 1/9/18.
 */
public class JsonParser {

    public String toJsonString(JsonParsable jsonParsable) {
        return jsonParsable.toJsonString();
    }

    public String toJsonString(String string) {
        return "\"" + string + "\"";
    }

    public String toJsonString(int integer) {
        return "\"" + integer + "\"";
    }

    public String moviesToJsonString(List<Movie> list) {
        String innerJsonListString = "";
        for (Movie movie : list) {
            innerJsonListString += weakToJsonString(movie) + ",";
        }

        if (innerJsonListString.length() > 0) {
            innerJsonListString = innerJsonListString.substring(0, innerJsonListString.length() - 1);
        }

        return "[" + innerJsonListString + "]";
    }

    public String stringsToJsonString(List<String> list) {
        String innerJsonListString = "";
        for (String string : list) {
            innerJsonListString += toJsonString(string) + ",";
        }

        if (innerJsonListString.length() > 0) {
            innerJsonListString = innerJsonListString.substring(0, innerJsonListString.length() - 1);
        }

        return "[" + innerJsonListString + "]";
    }

    private String weakToJsonString(Movie movie) {
        return "{" +
                toJsonString("id") + ": " +  toJsonString(movie.getId()) + "," +
                toJsonString("name") + ": " + toJsonString(movie.getName()) +
                "}";
    }
}
