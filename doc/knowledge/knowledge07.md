# 📘 Knowledge 07: GameAccountServiceImpl และระบบ Pack Pull Logging

> **Commit Reference**: `feat: implement GameAccountServiceImpl with pack pull logging`  
> **ผู้รับผิดชอบ**: นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2: Game Account Vault & Inventory Manager  
> **โมดูล**: Application / Service Layer (`com.pokevault.modules.vault.service`)  
> **สถานะ**: Implemented

---

## 1. 🎯 วัตถุประสงค์และภาพรวม (Overview & Objective)

`GameAccountServiceImpl` เป็นหัวใจหลักในการประมวลผลคำสั่งทางธุรกิจ (Core Business Orchestration) ของโมดูล Vault ในระบบ **PokéVault Commerce** ทำหน้าที่ควบคุม:
1. **Account Registry & Duplicate Guard**: ลงทะเบียนไอดีเกมของร้าน พร้อมป้องกันการลงทะเบียนรหัสบัญชีซ้ำ (`accountCode`)
2. **Pack Pull Recording Engine (+ Add Pull)**: บันทึกการเปิดซองการ์ดสุ่มในเกม Pokémon TCG Pocket เข้าสู่สต็อกของร้านอย่างถูกต้องและปลอดภัย
3. **Structured Audit Logging**: ติดตั้ง Slf4j Logger สำหรับตรวจสอบย้อนกลับ (Traceability) เมื่อมีการเพิ่มไอดี, เปิดซองสุ่มการ์ด, หรือเปลี่ยนสถานะการเทรด
4. **Transaction Boundary Management**: ควบคุมขอบเขตธุรกรรมฐานข้อมูลด้วย Spring `@Transactional` เพื่อรักษาหลัก ACID (Atomicity, Consistency, Isolation, Durability)

---

## 2. 🧩 โครงสร้างการทำงานและ Business Logic ภายใน

### 2.1 กลไกการบันทึกการเปิดซองการ์ด (`addPulledCard`)
ตาม **Sequence Diagram** ขั้นตอนที่ 7–14 กระบวนการบันทึกการ์ดที่เปิดได้ทำงานตามลำดับดังนี้:
1. ตรวจสอบความมีอยู่ของไอดีร้าน (`findById(accountId)`) หากไม่พบจะโยน `ResourceNotFoundException`
2. ตรวจสอบความมีอยู่ของการ์ดในแคตตาล็อก (`findById(cardId)`) หากไม่พบจะโยน `ResourceNotFoundException`
3. ค้นหาแถวสต็อกเดิมในคลังด้วย `cardInventoryRepository.findByCardIdAndGameAccountIdAndCondition(...)`:
   - **กรณีพบแถวเดิม**: เรียกใช้ `inventory.restoreStock(quantityToAdd)` เพื่อเพิ่มจำนวนสต็อกเดิม และอัปเดตราคาขาย (หากระบุมาใหม่) โดยไม่สร้างแถวซ้ำซ้อน
   - **กรณีเป็นการ์ดใบใหม่ในไอดีนั้น**: สร้างเอนทิตี `CardInventory` ใหม่ และผูก Foreign Key เชื่อมไปยัง `Card` และ `GameAccount`
4. บันทึกผลลัพธ์ลงฐานข้อมูลและแปลงเป็น `AccountCardResponse` DTO ส่งกลับ Controller

```java
// Logic ตัดสินใจระหว่าง Update กับ Insert แถวสต็อก
Optional<CardInventory> existingInventory = cardInventoryRepository
        .findByCardIdAndGameAccountIdAndCondition(card.getId(), account.getId(), condition);

CardInventory inventoryToSave;
if (existingInventory.isPresent()) {
    inventoryToSave = existingInventory.get();
    inventoryToSave.restoreStock(quantityToAdd);
    // อัปเดตราคาและพ่น Log การอัปเดตสต็อกเดิม
} else {
    inventoryToSave = CardInventory.builder()
            .card(card)
            .gameAccount(account)
            .condition(condition)
            .quantity(quantityToAdd)
            ...
            .build();
    // พ่น Log การสร้างสต็อกใหม่
}
```

### 2.2 โครงสร้าง Logging ด้วย Slf4j
คลาสใช้ `@Slf4j` จาก Lombok เพื่อบันทึกพฤติกรรมสำคัญในระดับ `INFO`:
- **ลงทะเบียนไอดีใหม่**:
  `log.info("Successfully registered game account [ID: {}]: code={}", saved.getId(), saved.getAccountCode());`
- **บันทึกการเปิดซอง**:
  `log.info("Updated existing card inventory [ID: {}] for account [{}]: card='{}' ({}), condition={}, added={}, newQty={}", ...)`
- **อัปเดตสถานะการเทรด**:
  `log.info("Updated trade status for account [{}]: newStatus={}", updated.getAccountCode(), status);`

---

## 3. 🛡️ Transactional Management & Fail-Fast

- คลาสถูกกำหนดด้วย `@Transactional(readOnly = true)` เป็นค่าเริ่มต้นในระดับคลาส เพื่อเพิ่มประสิทธิภาพในการ Query ข้อมูล
- เมธอดที่แก้ไขข้อมูล (`createAccount`, `addPulledCard`, `updateTradeStatus`) จะระบุ `@Transactional` กำกับไว้ เพื่อให้ Spring Rollback ข้อมูลทันทีหากเกิดข้อผิดพลาดระหว่างทาง
- เมธอด `createAccount` มี Guard ตรวจสอบ `existsByAccountCode` ก่อนทำการบันทึก ป้องกัน `DataIntegrityViolationException`

---

## 4. 🏛️ การสอดคล้องกับหลักการออกแบบซอฟต์แวร์ (SOLID Alignment)

| หลักการ | การประยุกต์ใช้ใน `GameAccountServiceImpl` |
| :--- | :--- |
| **SRP (Single Responsibility)** | รับผิดชอบเฉพาะตรรกะทางธุรกิจของไอดีเกมและสต็อกการ์ด โดยมอบหมายหน้าที่เก็บข้อมูลให้ Repositories |
| **DIP (Dependency Inversion)** | ฉีด Dependencies ทั้งหมดผ่าน Constructor Injection ด้วย `@RequiredArgsConstructor` อ้างอิงผ่าน Interface |
| **Information Hiding** | คืนค่าผลลัพธ์กลับไปยังภายนอกเป็น DTO เสมอ โดยไม่ปล่อย JPA Entity หลุดออกไปนอก Service Layer |

---

## 5. 🧪 ผลการทดสอบ (Verification)
- Compile ผ่านด้วย `./mvnw test-compile` (BUILD SUCCESS)
- Automated Unit Tests ทั้งหมดในระบบผ่าน 100%: **31/31 tests passed (0 failures, 0 errors)**
