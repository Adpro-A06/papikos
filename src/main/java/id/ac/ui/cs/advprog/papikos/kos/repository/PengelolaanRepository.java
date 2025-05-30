package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PengelolaanRepository extends JpaRepository<Kos, UUID> {
    List<Kos> id(UUID id);

    default Kos create(Kos kos) {
        if (kos == null) {
            throw new IllegalArgumentException("Objek Kos tidak boleh null");
        }
        return save(kos);
    }

    default Kos findByIdOrThrow(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID tidak boleh null");
        }
        return findById(id)
                .orElseThrow(() -> new KosNotFoundException("Kos dengan ID " + id + " tidak ditemukan."));
    }

    default Kos update(Kos kos) {
        if (kos == null || kos.getId() == null) {
            throw new IllegalArgumentException("Objek Kos atau ID tidak boleh null");
        }
        if (!existsById(kos.getId())) {
            throw new KosNotFoundException("Kos dengan ID " + kos.getId() + " tidak ditemukan.");
        }
        return save(kos);
    }

    default void delete(Kos kos) {
        if (kos == null || kos.getId() == null) {
            throw new IllegalArgumentException("Objek Kos atau ID tidak boleh null");
        }
        if (!existsById(kos.getId())) {
            throw new KosNotFoundException("Kos dengan ID " + kos.getId() + " tidak ditemukan.");
        }
        deleteById(kos.getId());
    }

    default List<Kos> findAllOrThrow() {
        return findAll();
    }

    @Query("SELECT p FROM Penyewaan p WHERE p.kos.pemilik.id = :pemilikId")
    List<Penyewaan> findAllSewaByPemilikId(UUID pemilikId);

    @Query("SELECT p FROM Penyewaan p WHERE p.id = :id")
    Penyewaan findPenyewaanById(UUID id);

    class KosNotFoundException extends RuntimeException {
        public KosNotFoundException(String message) {
            super(message);
        }
    }

    class PenyewaanNotFoundException extends RuntimeException {
        public PenyewaanNotFoundException(String message) {
            super(message);
        }
    }
}