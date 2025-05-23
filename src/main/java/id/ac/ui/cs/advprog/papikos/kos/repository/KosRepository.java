package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KosRepository extends JpaRepository<Kos, UUID> {
    List<Kos> findByStatus(String status);
    List<Kos> findByNamaContainingIgnoreCaseAndStatus(String nama, String status);
    List<Kos> findByAlamatContainingIgnoreCaseAndStatus(String alamat, String status);
    List<Kos> findByHargaBetweenAndStatus(Integer minHarga, Integer maxHarga, String status);
    List<Kos> findByPemilik(User pemilik);

    @Query("SELECT k FROM Kos k WHERE " +
            "(:keyword IS NULL OR " +
            "LOWER(k.nama) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(k.alamat) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(k.deskripsi) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND k.status = :status")
    List<Kos> searchByKeyword(@Param("keyword") String keyword, @Param("status") String status);
}