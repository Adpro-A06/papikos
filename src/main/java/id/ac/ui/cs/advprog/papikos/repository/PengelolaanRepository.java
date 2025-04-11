package id.ac.ui.cs.advprog.papikos.repository;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Repository
public class PengelolaanRepository {
    // Instance tunggal untuk Singleton
    private static final PengelolaanRepository instance = new PengelolaanRepository();
    private List<Kos> kosData = new ArrayList<>();

    // Constructor private untuk mencegah instansiasi langsung
    private PengelolaanRepository() {
    }

    // Metode untuk mendapatkan instance Singleton
    public static PengelolaanRepository getInstance() {
        return instance;
    }

    public Kos create(Kos kos) {
        if (kos.getId() == null) {
            kos.setId(generateId());
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

    private String generateId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        char[] chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        for (int i = 0; i < 8; i++) {
            sb.append(chars[random.nextInt(chars.length)]);
        }
        sb.append("-");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(chars[random.nextInt(chars.length)]);
            }
            sb.append("-");
        }
        for (int i = 0; i < 12; i++) {
            sb.append(chars[random.nextInt(chars.length)]);
        }
        return sb.toString();
    }

    public static class KosNotFoundException extends RuntimeException {
        public KosNotFoundException(String message) {
            super(message);
        }
    }
}