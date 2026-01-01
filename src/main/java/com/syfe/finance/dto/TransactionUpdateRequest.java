package com.syfe.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpdateRequest {

    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    private String category;

    private String description;
}
