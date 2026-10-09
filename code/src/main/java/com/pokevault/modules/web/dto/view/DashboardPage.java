package com.pokevault.modules.web.dto.view;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** ข้อมูลหน้าแรก: ตัวเลขสรุปและการ์ด Spotlight */
@Value
@Builder
public class DashboardPage {
    long totalCards;
    long totalExpansions;
    long totalOrders;
    List<CardView> featuredCards;
}
