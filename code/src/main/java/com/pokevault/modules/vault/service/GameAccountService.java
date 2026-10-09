package com.pokevault.modules.vault.service;

import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.modules.vault.dto.AccountCardResponse;
import com.pokevault.modules.vault.dto.AddPulledCardRequest;
import com.pokevault.modules.vault.dto.GameAccountRequest;
import com.pokevault.modules.vault.dto.GameAccountResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service Interface สำหรับจัดการไอดีเกมของร้านค้า (Vault) และคลังการ์ด
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
public interface GameAccountService {

    /**
     * ลงทะเบียนไอดีเกมของร้านใหม่เข้าสู่คลัง Vault
     *
     * @param request ข้อมูลไอดีเกมใหม่
     * @return ข้อมูลไอดีเกมที่บันทึกแล้ว
     */
    GameAccountResponse createAccount(GameAccountRequest request);

    /**
     * ดึงรายการไอดีเกมของร้านทั้งหมด พร้อมจำนวนการ์ดในแต่ละไอดี
     *
     * @return รายการไอดีเกมทั้งหมด
     */
    List<GameAccountResponse> getAllAccounts();

    /**
     * ดึงข้อมูลไอดีเกมตามรหัส ID
     *
     * @param id รหัสไอดีเกม
     * @return ข้อมูลไอดีเกม
     */
    GameAccountResponse getAccountById(Long id);

    /**
     * บันทึกการเปิดซองการ์ด (+ Add Pull) บันทึกสต็อกเข้าไอดีเกมที่ระบุ
     *
     * @param id      รหัสไอดีเกมที่เปิดซอง
     * @param request ข้อมูลการ์ดที่เปิดได้
     * @return ข้อมูลการ์ดในคลังที่บันทึกแล้ว
     */
    AccountCardResponse addPulledCard(Long id, AddPulledCardRequest request);

    /**
     * ดึงรายการการ์ดทั้งหมดที่จัดเก็บอยู่ในไอดีเกมที่ระบุ
     *
     * @param id รหัสไอดีเกม
     * @return รายการการ์ดทั้งหมดในไอดีนั้น
     */
    List<AccountCardResponse> getAccountCards(Long id);

    /**
     * อัปเดตสถานะความพร้อมในการเทรดของไอดีเกม (เช่น READY, BUSY_TRADING, COOLDOWN,
     * SUSPENDED)
     *
     * @param id     รหัสไอดีเกม
     * @param status สถานะใหม่
     * @return ข้อมูลไอดีเกมที่อัปเดตแล้ว
     */
    GameAccountResponse updateTradeStatus(Long id, AccountTradeStatus status);

    /**
     * คำนวณมูลค่าราคาทุนรวมของคลังการ์ดทั้งหมดในร้าน (Total Buy-in Vault Value)
     *
     * @return มูลค่าต้นทุนรวม
     */
    BigDecimal calculateTotalVaultCostValue();

    /**
     * คำนวณมูลค่าราคาขายรวมของคลังการ์ดทั้งหมดในร้าน (Total Selling Vault Value)
     */
    BigDecimal calculateTotalVaultSellingValue();

    /**
     * อัปเดตข้อมูลไอดีเกมของร้านค้า (Full Update)
     *
     * @param id      รหัสไอดีเกม
     * @param request ข้อมูลใหม่ที่ต้องการอัปเดต
     * @return ข้อมูลไอดีเกมที่อัปเดตแล้ว
     */
    GameAccountResponse updateAccount(Long id, GameAccountRequest request);

    /**
     * ลบไอดีเกมของร้านค้าออกจากระบบ
     * เงื่อนไข: บัญชีต้องว่างเปล่า (ไม่มี CardInventory) และไม่ถูกผูกกับออเดอร์ใดๆ
     * (ไม่มี OrderItem)
     *
     * @param id รหัสไอดีเกม
     */
    void deleteAccount(Long id);

}
