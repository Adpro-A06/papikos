package id.ac.ui.cs.advprog.papikos.service;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.repository.PengelolaanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        Iterator<Kos> kosIterator = pengelolaanRepository.findAll();
        List<Kos> allKos = new ArrayList<>();
        while (kosIterator.hasNext()) {
            allKos.add(kosIterator.next());
        }
        return allKos;
    }

    @Override
    public Kos findById(String id){
        return pengelolaanRepository.findById(id);
    }

    @Override
    public Kos update(Kos kos){
        return pengelolaanRepository.update(kos);
    }

    @Override
    public void delete(Kos kos){
        pengelolaanRepository.delete(kos);
    }
}