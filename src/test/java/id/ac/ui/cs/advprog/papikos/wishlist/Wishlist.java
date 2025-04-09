package id.ac.ui.cs.advprog.papikos.wishlist;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.model.User;
import java.util.ArrayList;
import java.util.List;

public class Wishlist {
    private String id;
    private String name;
    private List<Kos> kosList;
    private List<User> interestedUsers;

    public Wishlist() {
        this.kosList = new ArrayList<>();
        this.interestedUsers = new ArrayList<>();
    }

    public Wishlist(String name) {
        this();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Kos> getKosList() {
        return kosList;
    }

    public void setKosList(List<Kos> kosList) {
        this.kosList = kosList;
    }

    public List<User> getInterestedUsers() {
        return interestedUsers;
    }

    public void setInterestedUsers(List<User> interestedUsers) {
        this.interestedUsers = interestedUsers;
    }

    public void addKos(Kos kos) {
        this.kosList.add(kos);
    }
}
