package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PenyewaanRepository extends JpaRepository<Penyewaan, String> {
    @Query("SELECT p FROM Penyewaan p JOIN FETCH p.kos WHERE p.penyewa = :penyewa")
    List<Penyewaan> findByPenyewa(@Param("penyewa") User penyewa);
    List<Penyewaan> findByKos(Kos kos);
    @Query("SELECT p FROM Penyewaan p JOIN FETCH p.kos WHERE p.id = :id AND p.penyewa = :penyewa")
    Optional<Penyewaan> findByIdAndPenyewa(@Param("id") String id, @Param("penyewa") User penyewa);
    List<Penyewaan> findByStatus(StatusPenyewaan status);
    List<Penyewaan> findByPenyewaAndStatus(User penyewa, StatusPenyewaan status);
    List<Penyewaan> findByKosAndStatus(Kos kos, StatusPenyewaan status);
    long countByKosAndStatus(Kos kos, StatusPenyewaan status);
    List<Penyewaan> findByStatusAndTanggalCheckInGreaterThan(StatusPenyewaan status, LocalDate date);
    @Query("SELECT p FROM Penyewaan p JOIN FETCH p.kos WHERE p.kos.pemilik.id = :pemilikId")
    List<Penyewaan> findAllByKosPemilikId(UUID pemilikId);
    @Query("SELECT p FROM Penyewaan p JOIN FETCH p.kos k WHERE p.id = :id")
    Optional<Penyewaan> findByIdWithKos(@Param("id") String id);
}