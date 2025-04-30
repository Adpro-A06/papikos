package id.ac.ui.cs.advprog.papikos.repository;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PengelolaanRepository {
    private List<Kos> kosData = new ArrayList<>();

    public Kos create(Kos kos) {
        if (kos.getId() == null) {
            String id = UUID.randomUUID().toString();
            kos.setId(id);
        }
        kosData.add(kos);
        return kos;
    }

    public Iterator<Kos> findAll() {
        return kosData.iterator();
    }

    public Kos findById(String id) {
        for (Kos kos : kosData) {
            if (kos.getId().equals(id)) {
                return kos;
            }
        }
        throw new KosNotFoundException("Kos dengan ID " + id + " tidak ditemukan.");
    }

    public Kos update(Kos kos) {
        for (int i = 0; i < kosData.size(); i++) {
            if (kosData.get(i).getId().equals(kos.getId())) {
                kosData.set(i, kos);
                return kos;
            }
        }
        throw new KosNotFoundException("Kos dengan ID " + kos.getId() + " tidak ditemukan.");
    }

    public void delete(Kos kos) {
        Iterator<Kos> iterator = kosData.iterator();
        while (iterator.hasNext()) {
            Kos existingKos = iterator.next();
            if (existingKos.getId().equals(kos.getId())) {
                iterator.remove();
                return;
            }
        }
        throw new KosNotFoundException("Kos dengan ID " + kos.getId() + " tidak ditemukan.");
    }

    public static class KosNotFoundException extends RuntimeException {
        public KosNotFoundException(String message) {
            super(message);
        }
    }
}