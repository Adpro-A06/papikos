package id.ac.ui.cs.advprog.papikos.kos.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.StatusPenyewaan;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KosTest {
    private Kos kos;
    
    @Mock
    private User mockPemilik;
    
    @Mock
    private Penyewaan mockPenyewaan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        this.kos = new Kos();
        this.kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        this.kos.setNama("Tulip");
        this.kos.setJumlah(20);
        this.kos.setAlamat("Jl.Mangga");
        this.kos.setDeskripsi("Full furnish. Dilengkapi dengan AC");
        this.kos.setHarga(1500000);
        this.kos.setStatus("AVAILABLE");
        this.kos.setPemilik(mockPemilik);
        this.kos.setPenyewaan(new ArrayList<>());
    }

    @Test
    void testGetIdKos() {
        assertEquals("eb558e9f-1c39-460e-8860-71af6af63bd6", this.kos.getId());
    }

    @Test
    void testGetNamaKos() {
        Assertions.assertEquals("Tulip", this.kos.getNama());
    }

    @Test
    void testGetJumlahKos() {
        Assertions.assertEquals(20, this.kos.getJumlah());
    }

    @Test
    void testGetAlamatKos() {
        Assertions.assertEquals("Jl.Mangga", this.kos.getAlamat());
    }

    @Test
    void testGetDeskripsiKos() {
        Assertions.assertEquals("Full furnish. Dilengkapi dengan AC", this.kos.getDeskripsi());
    }

    @Test
    void testGetUrlFoto() {
        String testUrl = "https://example.com/images/kos1.jpg";
        kos.setUrlFoto(testUrl);
        assertEquals(testUrl, kos.getUrlFoto());
    }

    @Test
    void testValidUrlFoto() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        String[] validUrls = {
            "https://example.com/images/kos.jpg",
            "http://subdomain.example.co.id/path/to/image.png",
            "https://cdn.website.com/images/folder/image-1.jpg",
            "http://wikipedia.com/image.jpg",
        };
        
        for (String url : validUrls) {
            kos.setUrlFoto(url);
            Set<ConstraintViolation<Kos>> violations = validator.validate(kos);

            assertTrue(violations.isEmpty(), "URL valid: " + url);
        }
    }

    @Test
    void testInvalidUrlFoto() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        String[] invalidUrls = {
            "<script>alert('XSS')</script>",
            "javascript:alert('XSS')",
            "file:///etc/passwd",
            "not a url",
            "http://.com",
            "https://"
        };
        
        for (String url : invalidUrls) {
            kos.setUrlFoto(url);
            Set<ConstraintViolation<Kos>> violations = validator.validate(kos);

            assertFalse(violations.isEmpty(), "URL invalid seharusnya ditolak: " + url);
            boolean hasUrlFotoViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("urlFoto"));
            assertTrue(hasUrlFotoViolation, "Violation seharusnya untuk property urlFoto");
        }
    }

    @Test
    void testOnCreate() {
        assertNull(kos.getCreatedAt());
        assertNull(kos.getUpdatedAt());

        kos.onCreate();

        assertNotNull(kos.getCreatedAt());
        assertNotNull(kos.getUpdatedAt());
        assertTrue(LocalDateTime.now().minusMinutes(1).isBefore(kos.getCreatedAt()));
    }

    @Test
    void testOnUpdate() {
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        kos.setCreatedAt(initialTime);
        kos.setUpdatedAt(initialTime);

        kos.onUpdate();

        assertEquals(initialTime, kos.getCreatedAt());
        assertNotEquals(initialTime, kos.getUpdatedAt());
        assertTrue(kos.getUpdatedAt().isAfter(initialTime));
    }

    @Test
    void testNoApprovedPenyewaan() {
        assertEquals(20, kos.getJumlahTersedia());
    }

    @Test
    void testWithApprovedPenyewaan() {
        Penyewaan penyewaan1 = mock(Penyewaan.class);
        Penyewaan penyewaan2 = mock(Penyewaan.class);
        
        when(penyewaan1.getStatus()).thenReturn(StatusPenyewaan.APPROVED);
        when(penyewaan2.getStatus()).thenReturn(StatusPenyewaan.PENDING);
        
        List<Penyewaan> penyewaanList = new ArrayList<>();
        penyewaanList.add(penyewaan1);
        penyewaanList.add(penyewaan2);
        kos.setPenyewaan(penyewaanList);

        assertEquals(19, kos.getJumlahTersedia());
    }

    @Test
    void testWhenAvailableAndHasRooms() {
        kos.setStatus("AVAILABLE");
        assertTrue(kos.isAvailable());
    }

    @Test
    void testWhenNotAvailableStatus() {
        kos.setStatus("NOT_AVAILABLE");
        assertFalse(kos.isAvailable());
    }

    @Test
    void testWhenAvailableButNoRooms() {
        kos.setStatus("AVAILABLE");
 
        List<Penyewaan> penyewaanList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Penyewaan p = mock(Penyewaan.class);
            when(p.getStatus()).thenReturn(StatusPenyewaan.APPROVED);
            penyewaanList.add(p);
        }
        kos.setPenyewaan(penyewaanList);
        
        assertFalse(kos.isAvailable());
    }

    @Test
    void testAddPenyewaan() {
        Penyewaan newPenyewaan = new Penyewaan();
        kos.addPenyewaan(newPenyewaan);

        assertTrue(kos.getPenyewaan().contains(newPenyewaan));
        assertEquals(kos, newPenyewaan.getKos());
    }

    @Test
    void testRemovePenyewaan() {
        Penyewaan penyewaanToRemove = new Penyewaan();
        penyewaanToRemove.setKos(kos);
        kos.getPenyewaan().add(penyewaanToRemove);

        assertTrue(kos.getPenyewaan().contains(penyewaanToRemove));

        kos.removePenyewaan(penyewaanToRemove);

        assertFalse(kos.getPenyewaan().contains(penyewaanToRemove));
        assertNull(penyewaanToRemove.getKos());
    }
}