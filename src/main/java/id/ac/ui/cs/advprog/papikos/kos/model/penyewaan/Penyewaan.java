package id.ac.ui.cs.advprog.papikos.kos.model.penyewaan;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "penyewaan_kos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Penyewaan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kos_id", nullable = false)
    private Kos kos;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User penyewa;

    @NotBlank(message = "Nama lengkap tidak boleh kosong")
    @Size(min = 3, max = 100, message = "Nama lengkap harus antara 3-100 karakter")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Nama lengkap hanya boleh berisi huruf dan spasi")
    @Column(name = "nama_lengkap", nullable = false)
    private String namaLengkap;
    
    @NotBlank(message = "Nomor telepon tidak boleh kosong")
    @Pattern(regexp = "^[0-9]+$", message = "Nomor telepon hanya boleh berisi angka")
    @Size(min = 10, max = 15, message = "Nomor telepon harus antara 10-15 digit")
    @Column(name = "nomor_telepon", nullable = false)
    private String nomorTelepon;
    
    @NotNull(message = "Tanggal check-in tidak boleh kosong")
    @Future(message = "Tanggal check-in harus di masa depan")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "tanggal_check_in", nullable = false)
    private LocalDate tanggalCheckIn;

    @Min(value = 1, message = "Durasi sewa minimal 1 bulan")
    @Max(value = 12, message = "Durasi sewa maksimal 12 bulan")
    @Column(name = "durasi_sewa", nullable = false)
    private int durasiSewa;
    
    @Min(value = 0, message = "Total biaya tidak boleh negatif")
    @Column(name = "total_biaya", nullable = false)
    private int totalBiaya;

    @NotNull(message = "Status penyewaan tidak boleh kosong")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPenyewaan status;

    @Column(name = "waktu_pengajuan", nullable = false)
    private LocalDateTime waktuPengajuan;
    
    @Column(name = "waktu_perubahan")
    private LocalDateTime waktuPerubahan;

    @PrePersist
    protected void onCreate() {
        waktuPengajuan = LocalDateTime.now();
        waktuPerubahan = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        waktuPerubahan = LocalDateTime.now();
    }

    public void hitungTotalBiaya() {
        if (kos != null && durasiSewa > 0) {
            this.totalBiaya = kos.getHarga() * durasiSewa;
        }
    }
}