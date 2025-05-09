package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;

import java.util.List;
import java.util.UUID;

public interface PengelolaanService{
    Kos create(Kos kos);
    List<Kos> findAll();
    Kos findById(UUID id);
    Kos update(Kos kos);
    void delete(Kos kos);
}