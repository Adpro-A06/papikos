package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PengelolaanServiceImpl implements PengelolaanService {
    private final PengelolaanRepository pengelolaanRepository;

    @Autowired
    public PengelolaanServiceImpl(PengelolaanRepository pengelolaanRepository) {
        this.pengelolaanRepository = pengelolaanRepository;
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Kos> create(Kos kos) {
        return CompletableFuture.completedFuture(pengelolaanRepository.create(kos));
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<List<Kos>> findAll() {
        return CompletableFuture.completedFuture(pengelolaanRepository.findAllOrThrow());
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Kos> findById(UUID id) {
        return CompletableFuture.completedFuture(pengelolaanRepository.findByIdOrThrow(id));
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Kos> update(Kos kos) {
        return CompletableFuture.completedFuture(pengelolaanRepository.update(kos));
    }

    @Override
    @Async("pengelolaanTaskExecutor")
    public CompletableFuture<Void> delete(Kos kos) {
        pengelolaanRepository.delete(kos);
        return CompletableFuture.completedFuture(null);
    }
}