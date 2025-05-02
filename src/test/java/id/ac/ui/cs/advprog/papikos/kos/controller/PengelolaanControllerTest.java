package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.kos.service.PengelolaanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PengelolaanController.class)
@ExtendWith(SpringExtension.class)
public class PengelolaanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PengelolaanService pengelolaanService;

    @BeforeEach
    void setUp() {
        reset(pengelolaanService); // Reset mocks before each test
    }

    @Test
    void testMainPage() throws Exception {
        mockMvc.perform(get("/pemilik/mainpage"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/PemilikKosHome"));
    }

    @Test
    void testCreateKosPage() throws Exception {
        mockMvc.perform(get("/pemilik/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("pengelolaan/CreateKos"))
                .andExpect(model().attributeExists("kos"))
                .andExpect(model().attribute("kos", instanceOf(Kos.class)));
    }

    @Test
    void testShowAllKoss() throws Exception {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        kos.setNama("Kos Test");
        List<Kos> listKos = List.of(kos);

        when(pengelolaanService.findAll()).thenReturn(listKos);

        mockMvc.perform(get("/pemilik/daftarkos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pengelolaan/ListKos"))
                .andExpect(model().attribute("kos", listKos));

        verify(pengelolaanService, times(1)).findAll();
    }

    @Test
    void testCreateKosPost() throws Exception {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        kos.setNama("Kos Test");
        kos.setAlamat("Jl. Test");
        kos.setDeskripsi("Deskripsi Kos");
        kos.setJumlah(10);
        kos.setHarga(1000000);
        kos.setStatus("Tersedia");

        when(pengelolaanService.create(any(Kos.class))).thenReturn(kos);

        mockMvc.perform(post("/pemilik/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nama", kos.getNama())
                        .param("alamat", kos.getAlamat())
                        .param("deskripsi", kos.getDeskripsi())
                        .param("jumlah", String.valueOf(kos.getJumlah()))
                        .param("harga", String.valueOf(kos.getHarga()))
                        .param("status", kos.getStatus()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("daftarkos"));

        ArgumentCaptor<Kos> kosCaptor = ArgumentCaptor.forClass(Kos.class);
        verify(pengelolaanService, times(1)).create(kosCaptor.capture());
        Kos capturedKos = kosCaptor.getValue();
        assertEquals(kos.getNama(), capturedKos.getNama());
        assertEquals(kos.getAlamat(), capturedKos.getAlamat());
        assertEquals(kos.getDeskripsi(), capturedKos.getDeskripsi());
        assertEquals(kos.getJumlah(), capturedKos.getJumlah());
        assertEquals(kos.getHarga(), capturedKos.getHarga(), 0.01);
        assertEquals(kos.getStatus(), capturedKos.getStatus());
    }

    @Test
    void testUpdateKosPage() throws Exception {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        kos.setNama("Kos Test");

        when(pengelolaanService.findById(kos.getId())).thenReturn(kos);

        mockMvc.perform(get("/pemilik/edit/{id}", kos.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("pengelolaan/EditKos"))
                .andExpect(model().attribute("kos", kos));

        verify(pengelolaanService, times(1)).findById(kos.getId());
    }

    @Test
    void testUpdateKosPost() throws Exception {
        Kos updatedKos = new Kos();
        updatedKos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        updatedKos.setNama("Kos Updated");
        updatedKos.setAlamat("Jl. Updated");
        updatedKos.setDeskripsi("Deskripsi Updated");
        updatedKos.setJumlah(20);
        updatedKos.setHarga(2000000);
        updatedKos.setStatus("Penuh");

        when(pengelolaanService.update(any(Kos.class))).thenReturn(updatedKos);

        mockMvc.perform(post("/pemilik/edit/{id}", updatedKos.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nama", updatedKos.getNama())
                        .param("alamat", updatedKos.getAlamat())
                        .param("deskripsi", updatedKos.getDeskripsi())
                        .param("jumlah", String.valueOf(updatedKos.getJumlah()))
                        .param("harga", String.valueOf(updatedKos.getHarga()))
                        .param("status", updatedKos.getStatus()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pemilik/daftarkos"));

        ArgumentCaptor<Kos> kosCaptor = ArgumentCaptor.forClass(Kos.class);
        verify(pengelolaanService, times(1)).update(kosCaptor.capture());
        Kos capturedKos = kosCaptor.getValue();
        assertEquals(updatedKos.getId(), capturedKos.getId());
        assertEquals(updatedKos.getNama(), capturedKos.getNama());
        assertEquals(updatedKos.getAlamat(), capturedKos.getAlamat());
        assertEquals(updatedKos.getDeskripsi(), capturedKos.getDeskripsi());
        assertEquals(updatedKos.getJumlah(), capturedKos.getJumlah());
        assertEquals(updatedKos.getHarga(), capturedKos.getHarga(), 0.01);
        assertEquals(updatedKos.getStatus(), capturedKos.getStatus());
    }

    @Test
    void testUpdateKosPostNotFound() throws Exception {
        String id = "notexist-id";
        when(pengelolaanService.update(any(Kos.class)))
                .thenThrow(new PengelolaanRepository.KosNotFoundException("Kos not found"));

        mockMvc.perform(post("/pemilik/edit/{id}", id)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nama", "Test Kos")
                        .param("alamat", "Jl. Test")
                        .param("deskripsi", "Deskripsi Kos")
                        .param("jumlah", "10")
                        .param("harga", "1000000")
                        .param("status", "Tersedia"))
                .andExpect(status().isOk())
                .andExpect(view().name("pengelolaan/error/KosNotFound"));

        verify(pengelolaanService, times(1)).update(any(Kos.class));
    }

    @Test
    void testDeleteKos() throws Exception {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");

        when(pengelolaanService.findById(kos.getId())).thenReturn(kos);
        doNothing().when(pengelolaanService).delete(any(Kos.class));

        mockMvc.perform(get("/pemilik/delete/{id}", kos.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("daftarkos"));

        verify(pengelolaanService, times(1)).findById(kos.getId());
        verify(pengelolaanService, times(1)).delete(any(Kos.class));
    }
}