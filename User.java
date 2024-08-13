import java.util.List;
public class User {

        private static int counter = 0;
        private String username;
        private int identifier;
        private UserType type;

        public enum UserType {
            ADMIN, TEACHER
        }

        public User(String username, UserType type) {
            this.username = username;
            this.identifier = identifier;
            this.type = type;
            identifier = ++counter;
        }

        public String getUsername() {
            return username;
        }

        public int getId() {
            return identifier;
        }

        public UserType getType() {
            return type;
        }
}
