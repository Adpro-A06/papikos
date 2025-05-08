package id.ac.ui.cs.advprog.papikos.kos.model;

import lombok.Getter;
import lombok.Setter;

public class Kos {
    private String id;
    private String nama;
    private String alamat;
    private String deskripsi;
    private int jumlah;
    private int harga;
    private String status;
    private String url_foto;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public int getHarga() {
        return harga;
    }

    public void setHarga(int harga) {
        this.harga = harga;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl_foto() {return url_foto;}

    public void setUrl_foto(String url_foto) {this.url_foto = url_foto;}
}