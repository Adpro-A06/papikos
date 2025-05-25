package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.kos.repository.PenyewaanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
public class PengelolaanServiceImpl implements PengelolaanService {
    private final PengelolaanRepository pengelolaanRepository;
    private final PenyewaanRepository penyewaanRepository;
    private static final Logger logger = LoggerFactory.getLogger(PengelolaanServiceImpl.class);

    @Autowired
    public PengelolaanServiceImpl(PengelolaanRepository pengelolaanRepository, PenyewaanRepository penyewaanRepository) {
        this.pengelolaanRepository = pengelolaanRepository;
        this.penyewaanRepository = penyewaanRepository;
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Kos> create(Kos kos) {
        return CompletableFuture.completedFuture(pengelolaanRepository.create(kos));
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<List<Kos>> findAll() {
        return CompletableFuture.completedFuture(pengelolaanRepository.findAllOrThrow());
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Kos> findById(UUID id) {
        return CompletableFuture.completedFuture(pengelolaanRepository.findByIdOrThrow(id));
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Kos> update(Kos kos) {
        return CompletableFuture.completedFuture(pengelolaanRepository.update(kos));
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Void> delete(Kos kos) {
        pengelolaanRepository.delete(kos);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<Penyewaan>> findAllSewa(UUID pemilikId) {
        return CompletableFuture.supplyAsync(() -> {
            return penyewaanRepository.findAllByKosPemilikId(pemilikId);
        });
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Void> terimaSewa(String id, UUID pemilikId) {
        return CompletableFuture.runAsync(() -> terimaSewaSync(id, pemilikId));
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Void> tolakSewa(String id, UUID pemilikId) {
        return CompletableFuture.runAsync(() -> tolakSewaSync(id, pemilikId));
    }

    @Transactional
    public void terimaSewaSync(String id, UUID pemilikId) {
        Penyewaan penyewaan = penyewaanRepository.findByIdWithKos(id)
                .orElseThrow(() -> new PengelolaanRepository.PenyewaanNotFoundException("Penyewaan tidak ditemukan"));

        if (!penyewaan.getKos().getPemilik().getId().equals(pemilikId)) {
            throw new IllegalArgumentException("Anda tidak berhak mengubah penyewaan ini");
        }

        Kos kos = penyewaan.getKos();
        int jumlahSekarang = kos.getJumlah();
        if (jumlahSekarang <= 0) {
            throw new IllegalStateException("Tidak ada kamar tersedia untuk disewakan");
        }
        kos.setJumlah(jumlahSekarang - 1);
        pengelolaanRepository.save(kos);

        penyewaan.setStatus(StatusPenyewaan.APPROVED);
        penyewaan.setWaktuPerubahan(java.time.LocalDateTime.now());
        penyewaanRepository.save(penyewaan);
    }

    @Transactional
    public void tolakSewaSync(String id, UUID pemilikId) {
        Penyewaan penyewaan = penyewaanRepository.findByIdWithKos(id)
                .orElseThrow(() -> new PengelolaanRepository.PenyewaanNotFoundException("Penyewaan tidak ditemukan"));

        if (!penyewaan.getKos().getPemilik().getId().equals(pemilikId)) {
            throw new IllegalArgumentException("Anda tidak berhak mengubah penyewaan ini");
        }

        penyewaan.setStatus(StatusPenyewaan.REJECTED);
        penyewaan.setWaktuPerubahan(LocalDateTime.now());

        try {
            penyewaanRepository.save(penyewaan);
        } catch (Exception e) {
            logger.error("Gagal menolak penyewaan dengan id {}: ", id, e);
            throw e;
        }
    }
}