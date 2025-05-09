package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PengelolaanRepositoryTest {

    @Autowired
    private PengelolaanRepository pengelolaanRepository;

    @BeforeEach
    void setUp() {
        // Clear the repository data before each test
        pengelolaanRepository.deleteAll();
    }

    @Test
    void testCreateAndFind() {
        Kos kos = new Kos();
        kos.setNama("Tulip");
        kos.setJumlah(20);
        kos.setAlamat("Jl. Mangga");
        kos.setDeskripsi("Full furnish");
        kos.setHarga(1000000);
        kos.setStatus("AVAILABLE");
        kos.setUrlFoto("https://i.pinimg.com/736x/6e/df/3c/6edf3c96bcbd31aa41038c70c515daf2.jpg");

        Kos savedKos = pengelolaanRepository.create(kos);

        List<Kos> kosList = pengelolaanRepository.findAll();
        assertEquals(1, kosList.size());
        Kos retrievedKos = kosList.get(0);
        assertNotNull(retrievedKos.getId());
        assertEquals("Tulip", retrievedKos.getNama());
        assertEquals(20, retrievedKos.getJumlah());
        assertEquals("Jl. Mangga", retrievedKos.getAlamat());
        assertEquals("Full furnish", retrievedKos.getDeskripsi());
        assertEquals(1000000, retrievedKos.getHarga());
        assertEquals("AVAILABLE", retrievedKos.getStatus());
        assertEquals("https://i.pinimg.com/736x/6e/df/3c/6edf3c96bcbd31aa41038c70c515daf2.jpg", retrievedKos.getUrlFoto());
    }

    @Test
    void testFindAllIfEmpty() {
        List<Kos> kosList = pengelolaanRepository.findAll();
        assertTrue(kosList.isEmpty());
    }

    @Test
    void testFindAllIfMoreThanOneKos() {
        Kos kos1 = new Kos();
        kos1.setNama("Tulip");
        kos1.setJumlah(20);
        kos1.setAlamat("Jl. Mangga");
        kos1.setDeskripsi("Full furnish");
        kos1.setHarga(1000000);
        kos1.setStatus("AVAILABLE");
        kos1.setUrlFoto("https://i.pinimg.com/736x/f3/f2/a2/f3f2a2d6b0c26389bc95260364251cef.jpg");
        pengelolaanRepository.create(kos1);

        Kos kos2 = new Kos();
        kos2.setNama("Melati");
        kos2.setJumlah(15);
        kos2.setAlamat("Jl. Mawar");
        kos2.setDeskripsi("Minimalis");
        kos2.setHarga(1200000);
        kos2.setStatus("AVAILABLE");
        kos2.setUrlFoto("https://i.pinimg.com/736x/6e/df/3c/6edf3c96bcbd31aa41038c70c515daf2.jpg");
        pengelolaanRepository.create(kos2);

        List<Kos> kosList = pengelolaanRepository.findAll();
        assertEquals(2, kosList.size());
        assertTrue(kosList.stream().anyMatch(k -> "Tulip".equals(k.getNama())));
        assertTrue(kosList.stream().anyMatch(k -> "Melati".equals(k.getNama())));
    }

    @Test
    void testCreateAndEdit() {
        Kos kos = new Kos();
        kos.setNama("Tulip");
        kos.setJumlah(20);
        kos.setAlamat("Jl. Mangga");
        kos.setDeskripsi("Full furnish");
        kos.setHarga(1000000);
        kos.setStatus("AVAILABLE");
        kos.setUrlFoto("https://i.pinimg.com/736x/f3/f2/a2/f3f2a2d6b0c26389bc95260364251cef.jpg");
        Kos savedKos = pengelolaanRepository.create(kos);

        Kos updatedKos = new Kos();
        updatedKos.setId(savedKos.getId());
        updatedKos.setNama("Melati");
        updatedKos.setJumlah(30);
        updatedKos.setAlamat("Jl. Mawar");
        updatedKos.setDeskripsi("Minimalis");
        updatedKos.setHarga(1500000);
        updatedKos.setStatus("FULL");
        updatedKos.setUrlFoto("https://i.pinimg.com/736x/6e/df/3c/6edf3c96bcbd31aa41038c70c515daf2.jpg");
        pengelolaanRepository.update(updatedKos);

        Kos retrievedKos = pengelolaanRepository.findByIdOrThrow(savedKos.getId());
        assertEquals("Melati", retrievedKos.getNama());
        assertEquals(30, retrievedKos.getJumlah());
        assertEquals("Jl. Mawar", retrievedKos.getAlamat());
        assertEquals("Minimalis", retrievedKos.getDeskripsi());
        assertEquals(1500000, retrievedKos.getHarga());
        assertEquals("FULL", retrievedKos.getStatus());
        assertEquals("https://i.pinimg.com/736x/6e/df/3c/6edf3c96bcbd31aa41038c70c515daf2.jpg", retrievedKos.getUrlFoto());
    }

    @Test
    void testCreateAndDelete() {
        Kos kos = new Kos();
        kos.setNama("Tulip");
        kos.setJumlah(20);
        kos.setAlamat("Jl. Mangga");
        kos.setDeskripsi("Full furnish");
        kos.setHarga(1000000);
        kos.setStatus("AVAILABLE");
        kos.setUrlFoto("https://i.pinimg.com/736x/6e/df/3c/6edf3c96bcbd31aa41038c70c515daf2.jpg");
        Kos savedKos = pengelolaanRepository.create(kos);

        pengelolaanRepository.delete(savedKos);

        List<Kos> kosList = pengelolaanRepository.findAll();
        assertTrue(kosList.isEmpty());
    }

    @Test
    void testFindByKosIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        String errorMessage = "Kos dengan ID " + nonExistentId + " tidak ditemukan.";

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanRepository.findByIdOrThrow(nonExistentId)
        );

        assertEquals(errorMessage, thrown.getMessage());
    }

    @Test
    void testUpdateNotFound() {
        Kos kos = new Kos();
        UUID nonExistentId = UUID.randomUUID();
        kos.setId(nonExistentId);
        String errorMessage = "Kos dengan ID " + nonExistentId + " tidak ditemukan.";

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanRepository.update(kos)
        );

        assertEquals(errorMessage, thrown.getMessage());
    }

    @Test
    void testDeleteNotFound() {
        Kos kos = new Kos();
        UUID nonExistentId = UUID.randomUUID();
        kos.setId(nonExistentId);
        String errorMessage = "Kos dengan ID " + nonExistentId + " tidak ditemukan.";

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanRepository.delete(kos)
        );

        assertEquals(errorMessage, thrown.getMessage());
    }
}