package id.ac.ui.cs.advprog.papikos.service;

import id.ac.ui.cs.advprog.papikos.model.Kos;

import java.util.List;

public interface PengelolaanService{
    Kos create(Kos kos);
    List<Kos> findAll();
    Kos findById(String id);
    Kos update(Kos kos);
    void delete(Kos kos);
}