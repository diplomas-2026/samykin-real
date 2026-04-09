package com.company.product.api.controller;

import com.company.product.api.dto.payout.AiCommentRequest;
import com.company.product.api.dto.payout.AiCommentResponse;
import com.company.product.api.dto.payout.PayoutRequest;
import com.company.product.api.dto.payout.PayoutResponse;
import com.company.product.api.dto.payout.PayoutStatusUpdateRequest;
import com.company.product.api.service.PayoutService;
import com.company.product.api.service.ai.AiCommentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;
    private final AiCommentService aiCommentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public List<PayoutResponse> getAll() {
        return payoutService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public PayoutResponse getById(@PathVariable Long id) {
        return payoutService.getById(id);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<PayoutResponse> getMine(Authentication authentication) {
        return payoutService.getMine(authentication.getName());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public PayoutResponse create(@Valid @RequestBody PayoutRequest request, Authentication authentication) {
        return payoutService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public PayoutResponse update(@PathVariable Long id, @Valid @RequestBody PayoutRequest request, Authentication authentication) {
        return payoutService.update(id, request, authentication.getName());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public PayoutResponse updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody PayoutStatusUpdateRequest request,
        Authentication authentication
    ) {
        return payoutService.updateStatus(id, request.status(), authentication.getName());
    }

    @PostMapping("/generate-comment")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public AiCommentResponse generateComment(@Valid @RequestBody AiCommentRequest request) {
        return aiCommentService.generateComment(request);
    }
}
