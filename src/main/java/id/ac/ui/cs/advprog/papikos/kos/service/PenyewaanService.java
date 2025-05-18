package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;

import java.util.List;
import java.util.Optional;

public interface PenyewaanService {
    Penyewaan createPenyewaan(Penyewaan penyewaan, String kosId, User penyewa);
    List<Penyewaan> findByPenyewa(User penyewa);
    List<Penyewaan> findByPenyewaAndStatus(User penyewa, StatusPenyewaan status);
    Optional<Penyewaan> findById(String id);
    Optional<Penyewaan> findByIdAndPenyewa(String id, User penyewa);
    Penyewaan updatePenyewaan(Penyewaan penyewaan, String id, User penyewa);
    void cancelPenyewaan(String id, User penyewa);
    boolean isPenyewaanEditable(Penyewaan penyewaan);
    boolean isPenyewaanCancellable(Penyewaan penyewaan);
}