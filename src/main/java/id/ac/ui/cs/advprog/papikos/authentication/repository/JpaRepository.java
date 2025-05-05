package id.ac.ui.cs.advprog.papikos.authentication.repository;

import java.util.List;
import java.util.Optional;

/**
 * @param <T> Tipe entitas
 * @param <ID> Tipe ID entitas
 */
public interface JpaRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void delete(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
    long count();
    <S extends T> List<S> saveAll(Iterable<S> entities);
}