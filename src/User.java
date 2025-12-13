public class User {
    private int id;
    private String email;
    private String fullName;
    private String passwordHash;
    private String role = "user"; // "admin" or "user"
    private boolean disabled = false;


    public User() {}

    public User(int id, String email, String fullName, String passwordHash, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
    }
    public User(int id, String email, String fullName, String passwordHash, String role, boolean disabled) {
        this.id = id; this.email = email; this.fullName = fullName; this.passwordHash = passwordHash; this.role = role; this.disabled = disabled;
    }
    public User(String email, String fullName, String passwordHash, String role) {
        this.email = email; this.fullName = fullName; this.passwordHash = passwordHash; this.role = role;
    }
    public User(String email, String fullName, String passwordHash) {
        this.email = email; this.fullName = fullName; this.passwordHash = passwordHash;
    }

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
