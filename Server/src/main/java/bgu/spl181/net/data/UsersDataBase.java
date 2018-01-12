package bgu.spl181.net.data;

import bgu.spl181.net.api.users.User;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Created by avielber on 1/11/18.
 */
public class UsersDataBase extends AbstractDataBase<User> {
    @Override
    protected String getJsonPath() {
        return "./DataBase/Users.json";
    }

    @Override
    protected String getDataSpecification() {
        return "users";
    }

    public boolean isFree(User user) {
        return !prison.contains(user.getUsername());
    }
    public void free(User user) {
        prison.remove(user.getUsername());
    }
    public void takeHostage(User user) {
        prison.add(user.getUsername());
    }

    private List<String> prison = new LinkedList<>();
}
