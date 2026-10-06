# 📘 Knowledge 04: CardInventoryRepository และระบบค้นหาสต็อกการ์ด

> **Commit Reference**: `feat: create CardInventoryRepository with card and account lookups`  
> **ผู้รับผิดชอบ**: นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2: Game Account Vault & Inventory Manager  
> **โมดูล**: Persistence Layer (`com.pokevault.repository`)  
> **สถานะ**: Implemented

---

## 1. 🎯 วัตถุประสงค์และภาพรวม (Overview & Objective)

ในระบบ **PokéVault Commerce** เอนทิตี `CardInventory` ทำหน้าที่เป็นจุดเชื่อมต่อกลาง (Junction/Inventory Entity) ระหว่าง:
- **`Card` (จากโมดูล Catalog ของคนที่ 1)**: ข้อมูลการ์ดโปเกมอนแต่ละแบบ (เช่น Charizard ex, Pikachu ex)
- **`GameAccount` (จากโมดูล Vault ของคนที่ 2)**: ไอดีเกมของร้านที่เป็นผู้ถือครองการ์ดใบนั้นในเกม Pokémon TCG Pocket

`CardInventoryRepository` ถูกสร้างขึ้นเพื่อเป็น **Data Access Layer** สำหรับเข้าถึงและจัดการสต็อกการ์ด โดยมีหน้าที่หลัก 4 ด้าน:
1. **Account Vault Cards Lookup**: ดึงรายการการ์ดทั้งหมดที่จัดเก็บอยู่ในไอดีเกมแต่ละไอดี สำหรับหน้าเว็บและ REST API (`GET /api/v1/accounts/{id}/cards`)
2. **Pack Pull Exact Matching**: ค้นหาแถวสต็อกที่ตรงกันทั้งการ์ด, ไอดีเกม และสภาพการ์ด เพื่อรองรับฟังก์ชันบันทึกการเปิดซอง (`+ Add Pull`)
3. **Aggregate Stock Calculation**: คำนวณผลรวมสต็อกคงเหลือของการ์ดแต่ละใบจากทุกไอดีเกมของร้าน เพื่อรองรับ **`LowStockObserver` (GoF Observer Pattern)**
4. **Vault Valuation Engine**: คำนวณมูลค่ารวมทั้งราคาทุน (Buy-in) และราคาขาย (Selling) ของคลังสินค้าทั้งหมดของร้าน

---

## 2. 🧩 โครงสร้างอินเทอร์เฟซ (Interface Structure)

ไฟล์: `src/main/java/com/pokevault/repository/CardInventoryRepository.java`

```java
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

@Repository
public interface CardInventoryRepository extends JpaRepository<CardInventory, Long> {

    List<CardInventory> findByCardId(Long cardId);

    List<CardInventory> findByGameAccountId(Long gameAccountId);

    Optional<CardInventory> findByCardIdAndGameAccountId(Long cardId, Long gameAccountId);

    Optional<CardInventory> findByCardIdAndGameAccountIdAndCondition(Long cardId,
                                                                    Long gameAccountId,
                                                                    CardCondition condition);

    List<CardInventory> findByCardIdAndQuantityGreaterThan(Long cardId, int minQuantity);

    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CardInventory ci WHERE ci.card.id = :cardId")
    int sumQuantityByCardId(@Param("cardId") Long cardId);

    @Query("SELECT COALESCE(SUM(ci.quantity * ci.buyInPrice), 0) FROM CardInventory ci")
    BigDecimal calculateTotalVaultCostValue();

    @Query("SELECT COALESCE(SUM(ci.quantity * ci.sellingPrice), 0) FROM CardInventory ci")
    BigDecimal calculateTotalVaultSellingValue();
}
```

---

## 3. 🔍 เจาะลึก Query Methods และการทำงานร่วมกับระบบอื่น (Deep-Dive Analysis)

### 3.1 การค้นหาการ์ดแยกตามไอดีเกม (`findByGameAccountId`)
- **การนำไปใช้**: ตอบรับ Requirement ของ API `GET /api/v1/accounts/{id}/cards` (Roadmap ข้อ 12)
- **กลไก**: ดึงข้อมูลคลังการ์ดที่ผูกกับ `game_account_id` ที่กำหนด ช่วยให้ผู้ดูแลระบบมองเห็นได้ชัดเจนว่าไอดีนี้เก็บการ์ดอะไรอยู่บ้าง

### 3.2 การค้นหาแบบแม่นยำสำหรับการเปิดซอง (`findByCardIdAndGameAccountIdAndCondition`)
- **ปัญหาเดิม**: หากผู้ดูแลเปิดซองได้การ์ดใบเดิม สภาพเดิม (เช่น Charizard ex สภาพ MINT ในไอดี VAULT-01) หากสร้างแถวใหม่จะทำให้ข้อมูลกระจายตัวและนับสต็อกลำบาก
- **วิธีแก้**: เมธอดนี้จะค้นหาแถวเดิมก่อน หากพบ จะทำการบวก `quantity += count` แทนการสร้างแถวใหม่ แต่หากไม่พบ จึงจะทำการสร้างแถว Inventory ใหม่

### 3.3 การคำนวณผลรวมสต็อกสำหรับการแจ้งเตือน (`sumQuantityByCardId`)
```sql
SELECT COALESCE(SUM(ci.quantity), 0) 
FROM CardInventory ci 
WHERE ci.card.id = :cardId
```
- **การนำไปใช้**: เป็นหัวใจสำคัญของ **`LowStockObserver` (GoF Observer Pattern ในข้อ 9 และ 10)**
- เมื่อลูกค้าสั่งซื้อการ์ดผ่าน `OrderPlacedEvent` ตัว Observer จะนำ `cardId` มาเรียกเมธอดนี้เพื่อคำนวณจำนวนคงเหลือรวมของร้าน
- หากผลรวม $\le 2$ ใบ ระบบจะทำการส่งสัญญาณเตือน (Log Warning) ทันที
- ใช้ฟังก์ชัน `COALESCE(..., 0)` เพื่อป้องกันปัญหา `NullPointerException` ในกรณีที่ไม่มีข้อมูลหรือสต็อกหมดเกลี้ยง

### 3.4 การประเมินมูลค่าทรัพย์สินคลังการ์ด (`Valuation Queries`)
- **`calculateTotalVaultCostValue()`**: รวมผลคูณของ `quantity * buyInPrice` ของการ์ดทุกใบในคลัง เพื่อดูเงินลงทุนรวมของร้าน
- **`calculateTotalVaultSellingValue()`**: รวมผลคูณของ `quantity * sellingPrice` ของการ์ดทุกใบ เพื่อประเมินมูลค่าตามราคาตลาด

---

## 4. 🏛️ การสอดคล้องกับหลักการออกแบบซอฟต์แวร์ (SOLID Alignment)

| หลักการ | การประยุกต์ใช้ใน `CardInventoryRepository` |
| :--- | :--- |
| **SRP (Single Responsibility)** | รับผิดชอบเฉพาะการสอบถามและจัดเก็บข้อมูลสถานะสต็อกของ `CardInventory` เท่านั้น |
| **ISP (Interface Segregation)** | เมธอดถูกคัดเลือกเฉพาะที่จำเป็นต่อการจัดการคลัง, การเปิดซอง, และการแจ้งเตือนสต็อกต่ำ |
| **DIP (Dependency Inversion)** | คลาส `OrderServiceImpl`, `TradeMatchingServiceImpl`, และ `LowStockObserver` พึ่งพาผ่าน Interface นี้ผ่าน Spring Dependency Injection โดยตรง |

---

## 5. 🧪 ผลการทดสอบ (Verification)
- Compile ผ่านด้วย `./mvnw test-compile` (BUILD SUCCESS)
- Automated Unit Tests ผ่านครบ 100% ด้วย `./mvnw test` (18/18 tests passed)
