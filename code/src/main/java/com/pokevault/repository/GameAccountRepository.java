package com.pokevault.repository;

import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.enums.AccountTradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository สำหรับจัดการข้อมูลไอดีเกมของร้านค้า (GameAccount
 * Vault)
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    /**
     * ค้นหาไอดีเกมด้วย Account Code (เช่น "VAULT-01")
     */
    Optional<GameAccount> findByAccountCode(String accountCode);

    /**
     * ตรวจสอบว่ามี Account Code นี้ในระบบแล้วหรือไม่ (ใช้ตอน Validate
     * ลงทะเบียนไอดีใหม่)
     */
    boolean existsByAccountCode(String accountCode);

    /**
     * ค้นหาไอดีเกมตามสถานะความพร้อมในการเทรด (เช่น READY, BUSY_TRADING, COOLDOWN)
     */
    List<GameAccount> findByTradeStatus(AccountTradeStatus tradeStatus);

    /**
     * ค้นหาไอดีเกมตามสถานะ พร้อมเรียงลำดับจากเก่าไปใหม่ เพื่อหมุนเวียนคิวการใช้งาน
     * (Round-robin friendly)
     */
    List<GameAccount> findByTradeStatusOrderByIdAsc(AccountTradeStatus tradeStatus);

    /**
     * ค้นหาไอดีเกมตามชื่อ Trainer In-Game Name (ค้นหาแบบ Case-Insensitive)
     */
    List<GameAccount> findByInGameNameContainingIgnoreCase(String inGameName);

    /**
     * ค้นหาไอดีเกมด้วย Friend ID ในเกม
     */
    Optional<GameAccount> findByFriendId(String friendId);

    /**
     * ตรวจสอบว่า Friend ID นี้ถูกลงทะเบียนไว้ในคลังร้านแล้วหรือไม่
     */
    boolean existsByFriendId(String friendId);

    /**
     * นับจำนวนไอดีเกมตามสถานะ (ใช้สำหรับ Dashboard หรือสรุปสุขภาพคลัง)
     */
    long countByTradeStatus(AccountTradeStatus tradeStatus);

    /**
     * Custom JPQL Query: ค้นหาไอดีเกมที่ถือการ์ดใบที่ระบุ (มีสต็อก > 0)
     * และมีสถานะพร้อมเทรด (READY)
     * นำไปใช้ในระบบ Trade Matching Engine (คนที่ 4)
     * เพื่อแนะนำไอดีที่สามารถส่งมอบการ์ดให้ลูกค้าได้
     */
    @Query("SELECT DISTINCT ga FROM GameAccount ga " +
            "JOIN CardInventory ci ON ci.gameAccount = ga " +
            "WHERE ci.card.id = :cardId " +
            "AND ci.quantity > 0 " +
            "AND ga.tradeStatus = :status")
    List<GameAccount> findAvailableAccountsByCardId(@Param("cardId") Long cardId,
            @Param("status") AccountTradeStatus status);
}
