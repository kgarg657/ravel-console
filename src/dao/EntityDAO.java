package dao;

import java.sql.Connection;
import java.util.List;
import model.Entity;
import repository.Repository;

public class EntityDAO implements Repository<Entity> {
    @Override
    public void save(Entity entity) {
        throw new UnsupportedOperationException("EntityDAO.save not implemented yet");
    }

    @Override
    public Entity findById(String id) {
        throw new UnsupportedOperationException("EntityDAO.findById not implemented yet");
    }

    @Override
    public List<Entity> findAll() {
        throw new UnsupportedOperationException("EntityDAO.findAll not implemented yet");
    }

    @Override
    public void update(Entity entity) {
        throw new UnsupportedOperationException("EntityDAO.update not implemented yet");
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("EntityDAO.delete not implemented yet");
    }

    public void addRisk(Connection conn, String id, int delta) {
        throw new UnsupportedOperationException("EntityDAO.addRisk not implemented yet");
    }
}