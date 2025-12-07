package com.proyecto.demo.service;

import com.proyecto.demo.model.AuditLog;
import com.proyecto.demo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditRepo;

    public AuditLogService(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public void log(Long userId, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetails(details);
        auditRepo.save(log);
    }
}
