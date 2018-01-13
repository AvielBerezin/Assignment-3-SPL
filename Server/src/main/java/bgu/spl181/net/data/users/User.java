package bgu.spl181.net.data.users;

import bgu.spl181.net.data.IdataObjects.DataObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by avielber on 1/12/18.
 */
public class User implements DataObject{

    private String username;
    private String type;
    private String password;
    private String country;
    private List<PartialMovie> movies = null;
    private Integer balance;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<PartialMovie> getMovies() {
        return movies;
    }

    public void setMovies(List<PartialMovie> movies) {
        this.movies = movies;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}