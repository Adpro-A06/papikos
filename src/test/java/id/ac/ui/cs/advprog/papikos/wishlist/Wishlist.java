package id.ac.ui.cs.advprog.papikos.wishlist;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import java.util.ArrayList;
import java.util.List;

public class Wishlist {
    private String id;
    private String name;
    private List<Kos> kosList = new ArrayList<>();

    
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
    
    
    public void addKos(Kos kos) {
        kosList.add(kos);
    }
    
    public void removeKos(Kos kos) {
        kosList.remove(kos);
    }
}
