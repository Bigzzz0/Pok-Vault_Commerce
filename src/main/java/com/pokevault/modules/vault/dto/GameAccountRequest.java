package com.pokevault.modules.vault.dto;

import com.pokevault.domain.enums.AccountTradeStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO สำหรับรับข้อมูลลงทะเบียนหรืออัปเดตไอดีเกมของร้านค้า
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAccountRequest {

    @NotBlank(message = "Account code is required")
    @Size(max = 50, message = "Account code must not exceed 50 characters")
    private String accountCode;

    @NotBlank(message = "In-game name is required")
    @Size(max = 100, message = "In-game name must not exceed 100 characters")
    private String inGameName;

    @NotBlank(message = "Friend ID is required")
    @Pattern(regexp = "^\\d{4}-?\\d{4}-?\\d{4}-?\\d{4}$", message = "Friend ID must be a 16-digit Pokémon Pocket ID (e.g. 1234-5678-9012-3456)")
    private String friendId;

    @Builder.Default
    private AccountTradeStatus tradeStatus = AccountTradeStatus.READY;

    @DecimalMin(value = "0.00", message = "Buy-in cost must not be negative")
    @Builder.Default
    private BigDecimal buyInCost = BigDecimal.ZERO;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
