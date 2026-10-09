# Report 08: CardInventory Stock Deduction & Restoration

**ผู้รับผิดชอบ:** สมาชิกคนที่ 2 — Game Account Vault & Inventory Manager  
**Roadmap item:** `feat: add stock deduction and restoration methods in CardInventory`  
**สถานะ:** Implemented & Verified

## สิ่งที่ทำ

- รายงานความสมบูรณ์ของเมธอดการจัดการสต็อกในเอนทิตี `CardInventory`:
  1. `hasSufficientStock(int requestedQuantity)`: เมธอดตรวจสอบความพอเพียงของสต็อกการ์ด พร้อม Guard ป้องกันค่าจำนวน $\le 0$
  2. `deductStock(int count)`: เมธอดตัดสต็อกแบบ Fail-Fast หากสต็อกไม่พอจะโยน `InsufficientStockException` โดยไม่กระทบจำนวนเดิม
  3. `restoreStock(int count)`: เมธอดคืนสต็อกการ์ดกลับเข้าคลังอย่างปลอดภัย พร้อมระบบ Null-Safety
- ชี้แจงการรวมเมธอดเข้าเป็น Core Domain Invariants ใน `CardInventory.java` ตั้งแต่ Commit `9b41086` เพื่อให้เป็นไปตามหลัก Information Expert Pattern
- ตรวจสอบการเชื่อมโยงระบบ (Integration) ระหว่าง `CardInventory` กับ `OrderServiceImpl` (ตัดสต็อก), `CancelledOrderState` (คืนสต็อกอัตโนมัติ), และ `GameAccountServiceImpl` (เพิ่มสต็อกการเปิดซอง)

## ไฟล์ที่เกี่ยวข้อง

- `src/main/java/com/pokevault/domain/entity/CardInventory.java` (บรรทัดที่ 52–73)
- `src/test/java/com/pokevault/domain/entity/CardInventoryTest.java` (บรรทัดที่ 13–50)
- `knowledge/knowledge08.md`

## ผลตรวจสอบ

- รัน `./mvnw test` สำเร็จ: **31 tests ผ่าน 100%, 0 failures, 0 errors (BUILD SUCCESS)**

## งานถัดไป

ทำข้อ 9: `feat: implement LowStockObserver listener using @EventListener` ตาม roadmap
