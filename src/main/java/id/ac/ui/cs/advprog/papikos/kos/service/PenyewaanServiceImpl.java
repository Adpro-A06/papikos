package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import id.ac.ui.cs.advprog.papikos.kos.repository.PenyewaanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PenyewaanServiceImpl implements PenyewaanService {

    private final PenyewaanRepository penyewaanRepository;
    private final KosRepository kosRepository;

    @Autowired
    public PenyewaanServiceImpl(PenyewaanRepository penyewaanRepository, KosRepository kosRepository) {
        this.penyewaanRepository = penyewaanRepository;
        this.kosRepository = kosRepository;
    }

    @Override
    @Async("penyewaanTaskExecutor")
    @Transactional
    public CompletableFuture<Penyewaan> createPenyewaan(Penyewaan penyewaan, String kosId, User penyewa) {
        UUID kosUUID;
        try {
            kosUUID = UUID.fromString(kosId);
        } catch (IllegalArgumentException e) {
            CompletableFuture<Penyewaan> result = new CompletableFuture<>();
            result.completeExceptionally(new IllegalArgumentException("Invalid kos ID format: " + kosId));
            return result;
        }

        return kosRepository.findById(kosUUID)
                .map(kos -> {
                    CompletableFuture<Penyewaan> result = new CompletableFuture<>();
                    if (!"AVAILABLE".equals(kos.getStatus())) {
                        result.completeExceptionally(
                                new IllegalStateException("Kos tidak tersedia untuk disewa"));
                        return result;
                    }
                    if (kos.getJumlahTersedia() <= 0) {
                        result.completeExceptionally(
                                new IllegalStateException("Tidak ada kamar tersedia untuk disewa"));
                        return result;
                    }
                    if (penyewaan.getTanggalCheckIn().isBefore(LocalDate.now())) {
                        result.completeExceptionally(
                                new IllegalArgumentException("Tanggal check-in tidak boleh di masa lalu"));
                        return result;
                    }
                    if (penyewaan.getDurasiSewa() < 1) {
                        result.completeExceptionally(
                                new IllegalArgumentException("Durasi sewa minimal 1 bulan"));
                        return result;
                    }
                    if (penyewaan.getDurasiSewa() > 12) {
                        result.completeExceptionally(
                                new IllegalArgumentException("Durasi sewa maksimal 12 bulan"));
                        return result;
                    }

                    penyewaan.setKos(kos);
                    penyewaan.setPenyewa(penyewa);
                    penyewaan.setStatus(StatusPenyewaan.PENDING);
                    penyewaan.setWaktuPengajuan(LocalDateTime.now());

                    int totalBiaya = kos.getHarga() * penyewaan.getDurasiSewa();
                    penyewaan.setTotalBiaya(totalBiaya);

                    kos.setJumlah(kos.getJumlahTersedia() - 1);

                    Penyewaan saved = penyewaanRepository.save(penyewaan);
                    result.complete(saved);
                    return result;
                })
                .orElseGet(() -> {
                    CompletableFuture<Penyewaan> result = new CompletableFuture<>();
                    result.completeExceptionally(
                            new EntityNotFoundException("Kos tidak ditemukan dengan ID: " + kosId));
                    return result;
                });
    }

    @Override
    @Async("penyewaanTaskExecutor")
    public CompletableFuture<List<Penyewaan>> findByPenyewa(User penyewa) {
        List<Penyewaan> penyewaanList = penyewaanRepository.findByPenyewa(penyewa);
        penyewaanList.forEach(p -> {
            if (p.getTotalBiaya() == 0 && p.getKos() != null) {
                p.hitungTotalBiaya();
            }
        });
        return CompletableFuture.completedFuture(penyewaanList);
    }

    @Override
    @Async("penyewaanTaskExecutor")
    public CompletableFuture<List<Penyewaan>> findByPenyewaAndStatus(User penyewa, StatusPenyewaan status) {
        return CompletableFuture.completedFuture(penyewaanRepository.findByPenyewaAndStatus(penyewa, status));
    }

    @Override
    @Async("penyewaanTaskExecutor")
    public CompletableFuture<Optional<Penyewaan>> findById(String id) {
        return CompletableFuture.completedFuture(penyewaanRepository.findById(id));
    }

    @Override
    @Async("penyewaanTaskExecutor")
    public CompletableFuture<Optional<Penyewaan>> findByIdAndPenyewa(String id, User penyewa) {
        Optional<Penyewaan> penyewaanOpt = penyewaanRepository.findByIdAndPenyewa(id, penyewa);
        penyewaanOpt.ifPresent(penyewaan -> {
            if (penyewaan.getTotalBiaya() == 0 && penyewaan.getKos() != null) {
                penyewaan.hitungTotalBiaya();
            }
        });
        return CompletableFuture.completedFuture(penyewaanOpt);
    }

    @Override
    @Async("penyewaanTaskExecutor")
    @Transactional
    public CompletableFuture<Penyewaan> updatePenyewaan(Penyewaan updatedPenyewaan, String id, User penyewa) {
        return findByIdAndPenyewa(id, penyewa)
                .thenApply(optionalPenyewaan -> optionalPenyewaan.orElseThrow(
                        () -> new EntityNotFoundException("Penyewaan tidak ditemukan atau bukan milik penyewa ini")))
                .thenApply(existingPenyewaan -> {
                    if (!isPenyewaanEditable(existingPenyewaan)) {
                        throw new IllegalStateException(
                                "Penyewaan tidak dapat diedit karena status: " + existingPenyewaan.getStatus());
                    }
                    if (updatedPenyewaan.getTanggalCheckIn().isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException("Tanggal check-in tidak boleh di masa lalu");
                    }
                    if (updatedPenyewaan.getDurasiSewa() < 1 || updatedPenyewaan.getDurasiSewa() > 12) {
                        throw new IllegalArgumentException("Durasi sewa harus antara 1-12 bulan");
                    }

                    existingPenyewaan.setNamaLengkap(updatedPenyewaan.getNamaLengkap());
                    existingPenyewaan.setNomorTelepon(updatedPenyewaan.getNomorTelepon());
                    existingPenyewaan.setTanggalCheckIn(updatedPenyewaan.getTanggalCheckIn());
                    existingPenyewaan.setDurasiSewa(updatedPenyewaan.getDurasiSewa());
                    int totalBiaya = existingPenyewaan.getKos().getHarga() * existingPenyewaan.getDurasiSewa();
                    existingPenyewaan.setTotalBiaya(totalBiaya);

                    return penyewaanRepository.save(existingPenyewaan);
                });
    }

    @Override
    @Async("penyewaanTaskExecutor")
    @Transactional
    public CompletableFuture<Void> cancelPenyewaan(String id, User penyewa) {
        return findByIdAndPenyewa(id, penyewa)
                .thenApply(optionalPenyewaan -> optionalPenyewaan.orElseThrow(
                        () -> new EntityNotFoundException("Penyewaan tidak ditemukan atau bukan milik penyewa ini")))
                .thenAccept(penyewaan -> {
                    if (!isPenyewaanCancellable(penyewaan)) {
                        throw new IllegalStateException(
                                "Penyewaan tidak dapat dibatalkan karena status: " + penyewaan.getStatus());
                    }

                    penyewaan.setStatus(StatusPenyewaan.REJECTED);
                    penyewaanRepository.save(penyewaan);
                });
    }

    @Override
    public boolean isPenyewaanEditable(Penyewaan penyewaan) {
        return penyewaan.getStatus() == StatusPenyewaan.PENDING;
    }

    @Override
    public boolean isPenyewaanCancellable(Penyewaan penyewaan) {
        return penyewaan.getStatus() == StatusPenyewaan.PENDING ||
                (penyewaan.getStatus() == StatusPenyewaan.APPROVED &&
                        penyewaan.getTanggalCheckIn().isAfter(LocalDate.now()));
    }
}