package id.ac.ui.cs.advprog.papikos.kos.model;

import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "kos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kos {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @NotBlank(message = "Nama kos tidak boleh kosong")
    @Size(max = 100, message = "Nama kos maksimal 100 karakter")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Nama kos hanya boleh berisi huruf dan spasi")
    @Column(nullable = false)
    private String nama;

    @NotBlank(message = "Alamat tidak boleh kosong")
    @Size(max = 255, message = "Alamat maksimal 255 karakter")
    @Pattern(regexp = "^[A-Za-z0-9\\s.,\\-:()\"'/]+$",
            message = "Alamat mengandung karakter yang tidak diperbolehkan")
    @Column(nullable = false)
    private String alamat;

    @NotBlank(message = "Deskripsi tidak boleh kosong")
    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    @Pattern(regexp = "^[A-Za-z0-9\\s.,\\-:()\"'/!?*@]+$",
            message = "Deskripsi mengandung karakter yang tidak diperbolehkan")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String deskripsi;

    @Min(value = 1, message = "Jumlah kamar minimal 1")
    @Column(nullable = false)
    private int jumlah;

    @Min(value = 0, message = "Harga tidak boleh negatif")
    @Column(nullable = false)
    private int harga;

    @NotBlank(message = "Status tidak boleh kosong")
    @Column(length = 20, nullable = false)
    private String status;

    @Pattern(regexp = "^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,})(\\/[\\w\\.-]*)*\\/?$", 
            message = "Format URL foto tidak valid")
    @Column(name = "url_foto")
    private String urlFoto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pemilik_id")
    private User pemilik;

    @OneToMany(mappedBy = "kos", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Penyewaan> penyewaan = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "AVAILABLE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public int getJumlahTersedia() {
        long jumlahTersewa = penyewaan.stream()
                .filter(p -> {
                    if (p.getStatus() instanceof StatusPenyewaan) {
                        return StatusPenyewaan.APPROVED.equals(p.getStatus());
                    }
                    return false;
                })
                .count();

        return jumlah - (int) jumlahTersewa;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(status) && getJumlahTersedia() > 0;
    }

    public void addPenyewaan(Penyewaan penyewaanBaru) {
        penyewaan.add(penyewaanBaru);
        penyewaanBaru.setKos(this);
    }

    public void removePenyewaan(Penyewaan penyewaanHapus) {
        penyewaan.remove(penyewaanHapus);
        penyewaanHapus.setKos(null);
    }
}