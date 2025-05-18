package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import id.ac.ui.cs.advprog.papikos.kos.repository.PenyewaanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @Transactional
    public Penyewaan createPenyewaan(Penyewaan penyewaan, String kosId, User penyewa) {
        UUID kosUUID;
        try {
            kosUUID = UUID.fromString(kosId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid kos ID format: " + kosId);
        }

        Kos kos = kosRepository.findById(kosUUID)
                .orElseThrow(() -> new EntityNotFoundException("Kos tidak ditemukan dengan ID: " + kosId));

        if (!"AVAILABLE".equals(kos.getStatus())) {
            throw new IllegalStateException("Kos tidak tersedia untuk disewa");
        }

        int jumlahTersedia = kos.getJumlahTersedia();
        if (jumlahTersedia <= 0) {
            throw new IllegalStateException("Tidak ada kamar tersedia untuk disewa");
        }
        if (penyewaan.getTanggalCheckIn().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal check-in tidak boleh di masa lalu");
        }
        if (penyewaan.getDurasiSewa() < 1) {
            throw new IllegalArgumentException("Durasi sewa minimal 1 bulan");
        }
        if (penyewaan.getDurasiSewa() > 12) {
            throw new IllegalArgumentException("Durasi sewa maksimal 12 bulan");
        }

        penyewaan.setKos(kos);
        penyewaan.setPenyewa(penyewa);
        penyewaan.setStatus(StatusPenyewaan.PENDING);
        penyewaan.setWaktuPengajuan(LocalDateTime.now());

        int totalBiaya = kos.getHarga() * penyewaan.getDurasiSewa();
        penyewaan.setTotalBiaya(totalBiaya);
        return penyewaanRepository.save(penyewaan);
    }

    @Override
    public List<Penyewaan> findByPenyewa(User penyewa) {
        return penyewaanRepository.findByPenyewa(penyewa);
    }

    @Override
    public List<Penyewaan> findByPenyewaAndStatus(User penyewa, StatusPenyewaan status) {
        return penyewaanRepository.findByPenyewaAndStatus(penyewa, status);
    }

    @Override
    public Optional<Penyewaan> findById(String id) {
        return penyewaanRepository.findById(id);
    }

    @Override
    public Optional<Penyewaan> findByIdAndPenyewa(String id, User penyewa) {
        return penyewaanRepository.findByIdAndPenyewa(id, penyewa);
    }

    @Override
    @Transactional
    public Penyewaan updatePenyewaan(Penyewaan updatedPenyewaan, String id, User penyewa) {
        Penyewaan existingPenyewaan = penyewaanRepository.findByIdAndPenyewa(id, penyewa)
                .orElseThrow(() -> new EntityNotFoundException("Penyewaan tidak ditemukan atau bukan milik penyewa ini"));

        if (!isPenyewaanEditable(existingPenyewaan)) {
            throw new IllegalStateException("Penyewaan tidak dapat diedit karena status: " + existingPenyewaan.getStatus());
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
    }

    @Override
    @Transactional
    public void cancelPenyewaan(String id, User penyewa) {
        Penyewaan penyewaan = penyewaanRepository.findByIdAndPenyewa(id, penyewa)
                .orElseThrow(() -> new EntityNotFoundException("Penyewaan tidak ditemukan atau bukan milik penyewa ini"));

        if (!isPenyewaanCancellable(penyewaan)) {
            throw new IllegalStateException("Penyewaan tidak dapat dibatalkan karena status: " + penyewaan.getStatus());
        }

        penyewaan.setStatus(StatusPenyewaan.REJECTED);
        penyewaanRepository.save(penyewaan);
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