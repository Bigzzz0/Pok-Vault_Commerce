package com.pokevault.modules.web.dto.view;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class OrderItemView {
    String cardName;
    String imageUrl;
    String expansionCode;
    String cardNumber;
    Integer quantity;
    BigDecimal unitPrice;
}
