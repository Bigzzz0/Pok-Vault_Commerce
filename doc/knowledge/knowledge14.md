# 📘 Knowledge 14: การทดสอบ GoF Observer Pattern และ Event-Driven Architecture ใน LowStockObserver

> **Commit Reference**: `test: add unit test for LowStockObserver event handling`  
> **ผู้รับผิดชอบ**: นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2: Game Account Vault & Inventory Manager  
> **โมดูล**: Test Layer (`src/test/java/com/pokevault/modules/vault/observer`)  
> **สถานะ**: Implemented & Verified (7/7 Tests Passing)

---

## 1. 🎯 วัตถุประสงค์และภาพรวม (Overview & Testing Objectives)

ในระบบ **PokéVault Commerce** การสื่อสารระหว่างโมดูล Order (คนที่ 3) และโมดูล Vault (คนที่ 2) ถูกออกแบบโดยใช้ **GoF Observer Pattern (Event-Driven Architecture)** ผ่าน Spring `@EventListener`:
- **ความสำคัญของการทำ Unit Test ในส่วนนี้**:
  1. **รับประกันความถูกต้องของ Listener Handler**: ตรวจสอบว่าเมธอด `onOrderPlaced(OrderPlacedEvent event)` สามารถดึงรายการการ์ดที่สั่งซื้อและนำรหัสการ์ดไปตรวจสอบระดับสต็อกรวมได้อย่างถูกต้อง
  2. **ตรวจสอบ Boundary Condition ของเกณฑ์สต็อกต่ำ**: ตรวจสอบค่าขอบเขต (Threshold Boundary) ที่ `LOW_STOCK_THRESHOLD = 2` ว่าครอบคลุมทั้งกรณี $\le 2$ และ $> 2$ อย่างถูกต้อง
  3. **การทดสอบความทนทานต่อข้อผิดพลาด (Fault Tolerance & Defensive Guards)**: รับประกันว่าหากมี Event ที่มีข้อมูลไม่สมบูรณ์ (เช่น null event, empty items, detached entity) ระบบจะไม่เกิด `NullPointerException` (Fail-Safe)

---

## 2. 🧩 โครงสร้าง Test Cases (Test Matrix)

คลาสทดสอบ: `LowStockObserverTest.java` ประกอบด้วย **7 Test Cases** ครอบคลุมทุกเส้นทางการทำงาน:

| ลำดับ | Test Method | กรณีที่ทดสอบ | พฤติกรรมที่คาดหวัง |
| :---: | :--- | :--- | :--- |
| 1 | `onOrderPlaced_WhenStockBelowThreshold_ShouldQueryStockAndWarn` | สต็อกคงเหลือต่ำกว่าเกณฑ์ (เช่น 1 ใบ $\le 2$) | คิวรี `sumQuantityByCardId` และยิง Log Warning แจ้งเตือน |
| 2 | `onOrderPlaced_WhenStockEqualsThreshold_ShouldTriggerWarning` | สต็อกคงเหลือเท่ากับเกณฑ์พอดี (2 ใบ $== 2$) | ตรวจสอบ Boundary Condition และยิง Log Warning แจ้งเตือน |
| 3 | `onOrderPlaced_WhenStockAboveThreshold_ShouldLogNormal` | สต็อกคงเหลือมากกว่าเกณฑ์ (เช่น 5 ใบ $> 2$) | คิวรีสต็อกและบันทึกข้อมูลการตรวจสอบระดับปกติ (INFO) |
| 4 | `onOrderPlaced_WithMultipleItems_ShouldCheckAllCards` | ออเดอร์มีรายการการ์ดหลายชนิดในครั้งเดียว | วนลูปตรวจสอบสต็อกรวมครบทุกการ์ดอย่างถูกต้อง |
| 5 | `onOrderPlaced_WhenEventIsNull_ShouldReturnSafely` | Event เป็น `null` | ทำงานจบอย่างปลอดภัย โดยไม่เรียกคิวรีฐานข้อมูล |
| 6 | `onOrderPlaced_WhenItemsNullOrEmpty_ShouldReturnSafely` | รายการ `items` เป็น `null` หรือลิสต์ว่าง | ทำงานจบอย่างปลอดภัย โดยไม่เรียกคิวรีฐานข้อมูล |
| 7 | `onOrderPlaced_WhenItemHasNullInventoryOrCard_ShouldSkipGracefully` | ข้อมูล Inventory หรือ Card ในรายการสินค้าเป็น `null` | ข้ามไปยังรายการถัดไป โดยไม่เกิด Exception |

---

## 3. 💻 เทคนิคสำคัญในการทดสอบ Event Listener (Testing Techniques)

1. **Mockito Verification (`verify` & `verifyNoInteractions`)**:
   - ตรวจสอบจำนวนครั้งที่เรียก Repository ด้วย `verify(repo, times(1)).sumQuantityByCardId(cardId)`
   - ตรวจสอบกรณี Guard Clause ที่ต้องไม่มีการแตะ Repository เลยด้วย `verifyNoInteractions(repo)` หรือ `verify(repo, never())`
2. **`assertThatCode(...).doesNotThrowAnyException()`**:
   - ยืนยันว่าการทำงานของ Handler มีความปลอดภัยระดับ Defensive Programming ไม่โยน Unchecked Exception ออกไปรบกวน Thread หลักของระบบ

---

## 4. 🧪 ผลการรันเทสต์ (Test Execution Results)

- **LowStockObserverTest**: **7 tests ผ่าน 100% (0 Failures, 0 Errors, 0 Skipped)**
- **Total Project Test Suite**: **47 tests ผ่าน 100% (BUILD SUCCESS)**
  - `LowStockObserverTest`: 7 ผ่าน
  - `GameAccountServiceTest`: 9 ผ่าน
  - `CardInventoryTest`: 5 ผ่าน
  - `CardServiceTest`: 13 ผ่าน
  - `DiscountStrategyTest`: 6 ผ่าน
  - `OrderServiceTest`: 7 ผ่าน
