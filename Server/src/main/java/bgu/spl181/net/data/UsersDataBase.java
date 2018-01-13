package bgu.spl181.net.data;

import bgu.spl181.net.data.users.User;
import bgu.spl181.net.data.users.Users;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by avielber on 1/11/18.
 */
public class UsersDataBase extends AbstractDataBase<User> {
    @Override
    protected List<User> readAll() {
        Gson gson = new Gson();

        try (FileReader fileReader = new FileReader(getJsonPath())) {
            Users users = gson.fromJson(fileReader, Users.class);
            return users.getUsers();
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    @Override
    protected List<User> writeAll() {
        Gson gson = new Gson();

        try (FileWriter fileWriter = new FileWriter(getJsonPath())) {
            Users users = new Users();
            List<User> all = getAll();
            users.setUsers(all);

            gson.toJson(users, fileWriter);

            return all;
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    @Override
    protected String getJsonPath() {
        return "/users/studs/bsc/2016/avielber/Documents/git/Assignment-3-SPL/DataBase/Users.json";//"../../../../DataBase/Users.json";
    }

    public boolean isFree(User user) {
        return user != null && !prison.contains(user.getUsername());
    }
    public void free(User user) {
        prison.remove(user.getUsername());
    }
    public void takeHostage(User user) {
        prison.add(user.getUsername());
    }

    private List<String> prison = new LinkedList<>();
}
