package com.pokevault.domain.enums;

/**
 * สถานะการส่งมอบการ์ดในเกมสำหรับแต่ละ OrderItem
 */
public enum TradeFulfillmentStatus {
    UNASSIGNED,     // ยังไม่ได้มอบหมายไอดีเกมร้านค้า
    FRIEND_PENDING, // รอแอดเป็นเพื่อนในเกม Pokémon Pocket
    TRADE_SENT,     // ส่งคำขอเทรดการ์ดในเกมแล้ว
    COMPLETED       // ลูกค้ายืนยันรับการ์ดครบถ้วนเรียบร้อย
}
