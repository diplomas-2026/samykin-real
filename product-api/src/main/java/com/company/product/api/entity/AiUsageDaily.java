package com.company.product.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ai_usage_daily")
public class AiUsageDaily {

    @Id
    private LocalDate usageDate;

    @Column(nullable = false)
    private int usedTokens;
}
