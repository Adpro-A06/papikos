package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PenyewaanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PenyewaanRepository penyewaanRepository;

    private User penyewa1;
    private User penyewa2;
    private User pemilik;
    private Kos kos1;
    private Kos kos2;
    private Penyewaan penyewaan1;
    private Penyewaan penyewaan2;
    private Penyewaan penyewaan3;
    private Penyewaan penyewaan4;

    @BeforeEach
    void setUp() {
        penyewa1 = new User("penyewa1@example.com", "password123!", Role.PENYEWA);
        penyewa2 = new User("penyewa2@example.com", "password456!", Role.PENYEWA);
        pemilik = new User("pemilik@example.com", "password789!", Role.PEMILIK_KOS);

        entityManager.persist(penyewa1);
        entityManager.persist(penyewa2);
        entityManager.persist(pemilik);

        kos1 = new Kos();
        kos1.setNama("Kos Melati");
        kos1.setAlamat("Jl. Kenanga No. 10");
        kos1.setDeskripsi("Kos nyaman dekat kampus");
        kos1.setHarga(1500000);
        kos1.setJumlah(5);
        kos1.setStatus("AVAILABLE");
        kos1.setPemilik(pemilik);
        kos1.setPenyewaan(new ArrayList<>());
        entityManager.persist(kos1);

        kos2 = new Kos();
        kos2.setNama("Kos Anggrek");
        kos2.setAlamat("Jl. Mawar No. 5");
        kos2.setDeskripsi("Kos eksklusif");
        kos2.setHarga(2500000);
        kos2.setJumlah(3);
        kos2.setStatus("AVAILABLE");
        kos2.setPemilik(pemilik);
        kos2.setPenyewaan(new ArrayList<>());
        entityManager.persist(kos2);

        LocalDate today = LocalDate.now();

        penyewaan1 = new Penyewaan();
        penyewaan1.setPenyewa(penyewa1);
        penyewaan1.setKos(kos1);
        penyewaan1.setNamaLengkap("Penyewa Satu");
        penyewaan1.setNomorTelepon("08123456789");
        penyewaan1.setStatus(StatusPenyewaan.PENDING);
        penyewaan1.setTanggalCheckIn(today.plusDays(5));
        penyewaan1.setDurasiSewa(3);
        penyewaan1.setTotalBiaya(4500000);
        penyewaan1.setWaktuPengajuan(LocalDateTime.now().minusDays(1));
        entityManager.persist(penyewaan1);

        penyewaan2 = new Penyewaan();
        penyewaan2.setPenyewa(penyewa1);
        penyewaan2.setKos(kos2);
        penyewaan2.setNamaLengkap("Penyewa Satu");
        penyewaan2.setNomorTelepon("08123456789");
        penyewaan2.setStatus(StatusPenyewaan.APPROVED);
        penyewaan2.setTanggalCheckIn(today.plusDays(10));
        penyewaan2.setDurasiSewa(6);
        penyewaan2.setTotalBiaya(15000000);
        penyewaan2.setWaktuPengajuan(LocalDateTime.now().minusDays(2));
        entityManager.persist(penyewaan2);

        penyewaan3 = new Penyewaan();
        penyewaan3.setPenyewa(penyewa2);
        penyewaan3.setKos(kos1);
        penyewaan3.setNamaLengkap("Penyewa Dua");
        penyewaan3.setNomorTelepon("08987654321");
        penyewaan3.setStatus(StatusPenyewaan.APPROVED);
        penyewaan3.setTanggalCheckIn(today.plusDays(2));
        penyewaan3.setDurasiSewa(12);
        penyewaan3.setTotalBiaya(18000000);
        penyewaan3.setWaktuPengajuan(LocalDateTime.now().minusDays(10));
        entityManager.persist(penyewaan3);

        penyewaan4 = new Penyewaan();
        penyewaan4.setPenyewa(penyewa2);
        penyewaan4.setKos(kos2);
        penyewaan4.setNamaLengkap("Penyewa Dua");
        penyewaan4.setNomorTelepon("08987654321");
        penyewaan4.setStatus(StatusPenyewaan.REJECTED);
        penyewaan4.setTanggalCheckIn(today.plusDays(15));
        penyewaan4.setDurasiSewa(1);
        penyewaan4.setTotalBiaya(2500000);
        penyewaan4.setWaktuPengajuan(LocalDateTime.now().minusDays(5));
        entityManager.persist(penyewaan4);

        entityManager.flush();
    }

    @Test
    void testFindByPenyewa() {
        List<Penyewaan> penyewaan1List = penyewaanRepository.findByPenyewa(penyewa1);
        assertEquals(2, penyewaan1List.size());
        assertTrue(penyewaan1List.stream().allMatch(p -> p.getPenyewa().equals(penyewa1)));

        List<Penyewaan> penyewaan2List = penyewaanRepository.findByPenyewa(penyewa2);
        assertEquals(2, penyewaan2List.size());
        assertTrue(penyewaan2List.stream().allMatch(p -> p.getPenyewa().equals(penyewa2)));
    }

    @Test
    void testFindByKos() {
        List<Penyewaan> kos1List = penyewaanRepository.findByKos(kos1);
        assertEquals(2, kos1List.size());
        assertTrue(kos1List.stream().allMatch(p -> p.getKos().equals(kos1)));

        List<Penyewaan> kos2List = penyewaanRepository.findByKos(kos2);
        assertEquals(2, kos2List.size());
        assertTrue(kos2List.stream().allMatch(p -> p.getKos().equals(kos2)));
    }

    @Test
    void testFindByIdAndPenyewa() {
        Optional<Penyewaan> found = penyewaanRepository.findByIdAndPenyewa(penyewaan1.getId(), penyewa1);
        assertTrue(found.isPresent());
        assertEquals(penyewaan1.getId(), found.get().getId());

        Optional<Penyewaan> notFound = penyewaanRepository.findByIdAndPenyewa(penyewaan1.getId(), penyewa2);
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByStatus() {
        List<Penyewaan> pendingList = penyewaanRepository.findByStatus(StatusPenyewaan.PENDING);
        assertEquals(1, pendingList.size());
        assertEquals(StatusPenyewaan.PENDING, pendingList.get(0).getStatus());

        List<Penyewaan> approvedList = penyewaanRepository.findByStatus(StatusPenyewaan.APPROVED);
        assertEquals(2, approvedList.size());
        assertTrue(approvedList.stream().allMatch(p -> p.getStatus().equals(StatusPenyewaan.APPROVED)));

        List<Penyewaan> rejectedList = penyewaanRepository.findByStatus(StatusPenyewaan.REJECTED);
        assertEquals(1, rejectedList.size());
        assertEquals(StatusPenyewaan.REJECTED, rejectedList.get(0).getStatus());
    }

    @Test
    void testFindByPenyewaAndStatus() {
        List<Penyewaan> penyewa1Pending = penyewaanRepository.findByPenyewaAndStatus(penyewa1, StatusPenyewaan.PENDING);
        assertEquals(1, penyewa1Pending.size());
        assertEquals(penyewa1, penyewa1Pending.get(0).getPenyewa());
        assertEquals(StatusPenyewaan.PENDING, penyewa1Pending.get(0).getStatus());

        List<Penyewaan> penyewa1Approved = penyewaanRepository.findByPenyewaAndStatus(penyewa1,
                StatusPenyewaan.APPROVED);
        assertEquals(1, penyewa1Approved.size());
        assertEquals(penyewa1, penyewa1Approved.get(0).getPenyewa());
        assertEquals(StatusPenyewaan.APPROVED, penyewa1Approved.get(0).getStatus());

        List<Penyewaan> penyewa2Rejected = penyewaanRepository.findByPenyewaAndStatus(penyewa2,
                StatusPenyewaan.REJECTED);
        assertEquals(1, penyewa2Rejected.size());
        assertEquals(penyewa2, penyewa2Rejected.get(0).getPenyewa());
        assertEquals(StatusPenyewaan.REJECTED, penyewa2Rejected.get(0).getStatus());
    }

    @Test
    void testFindByKosAndStatus() {
        List<Penyewaan> kos1Approved = penyewaanRepository.findByKosAndStatus(kos1, StatusPenyewaan.APPROVED);
        assertEquals(1, kos1Approved.size());
        assertEquals(kos1, kos1Approved.get(0).getKos());
        assertEquals(StatusPenyewaan.APPROVED, kos1Approved.get(0).getStatus());

        List<Penyewaan> kos2Approved = penyewaanRepository.findByKosAndStatus(kos2, StatusPenyewaan.APPROVED);
        assertEquals(1, kos2Approved.size());
        assertEquals(kos2, kos2Approved.get(0).getKos());
        assertEquals(StatusPenyewaan.APPROVED, kos2Approved.get(0).getStatus());
    }

    @Test
    void testCountByKosAndStatus() {
        long kos1ApprovedCount = penyewaanRepository.countByKosAndStatus(kos1, StatusPenyewaan.APPROVED);
        assertEquals(1, kos1ApprovedCount);

        long kos2ApprovedCount = penyewaanRepository.countByKosAndStatus(kos2, StatusPenyewaan.APPROVED);
        assertEquals(1, kos2ApprovedCount);

        long kos1PendingCount = penyewaanRepository.countByKosAndStatus(kos1, StatusPenyewaan.PENDING);
        assertEquals(1, kos1PendingCount);
    }

    @Test
    void testFindByStatusAndTanggalCheckInGreaterThan() {
        LocalDate checkDate = LocalDate.now().plusDays(7);
        List<Penyewaan> upcomingApproved = penyewaanRepository.findByStatusAndTanggalCheckInGreaterThan(
                StatusPenyewaan.APPROVED, checkDate);

        assertEquals(1, upcomingApproved.size());
        assertEquals(StatusPenyewaan.APPROVED, upcomingApproved.get(0).getStatus());
        assertTrue(upcomingApproved.get(0).getTanggalCheckIn().isAfter(checkDate));
        assertEquals(penyewaan2.getId(), upcomingApproved.get(0).getId());
    }

    @Test
    void testSavePenyewaan() {
        Penyewaan newPenyewaan = new Penyewaan();
        newPenyewaan.setPenyewa(penyewa1);
        newPenyewaan.setKos(kos1);
        newPenyewaan.setNamaLengkap("Booking Test");
        newPenyewaan.setNomorTelepon("081234567890");
        newPenyewaan.setStatus(StatusPenyewaan.PENDING);
        newPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(20));
        newPenyewaan.setDurasiSewa(2);
        newPenyewaan.setTotalBiaya(3000000);
        newPenyewaan.setWaktuPengajuan(LocalDateTime.now());

        Penyewaan savedPenyewaan = penyewaanRepository.save(newPenyewaan);

        assertNotNull(savedPenyewaan.getId());

        Optional<Penyewaan> found = penyewaanRepository.findById(savedPenyewaan.getId());
        assertTrue(found.isPresent());
        assertEquals("Booking Test", found.get().getNamaLengkap());
    }

    @Test
    void testUpdatePenyewaan() {
        penyewaan1.setStatus(StatusPenyewaan.APPROVED);
        penyewaanRepository.save(penyewaan1);

        Optional<Penyewaan> updated = penyewaanRepository.findById(penyewaan1.getId());
        assertTrue(updated.isPresent());
        assertEquals(StatusPenyewaan.APPROVED, updated.get().getStatus());
    }

    @Test
    void testDeletePenyewaan() {
        penyewaanRepository.delete(penyewaan4);

        Optional<Penyewaan> deleted = penyewaanRepository.findById(penyewaan4.getId());
        assertFalse(deleted.isPresent());
    }
}