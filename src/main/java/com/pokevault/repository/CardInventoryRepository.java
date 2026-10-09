package com.pokevault.repository;

import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.enums.CardCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository สำหรับจัดการคลังการ์ดและการตรวจนับสต็อก
 * (CardInventory)
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
@Repository
public interface CardInventoryRepository extends JpaRepository<CardInventory, Long> {

    /**
     * ค้นหารายการสต็อกการ์ดทั้งหมดตาม Card ID
     * (ดูว่าการ์ดใบนี้มีอยู่ที่ไอดีใดบ้างในร้าน)
     */
    List<CardInventory> findByCardId(Long cardId);

    /**
     * ค้นหารายการการ์ดทั้งหมดที่จัดเก็บอยู่ในไอดีเกมที่ระบุ (GameAccount ID)
     * ใช้สำหรับ REST API: GET /api/v1/accounts/{id}/cards
     */
    List<CardInventory> findByGameAccountId(Long gameAccountId);

    /**
     * ค้นหารายการสต็อกของการ์ดใบใดใบหนึ่งในไอดีเกมที่ระบุ
     */
    Optional<CardInventory> findByCardIdAndGameAccountId(Long cardId, Long gameAccountId);

    /**
     * ค้นหาสต็อกที่ตรงทั้ง Card ID, GameAccount ID และ สภาพการ์ด (Condition)
     * ใช้ในฟังก์ชันบันทึกการเปิดซอง (+ Add Pull): ถ้ามีอยู่แล้วในสภาพเดียวกัน
     * ให้บวกจำนวนเพิ่ม
     */
    Optional<CardInventory> findByCardIdAndGameAccountIdAndCondition(Long cardId,
            Long gameAccountId,
            CardCondition condition);

    /**
     * ค้นหารายการสต็อกการ์ดที่มีจำนวนมากกว่าค่าที่กำหนด (เช่น quantity > 0)
     */
    List<CardInventory> findByCardIdAndQuantityGreaterThan(Long cardId, int minQuantity);

    /**
     * คำนวณจำนวนสต็อกคงเหลือรวมของการ์ดใบนั้นจากทุกไอดีเกมของร้าน
     * นำไปใช้ใน LowStockObserver (GoF Observer) เพื่อตรวจว่าสต็อกรวมลดลงจนเหลือ <=
     * 2 ใบหรือไม่
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CardInventory ci WHERE ci.card.id = :cardId")
    int sumQuantityByCardId(@Param("cardId") Long cardId);

    /**
     * คำนวณมูลค่าราคาทุนรวมของคลังการ์ดทั้งหมดในร้าน (Total Buy-in Vault Value)
     */
    @Query("SELECT COALESCE(SUM(ci.quantity * ci.buyInPrice), 0) FROM CardInventory ci")
    BigDecimal calculateTotalVaultCostValue();

    /**
     * คำนวณมูลค่าราคาขายรวมของคลังการ์ดทั้งหมดในร้าน (Total Selling Vault Value)
     */
    @Query("SELECT COALESCE(SUM(ci.quantity * ci.sellingPrice), 0) FROM CardInventory ci")
    BigDecimal calculateTotalVaultSellingValue();
}
