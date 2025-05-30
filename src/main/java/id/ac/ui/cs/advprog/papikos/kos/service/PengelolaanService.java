package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PengelolaanService{
    CompletableFuture<Kos> create(Kos kos);
    CompletableFuture<List<Kos>> findAll();
    CompletableFuture<Kos> findById(UUID id);
    CompletableFuture<Kos> update(Kos kos);
    CompletableFuture<Void> delete(Kos kos);
    CompletableFuture<List<Penyewaan>> findAllSewa(UUID pemilikId);
    CompletableFuture<Void> terimaSewa(String id, UUID pemilikId);
    CompletableFuture<Void> tolakSewa(String id, UUID pemilikId);
}