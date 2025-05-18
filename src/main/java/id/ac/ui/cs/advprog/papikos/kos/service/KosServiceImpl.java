package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KosServiceImpl implements KosService {

    private final KosRepository kosRepository;

    @Autowired
    public KosServiceImpl(KosRepository kosRepository) {
        this.kosRepository = kosRepository;
    }

    @Override
    public List<Kos> findAllAvailable() {
        return kosRepository.findByStatus("AVAILABLE");
    }

    @Override
    public List<Kos> searchByKeyword(String keyword) {
        return kosRepository.searchByKeyword(keyword, "AVAILABLE");
    }

    @Override
    public Optional<Kos> findById(String id) {
        return kosRepository.findById(id);
    }
}