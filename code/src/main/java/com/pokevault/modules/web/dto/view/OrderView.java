package com.pokevault.modules.web.dto.view;

import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** ออเดอร์หนึ่งรายการในหน้า /orders และ /my-orders */
@Value
@Builder
public class OrderView {
    Long id;
    String orderCode;
    LocalDateTime createdAt;
    String username;
    String customerFullName;
    String customerFriendId;
    String customerInGameName;
    MembershipTier membershipTier;
    OrderStatus orderStatus;
    List<OrderItemView> items;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
}
