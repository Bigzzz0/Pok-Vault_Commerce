# 📘 Knowledge 03: GameAccountRepository และ Custom Query Methods

> **Commit Reference**: `feat: create GameAccountRepository with custom query methods`  
> **ผู้รับผิดชอบ**: นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2: Game Account Vault & Inventory Manager  
> **โมดูล**: Persistence Layer (`com.pokevault.repository`)  
> **สถานะ**: Implemented

---

## 1. 🎯 วัตถุประสงค์และภาพรวม (Overview & Objective)

ในสถาปัตยกรรมของ **PokéVault Commerce** ไอดีเกมของร้านค้า (`GameAccount`) เป็นทรัพยากรหลักที่ใช้ในการเปิดซองการ์ด, เก็บสต็อกการ์ดในคลัง (`CardInventory`), และส่งมอบการ์ดให้แก่ลูกค้าผ่านกระบวนการเทรดในเกม Pokémon TCG Pocket

`GameAccountRepository` ถูกสร้างขึ้นเพื่อเป็น **Data Access Layer** สำหรับจัดการข้อมูล `GameAccount` ผ่าน Spring Data JPA โดยเน้น:
1. การค้นหาและตรวจสอบความซ้ำซ้อนของรหัสบัญชี (`accountCode`) และรหัสเพื่อน (`friendId`) เพื่อรักษาความถูกต้องของข้อมูลตามกฎฐานข้อมูล
2. การค้นหาไอดีเกมตามสถานะความพร้อมในการเทรด (`AccountTradeStatus`) เพื่อจัดสรรคิวงานและมอนิเตอร์สุขภาพของคลัง
3. การประยุกต์ใช้ **Custom JPQL Query** เพื่อค้นหาไอดีเกมที่ถือการ์ดใบที่ลูกค้าสั่งซื้อและมีสถานะพร้อมเทรด (`READY`) สำหรับส่งมอบงานให้แก่ **Trade Matching Engine (คนที่ 4)**

---

## 2. 🧩 โครงสร้างอินเทอร์เฟซ (Interface Structure)

ไฟล์: `src/main/java/com/pokevault/repository/GameAccountRepository.java`

```java
package com.pokevault.repository;

import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.enums.AccountTradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    Optional<GameAccount> findByAccountCode(String accountCode);

    boolean existsByAccountCode(String accountCode);

    List<GameAccount> findByTradeStatus(AccountTradeStatus tradeStatus);

    List<GameAccount> findByTradeStatusOrderByIdAsc(AccountTradeStatus tradeStatus);

    List<GameAccount> findByInGameNameContainingIgnoreCase(String inGameName);

    Optional<GameAccount> findByFriendId(String friendId);

    boolean existsByFriendId(String friendId);

    long countByTradeStatus(AccountTradeStatus tradeStatus);

    @Query("SELECT DISTINCT ga FROM GameAccount ga " +
           "JOIN CardInventory ci ON ci.gameAccount = ga " +
           "WHERE ci.card.id = :cardId " +
           "AND ci.quantity > 0 " +
           "AND ga.tradeStatus = :status")
    List<GameAccount> findAvailableAccountsByCardId(@Param("cardId") Long cardId,
                                                   @Param("status") AccountTradeStatus status);
}
```

---

## 3. 🔍 เจาะลึก Query Methods และการนำไปใช้ (Deep-Dive Analysis)

### 3.1 Derived Query Methods สำหรับตรวจสอบและป้องกันข้อมูลซ้ำ
- **`findByAccountCode(String accountCode)`**: คืนค่า `Optional<GameAccount>` สำหรับดึงข้อมูลไอดีร้านด้วยรหัสระบุเฉพาะ เช่น `"VAULT-01"`
- **`existsByAccountCode(String accountCode)`**: ใช้ตรวจสอบก่อนบันทึกบัญชีใหม่ใน `GameAccountService.registerAccount()` เพื่อป้องกัน `DataIntegrityViolationException`
- **`findByFriendId(String friendId)` & `existsByFriendId(String friendId)`**: ใช้ตรวจสอบว่า Friend ID 16 หลักของเกมนี้มีอยู่ในระบบคลังแล้วหรือไม่

### 3.2 การจัดการสถานะและการหมุนเวียนคิวไอดีเทรด
- **`findByTradeStatus(AccountTradeStatus tradeStatus)`**: ดึงไอดีเกมทั้งหมดตามสถานะ เช่น ดึงไอดีที่ติดสถานะ `COOLDOWN` หรือ `BUSY_TRADING`
- **`findByTradeStatusOrderByIdAsc(AccountTradeStatus tradeStatus)`**: ค้นหาไอดีที่พร้อมเทรด (`READY`) โดยเรียงลำดับจาก ID น้อยไปมาก เพื่อสนับสนุนการจัดคิวแบบ FIFO / Round-robin ช่วยกระจายการใช้งานไอดี ไม่ให้ไอดีเดิมชนขีดจำกัดการเทรดประจำวัน (Daily Trade Limit)
- **`countByTradeStatus(AccountTradeStatus tradeStatus)`**: สำหรับหน้า Dashboard สรุปจำนวนไอดีที่พร้อมใช้งาน, กำลังเทรด, หรือถูกระงับชั่วคราว

### 3.3 Custom JPQL Query ข้ามเอนทิตี (`findAvailableAccountsByCardId`)
```sql
SELECT DISTINCT ga FROM GameAccount ga
JOIN CardInventory ci ON ci.gameAccount = ga
WHERE ci.card.id = :cardId
  AND ci.quantity > 0
  AND ga.tradeStatus = :status
```
- **กลไกการทำงาน**: ทำการ Join ระหว่าง `GameAccount` กับ `CardInventory` โดยอ้างอิง `ci.card.id` ที่ตรงกับรหัสการ์ดที่ต้องการ
- **เงื่อนไขสำคัญ**:
  1. `ci.quantity > 0`: ไอดีนั้นต้องมีสต็อกการ์ดคงเหลือจริงมากกว่า 0 ใบ
  2. `ga.tradeStatus = :status`: สถานะของไอดีเกมต้องเป็น `READY`
  3. `DISTINCT`: ป้องกันผลลัพธ์ไอดีซ้ำในกรณีที่ไอดีนั้นมีการบันทึกสต็อกสภาพการ์ดแยกหลายแถว
- **การนำไปใช้**: สนับสนุน Sequence Diagram ขั้นตอนที่ 5 ของโมดูล `TradeMatchingServiceImpl` (คนที่ 4) เพื่อดึง Candidate Accounts มารอให้อัลกอริทึม `autoMatchBestAccount` มอบหมายงาน

---

## 4. 🏛️ การสอดคล้องกับหลักการออกแบบซอฟต์แวร์ (SOLID Alignment)

| หลักการ | การประยุกต์ใช้ใน `GameAccountRepository` |
| :--- | :--- |
| **SRP (Single Responsibility)** | รับผิดชอบเฉพาะการเข้าถึงและสอบถามข้อมูลระดับ Persistence ของ `GameAccount` โดยไม่ปะปน Business Logic |
| **ISP (Interface Segregation)** | กำหนดเฉพาะ Method signatures ที่จำเป็นสำหรับการทำงานของระบบ Vault และ Trade Matching |
| **DIP (Dependency Inversion)** | เลเยอร์ Service (`GameAccountServiceImpl`, `TradeMatchingServiceImpl`) พึ่งพา Interface นี้ผ่าน Spring IoC/DI โดยไม่ผูกติดกับการเชื่อมต่อฐานข้อมูลโดยตรง |

---

## 5. 🧪 ผลการทดสอบ (Verification)
- Compile ผ่านด้วย `./mvnw test-compile` (BUILD SUCCESS)
- Automated Unit Tests ผ่านครบ 100% ด้วย `./mvnw test` (18/18 tests passed)
