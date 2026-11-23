package repository;

import exception.DuplicateRecordException;
import exception.InvalidEntityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import model.HasId;

public class InMemoryRepository<T extends HasId> implements Repository<T> {
    private Map<String, T> byId;
    private List<T> items;

    public InMemoryRepository() {
        this.byId = new HashMap<>();
        this.items = new ArrayList<>();
    }

    @Override
    public void save(T item) {
        if (item == null || item.getId() == null || item.getId().trim().isEmpty()) {
            throw new InvalidEntityException("Record or ID cannot be null or blank.");
        }
        String id = item.getId();
        if (byId.containsKey(id)) {
            throw new DuplicateRecordException("Duplicate ID: " + id);
        }
        byId.put(id, item);
        items.add(item);
    }

    @Override
    public T findById(String id) {
        if (id == null) {
            return null;
        }
        return byId.get(id);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(items);
    }

    @Override
    public void update(T item) {
        if (item == null || item.getId() == null || item.getId().trim().isEmpty()) {
            throw new InvalidEntityException("Record or ID cannot be null or blank.");
        }
        String id = item.getId();
        if (!byId.containsKey(id)) {
            throw new InvalidEntityException("Record not found for ID: " + id);
        }
        byId.put(id, item);
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                items.set(i, item);
                break;
            }
        }
    }

    @Override
    public void delete(String id) {
        if (id == null) {
            return;
        }
        byId.remove(id);
        Iterator<T> it = items.iterator();
        while (it.hasNext()) {
            T current = it.next();
            if (id.equals(current.getId())) {
                it.remove();
                break;
            }
        }
    }
}