package service;

import dao.CaseDAO;
import dao.EntityDAO;
import exception.InvalidEntityException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import model.Entity;

public class EntityService {
    private final EntityDAO entityDAO;
    private final CaseDAO caseDAO;

    public EntityService(EntityDAO entityDAO, CaseDAO caseDAO) {
        if (entityDAO == null || caseDAO == null) {
            throw new IllegalArgumentException("EntityDAO and CaseDAO cannot be null.");
        }
        this.entityDAO = entityDAO;
        this.caseDAO = caseDAO;
    }

    public void registerEntity(Entity entity) {
        if (entity == null) {
            throw new InvalidEntityException("Entity cannot be null.");
        }
        entityDAO.save(entity);
    }

    public Entity getEntityById(String id) {
        return entityDAO.findById(id);
    }

    public List<Entity> getAllEntities() {
        return entityDAO.findAll();
    }

    public void updateEntity(Entity entity) {
        if (entity == null) {
            throw new InvalidEntityException("Entity cannot be null.");
        }
        entityDAO.update(entity);
    }

    public void delete(String id) {
        if (id == null || id.isBlank()) {
            System.out.println("Entity ID cannot be blank.");
            return;
        }

        String activeCaseId = caseDAO.findActiveCaseIdFor(id);
        if (activeCaseId != null) {
            System.out.println(
                    "Cannot delete entity " + id + " because it is linked to active case " + activeCaseId + ".");
            return;
        }

        try {
            entityDAO.delete(id);
            System.out.println("Entity " + id + " deleted.");
        } catch (RuntimeException e) {
            if (isForeignKeyViolation(e)) {
                System.out.println(
                        "Cannot delete entity " + id
                                + " because it is still referenced by existing cases, relationships, or audit logs.");
            } else {
                throw e;
            }
        }
    }

    private boolean isForeignKeyViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            if (current instanceof SQLException sqlEx) {
                String state = sqlEx.getSQLState();
                int code = sqlEx.getErrorCode();
                if ("23000".equals(state) || code == 1451 || code == 1452) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}