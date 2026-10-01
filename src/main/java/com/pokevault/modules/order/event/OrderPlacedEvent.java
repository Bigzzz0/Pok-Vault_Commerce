package com.pokevault.modules.order.event;

import com.pokevault.domain.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private Long orderId;
    private String orderCode;
    private List<OrderItem> items;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
