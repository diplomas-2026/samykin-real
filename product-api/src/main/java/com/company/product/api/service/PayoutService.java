package com.company.product.api.service;

import com.company.product.api.dto.payout.PayoutRequest;
import com.company.product.api.dto.payout.PayoutResponse;
import com.company.product.api.entity.Payout;
import com.company.product.api.entity.PayoutStatus;
import com.company.product.api.repository.PayoutRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final UserService userService;
    private final PayoutMapper payoutMapper;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<PayoutResponse> getAll() {
        return payoutRepository.findAllDetailed().stream().map(payoutMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PayoutResponse> getMine(String email) {
        return payoutRepository.findAllDetailedByEmployeeEmail(email).stream().map(payoutMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PayoutResponse getById(Long id) {
        return payoutMapper.toResponse(getEntity(id));
    }

    @Transactional
    public PayoutResponse create(PayoutRequest request, String actorEmail) {
        var actor = userService.findByEmail(actorEmail);
        var employee = userService.getEntity(request.employeeId());
        Payout payout = new Payout();
        payout.setPayoutCode(generateCode());
        payout.setEmployee(employee);
        payout.setCreatedBy(actor);
        payout.setStatus(PayoutStatus.CREATED);
        applyEditableFields(payout, request);
        Payout saved = payoutRepository.save(payout);
        auditService.log(actor, "CREATE_PAYOUT", "Создана выплата " + saved.getPayoutCode(), saved);
        return payoutMapper.toResponse(saved);
    }

    @Transactional
    public PayoutResponse update(Long id, PayoutRequest request, String actorEmail) {
        var actor = userService.findByEmail(actorEmail);
        Payout payout = getEntity(id);
        if (payout.getStatus() == PayoutStatus.PAID) {
            throw new IllegalArgumentException("Выданную выплату нельзя редактировать");
        }
        payout.setEmployee(userService.getEntity(request.employeeId()));
        applyEditableFields(payout, request);
        Payout saved = payoutRepository.save(payout);
        auditService.log(actor, "UPDATE_PAYOUT", "Обновлена выплата " + saved.getPayoutCode(), saved);
        return payoutMapper.toResponse(saved);
    }

    @Transactional
    public PayoutResponse updateStatus(Long id, PayoutStatus status, String actorEmail) {
        var actor = userService.findByEmail(actorEmail);
        Payout payout = getEntity(id);
        validateTransition(payout.getStatus(), status);
        payout.setStatus(status);
        if (status == PayoutStatus.PREPARED) {
            payout.setPreparedAt(OffsetDateTime.now());
        }
        if (status == PayoutStatus.PAID) {
            payout.setPaidAt(OffsetDateTime.now());
        }
        Payout saved = payoutRepository.save(payout);
        auditService.log(actor, "UPDATE_PAYOUT_STATUS", "Статус " + saved.getPayoutCode() + " -> " + status, saved);
        return payoutMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Payout getEntity(Long id) {
        return payoutRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Выплата не найдена"));
    }

    private void applyEditableFields(Payout payout, PayoutRequest request) {
        payout.setPayoutType(request.payoutType().trim());
        payout.setAmount(request.amount());
        payout.setPayoutDate(request.payoutDate());
        payout.setBasis(request.basis().trim());
        payout.setComment(request.comment().trim());
        payout.setPayoutNote(request.payoutNote() == null ? null : request.payoutNote().trim());
    }

    private void validateTransition(PayoutStatus current, PayoutStatus target) {
        if (current == target) {
            return;
        }
        boolean valid = switch (current) {
            case CREATED -> target == PayoutStatus.PREPARED || target == PayoutStatus.CANCELLED;
            case PREPARED -> target == PayoutStatus.PAID || target == PayoutStatus.CANCELLED;
            case PAID, CANCELLED -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Недопустимый переход статуса");
        }
    }

    private String generateCode() {
        AtomicLong sequence = new AtomicLong(payoutRepository.count() + 1);
        String code;
        do {
            code = "PAY-" + String.format("%05d", sequence.getAndIncrement());
        } while (payoutRepository.findByPayoutCode(code).isPresent());
        return code;
    }
}
