package com.pokevault.modules.vault.dto;

import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.enums.AccountTradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO สำหรับส่งข้อมูลไอดีเกมของร้านกลับไปยัง Client / UI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAccountResponse {

    private Long id;
    private String accountCode;
    private String inGameName;
    private String friendId;
    private AccountTradeStatus tradeStatus;
    private BigDecimal buyInCost;
    private String notes;
    private int totalCards;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GameAccountResponse fromEntity(GameAccount account) {
        if (account == null)
            return null;
        return GameAccountResponse.builder()
                .id(account.getId())
                .accountCode(account.getAccountCode())
                .inGameName(account.getInGameName())
                .friendId(account.getFriendId())
                .tradeStatus(account.getTradeStatus())
                .buyInCost(account.getBuyInCost())
                .notes(account.getNotes())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public static GameAccountResponse fromEntity(GameAccount account, int totalCards) {
        GameAccountResponse response = fromEntity(account);
        if (response != null) {
            response.setTotalCards(totalCards);
        }
        return response;
    }
}
