package com.company.product.api.repository;

import com.company.product.api.entity.AiUsageDaily;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiUsageDailyRepository extends JpaRepository<AiUsageDaily, LocalDate> {
}
