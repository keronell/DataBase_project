import java.util.List;
public class User {

        private String username;
        private int identifier;
        private UserType type;

        public enum UserType {
            ADMIN, TEACHER
        }

        public User(int identifier,String username, UserType type){
            this.username = username;
            this.identifier = identifier;
            this.type = type;
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

    @Override
    public String toString() {
        System.out.println("User: " + identifier + " " + username + " " + type);
        return super.toString();
    }
}
