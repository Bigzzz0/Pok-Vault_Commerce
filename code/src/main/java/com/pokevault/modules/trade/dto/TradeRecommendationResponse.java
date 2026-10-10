package com.pokevault.modules.trade.dto;

import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO ผลลัพธ์การแนะนำไอดีเกมที่เหมาะสมในการเทรดการ์ด (Trade Recommendation)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeRecommendationResponse {

    // ข้อมูลคำสั่งซื้อและรายการการ์ดที่ต้องการเทรด
    private Long orderId;
    private String orderCode;
    private Long orderItemId;
    private Long cardId;
    private String cardName;
    private String cardNumber;
    private Integer requestedQuantity;
    private TradeFulfillmentStatus fulfillmentStatus;
    private Long currentAssignedAccountId;

    // ไอดีเกมอันดับ 1 ที่ระบบแนะนำ (Best Match Recommendation)
    private Long recommendedAccountId;
    private String recommendedAccountCode;
    private String recommendedInGameName;
    private String recommendedFriendId;
    private AccountTradeStatus accountStatus;
    private Integer availableStock;
    private Boolean matchFound;
    private String recommendationReason;

    // ทางเลือกไอดีเกมสำรองอื่นๆ ที่ถือการ์ดใบนี้เช่นกัน (Candidate Accounts)
    @Builder.Default
    private List<CandidateAccountResponse> alternativeCandidates = new ArrayList<>();

    /**
     * DTO สำหรับแสดงข้อมูลไอดีเกมที่เป็นตัวเลือก (Candidate Account)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateAccountResponse {
        private Long accountId;
        private String accountCode;
        private String inGameName;
        private String friendId;
        private AccountTradeStatus tradeStatus;
        private Integer availableStock;
    }
}
