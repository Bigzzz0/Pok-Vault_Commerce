package com.pokevault.domain.enums;

/**
 * สถานะความพร้อมในการเทรดของการ์ดในไอดีเกมร้านค้า
 */
public enum AccountTradeStatus {
    READY, // พร้อมทำการเทรดทันที
    BUSY, // กำลังติดเทรดกับลูกค้ารายอื่น
    COOLDOWN, // ติดคูลดาวน์โควต้าการเทรดประจำวัน
    BANNED // ไอดีถูกระงับการใช้งาน
}
