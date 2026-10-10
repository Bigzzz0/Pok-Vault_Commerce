# 📘 Knowledge 06: GameAccountService Interface และ Service Contract ของโมดูล Vault

> **Commit Reference**: `feat: implement GameAccountService interface`  
> **ผู้รับผิดชอบ**: นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2: Game Account Vault & Inventory Manager  
> **โมดูล**: Application / Service Layer (`com.pokevault.modules.vault.service`)  
> **สถานะ**: Implemented

---

## 1. 🎯 วัตถุประสงค์และภาพรวม (Overview & Objective)

ในสถาปัตยกรรมแบบ Clean Layered Architecture ของ Spring Boot เลเยอร์ **Service Interface** ทำหน้าที่เป็น **Service Contract (ข้อตกลงการให้บริการทางธุรกิจ)** ที่คั่นกลางระหว่าง **Controller Layer** และ **Data Persistence Layer**

`GameAccountService` ถูกออกแบบขึ้นตามผัง **Class Diagram** (บรรทัดที่ 103–110) และ **Sequence Diagram** ของระบบ เพื่อเป็นจุดศูนย์กลางในการให้บริการฟังก์ชันคลังไอดีเกมและสต็อกการ์ด โดยแยกพฤติกรรมออกเป็น 4 มิติหลัก:
1. **Account Lifecycle & Registry**: การลงทะเบียนไอดีเกมร้านค้าใหม่ (`createAccount`), ค้นหาไอดีทั้งหมด (`getAllAccounts`), และดึงข้อมูลรายไอดี (`getAccountById`)
2. **Pack Pull Recording Engine**: บันทึกการเปิดซองการ์ดสุ่ม (`+ Add Pull`) เข้าไอดีเกมที่ระบุ (`addPulledCard`)
3. **Vault Inventory Retrieval**: ดึงรายการการ์ดที่เก็บอยู่ในแต่ละไอดีเกม (`getAccountCards`) สำหรับการแสดงผลหน้าบ้าน
4. **Vault Valuation & State Management**: การอัปเดตสถานะความพร้อมในการเทรด (`updateTradeStatus`) และการคำนวณมูลค่าต้นทุน/ราคาขายรวมของคลังการ์ดทั้งหมดในร้าน

---

## 2. 🧩 โครงสร้างอินเทอร์เฟซ (Interface Specifications)

ไฟล์: `src/main/java/com/pokevault/modules/vault/service/GameAccountService.java`

```java
package com.pokevault.modules.vault.service;

import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.modules.vault.dto.AccountCardResponse;
import com.pokevault.modules.vault.dto.AddPulledCardRequest;
import com.pokevault.modules.vault.dto.GameAccountRequest;
import com.pokevault.modules.vault.dto.GameAccountResponse;

import java.math.BigDecimal;
import java.util.List;

public interface GameAccountService {

    GameAccountResponse createAccount(GameAccountRequest request);

    List<GameAccountResponse> getAllAccounts();

    GameAccountResponse getAccountById(Long id);

    AccountCardResponse addPulledCard(Long id, AddPulledCardRequest request);

    List<AccountCardResponse> getAccountCards(Long id);

    GameAccountResponse updateTradeStatus(Long id, AccountTradeStatus status);

    BigDecimal calculateTotalVaultCostValue();

    BigDecimal calculateTotalVaultSellingValue();
}
```

---

## 3. 🔍 รายละเอียดฟังก์ชันและการทำงานร่วมกันระหว่างโมดูล (Detailed Specifications)

### 3.1 การจัดการข้อมูลและวงจรชีวิตของไอดีเกม
- **`createAccount(GameAccountRequest request)`**: รับข้อมูลที่ผ่าน Bean Validation (`@Valid`) จาก Controller ตรวจสอบรหัสซ้ำ และบันทึกไอดีเกมใหม่
- **`getAllAccounts()`**: ดึงรายการไอดีเกมทั้งหมด พร้อมจำนวนการ์ดรวมในแต่ละไอดี เพื่อแสดงในหน้าตาราง Vault Management
- **`updateTradeStatus(Long id, AccountTradeStatus status)`**: อนุญาตให้แอดมินหรือระบบปรับเปลี่ยนสถานะไอดี (เช่น เปลี่ยนเป็น `COOLDOWN` หรือ `SUSPENDED`)

### 3.2 ระบบบันทึกการเปิดซองการ์ด (`addPulledCard`)
- เป็นหัวใจสำคัญของฟีเจอร์ **"+ Add Pull"** ตาม Sequence Diagram ขั้นตอนที่ 7–14
- เมื่อผู้ดูแลเปิดซองได้การ์ด เมธอดนี้จะค้นหาว่าในไอดีเกมนั้นมีการ์ดใบนี้สภาพเดิมอยู่แล้วหรือไม่
  - ถ้ามี: ทำการเพิ่มจำนวนสต็อก (`quantity += count`)
  - ถ้าไม่มี: ทำการสร้าง `CardInventory` แถวใหม่และผูกความสัมพันธ์เข้ากับ `GameAccount`
- ส่งคืน `AccountCardResponse` ให้ Controller เพื่อตอบกลับ HTTP 201 Created

### 3.3 การประเมินมูลค่าทรัพย์สินคลังการ์ด (`Valuation`)
- **`calculateTotalVaultCostValue()`**: ดึงผลรวมมูลค่าต้นทุนซื้อเข้าทั้งหมดของร้าน เพื่อดูเงินลงทุนสะสม
- **`calculateTotalVaultSellingValue()`**: ดึงผลรวมมูลค่าราคาขายของสต็อกทั้งหมด เพื่อประเมินมูลค่าตามราคาตลาด

---

## 4. 🏛️ การสอดคล้องกับหลักการออกแบบซอฟต์แวร์ (SOLID Alignment)

| หลักการ | การประยุกต์ใช้ใน `GameAccountService` | ประโยชน์ที่ได้รับ |
| :--- | :--- | :--- |
| **DIP (Dependency Inversion)** | `GameAccountApiController` และ `WebViewController` พึ่งพา Interface นี้แทนที่จะผูกติดกับ Concrete Class โดยตรง | ทำให้สามารถเปลี่ยน Implementation หรือเขียน Mock Unit Test ได้อย่างอิสระ |
| **ISP (Interface Segregation)** | กำหนดเฉพาะเมธอดที่เกี่ยวข้องกับโดเมน Vault โดยไม่รวมฟังก์ชันสั่งซื้อหรือจับคู่เทรดของโมดูลอื่นเข้ามาปะปน | Interface มีขนาดกะทัดรัด ชัดเจน ไม่บังคับให้คลาสอื่นต้องพึ่งพาเมธอดที่ไม่จำเป็น |
| **SRP (Single Responsibility)** | รับผิดชอบการประสานงานทางธุรกิจ (Orchestration) ของคลังไอดีและสต็อกการ์ดเท่านั้น | ง่ายต่อการบำรุงรักษาและการขยายระบบในอนาคต |

---

## 5. 🧪 ผลการทดสอบ (Verification)
- Compile ผ่านด้วย `./mvnw test-compile` (BUILD SUCCESS)
- Automated Unit Tests ทั้งหมดในระบบผ่าน 100%: **31/31 tests passed (0 failures, 0 errors)**
