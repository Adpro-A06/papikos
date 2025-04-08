package id.ac.ui.cs.advprog.papikos.model;

public class Kos {
    private String id;
    private String name;

    public Kos(String id, String name) {
        this.id = id;
        this.name = name;
    }

    
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Kos other = (Kos) obj;
        return id != null ? id.equals(other.id) : other.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
