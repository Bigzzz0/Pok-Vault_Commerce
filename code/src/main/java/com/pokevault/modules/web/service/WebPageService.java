package com.pokevault.modules.web.service;

import com.pokevault.modules.web.dto.view.AccountsPage;
import com.pokevault.modules.web.dto.view.CardGalleryPage;
import com.pokevault.modules.web.dto.view.DashboardPage;
import com.pokevault.modules.web.dto.view.InventoryPage;
import com.pokevault.modules.web.dto.view.OrderView;

import java.util.List;
import java.util.Optional;

/**
 * ข้อมูลของหน้าเว็บ Thymeleaf (WebViewController) — controller เรียกผ่าน interface นี้เท่านั้น
 * ไม่เรียก Repository ตรง ส่วนกฎของร้าน (แสดงเฉพาะการ์ดที่มีสต็อก, เลือกล็อตที่ขาย, กรองลูกค้า) อยู่ใน implementation
 */
public interface WebPageService {

    /** id ของผู้ใช้ที่ล็อกอิน ใช้กับฟอร์มสั่งซื้อของลูกค้า; ว่างถ้าไม่พบ */
    Optional<Long> findUserId(String username);

    DashboardPage getDashboard();

    /** ค่าตัวกรองที่ไม่ตรงกับ enum จะถูกเมิน (เท่ากับไม่กรองแถวนั้น) */
    CardGalleryPage getCardGallery(String element, String rarity, String type, String search);

    InventoryPage getInventoryPage();

    AccountsPage getAccountsPage();

    /** ออเดอร์ทั้งหมดของร้าน ใหม่สุดก่อน (หน้า /orders ของพนักงาน) */
    List<OrderView> getAllOrders();

    /** ออเดอร์ของผู้ใช้คนนี้เท่านั้น ใหม่สุดก่อน (หน้า /my-orders) */
    List<OrderView> getOrdersOfUser(String username);
}
