package db.model;

public class DbUser {
    private final String id;
    private final String username;
    private final String email;

    public DbUser(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String id() { return id; }
    public String username() { return username; }
    public String email() { return email; }
}
