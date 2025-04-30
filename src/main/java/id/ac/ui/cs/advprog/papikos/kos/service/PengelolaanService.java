package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;

import java.util.List;

public interface PengelolaanService{
    Kos create(Kos kos);
    List<Kos> findAll();
    Kos findById(String id);
    Kos update(Kos kos);
    void delete(Kos kos);
}