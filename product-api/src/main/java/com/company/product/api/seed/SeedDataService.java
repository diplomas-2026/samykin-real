package com.company.product.api.seed;

import com.company.product.api.config.AppProperties;
import com.company.product.api.entity.Payout;
import com.company.product.api.entity.PayoutStatus;
import com.company.product.api.entity.UserAccount;
import com.company.product.api.entity.UserRole;
import com.company.product.api.repository.PayoutRepository;
import com.company.product.api.repository.UserAccountRepository;
import com.company.product.api.service.ai.AiSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SeedDataService implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final UserAccountRepository userAccountRepository;
    private final PayoutRepository payoutRepository;
    private final PasswordEncoder passwordEncoder;
    private final AiSettingsService aiSettingsService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        aiSettingsService.ensureSettings();
        SeedDataModels seedData = load();
        upsertUsers(seedData.users());
        upsertPayouts(seedData.payouts());
        writeUsersFile(seedData.users());
    }

    private SeedDataModels load() throws IOException {
        Path seedPath = Path.of(appProperties.seedDataDir(), "seed-data.json");
        return objectMapper.readValue(Files.readString(seedPath), SeedDataModels.class);
    }

    private void upsertUsers(List<SeedDataModels.SeedUser> users) {
        for (var seeded : users) {
            UserAccount user = userAccountRepository.findByEmailIgnoreCase(seeded.email())
                .orElseGet(UserAccount::new);
            user.setEmail(seeded.email().toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(seeded.password()));
            user.setFullName(seeded.fullName());
            user.setDepartment(seeded.department());
            user.setPosition(seeded.position());
            user.setEmployeeCode(seeded.employeeCode());
            user.setPhotoUrl(seeded.photoUrl());
            user.setRole(seeded.role());
            user.setActive(seeded.active());
            userAccountRepository.save(user);
        }
    }

    private void upsertPayouts(List<SeedDataModels.SeedPayout> payouts) {
        for (var seeded : payouts) {
            Payout payout = payoutRepository.findByPayoutCode(seeded.payoutCode())
                .orElseGet(Payout::new);
            payout.setPayoutCode(seeded.payoutCode());
            payout.setEmployee(getUser(seeded.employeeEmail(), UserRole.EMPLOYEE));
            payout.setCreatedBy(getUser(seeded.createdByEmail(), null));
            payout.setPayoutType(seeded.payoutType());
            payout.setAmount(seeded.amount());
            payout.setPayoutDate(seeded.payoutDate());
            payout.setStatus(seeded.status());
            payout.setBasis(seeded.basis());
            payout.setComment(seeded.comment());
            payout.setPayoutNote(seeded.payoutNote());
            if (seeded.status() == PayoutStatus.PREPARED && payout.getPreparedAt() == null) {
                payout.setPreparedAt(OffsetDateTime.now().minusDays(1));
            }
            if (seeded.status() == PayoutStatus.PAID && payout.getPaidAt() == null) {
                payout.setPreparedAt(OffsetDateTime.now().minusDays(2));
                payout.setPaidAt(OffsetDateTime.now().minusDays(1));
            }
            payoutRepository.save(payout);
        }
    }

    private UserAccount getUser(String email, UserRole requiredRole) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Не найден seed-пользователь " + email));
        if (requiredRole != null && user.getRole() != requiredRole) {
            throw new IllegalArgumentException("Пользователь " + email + " должен иметь роль " + requiredRole);
        }
        return user;
    }

    private void writeUsersFile(List<SeedDataModels.SeedUser> users) throws IOException {
        Path output = Path.of(appProperties.usersFile());
        List<String> lines = users.stream()
            .sorted(Comparator.comparing(SeedDataModels.SeedUser::role).thenComparing(SeedDataModels.SeedUser::email))
            .map(user -> "email=%s; password=%s; role=%s".formatted(user.email(), user.password(), user.role()))
            .toList();
        Files.writeString(output, String.join(System.lineSeparator(), lines));
    }
}
