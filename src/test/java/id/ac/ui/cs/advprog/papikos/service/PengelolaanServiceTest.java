package id.ac.ui.cs.advprog.papikos.service;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.repository.PengelolaanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PengelolaanServiceTest {

    @Mock
    private PengelolaanRepository pengelolaanRepository;

    @InjectMocks
    private PengelolaanServiceImpl pengelolaanService;

    @Test
    void testCreateKos() {
        Kos kos = new Kos();
        kos.setNama("Tulip");
        kos.setJumlah(20);
        kos.setAlamat("Jl. Mangga");
        kos.setDeskripsi("Full furnish");
        kos.setHarga(1000000);
        kos.setStatus("Tersedia");

        Kos savedKos = new Kos();
        savedKos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6"); // Mocked ID
        savedKos.setNama("Tulip");
        savedKos.setJumlah(20);
        savedKos.setAlamat("Jl. Mangga");
        savedKos.setDeskripsi("Full furnish");
        savedKos.setHarga(1000000);
        savedKos.setStatus("Tersedia");

        when(pengelolaanRepository.create(any(Kos.class))).thenReturn(savedKos);
        when(pengelolaanRepository.findAll()).thenReturn(Arrays.asList(savedKos).iterator());

        Kos createdKos = pengelolaanService.create(kos);

        assertEquals("eb558e9f-1c39-460e-8860-71af6af63bd6", createdKos.getId());
        assertEquals("Tulip", createdKos.getNama());
        assertEquals(20, createdKos.getJumlah());
        assertEquals("Jl. Mangga", createdKos.getAlamat());
        assertEquals("Full furnish", createdKos.getDeskripsi());
        assertEquals(1000000, createdKos.getHarga());
        assertEquals("Tersedia", createdKos.getStatus());

        List<Kos> retrievedKosList = pengelolaanService.findAll();
        assertFalse(retrievedKosList.isEmpty());
        Kos retrievedKos = retrievedKosList.get(0);
        assertEquals("eb558e9f-1c39-460e-8860-71af6af63bd6", retrievedKos.getId());

        verify(pengelolaanRepository, times(1)).create(any(Kos.class));
    }

    @Test
    void testUpdateKos() {
        Kos existingKos = new Kos();
        existingKos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        existingKos.setNama("Tulip");
        existingKos.setJumlah(20);
        existingKos.setAlamat("Jl. Mangga");
        existingKos.setDeskripsi("Full furnish");
        existingKos.setHarga(1000000);
        existingKos.setStatus("Tersedia");

        Kos updatedKos = new Kos();
        updatedKos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        updatedKos.setNama("Melati");
        updatedKos.setJumlah(30);
        updatedKos.setAlamat("Jl. Mawar");
        updatedKos.setDeskripsi("Minimalis");
        updatedKos.setHarga(1500000);
        updatedKos.setStatus("Penuh");

        when(pengelolaanRepository.update(any(Kos.class))).thenReturn(updatedKos);
        when(pengelolaanRepository.findById("eb558e9f-1c39-460e-8860-71af6af63bd6")).thenReturn(updatedKos);

        Kos result = pengelolaanService.update(updatedKos);

        assertEquals("Melati", result.getNama());
        assertEquals(30, result.getJumlah());
        assertEquals("Jl. Mawar", result.getAlamat());
        assertEquals("Minimalis", result.getDeskripsi());
        assertEquals(1500000, result.getHarga());
        assertEquals("Penuh", result.getStatus());

        Kos retrievedKos = pengelolaanService.findById("eb558e9f-1c39-460e-8860-71af6af63bd6");
        assertEquals("Melati", retrievedKos.getNama());
        assertEquals(30, retrievedKos.getJumlah());

        verify(pengelolaanRepository, times(1)).update(updatedKos);
    }

    @Test
    void testDeleteKos() {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        kos.setNama("Tulip");

        doNothing().when(pengelolaanRepository).delete(kos);
        when(pengelolaanRepository.findAll()).thenReturn(Collections.emptyIterator());

        pengelolaanService.delete(kos);
        List<Kos> retrievedKosList = pengelolaanService.findAll();
        assertTrue(retrievedKosList.isEmpty());

        verify(pengelolaanRepository, times(1)).delete(kos);
    }

    @Test
    void testFindAllEmpty() {
        when(pengelolaanRepository.findAll()).thenReturn(Collections.emptyIterator());

        List<Kos> kosList = pengelolaanService.findAll();
        assertTrue(kosList.isEmpty());
    }

    @Test
    void testFindById() {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        kos.setNama("Tulip");

        when(pengelolaanRepository.findById("eb558e9f-1c39-460e-8860-71af6af63bd6")).thenReturn(kos);

        Kos retrievedKos = pengelolaanService.findById("eb558e9f-1c39-460e-8860-71af6af63bd6");
        assertEquals("Tulip", retrievedKos.getNama());
        assertEquals("eb558e9f-1c39-460e-8860-71af6af63bd6", retrievedKos.getId());

        verify(pengelolaanRepository, times(1)).findById("eb558e9f-1c39-460e-8860-71af6af63bd6");
    }
}