package com.pokevault.modules.web.dto.view;

import com.pokevault.domain.enums.AccountTradeStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/** บัญชีเกมหนึ่งบัญชีในหน้า /accounts พร้อมจำนวนการ์ดที่เก็บไว้ */
@Value
@Builder
public class GameAccountView {
    Long id;
    String accountCode;
    String inGameName;
    String friendId;
    AccountTradeStatus tradeStatus;
    BigDecimal buyInCost;
    String notes;
    int totalCardsCount;
}
