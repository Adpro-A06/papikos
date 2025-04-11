package id.ac.ui.cs.advprog.papikos.model;

public class User {

    private String id;
    private String name;
    private boolean isAdmin;

    public User(String id, String name, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.isAdmin = isAdmin;
    }

    // Getter (setter jika perlu)
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean isAdmin() {
        return isAdmin;
    }
}
