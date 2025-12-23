package com.proyecto.demo.service;

import com.proyecto.demo.model.AuditLog;
import com.proyecto.demo.repository.AuditLogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository repo;

    public AuditLogService(AuditLogRepository repo) {
        this.repo = repo;
    }

    public List<AuditLog> listar() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    public void registrar(Long userId, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        repo.save(log);
    }

    // ✅ Alias para que los listeners compilen
    public void log(Long userId, String action, String details) {
        registrar(userId, action, details);
    }
}
