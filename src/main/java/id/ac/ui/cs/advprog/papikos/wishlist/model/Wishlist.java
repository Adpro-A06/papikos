package id.ac.ui.cs.advprog.papikos.wishlist.model;

import java.util.ArrayList;
import java.util.List;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;

public class Wishlist {
    private Integer id;
    private String name;
    private List<Kos> kosList;
    private List<User> interestedUsers;
    private String userId;
    private String kosId;

    public Wishlist(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.kosList = new ArrayList<>();
        this.interestedUsers = new ArrayList<>();
    }

    public Wishlist(String name) {
        this(name, null);
    }

    public Wishlist() {
        this.kosList = new ArrayList<>();
        this.interestedUsers = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public void setKosList(List<Kos> kosUniq) {
        this.kosList = kosUniq;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String kosUniq) {
        this.userId = kosUniq;
    }

    public String getKosId() {
        return kosId;
    }

    public void setKosId(String kosId) {
        this.kosId = kosId;
    }
}