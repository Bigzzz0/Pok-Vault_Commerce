package com.pokevault.modules.web.dto.view;

import com.pokevault.domain.enums.CardCondition;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/** สต็อกหนึ่งล็อตในหน้า /inventory */
@Value
@Builder
public class InventoryView {
    Long id;
    String cardName;
    String cardNumber;
    String expansionCode;
    String imageUrl;
    CardCondition condition;
    Integer quantity;
    boolean lowStock;
    boolean outOfStock;
    BigDecimal buyInPrice;
    BigDecimal sellingPrice;
    String storageSlot;
}
