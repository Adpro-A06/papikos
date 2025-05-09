package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KosServiceImplTest {

    @Mock
    private KosRepository kosRepository;

    @InjectMocks
    private KosServiceImpl kosService;

    private Kos kos1;
    private Kos kos2;
    private Kos kos3;
    private User pemilik;

    @BeforeEach
    void setUp() {
        pemilik = new User("pemilik@example.com", "p@ssword123", Role.PEMILIK_KOS);

        kos1 = new Kos();
        kos1.setId("1");
        kos1.setNama("Kos Melati");
        kos1.setAlamat("Jl. Kenanga No. 10");
        kos1.setDeskripsi("Kos nyaman dekat kampus");
        kos1.setHarga(1500000);
        kos1.setJumlah(5);
        kos1.setStatus("AVAILABLE");
        kos1.setPemilik(pemilik);

        kos2 = new Kos();
        kos2.setId("2");
        kos2.setNama("Kos Anggrek");
        kos2.setAlamat("Jl. Mawar No. 5");
        kos2.setDeskripsi("Kos eksklusif dengan AC");
        kos2.setHarga(2500000);
        kos2.setJumlah(3);
        kos2.setStatus("AVAILABLE");
        kos2.setPemilik(pemilik);

        kos3 = new Kos();
        kos3.setId("3");
        kos3.setNama("Kos Cendana");
        kos3.setAlamat("Jl. Dahlia No. 15");
        kos3.setDeskripsi("Kos strategis dekat stasiun");
        kos3.setHarga(1800000);
        kos3.setJumlah(4);
        kos3.setStatus("FULL");
        kos3.setPemilik(pemilik);
    }

    @Test
    void testFindAllAvailable() {
        List<Kos> availableKos = Arrays.asList(kos1, kos2);
        when(kosRepository.findByStatus("AVAILABLE")).thenReturn(availableKos);

        List<Kos> result = kosService.findAllAvailable();
        assertEquals(2, result.size());
        assertEquals("Kos Melati", result.get(0).getNama());
        assertEquals("Kos Anggrek", result.get(1).getNama());
        verify(kosRepository, times(1)).findByStatus("AVAILABLE");
    }

    @Test
    void testFindNoAvailableKos() {
        when(kosRepository.findByStatus("AVAILABLE")).thenReturn(Arrays.asList());

        List<Kos> result = kosService.findAllAvailable();
        assertTrue(result.isEmpty());
        verify(kosRepository, times(1)).findByStatus("AVAILABLE");
    }

    @Test
    void testSearchByKeyword() {
        List<Kos> matchingKos = Arrays.asList(kos1);
        when(kosRepository.searchByKeyword("melati", "AVAILABLE")).thenReturn(matchingKos);

        List<Kos> result = kosService.searchByKeyword("melati");
        assertEquals(1, result.size());
        assertEquals("Kos Melati", result.get(0).getNama());
        verify(kosRepository, times(1)).searchByKeyword("melati", "AVAILABLE");
    }

    @Test
    void testSearchByKeywordNoMatches() {
        when(kosRepository.searchByKeyword("nonexistent", "AVAILABLE")).thenReturn(Arrays.asList());

        List<Kos> result = kosService.searchByKeyword("nonexistent");
        assertTrue(result.isEmpty());
        verify(kosRepository, times(1)).searchByKeyword("nonexistent", "AVAILABLE");
    }

    @Test
    void testSearchByNullKeyword() {
        List<Kos> allAvailableKos = Arrays.asList(kos1, kos2);
        when(kosRepository.searchByKeyword(null, "AVAILABLE")).thenReturn(allAvailableKos);

        List<Kos> result = kosService.searchByKeyword(null);
        assertEquals(2, result.size());
        verify(kosRepository, times(1)).searchByKeyword(null, "AVAILABLE");
    }

    @Test
    void testFindById() {
        when(kosRepository.findById("1")).thenReturn(Optional.of(kos1));

        Optional<Kos> result = kosService.findById("1");
        assertTrue(result.isPresent());
        assertEquals("Kos Melati", result.get().getNama());
        verify(kosRepository, times(1)).findById("1");
    }

    @Test
    void testFindByIdNotFound() {
        when(kosRepository.findById("999")).thenReturn(Optional.empty());

        Optional<Kos> result = kosService.findById("999");
        assertFalse(result.isPresent());
        verify(kosRepository, times(1)).findById("999");
    }

    @Test
    void testFindNullId() {
        when(kosRepository.findById(null)).thenReturn(Optional.empty());

        Optional<Kos> result = kosService.findById(null);
        assertFalse(result.isPresent());
        verify(kosRepository, times(1)).findById(null);
    }
}