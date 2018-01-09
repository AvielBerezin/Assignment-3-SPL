package bgu.spl181.net.api.users;

import java.util.List;

/**
 * Created by avielber on 1/9/18.
 */
public class NormalUser extends User {
    public NormalUser(String username, String password, String country, List<Movie> movies, int balance) {
        super(username, password, country, movies, balance);
    }

    @Override
    protected String getType() {
        return "normal";
    }
}
