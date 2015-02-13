package de.uniko.sebschlicht.titan.extensions.bootstrap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserManager {

    private Map<Long, User> _users;

    public UserManager() {
        _users = new HashMap<Long, User>();
    }

    public Set<Long> getIds() {
        return _users.keySet();
    }

    public User loadUser(long id) {
        User user = _users.get(id);
        if (user != null) {
            return user;
        }
        return addUser(id);
    }

    public User addUser(long id) {
        User user = new User();
        _users.put(id, user);
        return user;
    }
}
