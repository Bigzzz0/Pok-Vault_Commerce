package com.pokevault.modules.trade.service;

import com.pokevault.modules.trade.dto.TradeRecommendationResponse;

import java.util.List;

public interface TradeMatchingService {

    /**
     * ค้นหาและแนะนำไอดีเกมที่เหมาะสมที่สุดสำหรับทุกรายการในคำสั่งซื้อ (Order)
     *
     * @param orderId รหัสคำสั่งซื้อ
     * @return รายการคำแนะนำ TradeRecommendationResponse สำหรับแต่ละ OrderItem
     *         ในคำสั่งซื้อ
     */
    List<TradeRecommendationResponse> getRecommendations(Long orderId);

    /**
     * ค้นหาและแนะนำไอดีเกมที่เหมาะสมสำหรับ OrderItem รายการใดรายการหนึ่งโดยเฉพาะ
     *
     * @param orderItemId รหัสรายการสินค้าในคำสั่งซื้อ
     * @return TradeRecommendationResponse ผลการแนะนำไอดีเกม
     */
    TradeRecommendationResponse getRecommendationForItem(Long orderItemId);
}
