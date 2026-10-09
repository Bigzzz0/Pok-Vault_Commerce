package com.pokevault.common.exception;

/**
 * Exception ที่ถูกโยนเมื่อการเปลี่ยนสถานะขัดแย้งกับลำดับขั้นตอนทางธุรกิจ (Business Invariant Conflict)
 * เช่น การพยายามส่งเทรดขณะยังไม่จับคู่บัญชี, ออเดอร์ยังไม่อยู่ในสถานะ SHIPPING,
 * การข้ามขั้นหรือย้อนสถานะ หรือการแก้ไขออเดอร์ในสถานะสิ้นสุด (CANCELLED / COMPLETED)
 * จัดการโดย GlobalExceptionHandler และแปลงเป็น HTTP 409 CONFLICT
 */
public class TradeStateConflictException extends RuntimeException {

    public TradeStateConflictException(String message) {
        super(message);
    }

    public TradeStateConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
