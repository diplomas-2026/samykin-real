package com.company.product.api.service;

import com.company.product.api.entity.AuditLog;
import com.company.product.api.entity.Payout;
import com.company.product.api.entity.UserAccount;
import com.company.product.api.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(UserAccount actor, String action, String details, Payout payout) {
        AuditLog log = new AuditLog();
        log.setActorEmail(actor.getEmail());
        log.setActorUser(actor);
        log.setPayout(payout);
        log.setAction(action);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
