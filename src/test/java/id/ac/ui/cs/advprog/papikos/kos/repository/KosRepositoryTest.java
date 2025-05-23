package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class KosRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KosRepository kosRepository;

    private User pemilik1;
    private User pemilik2;
    private Kos kos1;
    private Kos kos2;
    private Kos kos3;

    @BeforeEach
    void setUp() {
        pemilik1 = new User("pemilik1@example.com", "password123!", Role.PEMILIK_KOS);
        entityManager.persist(pemilik1);

        pemilik2 = new User("pemilik2@example.com", "password456!", Role.PEMILIK_KOS);
        entityManager.persist(pemilik2);

        kos1 = new Kos();
        kos1.setNama("Kos Melati Putih");
        kos1.setAlamat("Jl. Kenanga No. 10");
        kos1.setDeskripsi("Kos nyaman untuk mahasiswa");
        kos1.setHarga(1500000);
        kos1.setJumlah(5);
        kos1.setStatus("AVAILABLE");
        kos1.setPemilik(pemilik1);
        kos1.setPenyewaan(new ArrayList<>());
        kos1.setCreatedAt(LocalDateTime.now());
        kos1.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(kos1);

        kos2 = new Kos();
        kos2.setNama("Kos Anggrek Residence");
        kos2.setAlamat("Jl. Mawar No. 5");
        kos2.setDeskripsi("Kos eksklusif dengan AC");
        kos2.setHarga(2500000);
        kos2.setJumlah(3);
        kos2.setStatus("AVAILABLE");
        kos2.setPemilik(pemilik1);
        kos2.setPenyewaan(new ArrayList<>());
        kos2.setCreatedAt(LocalDateTime.now());
        kos2.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(kos2);

        kos3 = new Kos();
        kos3.setNama("Kos Cendana");
        kos3.setAlamat("Jl. Kenanga No. 15");
        kos3.setDeskripsi("Kos sederhana dan nyaman");
        kos3.setHarga(1000000);
        kos3.setJumlah(10);
        kos3.setStatus("FULL");
        kos3.setPemilik(pemilik2);
        kos3.setPenyewaan(new ArrayList<>());
        kos3.setCreatedAt(LocalDateTime.now());
        kos3.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(kos3);

        entityManager.flush();
    }

    @Test
    void testFindByStatus() {
        List<Kos> availableKos = kosRepository.findByStatus("AVAILABLE");
        assertEquals(2, availableKos.size());
        assertTrue(availableKos.stream().allMatch(k -> k.getStatus().equals("AVAILABLE")));
        
        List<Kos> notAvailableKos = kosRepository.findByStatus("FULL");
        assertEquals(1, notAvailableKos.size());
        assertTrue(notAvailableKos.stream().allMatch(k -> k.getStatus().equals("FULL")));
    }

    @Test
    void testFindByNamaContainingIgnoreCaseAndStatus() {
        List<Kos> result1 = kosRepository.findByNamaContainingIgnoreCaseAndStatus("melati", "AVAILABLE");
        assertEquals(1, result1.size());
        assertEquals("Kos Melati Putih", result1.get(0).getNama());

        List<Kos> result2 = kosRepository.findByNamaContainingIgnoreCaseAndStatus("ANGGREK", "AVAILABLE");
        assertEquals(1, result2.size());
        assertEquals("Kos Anggrek Residence", result2.get(0).getNama());

        List<Kos> result3 = kosRepository.findByNamaContainingIgnoreCaseAndStatus("kos", "AVAILABLE");
        assertEquals(2, result3.size());

        List<Kos> result4 = kosRepository.findByNamaContainingIgnoreCaseAndStatus("nonexistent", "AVAILABLE");
        assertEquals(0, result4.size());
    }

    @Test
    void testFindByAlamatContainingIgnoreCaseAndStatus() {
        List<Kos> result1 = kosRepository.findByAlamatContainingIgnoreCaseAndStatus("kenanga", "AVAILABLE");
        assertEquals(1, result1.size());
        assertEquals("Kos Melati Putih", result1.get(0).getNama());

        List<Kos> result2 = kosRepository.findByAlamatContainingIgnoreCaseAndStatus("kenanga", "FULL");
        assertEquals(1, result2.size());
        assertEquals("Kos Cendana", result2.get(0).getNama());
    }

    @Test
    void testFindByHargaBetweenAndStatus() {
        List<Kos> result1 = kosRepository.findByHargaBetweenAndStatus(1000000, 2000000, "AVAILABLE");
        assertEquals(1, result1.size());
        assertEquals("Kos Melati Putih", result1.get(0).getNama());

        List<Kos> result2 = kosRepository.findByHargaBetweenAndStatus(1000000, 3000000, "AVAILABLE");
        assertEquals(2, result2.size());

        List<Kos> result3 = kosRepository.findByHargaBetweenAndStatus(3000000, 5000000, "AVAILABLE");
        assertEquals(0, result3.size());
    }

    @Test
    void testFindByPemilik() {
        List<Kos> result1 = kosRepository.findByPemilik(pemilik1);
        assertEquals(2, result1.size());
        assertTrue(result1.stream().allMatch(k -> k.getPemilik().equals(pemilik1)));
        
        List<Kos> result2 = kosRepository.findByPemilik(pemilik2);
        assertEquals(1, result2.size());
        assertEquals("Kos Cendana", result2.get(0).getNama());
        assertEquals("pemilik2@example.com", result2.get(0).getPemilik().getEmail());
    }

    @Test
    void testSearchByKeyword() {
        List<Kos> result1 = kosRepository.searchByKeyword("melati", "AVAILABLE");
        assertEquals(1, result1.size());
        assertEquals("Kos Melati Putih", result1.get(0).getNama());

        List<Kos> result2 = kosRepository.searchByKeyword("mawar", "AVAILABLE");
        assertEquals(1, result2.size());
        assertEquals("Kos Anggrek Residence", result2.get(0).getNama());

        List<Kos> result3 = kosRepository.searchByKeyword("AC", "AVAILABLE");
        assertEquals(1, result3.size());
        assertEquals("Kos Anggrek Residence", result3.get(0).getNama());

        List<Kos> result4 = kosRepository.searchByKeyword(null, "AVAILABLE");
        assertEquals(2, result4.size());

        List<Kos> result5 = kosRepository.searchByKeyword("", "AVAILABLE");
        assertEquals(2, result5.size());

        List<Kos> result6 = kosRepository.searchByKeyword("nonexistent", "AVAILABLE");
        assertEquals(0, result6.size());
    }

    @Test
    void testSaveKos() {
        User newPemilik = new User("newpemilik@example.com", "password789!", Role.PEMILIK_KOS);
        entityManager.persist(newPemilik);
        
        Kos newKos = new Kos();
        newKos.setNama("Kos Baru");
        newKos.setAlamat("Jl. Baru No. 1");
        newKos.setDeskripsi("Kos baru yang nyaman");
        newKos.setHarga(1800000);
        newKos.setJumlah(8);
        newKos.setStatus("AVAILABLE");
        newKos.setPemilik(newPemilik);
        newKos.setPenyewaan(new ArrayList<>());
        
        Kos savedKos = kosRepository.save(newKos);
        assertNotNull(savedKos.getId());

        Optional<Kos> foundKos = kosRepository.findById(savedKos.getId());
        assertTrue(foundKos.isPresent());
        assertEquals("Kos Baru", foundKos.get().getNama());
        assertEquals("newpemilik@example.com", foundKos.get().getPemilik().getEmail());
    }

    @Test
    void testUpdateKos() {
        kos1.setHarga(1600000);
        kos1.setNama("Kos Melati Putih Updated");

        Optional<Kos> foundKos = kosRepository.findById(kos1.getId());
        assertTrue(foundKos.isPresent());
        assertEquals(1600000, foundKos.get().getHarga());
        assertEquals("Kos Melati Putih Updated", foundKos.get().getNama());
        assertEquals("pemilik1@example.com", foundKos.get().getPemilik().getEmail());
    }

    @Test
    void testDeleteKos() {
        kosRepository.delete(kos1);

        Optional<Kos> foundKos = kosRepository.findById(kos1.getId());
        assertFalse(foundKos.isPresent());
    }
}