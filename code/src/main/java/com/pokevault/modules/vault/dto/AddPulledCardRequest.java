package com.pokevault.modules.vault.dto;

import com.pokevault.domain.enums.CardCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO สำหรับรับข้อมูลการเปิดซองสุ่มการ์ด (+ Add Pull) บันทึกเข้าไอดีเกม
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPulledCardRequest {

    @NotNull(message = "Card ID is required")
    private Long cardId;

    @NotNull(message = "Card condition is required")
    @Builder.Default
    private CardCondition condition = CardCondition.MINT;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private int quantity = 1;

    @DecimalMin(value = "0.00", message = "Buy-in price must not be negative")
    @Builder.Default
    private BigDecimal buyInPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Selling price must not be negative")
    @Builder.Default
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Size(max = 50, message = "Storage slot must not exceed 50 characters")
    private String storageSlot;
}
