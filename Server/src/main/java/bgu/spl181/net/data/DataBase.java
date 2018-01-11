package bgu.spl181.net.data;

/**
 * Created by avielber on 1/9/18.
 */
public class DataBase {
    public final UsersDataBase users;
    public final MoviesDataBase movies;

    public DataBase() {
        users = new UsersDataBase();
        movies = new MoviesDataBase();
    }
}
