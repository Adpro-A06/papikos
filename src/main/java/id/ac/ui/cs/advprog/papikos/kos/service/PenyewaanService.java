package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PenyewaanService {
    CompletableFuture<Penyewaan> createPenyewaan(Penyewaan penyewaan, String kosId, User penyewa);
    CompletableFuture<List<Penyewaan>> findByPenyewa(User penyewa);
    CompletableFuture<List<Penyewaan>> findByPenyewaAndStatus(User penyewa, StatusPenyewaan status);
    CompletableFuture<Optional<Penyewaan>> findById(String id);
    CompletableFuture<Optional<Penyewaan>> findByIdAndPenyewa(String id, User penyewa);
    CompletableFuture<Penyewaan> updatePenyewaan(Penyewaan updatedPenyewaan, String id, User penyewa);
    CompletableFuture<Void> cancelPenyewaan(String id, User penyewa);
    boolean isPenyewaanEditable(Penyewaan penyewaan);
    boolean isPenyewaanCancellable(Penyewaan penyewaan);
}