package id.ac.ui.cs.advprog.papikos.kos.model.penyewaan;

import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PenyewaanTest {

    private static Validator validator;
    private Penyewaan penyewaan;
    private User mockPenyewa;
    private Kos mockKos;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        mockPenyewa = Mockito.mock(User.class);
        when(mockPenyewa.getEmail()).thenReturn("penyewa@example.com");
   
        mockKos = Mockito.mock(Kos.class);
        when(mockKos.getNama()).thenReturn("Kos Bahagia");
        when(mockKos.getHarga()).thenReturn(1500000);

        penyewaan = Penyewaan.builder()
            .id(UUID.randomUUID().toString())
            .kos(mockKos)
            .penyewa(mockPenyewa)
            .namaLengkap("John Doe")
            .nomorTelepon("08123456789")
            .tanggalCheckIn(LocalDate.now().plusDays(7))
            .durasiSewa(3)
            .totalBiaya(4500000)
            .status(StatusPenyewaan.PENDING)
            .build();
    }

    @Test
    void testPenyewaanCreation() {
        assertNotNull(penyewaan);
        assertEquals("John Doe", penyewaan.getNamaLengkap());
        assertEquals("08123456789", penyewaan.getNomorTelepon());
        assertEquals(3, penyewaan.getDurasiSewa());
        assertEquals(4500000, penyewaan.getTotalBiaya());
        assertEquals(StatusPenyewaan.PENDING, penyewaan.getStatus());
        assertEquals(mockKos, penyewaan.getKos());
        assertEquals(mockPenyewa, penyewaan.getPenyewa());
    }

    @Test
    void testNoArgsConstructor() {
        Penyewaan emptyPenyewaan = new Penyewaan();
        assertNotNull(emptyPenyewaan);
        assertNull(emptyPenyewaan.getId());
        assertNull(emptyPenyewaan.getNamaLengkap());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        
        Penyewaan fullPenyewaan = new Penyewaan(
            "test-id", mockKos, mockPenyewa, "Jane Doe", "08123456789",
            LocalDate.now().plusDays(10), 6, 9000000, StatusPenyewaan.APPROVED,
            now, now
        );
        
        assertEquals("test-id", fullPenyewaan.getId());
        assertEquals("Jane Doe", fullPenyewaan.getNamaLengkap());
        assertEquals(StatusPenyewaan.APPROVED, fullPenyewaan.getStatus());
        assertEquals(now, fullPenyewaan.getWaktuPengajuan());
    }

    @Test
    void testBuilder() {
        Penyewaan builtPenyewaan = Penyewaan.builder()
            .id("builder-id")
            .namaLengkap("Builder Name")
            .nomorTelepon("0811111111")
            .status(StatusPenyewaan.REJECTED)
            .build();
        
        assertEquals("builder-id", builtPenyewaan.getId());
        assertEquals("Builder Name", builtPenyewaan.getNamaLengkap());
        assertEquals(StatusPenyewaan.REJECTED, builtPenyewaan.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        Penyewaan testPenyewaan = new Penyewaan();
        LocalDate checkInDate = LocalDate.now().plusDays(5);
        
        testPenyewaan.setId("setter-id");
        testPenyewaan.setNamaLengkap("Setter Name");
        testPenyewaan.setNomorTelepon("0822222222");
        testPenyewaan.setTanggalCheckIn(checkInDate);
        testPenyewaan.setDurasiSewa(2);
        testPenyewaan.setKos(mockKos);
        testPenyewaan.setPenyewa(mockPenyewa);
        testPenyewaan.setStatus(StatusPenyewaan.PENDING);
        testPenyewaan.setTotalBiaya(3000000);
        
        assertEquals("setter-id", testPenyewaan.getId());
        assertEquals("Setter Name", testPenyewaan.getNamaLengkap());
        assertEquals("0822222222", testPenyewaan.getNomorTelepon());
        assertEquals(checkInDate, testPenyewaan.getTanggalCheckIn());
        assertEquals(2, testPenyewaan.getDurasiSewa());
        assertEquals(mockKos, testPenyewaan.getKos());
        assertEquals(mockPenyewa, testPenyewaan.getPenyewa());
        assertEquals(StatusPenyewaan.PENDING, testPenyewaan.getStatus());
        assertEquals(3000000, testPenyewaan.getTotalBiaya());
    }

    @Test
    void testHitungTotalBiaya() {
        Penyewaan testPenyewaan = new Penyewaan();
        testPenyewaan.setKos(mockKos);
        testPenyewaan.setDurasiSewa(4);
        
        testPenyewaan.hitungTotalBiaya();
        
        assertEquals(6000000, testPenyewaan.getTotalBiaya());
    }

    @Test
    void testHitungTotalBiayaWithNullKos() {
        Penyewaan testPenyewaan = new Penyewaan();
        testPenyewaan.setKos(null);
        testPenyewaan.setDurasiSewa(4);
        testPenyewaan.setTotalBiaya(100);
        
        testPenyewaan.hitungTotalBiaya();

        assertEquals(100, testPenyewaan.getTotalBiaya());
    }

    @Test
    void testHitungTotalBiayaWithInvalidDurasi() {
        Penyewaan testPenyewaan = new Penyewaan();
        testPenyewaan.setKos(mockKos);
        testPenyewaan.setDurasiSewa(0);
        testPenyewaan.setTotalBiaya(100);
        
        testPenyewaan.hitungTotalBiaya();

        assertEquals(100, testPenyewaan.getTotalBiaya());
    }

    @Test
    void testPrePersist() throws Exception {
        Penyewaan newPenyewaan = new Penyewaan();
        assertNull(newPenyewaan.getWaktuPengajuan());
        assertNull(newPenyewaan.getWaktuPerubahan());

        newPenyewaan.onCreate();
        assertNotNull(newPenyewaan.getWaktuPengajuan());
        assertNotNull(newPenyewaan.getWaktuPerubahan());
    }

    @Test
    void testPreUpdate() throws Exception {
        LocalDateTime initialTime = LocalDateTime.now().minusDays(1);
        Penyewaan existingPenyewaan = new Penyewaan();
        existingPenyewaan.setWaktuPerubahan(initialTime);

        existingPenyewaan.onUpdate();
        assertTrue(existingPenyewaan.getWaktuPerubahan().isAfter(initialTime));
    }

    @Test
    void testValidationNamaLengkap() {
        penyewaan.setNamaLengkap("");
        Set<ConstraintViolation<Penyewaan>> violations = validator.validate(penyewaan);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("namaLengkap")));
    }

    @Test
    void testValidationNamaLengkapPattern() {
        penyewaan.setNamaLengkap("John123");
        Set<ConstraintViolation<Penyewaan>> violations = validator.validate(penyewaan);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("namaLengkap") && 
                      v.getMessage().contains("hanya boleh berisi huruf dan spasi")));
    }

    @Test
    void testValidationNomorTelepon() {
        penyewaan.setNomorTelepon("12345");
        Set<ConstraintViolation<Penyewaan>> violations = validator.validate(penyewaan);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nomorTelepon")));
    }

    @Test
    void testValidationNomorTeleponPattern() {
        penyewaan.setNomorTelepon("08123ABC456");
        Set<ConstraintViolation<Penyewaan>> violations = validator.validate(penyewaan);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("nomorTelepon") && 
                      v.getMessage().contains("hanya boleh berisi angka")));
    }

    @Test
    void testValidationTanggalCheckIn() {
        penyewaan.setTanggalCheckIn(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<Penyewaan>> violations = validator.validate(penyewaan);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("tanggalCheckIn") && 
                     v.getMessage().contains("masa depan")));
    }

    @Test
    void testValidationDurasiSewa() {
        penyewaan.setDurasiSewa(0);
        Set<ConstraintViolation<Penyewaan>> violations = validator.validate(penyewaan);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("durasiSewa")));

        penyewaan.setDurasiSewa(13);
        violations = validator.validate(penyewaan);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("durasiSewa")));
    }
    
    @Test
    void testValidationStatus() {
        penyewaan.setStatus(null);
        Set<ConstraintViolation<Penyewaan>> violations = validator.validate(penyewaan);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    void testToString() {
        String penyewaanString = penyewaan.toString();
        
        assertNotNull(penyewaanString);
        assertTrue(penyewaanString.contains("namaLengkap=John Doe"));
        assertTrue(penyewaanString.contains("nomorTelepon=08123456789"));
        assertTrue(penyewaanString.contains("durasiSewa=3"));
        assertTrue(penyewaanString.contains("status=PENDING"));
    }
}