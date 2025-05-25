package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.kos.repository.PenyewaanRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PengelolaanServiceTest {

    @Mock
    private PengelolaanRepository pengelolaanRepository;

    @Mock
    private PenyewaanRepository penyewaanRepository;

    @InjectMocks
    private PengelolaanServiceImpl pengelolaanService;

    @Mock
    private User mockUser;

    private String penyewaanId;
    private Penyewaan penyewaan;
    private Kos kos;
    private User pemilik;
    private User penyewa;

    @BeforeEach
    void setUp() {
        penyewaanId = "penyewaan-123";

        pemilik = new User("pemilik@example.com", "password456!", Role.PEMILIK_KOS);
        penyewa = new User("penyewa@example.com", "password123!", Role.PENYEWA);

        kos = new Kos();
        kos.setId(UUID.randomUUID());
        kos.setNama("Tulip");
        kos.setJumlah(5);
        kos.setAlamat("Jl. Mangga");
        kos.setDeskripsi("Full furnish");
        kos.setHarga(1500000);
        kos.setStatus("AVAILABLE");
        kos.setUrlFoto("https://example.com/kos.jpg");
        kos.setPemilik(pemilik);

        penyewaan = new Penyewaan();
        penyewaan.setId(penyewaanId);
        penyewaan.setKos(kos);
        penyewaan.setPenyewa(penyewa);
        penyewaan.setNamaLengkap("Antony");
        penyewaan.setNomorTelepon("08123456789");
        penyewaan.setStatus(StatusPenyewaan.PENDING);
        penyewaan.setTanggalCheckIn(LocalDate.now().plusDays(7));
        penyewaan.setDurasiSewa(3);
        penyewaan.setTotalBiaya(4500000);
        penyewaan.setWaktuPengajuan(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testCreateKos() {
        Kos kos = new Kos();
        kos.setNama("Tulip");
        kos.setJumlah(20);
        kos.setAlamat("Jl. Mangga");
        kos.setDeskripsi("Full furnish");
        kos.setHarga(1000000);
        kos.setStatus("AVAILABLE");
        kos.setUrlFoto("https://example.com/kos.jpg");
        kos.setPemilik(mockUser);

        Kos savedKos = new Kos();
        savedKos.setId(UUID.randomUUID());
        savedKos.setNama("Tulip");
        savedKos.setJumlah(20);
        savedKos.setAlamat("Jl. Mangga");
        savedKos.setDeskripsi("Full furnish");
        savedKos.setHarga(1000000);
        savedKos.setStatus("AVAILABLE");
        savedKos.setUrlFoto("https://example.com/kos.jpg");
        savedKos.setPemilik(mockUser);

        when(pengelolaanRepository.create(any(Kos.class))).thenReturn(savedKos);
        when(pengelolaanRepository.findAllOrThrow()).thenReturn(Arrays.asList(savedKos));

        Kos createdKos = pengelolaanService.create(kos).join();

        assertNotNull(createdKos.getId());
        assertEquals("Tulip", createdKos.getNama());
        assertEquals(20, createdKos.getJumlah());
        assertEquals("Jl. Mangga", createdKos.getAlamat());
        assertEquals("Full furnish", createdKos.getDeskripsi());
        assertEquals(1000000, createdKos.getHarga());
        assertEquals("AVAILABLE", createdKos.getStatus());
        assertEquals("https://example.com/kos.jpg", createdKos.getUrlFoto());
        assertEquals(mockUser, createdKos.getPemilik());

        List<Kos> retrievedKosList = pengelolaanService.findAll().join();
        assertEquals(1, retrievedKosList.size());
        Kos retrievedKos = retrievedKosList.get(0);
        assertEquals(createdKos.getId(), retrievedKos.getId());

        verify(pengelolaanRepository, times(1)).create(any(Kos.class));
    }

    @Test
    void testUpdateKos() {
        UUID kosId = UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6");

        Kos existingKos = new Kos();
        existingKos.setId(kosId);
        existingKos.setNama("Tulip");
        existingKos.setJumlah(20);
        existingKos.setAlamat("Jl. Mangga");
        existingKos.setDeskripsi("Full furnish");
        existingKos.setHarga(1000000);
        existingKos.setStatus("AVAILABLE");
        existingKos.setUrlFoto("https://i.pinimg.com/736x/6e/df/3c/6edf3c96bcbd31aa41038c70c515daf2.jpg");
        existingKos.setPemilik(mockUser);

        Kos updatedKos = new Kos();
        updatedKos.setId(kosId);
        updatedKos.setNama("Melati");
        updatedKos.setJumlah(30);
        updatedKos.setAlamat("Jl. Mawar");
        updatedKos.setDeskripsi("Minimalis");
        updatedKos.setHarga(1500000);
        updatedKos.setStatus("FULL");
        updatedKos.setUrlFoto("https://i.pinimg.com/736x/f3/f2/a2/f3f2a2d6b0c26389bc95260364251cef.jpg");
        updatedKos.setPemilik(mockUser);

        when(pengelolaanRepository.update(any(Kos.class))).thenReturn(updatedKos);
        when(pengelolaanRepository.findByIdOrThrow(kosId)).thenReturn(updatedKos);

        Kos result = pengelolaanService.update(updatedKos).join();

        assertEquals("Melati", result.getNama());
        assertEquals(30, result.getJumlah());
        assertEquals("Jl. Mawar", result.getAlamat());
        assertEquals("Minimalis", result.getDeskripsi());
        assertEquals(1500000, result.getHarga());
        assertEquals("FULL", result.getStatus());
        assertEquals("https://i.pinimg.com/736x/f3/f2/a2/f3f2a2d6b0c26389bc95260364251cef.jpg", result.getUrlFoto());
        assertEquals(mockUser, result.getPemilik());

        Kos retrievedKos = pengelolaanService.findById(kosId).join();
        assertEquals("Melati", retrievedKos.getNama());
        assertEquals(30, retrievedKos.getJumlah());

        verify(pengelolaanRepository, times(1)).update(updatedKos);
        verify(pengelolaanRepository, times(1)).findByIdOrThrow(kosId);
    }

    @Test
    void testDeleteKos() {
        UUID kosId = UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6");

        Kos kos = new Kos();
        kos.setId(kosId);
        kos.setNama("Tulip");
        kos.setPemilik(mockUser);

        doNothing().when(pengelolaanRepository).delete(kos);
        when(pengelolaanRepository.findAllOrThrow()).thenReturn(Collections.emptyList());

        pengelolaanService.delete(kos).join();
        List<Kos> retrievedKosList = pengelolaanService.findAll().join();
        assertTrue(retrievedKosList.isEmpty());

        verify(pengelolaanRepository, times(1)).delete(kos);
    }

    @Test
    void testFindAllEmpty() {
        when(pengelolaanRepository.findAllOrThrow()).thenReturn(Collections.emptyList());
        List<Kos> kosList = pengelolaanService.findAll().join();
        assertTrue(kosList.isEmpty());
    }

    @Test
    void testFindById() {
        UUID kosId = UUID.fromString("eb558e9f-1c39-460e-8860-71af6af63bd6");

        Kos kos = new Kos();
        kos.setId(kosId);
        kos.setNama("Tulip");
        kos.setPemilik(mockUser);

        when(pengelolaanRepository.findByIdOrThrow(kosId)).thenReturn(kos);

        Kos retrievedKos = pengelolaanService.findById(kosId).join();
        assertEquals("Tulip", retrievedKos.getNama());
        assertEquals(kosId, retrievedKos.getId());
        assertEquals(mockUser, retrievedKos.getPemilik());

        verify(pengelolaanRepository, times(1)).findByIdOrThrow(kosId);
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(pengelolaanRepository.findByIdOrThrow(nonExistentId))
                .thenThrow(new PengelolaanRepository.KosNotFoundException(
                        "Kos dengan ID " + nonExistentId + " tidak ditemukan."));

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanService.findById(nonExistentId).join());

        assertEquals("Kos dengan ID " + nonExistentId + " tidak ditemukan.", thrown.getMessage());
        verify(pengelolaanRepository, times(1)).findByIdOrThrow(nonExistentId);
    }

    @Test
    void testUpdateKosNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Kos kos = new Kos();
        kos.setId(nonExistentId);
        kos.setNama("Melati");

        when(pengelolaanRepository.update(any(Kos.class)))
                .thenThrow(new PengelolaanRepository.KosNotFoundException(
                        "Kos dengan ID " + nonExistentId + " tidak ditemukan."));

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanService.update(kos).join());

        assertEquals("Kos dengan ID " + nonExistentId + " tidak ditemukan.", thrown.getMessage());
        verify(pengelolaanRepository, times(1)).update(kos);
    }

    @Test
    void testDeleteKosNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Kos kos = new Kos();
        kos.setId(nonExistentId);
        kos.setNama("Tulip");

        doThrow(new PengelolaanRepository.KosNotFoundException("Kos dengan ID " + nonExistentId + " tidak ditemukan."))
                .when(pengelolaanRepository).delete(kos);

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanService.delete(kos).join());

        assertEquals("Kos dengan ID " + nonExistentId + " tidak ditemukan.", thrown.getMessage());
        verify(pengelolaanRepository, times(1)).delete(kos);
    }

    @Test
    void testFindAllSewa() throws ExecutionException, InterruptedException {
        List<Penyewaan> penyewaanList = Arrays.asList(penyewaan);
        when(penyewaanRepository.findAllByKosPemilikId(pemilik.getId())).thenReturn(penyewaanList);
        CompletableFuture<List<Penyewaan>> result = pengelolaanService.findAllSewa(pemilik.getId());
        assertEquals(penyewaanList, result.get());
        verify(penyewaanRepository).findAllByKosPemilikId(pemilik.getId());
    }

    @Test
    void testTerimaSewaSuccess() throws ExecutionException, InterruptedException {
        when(penyewaanRepository.findByIdWithKos(penyewaanId)).thenReturn(Optional.of(penyewaan));
        CompletableFuture<Void> result = pengelolaanService.terimaSewa(penyewaanId, pemilik.getId());
        result.get();
        verify(penyewaanRepository).findByIdWithKos(penyewaanId);
        verify(penyewaanRepository).save(any(Penyewaan.class));
        verify(pengelolaanRepository).save(kos);
    }

    @Test
    void testTolakSewaSuccess() throws ExecutionException, InterruptedException {
        when(penyewaanRepository.findByIdWithKos(penyewaanId)).thenReturn(Optional.of(penyewaan));
        CompletableFuture<Void> result = pengelolaanService.tolakSewa(penyewaanId, pemilik.getId());
        result.get();
        verify(penyewaanRepository).findByIdWithKos(penyewaanId);
        verify(penyewaanRepository).save(any(Penyewaan.class));
    }

    @Test
    void testTerimaSewaSyncSuccess() {
        when(penyewaanRepository.findByIdWithKos(penyewaanId)).thenReturn(Optional.of(penyewaan));
        pengelolaanService.terimaSewaSync(penyewaanId, pemilik.getId());
        assertEquals(StatusPenyewaan.APPROVED, penyewaan.getStatus());
        assertEquals(4, kos.getJumlah());
        assertNotNull(penyewaan.getWaktuPerubahan());
        verify(pengelolaanRepository).save(kos);
        verify(penyewaanRepository).save(penyewaan);
    }

    @Test
    void testTerimaSewaSyncWhenPenyewaanNotFound() {
        when(penyewaanRepository.findByIdWithKos(penyewaanId))
                .thenReturn(Optional.empty());
        assertThrows(PengelolaanRepository.PenyewaanNotFoundException.class,
                () -> pengelolaanService.terimaSewaSync(penyewaanId, pemilik.getId()));
        verify(penyewaanRepository).findByIdWithKos(penyewaanId);
        verifyNoMoreInteractions(pengelolaanRepository);
    }

    @Test
    void testTerimaSewaSyncWhenNotAuthorized() {
        User differentPemilik = new User("other@example.com", "password789!", Role.PEMILIK_KOS);
        when(penyewaanRepository.findByIdWithKos(penyewaanId)).thenReturn(Optional.of(penyewaan));
        assertThrows(IllegalArgumentException.class,
                () -> pengelolaanService.terimaSewaSync(penyewaanId, differentPemilik.getId()));
        verify(penyewaanRepository).findByIdWithKos(penyewaanId);
        verifyNoMoreInteractions(pengelolaanRepository);
    }

    @Test
    void testTerimaSewaSyncWhenNoRoomsAvailable() {
        kos.setJumlah(0);
        when(penyewaanRepository.findByIdWithKos(penyewaanId)).thenReturn(Optional.of(penyewaan));
        assertThrows(IllegalStateException.class,
                () -> pengelolaanService.terimaSewaSync(penyewaanId, pemilik.getId()));
        verify(penyewaanRepository).findByIdWithKos(penyewaanId);
        verifyNoMoreInteractions(pengelolaanRepository);
    }

    @Test
    void testTolakSewaSyncSuccess() {
        when(penyewaanRepository.findByIdWithKos(penyewaanId)).thenReturn(Optional.of(penyewaan));
        pengelolaanService.tolakSewaSync(penyewaanId, pemilik.getId());
        assertEquals(StatusPenyewaan.REJECTED, penyewaan.getStatus());
        assertNotNull(penyewaan.getWaktuPerubahan());
        verify(penyewaanRepository).save(penyewaan);
    }

    @Test
    void testTolakSewaSyncWhenPenyewaanNotFound() {
        when(penyewaanRepository.findByIdWithKos(penyewaanId))
                .thenReturn(Optional.empty());
        assertThrows(PengelolaanRepository.PenyewaanNotFoundException.class,
                () -> pengelolaanService.tolakSewaSync(penyewaanId, pemilik.getId()));
        verify(penyewaanRepository).findByIdWithKos(penyewaanId);
        verifyNoMoreInteractions(penyewaanRepository);
    }

    @Test
    void testTolakSewaSyncWhenNotAuthorized() {
        User differentPemilik = new User("other@example.com", "password789!", Role.PEMILIK_KOS);
        when(penyewaanRepository.findByIdWithKos(penyewaanId)).thenReturn(Optional.of(penyewaan));
        assertThrows(IllegalArgumentException.class,
                () -> pengelolaanService.tolakSewaSync(penyewaanId, differentPemilik.getId()));
        verify(penyewaanRepository).findByIdWithKos(penyewaanId);
        verifyNoMoreInteractions(penyewaanRepository);
    }

    @Test
    void testTolakSewaSyncWhenSaveThrowsException() {
        when(penyewaanRepository.findByIdWithKos(penyewaanId)).thenReturn(Optional.of(penyewaan));
        doThrow(new RuntimeException("Database error")).when(penyewaanRepository).save(any(Penyewaan.class));
        assertThrows(RuntimeException.class,
                () -> pengelolaanService.tolakSewaSync(penyewaanId, pemilik.getId()));
        assertEquals(StatusPenyewaan.REJECTED, penyewaan.getStatus());
        verify(penyewaanRepository).findByIdWithKos(penyewaanId);
        verify(penyewaanRepository).save(penyewaan);
    }
}