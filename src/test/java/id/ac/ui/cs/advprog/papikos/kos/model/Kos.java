package id.ac.ui.cs.advprog.papikos.kos.model;

public class Kos {
    private String id;
    private String name;
    private KosType type; 

    
    public Kos(String id, String name) {
        this.id = id;
        this.name = name;
        this.type = KosType.CAMPUR; 
    }

    
    public Kos(String id, String name, KosType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public KosType getType() {
        return type;
    }

    public void setType(KosType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        id.ac.ui.cs.advprog.papikos.kos.model.Kos other = (id.ac.ui.cs.advprog.papikos.kos.model.Kos) obj;
        return id != null ? id.equals(other.id) : other.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
