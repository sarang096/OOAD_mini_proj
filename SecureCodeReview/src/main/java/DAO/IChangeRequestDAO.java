package main.java.DAO;

import main.java.model.ChangeRequest;
import main.java.model.enums.ChangeStatus;
import java.util.List;

/**
 * Dependency Inversion Principle —
 * High level modules depend on this abstraction
 * NOT on the concrete ChangeRequestDAO class
 */
public interface IChangeRequestDAO {
    void save(ChangeRequest cr);
    void updateStatus(int requestId, ChangeStatus status);
    ChangeRequest findById(int requestId);
    List<ChangeRequest> findByDeveloper(int devId);
    List<ChangeRequest> findByStatus(ChangeStatus status);
    List<ChangeRequest> getAll();
}