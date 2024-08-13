import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private List<User> users;

    public  UserManager() {
        this.users = new ArrayList<>();
    }

    public User getUserByUsername(String username) {
        for (User user : this.users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void addUser(User user) {
        this.users.add(user);
    }
    public void deleteUser(int id) {
        User user = getUserById(id);
        if (user != null) {
            this.users.remove(user);
        }
    }

    public User getUserById(int id) {
        for (User user : this.users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public User.UserType getUserTypeById(int id) {
        User user = getUserById(id);
        if (user != null) {
            return user.getType();
        }
        return null;
    }

    public List<User> getUsers() {
        return this.users;
    }
}