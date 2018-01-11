package bgu.spl181.net.data;

import bgu.spl181.net.api.users.User;

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
}
