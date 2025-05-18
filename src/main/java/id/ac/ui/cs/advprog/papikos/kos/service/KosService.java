package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KosService {
    List<Kos> findAllAvailable();
    List<Kos> searchByKeyword(String keyword);
    Optional<Kos> findById(UUID id);
}