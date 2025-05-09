package id.ac.ui.cs.advprog.papikos.kos.model;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KosTest {
    private Kos kos;

    @Mock
    private User mockPemilik;

    @Mock
    private Penyewaan mockPenyewaan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.kos = new Kos();
        this.kos.setId(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"));
        this.kos.setNama("Tulip");
        this.kos.setJumlah(20);
        this.kos.setAlamat("Jl.Mangga");
        this.kos.setDeskripsi("Full furnish. Dilengkapi dengan AC");
        this.kos.setHarga(1500000);
        this.kos.setStatus("AVAILABLE");
        this.kos.setPemilik(mockPemilik);
        this.kos.setPenyewaan(new ArrayList<>());
    }

    @Test
    void testGetIdKos() {
        assertEquals(UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6"), this.kos.getId());
    }

    @Test
    void testGetNamaKos() {
        assertEquals("Tulip", this.kos.getNama());
    }

    @Test
    void testGetJumlahKos() {
        assertEquals(20, this.kos.getJumlah());
    }

    @Test
    void testGetAlamatKos() {
        assertEquals("Jl.Mangga", this.kos.getAlamat());
    }

    @Test
    void testGetDeskripsiKos() {
        assertEquals("Full furnish. Dilengkapi dengan AC", this.kos.getDeskripsi());
    }

    @Test
    void testOnCreate() {
        assertNull(kos.getCreatedAt());
        assertNull(kos.getUpdatedAt());

        kos.onCreate();

        assertNotNull(kos.getCreatedAt());
        assertNotNull(kos.getUpdatedAt());
        assertTrue(LocalDateTime.now().minusMinutes(1).isBefore(kos.getCreatedAt()));
    }

    @Test
    void testOnUpdate() {
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        kos.setCreatedAt(initialTime);
        kos.setUpdatedAt(initialTime);

        kos.onUpdate();

        assertEquals(initialTime, kos.getCreatedAt());
        assertNotEquals(initialTime, kos.getUpdatedAt());
        assertTrue(kos.getUpdatedAt().isAfter(initialTime));
    }

    @Test
    void testNoApprovedPenyewaan() {
        assertEquals(20, kos.getJumlahTersedia());
    }

    @Test
    void testWithApprovedPenyewaan() {
        Penyewaan penyewaan1 = mock(Penyewaan.class);
        Penyewaan penyewaan2 = mock(Penyewaan.class);

        when(penyewaan1.getStatus()).thenReturn(StatusPenyewaan.APPROVED);
        when(penyewaan2.getStatus()).thenReturn(StatusPenyewaan.PENDING);

        List<Penyewaan> penyewaanList = new ArrayList<>();
        penyewaanList.add(penyewaan1);
        penyewaanList.add(penyewaan2);
        kos.setPenyewaan(penyewaanList);

        assertEquals(19, kos.getJumlahTersedia());
    }

    @Test
    void testWhenAvailableAndHasRooms() {
        kos.setStatus("AVAILABLE");
        assertTrue(kos.isAvailable());
    }

    @Test
    void testWhenNotAvailableStatus() {
        kos.setStatus("FULL");
        assertFalse(kos.isAvailable());
    }

    @Test
    void testWhenAvailableButNoRooms() {
        kos.setStatus("AVAILABLE");

        List<Penyewaan> penyewaanList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Penyewaan p = mock(Penyewaan.class);
            when(p.getStatus()).thenReturn(StatusPenyewaan.APPROVED);
            penyewaanList.add(p);
        }
        kos.setPenyewaan(penyewaanList);

        assertFalse(kos.isAvailable());
    }

    @Test
    void testAddPenyewaan() {
        Penyewaan newPenyewaan = new Penyewaan();
        kos.addPenyewaan(newPenyewaan);

        assertTrue(kos.getPenyewaan().contains(newPenyewaan));
        assertEquals(kos, newPenyewaan.getKos());
    }

    @Test
    void testRemovePenyewaan() {
        Penyewaan penyewaanToRemove = new Penyewaan();
        penyewaanToRemove.setKos(kos);
        kos.getPenyewaan().add(penyewaanToRemove);

        assertTrue(kos.getPenyewaan().contains(penyewaanToRemove));

        kos.removePenyewaan(penyewaanToRemove);

        assertFalse(kos.getPenyewaan().contains(penyewaanToRemove));
        assertNull(penyewaanToRemove.getKos());
    }
}