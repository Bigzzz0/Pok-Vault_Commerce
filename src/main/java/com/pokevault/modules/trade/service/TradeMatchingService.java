package com.pokevault.modules.trade.service;

import com.pokevault.modules.trade.dto.TradeRecommendationResponse;

import java.util.List;

public interface TradeMatchingService {

    /**
     * ค้นหาและแนะนำไอดีเกมที่เหมาะสมที่สุดสำหรับทุกรายการในคำสั่งซื้อ (Order)
     *
     * @param orderId รหัสคำสั่งซื้อ
     * @return รายการคำแนะนำ TradeRecommendationResponse สำหรับแต่ละ OrderItem ในคำสั่งซื้อ
     */
    List<TradeRecommendationResponse> getRecommendations(Long orderId);

    /**
     * ค้นหาและแนะนำไอดีเกมที่เหมาะสมสำหรับ OrderItem รายการใดรายการหนึ่งโดยเฉพาะ
     *
     * @param orderItemId รหัสรายการสินค้าในคำสั่งซื้อ
     * @return TradeRecommendationResponse ผลการแนะนำไอดีเกม
     */
    TradeRecommendationResponse getRecommendationForItem(Long orderItemId);

    /**
     * ค้นหาและผูกไอดีเกมที่พร้อมที่สุด (READY + สต็อกสูงสุด) ให้กับ OrderItem โดยอัตโนมัติ
     * พร้อมอัปเดต assignedAccount และเปลี่ยนสถานะเป็น FRIEND_PENDING
     *
     * @param orderItemId รหัสรายการสินค้าในคำสั่งซื้อ
     * @return TradeRecommendationResponse ผลการจับคู่และมอบหมายงาน
     */
    TradeRecommendationResponse autoMatchOrderItem(Long orderItemId);

    /**
     * ดำเนินการ Auto-match และมอบหมายไอดีเกมให้กับทุกรายการในคำสั่งซื้อ (Order)
     *
     * @param orderId รหัสคำสั่งซื้อ
     * @return รายการผลการจับคู่ของทุก OrderItem ในคำสั่งซื้อ
     */
    List<TradeRecommendationResponse> autoMatchOrder(Long orderId);

    /**
     * มอบหมายไอดีเกมให้กับ OrderItem ด้วยตนเอง (Manual Assignment)
     *
     * @param orderItemId รหัสรายการสินค้าในคำสั่งซื้อ
     * @param accountId รหัสไอดีเกมที่ต้องการมอบหมาย
     * @return TradeRecommendationResponse ผลการมอบหมายงาน
     */
    TradeRecommendationResponse assignAccountToOrderItem(Long orderItemId, Long accountId);
}
