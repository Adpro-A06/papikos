package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PengelolaanServiceTest {

    @Mock
    private PengelolaanRepository pengelolaanRepository;

    @InjectMocks
    private PengelolaanServiceImpl pengelolaanService;

    @Mock
    private User mockUser;

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
        savedKos.setId(UUID.randomUUID()); // Simulate JPA-generated ID
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

        Kos createdKos = pengelolaanService.create(kos);

        assertNotNull(createdKos.getId());
        assertEquals("Tulip", createdKos.getNama());
        assertEquals(20, createdKos.getJumlah());
        assertEquals("Jl. Mangga", createdKos.getAlamat());
        assertEquals("Full furnish", createdKos.getDeskripsi());
        assertEquals(1000000, createdKos.getHarga());
        assertEquals("AVAILABLE", createdKos.getStatus());
        assertEquals("https://example.com/kos.jpg", createdKos.getUrlFoto());
        assertEquals(mockUser, createdKos.getPemilik());

        List<Kos> retrievedKosList = pengelolaanService.findAll();
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

        Kos result = pengelolaanService.update(updatedKos);

        assertEquals("Melati", result.getNama());
        assertEquals(30, result.getJumlah());
        assertEquals("Jl. Mawar", result.getAlamat());
        assertEquals("Minimalis", result.getDeskripsi());
        assertEquals(1500000, result.getHarga());
        assertEquals("FULL", result.getStatus());
        assertEquals("https://i.pinimg.com/736x/f3/f2/a2/f3f2a2d6b0c26389bc95260364251cef.jpg", result.getUrlFoto());
        assertEquals(mockUser, result.getPemilik());

        Kos retrievedKos = pengelolaanService.findById(kosId);
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

        pengelolaanService.delete(kos);
        List<Kos> retrievedKosList = pengelolaanService.findAll();
        assertTrue(retrievedKosList.isEmpty());

        verify(pengelolaanRepository, times(1)).delete(kos);
    }

    @Test
    void testFindAllEmpty() {
        when(pengelolaanRepository.findAllOrThrow()).thenReturn(Collections.emptyList());
        List<Kos> kosList = pengelolaanService.findAll();
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

        Kos retrievedKos = pengelolaanService.findById(kosId);
        assertEquals("Tulip", retrievedKos.getNama());
        assertEquals(kosId, retrievedKos.getId());
        assertEquals(mockUser, retrievedKos.getPemilik());

        verify(pengelolaanRepository, times(1)).findByIdOrThrow(kosId);
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(pengelolaanRepository.findByIdOrThrow(nonExistentId))
                .thenThrow(new PengelolaanRepository.KosNotFoundException("Kos dengan ID " + nonExistentId + " tidak ditemukan."));

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanService.findById(nonExistentId)
        );

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
                .thenThrow(new PengelolaanRepository.KosNotFoundException("Kos dengan ID " + nonExistentId + " tidak ditemukan."));

        PengelolaanRepository.KosNotFoundException thrown = assertThrows(
                PengelolaanRepository.KosNotFoundException.class,
                () -> pengelolaanService.update(kos)
        );

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
                () -> pengelolaanService.delete(kos)
        );

        assertEquals("Kos dengan ID " + nonExistentId + " tidak ditemukan.", thrown.getMessage());
        verify(pengelolaanRepository, times(1)).delete(kos);
    }
}