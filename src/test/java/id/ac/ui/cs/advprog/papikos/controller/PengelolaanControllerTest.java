package id.ac.ui.cs.advprog.papikos.controller;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.service.PengelolaanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PengelolaanController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class PengelolaanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PengelolaanService pengelolaanService;

    @Test
    void testShowAllKoss() throws Exception {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        kos.setNama("Kos Test");
        List<Kos> listKos = List.of(kos);

        when(pengelolaanService.findAll()).thenReturn(listKos);

        mockMvc.perform(get("/pemilik/daftarkos"))
                .andExpect(model().attribute("kos", listKos))
                .andExpect(view().name("pengelolaan/ListKos"));

        verify(pengelolaanService).findAll();
    }

    @Test
    void testCreateKosPage() throws Exception {
        mockMvc.perform(get("/pemilik/create"))
                .andExpect(view().name("pengelolaan/CreateKos"))
                .andExpect(model().attributeExists("kos"));
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
                .andExpect(redirectedUrl("daftarkos"));

        ArgumentCaptor<Kos> kosCaptor = ArgumentCaptor.forClass(Kos.class);
        verify(pengelolaanService).create(kosCaptor.capture());
        Kos capturedKos = kosCaptor.getValue();
        assertEquals(kos.getNama(), capturedKos.getNama());
        assertEquals(kos.getAlamat(), capturedKos.getAlamat());
        assertEquals(kos.getJumlah(), capturedKos.getJumlah());
        assertEquals(kos.getStatus(), capturedKos.getStatus());
    }

    @Test
    void testUpdateKosPage() throws Exception {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        kos.setNama("Kos Test");

        when(pengelolaanService.findById(kos.getId())).thenReturn(kos);

        mockMvc.perform(get("/pemilik/edit/{id}", kos.getId()))
                .andExpect(view().name("pengelolaan/EditKos"))
                .andExpect(model().attribute("kos", kos));

        verify(pengelolaanService).findById(kos.getId());
    }

    @Test
    void testUpdateKosPost() throws Exception {
        Kos existingKos = new Kos();
        existingKos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");
        existingKos.setNama("Kos Test");
        existingKos.setAlamat("Jl. Test");
        existingKos.setDeskripsi("Deskripsi Kos");
        existingKos.setJumlah(10);
        existingKos.setHarga(1000000);
        existingKos.setStatus("Tersedia");

        Kos updatedKos = new Kos();
        updatedKos.setId(existingKos.getId());
        updatedKos.setNama("Kos Updated");
        updatedKos.setAlamat("Jl. Updated");
        updatedKos.setDeskripsi("Deskripsi Updated");
        updatedKos.setJumlah(20);
        updatedKos.setHarga(2000000);
        updatedKos.setStatus("Penuh");

        when(pengelolaanService.update(any(Kos.class))).thenReturn(updatedKos);

        mockMvc.perform(post("/pemilik/edit/{id}", existingKos.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nama", updatedKos.getNama())
                        .param("alamat", updatedKos.getAlamat())
                        .param("deskripsi", updatedKos.getDeskripsi())
                        .param("jumlah", String.valueOf(updatedKos.getJumlah()))
                        .param("harga", String.valueOf(updatedKos.getHarga()))
                        .param("status", updatedKos.getStatus()))
                .andExpect(redirectedUrl("daftarkos"));

        ArgumentCaptor<Kos> kosCaptor = ArgumentCaptor.forClass(Kos.class);
        verify(pengelolaanService).update(kosCaptor.capture());
        Kos capturedKos = kosCaptor.getValue();
        assertEquals(updatedKos.getNama(), capturedKos.getNama());
        assertEquals(updatedKos.getAlamat(), capturedKos.getAlamat());
        assertEquals(updatedKos.getJumlah(), capturedKos.getJumlah());
        assertEquals(updatedKos.getStatus(), capturedKos.getStatus());
    }

    @Test
    void testUpdateKosPost_NotFound() throws Exception {
        String id = "notexist-id";
        when(pengelolaanService.update(any(Kos.class)))
                .thenThrow(new PengelolaanRepository.KosNotFoundException("Kos tidak ditemukan"));

        mockMvc.perform(post("/pemilik/edit/{id}", id)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nama", "Test Kos")
                        .param("alamat", "Jl. Test")
                        .param("deskripsi", "Deskripsi Kos")
                        .param("jumlah", "10")
                        .param("harga", "1000000")
                        .param("status", "Tersedia"))
                .andExpect(view().name("pengelolaan/error/KosNotFound"));

        verify(pengelolaanService).update(any(Kos.class));
    }

    @Test
    void testDeleteKos() throws Exception {
        Kos kos = new Kos();
        kos.setId("eb558e9f-1c39-460e-8860-71af6af63bd6");

        when(pengelolaanService.findById(kos.getId())).thenReturn(kos);
        doNothing().when(pengelolaanService).delete(any(Kos.class));

        mockMvc.perform(get("/pemilik/delete/{id}", kos.getId()))
                .andExpect(redirectedUrl("daftarkos"));

        verify(pengelolaanService).findById(kos.getId());
        verify(pengelolaanService).delete(any(Kos.class));
    }

}