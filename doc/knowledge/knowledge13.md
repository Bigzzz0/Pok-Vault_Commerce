# 📘 Knowledge 13: กลยุทธ์การเขียน Unit Tests ด้วย JUnit 5 และ Mockito ใน GameAccountService

> **Commit Reference**: `test: add unit test for GameAccountService pack pull recording`  
> **ผู้รับผิดชอบ**: นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2: Game Account Vault & Inventory Manager  
> **โมดูล**: Test Layer (`src/test/java/com/pokevault/modules/vault/service`)  
> **สถานะ**: Implemented & Verified (9/9 Tests Passing)

---

## 1. 🎯 วัตถุประสงค์และภาพรวม (Overview & Testing Strategy)

ในกระบวนการพัฒนาซอฟต์แวร์ระดับมืออาชีพ ชั้น **Business Service Layer** เป็นจุดที่รวมตรรกะทางธุรกิจที่สำคัญที่สุด การเขียน **Unit Test** จึงมีเป้าหมายเพื่อ:
1. **ทดสอบ Business Invariants โดยตรง**: รับประกันว่าพฤติกรรมการเปิดซองการ์ด (+ Add Pull), การทบยอดสต็อก และการป้องกันรหัสไอดีซ้ำ จะทำงานได้อย่างถูกต้องแม่นยำ
2. **การแยกส่วนด้วย Mocking (Isolation with Mockito)**:
   - ใช้ `@Mock` จำลอง `GameAccountRepository`, `CardInventoryRepository`, และ `CardRepository`
   - ทำให้เทสต์ทำงานได้อย่างรวดเร็วในหน่วยมิลลิวินาที (In-Memory Execution) โดยไม่ต้องเปิดการเชื่อมต่อฐานข้อมูลจริง และไม่มีผลข้างเคียง (Side Effects) กับข้อมูลอื่น
3. **การทดสอบพฤติกรรมทั้ง Happy Paths และ Edge Cases (Defensive Tests)**:
   - ตรวจสอบทั้งกรณีการทำงานปกติ และกรณีที่ข้อมูลผิดพลาด (เช่น ไม่พบไอดี, ไม่พบการ์ด, หรือรหัสซ้ำ) ว่าระบบจะโยน Exception ที่ถูกต้องตามสเปกหรือไม่

---

## 2. 🧩 โครงสร้าง Test Cases (Test Matrix)

คลาสทดสอบ: `GameAccountServiceTest.java` ประกอบด้วย **9 Test Cases** แบ่งเป็น 2 กลุ่มหลัก:

### 2.1 กลุ่มการทดสอบการเปิดซองการ์ด (`AddPulledCardTests`)
| ลำดับ | Test Method | กรณีที่ทดสอบ | พฤติกรรมที่คาดหวัง |
| :---: | :--- | :--- | :--- |
| 1 | `addPulledCard_WhenExistingInventory_ShouldIncrementStock` | เปิดได้การ์ดที่มีอยู่ในไอดีเกมแล้ว | ทบยอดสต็อกเดิมเพิ่มขึ้น (`previousQty + addedQty`) และอัปเดตราคาขาย |
| 2 | `addPulledCard_WhenNewInventory_ShouldCreateNewRecord` | เปิดได้การ์ดใหม่ที่ยังไม่เคยมีในไอดี | สร้าง `CardInventory` ใหม่ และผูกความสัมพันธ์กับไอดีเกมถูกต้อง |
| 3 | `addPulledCard_WhenAccountNotFound_ShouldThrowException` | ระบุรหัสไอดีเกมที่ไม่พบในระบบ | โยน `ResourceNotFoundException("GameAccount")` |
| 4 | `addPulledCard_WhenCardNotFound_ShouldThrowException` | ระบุรหัสการ์ดที่ไม่พบในระบบ | โยน `ResourceNotFoundException("Card")` |

### 2.2 กลุ่มการทดสอบการบริหารจัดการไอดี (`AccountManagementTests`)
| ลำดับ | Test Method | กรณีที่ทดสอบ | พฤติกรรมที่คาดหวัง |
| :---: | :--- | :--- | :--- |
| 5 | `createAccount_Success` | ลงทะเบียนไอดีเกมใหม่สำเร็จ | บันทึกข้อมูลและคืนค่า `GameAccountResponse` ถูกต้อง |
| 6 | `createAccount_WhenDuplicateCode_ShouldThrowException` | ระบุ `accountCode` ที่มีอยู่แล้วในระบบ | โยน `IllegalArgumentException("Account code already exists")` |
| 7 | `getAccountById_Success` | ค้นหาไอดีเกมตามรหัส ID | คืนค่าข้อมูลไอดีพร้อมผลรวมสต็อกการ์ดทั้งหมด (`totalCards`) |
| 8 | `getAccountCards_Success` | ดึงรายการการ์ดทั้งหมดในไอดี | คืนค่า `List<AccountCardResponse>` ตรงตามจำนวนการ์ดที่ถือครอง |
| 9 | `updateTradeStatus_Success` | ปรับสถานะความพร้อมในการเทรด | อัปเดตสถานะใหม่ (เช่น `BUSY_TRADING`) และบันทึกลงระบบ |

---

## 3. 💻 เทคนิคสำคัญที่นำมาใช้ใน Unit Tests (Best Practices)

1. **`@Nested` Hierarchical Test Structure**:
   - จัดหมวดหมู่เทสต์เป็นกลุ่มย่อย เพิ่มความชัดเจนและอ่านง่ายใน Test Runner
2. **`ArgumentCaptor` Verification**:
   - ใช้ `ArgumentCaptor<CardInventory>` เพื่อดักจับอ็อบเจกต์ก่อนที่จะถูกส่งให้ `save()` ทำให้สามารถตรวจสอบค่า `quantity`, `storageSlot` และความสัมพันธ์ระหว่าง Entity ได้อย่างละเอียด
3. **AssertJ Fluent Assertions**:
   - ใช้ `assertThat(...)` และ `assertThatThrownBy(...)` ช่วยให้อ่านเงื่อนไขการตรวจสอบเสมือนภาษาพูดธรรมชาติ เช่น:
   ```java
   assertThat(response.getQuantity()).isEqualTo(5);
   assertThatThrownBy(() -> gameAccountService.createAccount(request))
           .isInstanceOf(IllegalArgumentException.class)
           .hasMessageContaining("Account code already exists");
   ```

---

## 4. 🧪 ผลการรันเทสต์ (Test Execution Results)

- **GameAccountServiceTest**: **9 tests ผ่าน 100% (0 Failures, 0 Errors, 0 Skipped)**
- **Total Project Test Suite**: **40 tests ผ่าน 100% (BUILD SUCCESS)**
  - `GameAccountServiceTest`: 9 ผ่าน
  - `CardInventoryTest`: 5 ผ่าน
  - `CardServiceTest`: 13 ผ่าน
  - `DiscountStrategyTest`: 6 ผ่าน
  - `OrderServiceTest`: 7 ผ่าน
