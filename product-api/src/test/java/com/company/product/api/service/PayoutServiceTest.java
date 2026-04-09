package com.company.product.api.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.product.api.dto.payout.PayoutRequest;
import com.company.product.api.entity.Payout;
import com.company.product.api.entity.PayoutStatus;
import com.company.product.api.entity.UserAccount;
import com.company.product.api.entity.UserRole;
import com.company.product.api.repository.PayoutRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    private PayoutService payoutService;

    @BeforeEach
    void setUp() {
        payoutService = new PayoutService(payoutRepository, userService, new PayoutMapper(), auditService);
    }

    @Test
    void shouldPreventEditingPaidPayout() {
        UserAccount employee = new UserAccount();
        employee.setId(1L);
        employee.setRole(UserRole.EMPLOYEE);
        Payout payout = new Payout();
        payout.setId(1L);
        payout.setStatus(PayoutStatus.PAID);
        payout.setEmployee(employee);
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));

        PayoutRequest request = new PayoutRequest(
            1L,
            "Премия",
            BigDecimal.TEN,
            LocalDate.now(),
            "Основание",
            "Комментарий",
            null
        );

        assertThatThrownBy(() -> payoutService.update(1L, request, "buhgalter@samykin.local"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("нельзя редактировать");
    }
}
