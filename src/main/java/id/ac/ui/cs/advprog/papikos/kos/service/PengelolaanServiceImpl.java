package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PengelolaanServiceImpl implements PengelolaanService {
    private final PengelolaanRepository pengelolaanRepository;

    @Autowired
    public PengelolaanServiceImpl(PengelolaanRepository pengelolaanRepository) {
        this.pengelolaanRepository = pengelolaanRepository;
    }

    @Override
    public Kos create(Kos kos) {
        return pengelolaanRepository.create(kos);
    }

    @Override
    public List<Kos> findAll() {
        return pengelolaanRepository.findAllOrThrow();
    }

    @Override
    public Kos findById(UUID id) {
        return pengelolaanRepository.findByIdOrThrow(id);
    }

    @Override
    public Kos update(Kos kos) {
        return pengelolaanRepository.update(kos);
    }

    @Override
    public void delete(Kos kos) {
        pengelolaanRepository.delete(kos);
    }
}