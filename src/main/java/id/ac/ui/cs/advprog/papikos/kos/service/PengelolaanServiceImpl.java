package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.kos.repository.PenyewaanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<List<Penyewaan>> findAllSewa(UUID pemilikId) {
        return CompletableFuture.completedFuture(pengelolaanRepository.findAllSewaByPemilikId(pemilikId));
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Void> acceptSewa(String penyewaanId, UUID pemilikId) {
        logger.info("Mencoba menerima penyewaan dengan ID {} untuk pemilik {}", penyewaanId, pemilikId);
        Penyewaan penyewaan = penyewaanRepository.findById(penyewaanId)
                .orElseThrow(() -> {
                    logger.error("Penyewaan dengan ID {} tidak ditemukan", penyewaanId);
                    return new IllegalArgumentException("Penyewaan dengan ID " + penyewaanId + " tidak ditemukan");
                });
        if (!penyewaan.getKos().getPemilik().getId().equals(pemilikId)) {
            logger.warn("Penyewaan dengan ID {} tidak dimiliki oleh pemilik {}", penyewaanId, pemilikId);
            throw new IllegalArgumentException("Penyewaan tidak dimiliki oleh pemilik ini");
        }
        if (penyewaan.getStatus() != StatusPenyewaan.PENDING) {
            logger.warn("Penyewaan dengan ID {} tidak dalam status PENDING", penyewaanId);
            throw new IllegalStateException("Penyewaan harus dalam status PENDING untuk diterima");
        }
        if (penyewaan.getKos().getJumlahTersedia() <= 0) {
            logger.warn("Kos dengan ID {} tidak memiliki kamar tersedia", penyewaan.getKos().getId());
            throw new IllegalStateException("Tidak ada kamar tersedia di kos ini");
        }
        penyewaan.setStatus(StatusPenyewaan.APPROVED);
        penyewaanRepository.save(penyewaan);
        logger.info("Penyewaan dengan ID {} berhasil diterima", penyewaanId);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Void> reduceKosJumlah(UUID kosId, UUID pemilikId, int newJumlah) {
        logger.info("Mencoba mengurangi jumlah kamar untuk kos {} oleh pemilik {}", kosId, pemilikId);
        Kos kos = pengelolaanRepository.findByIdOrThrow(kosId);
        if (!kos.getPemilik().getId().equals(pemilikId)) {
            logger.warn("Kos dengan ID {} tidak dimiliki oleh pemilik {}", kosId, pemilikId);
            throw new IllegalArgumentException("Kos tidak dimiliki oleh pemilik ini");
        }
        if (newJumlah < 1) {
            logger.warn("Jumlah kamar baru {} tidak valid untuk kos {}", newJumlah, kosId);
            throw new IllegalArgumentException("Jumlah kamar minimal 1");
        }
        long jumlahApproved = penyewaanRepository.countByKosAndStatus(kos, StatusPenyewaan.APPROVED);
        if (newJumlah < jumlahApproved) {
            logger.warn("Jumlah kamar baru {} lebih kecil dari jumlah penyewaan APPROVED {} untuk kos {}", newJumlah, jumlahApproved, kosId);
            throw new IllegalStateException("Jumlah kamar tidak boleh kurang dari penyewaan yang sudah disetujui");
        }
        kos.setJumlah(newJumlah);
        pengelolaanRepository.save(kos);
        logger.info("Jumlah kamar untuk kos {} berhasil diperbarui menjadi {}", kosId, newJumlah);
        return CompletableFuture.completedFuture(null);
    }
}