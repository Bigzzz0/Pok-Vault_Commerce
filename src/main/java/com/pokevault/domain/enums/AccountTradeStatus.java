package com.pokevault.domain.enums;

/**
 * สถานะความพร้อมในการเทรดของการ์ดในไอดีเกมร้านค้า
 */
public enum AccountTradeStatus {
    READY,          // พร้อมทำการเทรดทันที
    COOLDOWN,       // ติดคูลดาวน์โควต้าการเทรดประจำวัน
    BUSY,           // กำลังติดเทรดกับลูกค้ารายอื่น
    BUSY_TRADING,   // กำลังส่งมอบการ์ด
    BANNED,         // ไอดีถูกแบน
    SUSPENDED       // ระงับการใช้งานชั่วคราว
}
