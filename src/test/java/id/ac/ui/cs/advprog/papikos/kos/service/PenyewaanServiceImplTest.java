package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import id.ac.ui.cs.advprog.papikos.kos.repository.PenyewaanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenyewaanServiceImplTest {

    @Mock
    private PenyewaanRepository penyewaanRepository;

    @Mock
    private KosRepository kosRepository;

    @InjectMocks
    private PenyewaanServiceImpl penyewaanService;

    private Kos kos;
    private User penyewa;
    private User pemilik;
    private Penyewaan penyewaanPending;
    private Penyewaan penyewaanApproved;
    private Penyewaan penyewaanRejected;

    private String kosId;

    @BeforeEach
    void setUp() {
        kosId = UUID.randomUUID().toString();

        penyewa = new User("penyewa@example.com", "p@ssword123", Role.PENYEWA);
        pemilik = new User("pemilik@example.com", "password456!", Role.PEMILIK_KOS);

        kos = new Kos();
        kos.setId(UUID.fromString(kosId));
        kos.setNama("Kos Melati");
        kos.setAlamat("Jl. Kenanga No. 10");
        kos.setDeskripsi("Kos nyaman dekat kampus");
        kos.setHarga(1500000);
        kos.setJumlah(5);
        kos.setStatus("AVAILABLE");
        kos.setPemilik(pemilik);

        penyewaanPending = new Penyewaan();
        penyewaanPending.setId("pending-123");
        penyewaanPending.setKos(kos);
        penyewaanPending.setPenyewa(penyewa);
        penyewaanPending.setNamaLengkap("Antony");
        penyewaanPending.setNomorTelepon("08123456789");
        penyewaanPending.setStatus(StatusPenyewaan.PENDING);
        penyewaanPending.setTanggalCheckIn(LocalDate.now().plusDays(7));
        penyewaanPending.setDurasiSewa(3);
        penyewaanPending.setTotalBiaya(4500000);
        penyewaanPending.setWaktuPengajuan(LocalDateTime.now().minusDays(1));

        penyewaanApproved = new Penyewaan();
        penyewaanApproved.setId("approved-123");
        penyewaanApproved.setKos(kos);
        penyewaanApproved.setPenyewa(penyewa);
        penyewaanApproved.setNamaLengkap("Antony");
        penyewaanApproved.setNomorTelepon("08123456789");
        penyewaanApproved.setStatus(StatusPenyewaan.APPROVED);
        penyewaanApproved.setTanggalCheckIn(LocalDate.now().plusDays(14));
        penyewaanApproved.setDurasiSewa(6);
        penyewaanApproved.setTotalBiaya(9000000);
        penyewaanApproved.setWaktuPengajuan(LocalDateTime.now().minusDays(2));

        penyewaanRejected = new Penyewaan();
        penyewaanRejected.setId("rejected-123");
        penyewaanRejected.setKos(kos);
        penyewaanRejected.setPenyewa(penyewa);
        penyewaanRejected.setNamaLengkap("Antony");
        penyewaanRejected.setNomorTelepon("08123456789");
        penyewaanRejected.setStatus(StatusPenyewaan.REJECTED);
        penyewaanRejected.setTanggalCheckIn(LocalDate.now().plusDays(10));
        penyewaanRejected.setDurasiSewa(1);
        penyewaanRejected.setTotalBiaya(1500000);
        penyewaanRejected.setWaktuPengajuan(LocalDateTime.now().minusDays(3));
    }

    @Test
    void testCreatePenyewaanSuccess() throws ExecutionException, InterruptedException {
        when(kosRepository.findById(UUID.fromString(kosId))).thenReturn(Optional.of(kos));
        when(penyewaanRepository.save(any(Penyewaan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Penyewaan newPenyewaan = new Penyewaan();
        newPenyewaan.setNamaLengkap("Jane Smith");
        newPenyewaan.setNomorTelepon("08987654321");
        newPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(5));
        newPenyewaan.setDurasiSewa(2);

        Penyewaan result = penyewaanService.createPenyewaan(newPenyewaan, kosId, penyewa).get();
        assertNotNull(result);
        assertEquals(penyewa, result.getPenyewa());
        assertEquals(kos, result.getKos());
        assertEquals(StatusPenyewaan.PENDING, result.getStatus());
        assertEquals(3000000, result.getTotalBiaya());
        verify(kosRepository).findById(UUID.fromString(kosId));
        verify(penyewaanRepository).save(any(Penyewaan.class));
    }

    @Test
    void testCreatePenyewaanKosNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        Penyewaan newPenyewaan = new Penyewaan();
        when(kosRepository.findById(UUID.fromString(nonExistentId))).thenReturn(Optional.empty());

        CompletableFuture<Penyewaan> future = penyewaanService.createPenyewaan(newPenyewaan, nonExistentId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof EntityNotFoundException);

        verify(kosRepository).findById(UUID.fromString(nonExistentId));
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testCreatePenyewaanKosNotAvailable() {
        Penyewaan newPenyewaan = new Penyewaan();
        kos.setStatus("FULL");
        when(kosRepository.findById(UUID.fromString(kosId))).thenReturn(Optional.of(kos));

        CompletableFuture<Penyewaan> future = penyewaanService.createPenyewaan(newPenyewaan, kosId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);

        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("Kos tidak tersedia untuk disewa", exception.getCause().getMessage());
        verify(kosRepository).findById(UUID.fromString(kosId));
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
        kos.setStatus("AVAILABLE");
    }

    @Test
    void testCreatePenyewaanNoRoomsAvailable() {
        Penyewaan newPenyewaan = new Penyewaan();
        kos.setJumlah(0);
        when(kosRepository.findById(UUID.fromString(kosId))).thenReturn(Optional.of(kos));

        CompletableFuture<Penyewaan> future = penyewaanService.createPenyewaan(newPenyewaan, kosId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);

        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("Tidak ada kamar tersedia untuk disewa", exception.getCause().getMessage());
        verify(kosRepository).findById(UUID.fromString(kosId));
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
        kos.setJumlah(5);
    }

    @Test
    void testCreatePenyewaanInvalidCheckInDate() {
        Penyewaan newPenyewaan = new Penyewaan();
        newPenyewaan.setTanggalCheckIn(LocalDate.now().minusDays(1)); // Past date
        newPenyewaan.setDurasiSewa(2);
        when(kosRepository.findById(UUID.fromString(kosId))).thenReturn(Optional.of(kos));

        CompletableFuture<Penyewaan> future = penyewaanService.createPenyewaan(newPenyewaan, kosId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Tanggal check-in tidak boleh di masa lalu", exception.getCause().getMessage());
        verify(kosRepository).findById(UUID.fromString(kosId));
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testCreatePenyewaanInvalidDurasi() {
        Penyewaan newPenyewaan = new Penyewaan();
        newPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(5));
        newPenyewaan.setDurasiSewa(0);
        when(kosRepository.findById(UUID.fromString(kosId))).thenReturn(Optional.of(kos));

        CompletableFuture<Penyewaan> future = penyewaanService.createPenyewaan(newPenyewaan, kosId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Durasi sewa minimal 1 bulan", exception.getCause().getMessage());
        verify(kosRepository).findById(UUID.fromString(kosId));
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testFindByPenyewaSuccess() throws ExecutionException, InterruptedException {
        List<Penyewaan> penyewaanList = Arrays.asList(penyewaanPending, penyewaanApproved, penyewaanRejected);
        when(penyewaanRepository.findByPenyewa(penyewa)).thenReturn(penyewaanList);

        List<Penyewaan> result = penyewaanService.findByPenyewa(penyewa).get();
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(penyewaanRepository).findByPenyewa(penyewa);
    }

    @Test
    void testFindByPenyewaAndStatusSuccess() throws ExecutionException, InterruptedException {
        List<Penyewaan> pendingList = Arrays.asList(penyewaanPending);
        when(penyewaanRepository.findByPenyewaAndStatus(penyewa, StatusPenyewaan.PENDING)).thenReturn(pendingList);

        List<Penyewaan> result = penyewaanService.findByPenyewaAndStatus(penyewa, StatusPenyewaan.PENDING).get();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(StatusPenyewaan.PENDING, result.get(0).getStatus());
        verify(penyewaanRepository).findByPenyewaAndStatus(penyewa, StatusPenyewaan.PENDING);
    }

    @Test
    void testFindByIdSuccess() throws ExecutionException, InterruptedException {
        String penyewaanId = "pending-123";
        when(penyewaanRepository.findById(penyewaanId)).thenReturn(Optional.of(penyewaanPending));

        Optional<Penyewaan> result = penyewaanService.findById(penyewaanId).get();
        assertTrue(result.isPresent());
        assertEquals(penyewaanPending, result.get());
        verify(penyewaanRepository).findById(penyewaanId);
    }

    @Test
    void testFindByIdAndPenyewaSuccess() throws ExecutionException, InterruptedException {
        String penyewaanId = "pending-123";
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.of(penyewaanPending));

        Optional<Penyewaan> result = penyewaanService.findByIdAndPenyewa(penyewaanId, penyewa).get();
        assertTrue(result.isPresent());
        assertEquals(penyewaanPending, result.get());
        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
    }

    @Test
    void testUpdatePenyewaanSuccess() throws ExecutionException, InterruptedException {
        String penyewaanId = "pending-123";
        Penyewaan updatedPenyewaan = new Penyewaan();
        updatedPenyewaan.setNamaLengkap("Updated Name");
        updatedPenyewaan.setNomorTelepon("0812345678");
        updatedPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(10));
        updatedPenyewaan.setDurasiSewa(4);

        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.of(penyewaanPending));
        when(penyewaanRepository.save(any(Penyewaan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Penyewaan result = penyewaanService.updatePenyewaan(updatedPenyewaan, penyewaanId, penyewa).get();
        assertNotNull(result);
        assertEquals("Updated Name", result.getNamaLengkap());
        assertEquals("0812345678", result.getNomorTelepon());
        assertEquals(LocalDate.now().plusDays(10), result.getTanggalCheckIn());
        assertEquals(4, result.getDurasiSewa());
        assertEquals(6000000, result.getTotalBiaya());
        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository).save(any(Penyewaan.class));
    }

    @Test
    void testUpdatePenyewaanNotFound() {
        String penyewaanId = "nonexistent";
        Penyewaan updatedPenyewaan = new Penyewaan();
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.empty());

        CompletableFuture<Penyewaan> future = penyewaanService.updatePenyewaan(updatedPenyewaan, penyewaanId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof EntityNotFoundException);

        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testUpdatePenyewaanNotEditable() {
        String penyewaanId = "approved-123";
        Penyewaan updatedPenyewaan = new Penyewaan();
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.of(penyewaanApproved));

        CompletableFuture<Penyewaan> future = penyewaanService.updatePenyewaan(updatedPenyewaan, penyewaanId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof IllegalStateException);

        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testUpdatePenyewaanInvalidCheckInDate() {
        String penyewaanId = "pending-123";
        Penyewaan updatedPenyewaan = new Penyewaan();
        updatedPenyewaan.setTanggalCheckIn(LocalDate.now().minusDays(1));
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.of(penyewaanPending));

        CompletableFuture<Penyewaan> future = penyewaanService.updatePenyewaan(updatedPenyewaan, penyewaanId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);

        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testUpdatePenyewaanInvalidDurasi() {
        String penyewaanId = "pending-123";
        Penyewaan updatedPenyewaan = new Penyewaan();
        updatedPenyewaan.setTanggalCheckIn(LocalDate.now().plusDays(5));
        updatedPenyewaan.setDurasiSewa(13);
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.of(penyewaanPending));

        CompletableFuture<Penyewaan> future = penyewaanService.updatePenyewaan(updatedPenyewaan, penyewaanId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);

        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testCancelPenyewaanSuccess() throws ExecutionException, InterruptedException {
        String penyewaanId = "pending-123";
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.of(penyewaanPending));
        when(penyewaanRepository.save(any(Penyewaan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        penyewaanService.cancelPenyewaan(penyewaanId, penyewa).get();
        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository).save(any(Penyewaan.class));
        assertEquals(StatusPenyewaan.REJECTED, penyewaanPending.getStatus());
    }

    @Test
    void testCancelPenyewaanFutureCheckInSuccess() throws ExecutionException, InterruptedException {
        String penyewaanId = "approved-123";
        penyewaanApproved.setTanggalCheckIn(LocalDate.now().plusDays(5));
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.of(penyewaanApproved));
        when(penyewaanRepository.save(any(Penyewaan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        penyewaanService.cancelPenyewaan(penyewaanId, penyewa).get();
        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository).save(any(Penyewaan.class));
        assertEquals(StatusPenyewaan.REJECTED, penyewaanApproved.getStatus());
    }

    @Test
    void testCancelPenyewaanNotFound() {
        String penyewaanId = "nonexistent";
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.empty());

        CompletableFuture<Void> future = penyewaanService.cancelPenyewaan(penyewaanId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof EntityNotFoundException);

        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testCancelPenyewaanNotCancellable() {
        String penyewaanId = "rejected-123";
        when(penyewaanRepository.findByIdAndPenyewa(penyewaanId, penyewa)).thenReturn(Optional.of(penyewaanRejected));

        CompletableFuture<Void> future = penyewaanService.cancelPenyewaan(penyewaanId, penyewa);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof IllegalStateException);

        verify(penyewaanRepository).findByIdAndPenyewa(penyewaanId, penyewa);
        verify(penyewaanRepository, never()).save(any(Penyewaan.class));
    }

    @Test
    void testIsPenyewaanEditable() {
        assertTrue(penyewaanService.isPenyewaanEditable(penyewaanPending));
    }

    @Test
    void testApprovedIsNotEditable() {
        assertFalse(penyewaanService.isPenyewaanEditable(penyewaanApproved));
    }

    @Test
    void testRejectedIsNotEditable() {
        assertFalse(penyewaanService.isPenyewaanEditable(penyewaanRejected));
    }

    @Test
    void testisPenyewaanCancellable() {
        assertTrue(penyewaanService.isPenyewaanCancellable(penyewaanPending));
    }

    @Test
    void testApprovedWithFutureCheckInIsCancellable() {
        penyewaanApproved.setTanggalCheckIn(LocalDate.now().plusDays(5));
        assertTrue(penyewaanService.isPenyewaanCancellable(penyewaanApproved));
    }

    @Test
    void testApprovedWithPastCheckInIsNotCancellable() {
        penyewaanApproved.setTanggalCheckIn(LocalDate.now().minusDays(1));
        assertFalse(penyewaanService.isPenyewaanCancellable(penyewaanApproved));
    }

    @Test
    void testRejectedIsNotCancellable() {
        assertFalse(penyewaanService.isPenyewaanCancellable(penyewaanRejected));
    }
}