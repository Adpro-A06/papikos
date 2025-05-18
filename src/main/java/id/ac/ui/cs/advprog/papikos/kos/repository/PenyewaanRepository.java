package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PenyewaanRepository extends JpaRepository<Penyewaan, String> {
    List<Penyewaan> findByPenyewa(User penyewa);
    List<Penyewaan> findByKos(Kos kos);
    Optional<Penyewaan> findByIdAndPenyewa(String id, User penyewa);
    List<Penyewaan> findByStatus(StatusPenyewaan status);
    List<Penyewaan> findByPenyewaAndStatus(User penyewa, StatusPenyewaan status);
    List<Penyewaan> findByKosAndStatus(Kos kos, StatusPenyewaan status);
    long countByKosAndStatus(Kos kos, StatusPenyewaan status);
    List<Penyewaan> findByStatusAndTanggalCheckInGreaterThan(StatusPenyewaan status, LocalDate date);
}