package repository;

import java.util.ArrayList;
import java.util.List;
import model.HasId;

public interface Repository<T extends HasId> {
    void save(T t);

    T findById(String id);

    default List<T> findAll() {
        return new ArrayList<>();
    }

    void update(T t);

    void delete(String id);
}