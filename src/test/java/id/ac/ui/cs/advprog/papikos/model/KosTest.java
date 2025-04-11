package id.ac.ui.cs.advprog.papikos.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KosTest {
    private Kos kos;

    @BeforeEach
    void setUp() {
        this.kos = new Kos();
        this.kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        this.kos.setNama("Tulip");
        this.kos.setJumlah(20);
        this.kos.setAlamat("Jl.Mangga");
        this.kos.setDeskripsi("Full furnish. Dilengkapi dengan AC");
    }

    @Test
    void testGetIdKos() {
        assertEquals("eb558e9f-1c39-460e-8860-71af6af63bd6", this.kos.getId());
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
}