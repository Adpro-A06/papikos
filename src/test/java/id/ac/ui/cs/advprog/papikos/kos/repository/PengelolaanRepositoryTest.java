package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PengelolaanRepositoryTest {

    @Autowired
    private PengelolaanRepository pengelolaanRepository;

    @Configuration
    static class TestConfig {
        @Bean
        public PengelolaanRepository pengelolaanRepository() {
            return new PengelolaanRepository();
        }
    }

    @BeforeEach
    void setUp() {
        // Clear the repository data before each test
        Iterator<Kos> iterator = pengelolaanRepository.findAll();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    @Test
    void testCreateAndFind() {
        Kos kos = new Kos();
        kos.setNama("Tulip");
        kos.setJumlah(20);
        pengelolaanRepository.create(kos);

        Iterator<Kos> kosIterator = pengelolaanRepository.findAll();
        assertTrue(kosIterator.hasNext());
        Kos savedKos = kosIterator.next();
        assertNotNull(savedKos.getId());
        assertEquals("Tulip", savedKos.getNama());
        assertEquals(20, savedKos.getJumlah());
    }

    @Test
    void testFindAllIfEmpty() {
        Iterator<Kos> kosIterator = pengelolaanRepository.findAll();
        assertFalse(kosIterator.hasNext());
    }

    @Test
    void testFindAllIfMoreThanOneKos() {
        Kos kos1 = new Kos();
        kos1.setNama("Tulip");
        pengelolaanRepository.create(kos1);

        Kos kos2 = new Kos();
        kos2.setNama("Melati");
        pengelolaanRepository.create(kos2);

        Iterator<Kos> kosIterator = pengelolaanRepository.findAll();
        assertTrue(kosIterator.hasNext());
        Kos savedKos1 = kosIterator.next();
        assertEquals("Tulip", savedKos1.getNama());
        assertTrue(kosIterator.hasNext());
        Kos savedKos2 = kosIterator.next();
        assertEquals("Melati", savedKos2.getNama());
        assertFalse(kosIterator.hasNext());
    }

    @Test
    void testCreateAndEdit() {
        Kos kos = new Kos();
        kos.setNama("Tulip");
        kos.setJumlah(20);
        kos.setAlamat("Jl. Mangga");
        kos.setDeskripsi("Full furnish");
        kos.setHarga(1000000);
        kos.setStatus("Tersedia");
        pengelolaanRepository.create(kos);

        Iterator<Kos> kosIterator = pengelolaanRepository.findAll();
        assertTrue(kosIterator.hasNext());
        Kos savedKos = kosIterator.next();

        Kos updatedKos = new Kos();
        updatedKos.setId(savedKos.getId());
        updatedKos.setNama("Melati");
        updatedKos.setJumlah(30);
        updatedKos.setAlamat("Jl. Mawar");
        updatedKos.setDeskripsi("Minimalis");
        updatedKos.setHarga(1500000);
        updatedKos.setStatus("Penuh");
        pengelolaanRepository.update(updatedKos);

        Kos retrievedKos = pengelolaanRepository.findById(savedKos.getId());
        assertEquals("Melati", retrievedKos.getNama());
        assertEquals(30, retrievedKos.getJumlah());
        assertEquals("Jl. Mawar", retrievedKos.getAlamat());
        assertEquals("Minimalis", retrievedKos.getDeskripsi());
        assertEquals(1500000, retrievedKos.getHarga());
        assertEquals("Penuh", retrievedKos.getStatus());
    }

    @Test
    void testCreateAndDelete() {
        Kos kos = new Kos();
        kos.setNama("Tulip");
        pengelolaanRepository.create(kos);

        Iterator<Kos> kosIterator = pengelolaanRepository.findAll();
        assertTrue(kosIterator.hasNext());

        pengelolaanRepository.delete(kos);

        kosIterator = pengelolaanRepository.findAll();
        assertFalse(kosIterator.hasNext());
    }

    @Test
    void testFindByKosIdNotFound() {
        String errorMessage = "Kos dengan ID notexist-id tidak ditemukan.";

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanRepository.findById("notexist-id")
        );

        assertEquals(errorMessage, thrown.getMessage());
    }

    @Test
    void testUpdateNotFound() {
        Kos kos = new Kos();
        kos.setId("notexist-id");
        String errorMessage = "Kos dengan ID notexist-id tidak ditemukan.";

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanRepository.update(kos)
        );

        assertEquals(errorMessage, thrown.getMessage());
    }

    @Test
    void testDeleteNotFound() {
        Kos kos = new Kos();
        kos.setId("notexist-id");
        String errorMessage = "Kos dengan ID notexist-id tidak ditemukan.";

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanRepository.delete(kos)
        );

        assertEquals(errorMessage, thrown.getMessage());
    }
}