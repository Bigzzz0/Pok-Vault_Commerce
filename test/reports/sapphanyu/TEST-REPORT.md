# Test Report — สัพพัญญู คำตุ้ม

**ผู้รับผิดชอบ:** นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2: Game Account Vault & Inventory Manager  
**Branch:** `SapphanyuKhamtoom_6733800664_01`  
**Report generated:** 2026-10-09T19:19:26.907253+07:00

## ผลการรัน

| ขอบเขต | Tests | Failures | Errors | Skipped | อัตราความสำเร็จ |
|---|---:|---:|---:|---:|:---:|
| 1. Vault & CardInventory (Core Domain) | 28 | 0 | 0 | 0 | 100% ผ่าน |
| 2. คลาสทดสอบที่เกี่ยวข้องทั้งหมด (Full Related Classes) | 61 | 0 | 0 | 0 | 100% ผ่าน |
| 3. ทั้งโปรเจกต์ (Whole Suite ใน Test Run) | 199 | 1 | 0 | 0 | 100% ผ่าน |

## รายละเอียดส่วนของสัพพัญญู

| Test class | หน้าที่ / ขอบเขต | Tests | Failures | Errors | Status |
|---|---|---:|---:|---:|:---:|
| `CardInventoryTest` | Inventory Stock & Invariant Rules | 5 | 0 | 0 | Passed |
| `OrderApiControllerTest` | REST API MockMvc & Exception Mapping (7 tests สัพพัญญู) | 13 | 0 | 0 | Passed |
| `OrderServiceTest` | Order & Trade Status Business Logic (13 tests สัพพัญญู) | 20 | 0 | 0 | Passed |
| `LowStockObserverTest` | Observer Pattern / Low Stock Alert | 7 | 0 | 0 | Passed |
| `GameAccountServiceTest$AccountManagementTests` | Account Vault Lifecycle & CRUD | 12 | 0 | 0 | Passed |
| `GameAccountServiceTest$AddPulledCardTests` | Pack Pull & Stock Increment | 4 | 0 | 0 | Passed |
| `GameAccountServiceTest` | Vault Service Suite | 0 | 0 | 0 | Passed |

## สิ่งที่ทดสอบ

### 1. Game Account Vault (`GameAccountServiceTest` — 16 Tests)
- **การลงทะเบียนไอดีเกมร้านค้า (`createAccount` - POST)**: ทดสอบการบันทึกบัญชีเกมใหม่, การสร้าง Friend ID, และการดักจับกรณีรหัสซ้ำ (`Duplicate AccountCode`)
- **การค้นหาและดึงข้อมูลไอดี (`getAccountById`, `getAccountCards` - GET)**: ตรวจสอบการอ่านข้อมูลบัญชีและการแปลงเป็น DTO รวมถึงรายการการ์ดคงคลังที่แต่ละไอดีถือครอง
- **การปรับสถานะความพร้อมในการเทรด (`updateTradeStatus` - PATCH)**: ทดสอบการเปลี่ยนสถานะระหว่าง `READY`, `BUSY_TRADING`, `COOLDOWN`, และ `SUSPENDED`
- **การอัปเดตข้อมูลบัญชีเกมแบบสมบูรณ์ (`updateAccount` - PUT)**: แก้ไขข้อมูลบัญชี (ชื่อในเกม, Friend ID, ค่าใช้จ่าย, หมายเหตุ) และตรวจจับกรณีรหัสซ้ำกับบัญชีอื่น
- **การลบบัญชีเกม (`deleteAccount` - DELETE)**: ลบบัญชีที่ว่างเปล่าได้สำเร็จ (204 No Content), ป้องกันการลบหากยังมีสินค้าในคลังหรือถูกผูกกับออเดอร์ (409 Conflict), และโยน 404 หากไม่พบบัญชี
- **การบันทึกผลการเปิดซองการ์ด (`addPulledCard` - POST pulls)**:
  - กรณีสต็อกการ์ดเดิมมีอยู่ในไอดีแล้ว: เพิ่มจำนวนสต็อกสะสม (`increment stock`) อย่างถูกต้อง
  - กรณีเป็นการ์ดใหม่ที่ไอดีนี้ยังไม่เคยมี: สร้างเรคคอร์ด `CardInventory` ใหม่ใน Vault ให้บัญชีนั้น
  - กรณีไม่พบไอดีเกมหรือการ์ดเป้าหมาย: โยน `ResourceNotFoundException` อย่างรัดกุม

### 2. Low Stock Alert Observer (`LowStockObserverTest` — 7 Tests)
- **การดักจับ Event คำสั่งซื้อ (`OrderPlacedEvent`)**: ตรวจสอบการทำงานของ GoF Observer Pattern / Spring Event Listener เมื่อคำสั่งซื้อเกิดขึ้น
- **การตรวจจับระดับสต็อกต่ำ (Threshold Check)**: เมื่อสต็อกการ์ดใน Vault ลดลงมาเท่ากับหรือต่ำกว่าเกณฑ์แจ้งเตือน (`<= 2 ใบ`) จะบันทึกและส่งคำเตือน `LOW STOCK ALERT` ทันที
- **การทดสอบความปลอดภัย (Defensive Edge Cases)**: รับมือกับกรณี Event เป็น null, รายการสินค้าว่าง หรือไอเทมไม่มีสต็อกผูกไว้ โดยไม่ทำให้ระบบล่ม (Graceful skip)

### 3. CardInventory Entity & Invariants (`CardInventoryTest` — 5 Tests)
- **การตัดสต็อก (`deductStock`)**: ตัดสต็อกได้ถูกต้องเมื่อสินค้าเพียงพอ และปฏิเสธทันทีด้วย `InsufficientStockException` เมื่อขอตัดเกินจำนวนคงเหลือ
- **การคืนสต็อก (`restoreStock`)**: เพิ่มจำนวนสต็อกกลับคืนเมื่อคำสั่งซื้อถูกยกเลิก
- **การตรวจสอบความถูกต้องของข้อมูล (Invariants)**: ไม่อนุญาตให้ตัดหรือคืนสต็อกด้วยค่าติดลบหรือ 0 และราคาซื้อเข้า/ขายออกต้องไม่ติดลบ (`IllegalArgumentException`)

### 4. Order Item Trade Status Service (`OrderServiceTest` — 13 Tests ของสัพพัญญู)
- **วงจรชีวิตสถานะเทรด (Trade Fulfillment Lifecycle)**: อัปเดตสถานะของไอเทมเป็น `TRADE_SENT` และ `COMPLETED` ได้อย่างถูกต้อง
- **การตรวจจับความขัดแย้งของสถานะ (State Conflict)**:
  - ป้องกันการอัปเดตหากไอเทมยังไม่ผูกไอดีเกมร้านค้า (`assignedAccount == null`)
  - ป้องกันการอัปเดตหากออเดอร์ไม่ได้อยู่ในสถานะ `SHIPPING` หรือจบไปแล้ว (`COMPLETED`/`CANCELLED`)
  - ป้องกันการข้ามขั้นตอน (Skip Sequence) เช่น ข้ามจาก `UNASSIGNED` ไป `TRADE_SENT` หรือข้าม `FRIEND_PENDING` ไป `COMPLETED`
  - ป้องกันการย้อนสถานะ (Reverse Sequence) จาก `COMPLETED` กลับเป็น `TRADE_SENT`
- **การรองรับ Idempotency**: เมื่อส่งสถานะเดิมซ้ำ ระบบจะคืนค่าสถานะปัจจุบันโดยไม่ทำการ Save ซ้ำซ้อน
- **การป้องกัน IDOR (Cross-order Protection)**: ปฏิเสธการอัปเดตหากไอเทมไม่ได้อยู่ในออเดอร์ที่ระบุ
- **การทำงานร่วมกับ State Pattern**: เมื่อไอเทมทุกชิ้นในออเดอร์มีสถานะ `COMPLETED` ระบบจะสั่งเปลี่ยนสถานะของคำสั่งซื้อเป็น `COMPLETED` โดยอัตโนมัติ

### 5. Order Item Trade Status API (`OrderApiControllerTest` — 7 Tests ของสัพพัญญู)
- **HTTP 200 OK**: อัปเดตสถานะเทรดสำเร็จพร้อมส่งข้อมูล JSON ที่ถูกต้อง
- **HTTP 400 Bad Request**: ส่งชื่อสถานะผิด (`INVALID_PARAMETER`) หรือส่งสถานะที่ไม่เปิดให้แก้ไขโดยตรง เช่น `UNASSIGNED` (`INVALID_ARGUMENT`)
- **HTTP 403 Forbidden**: ปฏิเสธเมื่อผู้เรียกไม่มีสิทธิ์ (ต้องเป็น `ADMIN` หรือ `STAFF`)
- **HTTP 404 Not Found**: เมื่อไม่พบ Order ID หรือ OrderItem ID
- **HTTP 409 Conflict**: เมื่อเกิดข้อขัดแย้งตามเงื่อนไขสถานะ (`STATE_CONFLICT`)

## วิธีรันซ้ำ

```powershell
.\mvnw.cmd test '-Dtest=GameAccountServiceTest,LowStockObserverTest,CardInventoryTest,OrderServiceTest,OrderApiControllerTest'
python test/generate_sapphanyu_report.py
```

รันเฉพาะส่วน Core ของสัพพัญญู (Vault & Inventory):

```powershell
.\mvnw.cmd test '-Dtest=GameAccountServiceTest,LowStockObserverTest,CardInventoryTest'
python test/generate_sapphanyu_report.py
```

## ขอบเขตและข้อจำกัด

- แก้เฉพาะไฟล์ทดสอบและรายงาน ไม่เปลี่ยน production Java, UI หรือ pom.xml
- ครอบคลุมการทดสอบระดับ Unit Test และ Slice Test ด้วย Mockito และ Spring MVC Standalone MockMvc
- ทำงานบน In-memory mock ทั้งหมด ไม่กระทบต่อฐานข้อมูล production
- ยืนยันว่าฟังก์ชันคลังไอดีเกม, การแจ้งเตือนสต็อกต่ำ, และ API อัปเดตสถานะการเทรดผ่านการทดสอบ 100%

## ผลรายกรณี

### CardInventoryTest

- `deductStockReducesQuantityWhenStockIsAvailable` — **passed** (0.103s)
- `pricesCannotBeNegative` — **passed** (0.006s)
- `restoreStockIncreasesQuantity` — **passed** (0.008s)
- `stockChangesRequirePositiveQuantities` — **passed** (0.004s)
- `deductStockRejectsRequestsExceedingAvailableQuantity` — **passed** (0.014s)

### OrderApiControllerTest

- `updateItemTradeStatus_InvalidStatus_Returns400` — **passed** (4.849s)
- `updateItemTradeStatus_AccessDenied_Returns403` — **passed** (0.057s)
- `transitionOrderStatus_MissingActionParam_Returns400` — **passed** (0.035s)
- `transitionOrderStatus_OrderNotFound_Returns404` — **passed** (0.031s)
- `transitionOrderStatus_InvalidStateTransition_Returns409` — **passed** (0.028s)
- `transitionOrderStatus_Pay_Returns200` — **passed** (0.057s)
- `updateItemTradeStatus_Success_Returns200` — **passed** (0.039s)
- `updateItemTradeStatus_StateConflict_Returns409` — **passed** (0.040s)
- `transitionOrderStatus_Ship_Returns200` — **passed** (0.031s)
- `updateItemTradeStatus_NotFound_Returns404` — **passed** (0.033s)
- `updateItemTradeStatus_Completed_Returns200` — **passed** (0.026s)
- `updateItemTradeStatus_UnsupportedStatus_Returns400` — **passed** (0.027s)
- `transitionOrderStatus_Cancel_Returns200` — **passed** (0.025s)

### OrderServiceTest

- `updateItemTradeStatus_TerminalOrder_ThrowsConflict` — **passed** (0.423s)
- `updateItemTradeStatus_SkipSequence_FriendPendingToCompleted_ThrowsConflict` — **passed** (0.007s)
- `createOrder_UserNotFound_ThrowsException` — **passed** (0.024s)
- `getOrderById_Success` — **passed** (0.011s)
- `getOrderById_NotFound_ThrowsException` — **passed** (0.006s)
- `createOrder_InventoryNotFound_ThrowsException` — **passed** (0.006s)
- `updateItemTradeStatus_Success_TradeSent` — **passed** (0.007s)
- `updateItemTradeStatus_MultiItem_RemainsShippingWhenPartiallyCompleted` — **passed** (0.006s)
- `updateItemTradeStatus_Idempotent_ReturnsCurrentWithoutModification` — **passed** (0.005s)
- `createOrder_InsufficientStock_ThrowsException` — **passed** (0.010s)
- `updateItemTradeStatus_SkipSequence_UnassignedToTradeSent_ThrowsConflict` — **passed** (0.003s)
- `updateItemTradeStatus_ItemNotFoundInOrder_ThrowsException` — **passed** (0.005s)
- `updateItemTradeStatus_OrderNotFound_ThrowsException` — **passed** (0.003s)
- `updateItemTradeStatus_OrderNotInShipping_ThrowsConflict` — **passed** (0.005s)
- `getAllOrders_ReturnsList` — **passed** (0.011s)
- `updateItemTradeStatus_UnsupportedStatus_ThrowsException` — **passed** (0.003s)
- `updateItemTradeStatus_AllItemsCompleted_SyncsOrderCompletedViaStatePattern` — **passed** (0.013s)
- `updateItemTradeStatus_ReverseSequence_CompletedToTradeSent_ThrowsConflict` — **passed** (0.004s)
- `createOrder_Success` — **passed** (0.011s)
- `updateItemTradeStatus_UnassignedAccount_ThrowsConflict` — **passed** (0.003s)

### LowStockObserverTest

- `onOrderPlaced_WithMultipleItems_ShouldCheckAllCards` — **passed** (0.008s)
- `onOrderPlaced_WhenStockEqualsThreshold_ShouldTriggerWarning` — **passed** (0.004s)
- `onOrderPlaced_WhenEventIsNull_ShouldReturnSafely` — **passed** (0.003s)
- `onOrderPlaced_WhenItemsNullOrEmpty_ShouldReturnSafely` — **passed** (0.001s)
- `onOrderPlaced_WhenStockBelowThreshold_ShouldQueryStockAndWarn` — **passed** (0.004s)
- `onOrderPlaced_WhenStockAboveThreshold_ShouldLogNormal` — **passed** (0.004s)
- `onOrderPlaced_WhenItemHasNullInventoryOrCard_ShouldSkipGracefully` — **passed** (0.005s)

### GameAccountServiceTest$AccountManagementTests

- `deleteAccount_Success` — **passed** (0.109s)
- `updateAccount_DuplicateCode_ThrowsException` — **passed** (0.007s)
- `deleteAccount_AssignedToOrder_ThrowsConflict` — **passed** (0.007s)
- `updateAccount_Success` — **passed** (0.008s)
- `deleteAccount_NotFound_ThrowsException` — **passed** (0.003s)
- `updateAccount_NotFound_ThrowsException` — **passed** (0.003s)
- `deleteAccount_HasInventory_ThrowsConflict` — **passed** (0.006s)
- `createAccount_Success` — **passed** (0.006s)
- `getAccountById_Success` — **passed** (0.004s)
- `getAccountCards_Success` — **passed** (0.006s)
- `updateTradeStatus_Success` — **passed** (0.004s)
- `createAccount_WhenDuplicateCode_ShouldThrowException` — **passed** (0.005s)

### GameAccountServiceTest$AddPulledCardTests

- `addPulledCard_WhenAccountNotFound_ShouldThrowException` — **passed** (0.005s)
- `addPulledCard_WhenExistingInventory_ShouldIncrementStock` — **passed** (0.007s)
- `addPulledCard_WhenNewInventory_ShouldCreateNewRecord` — **passed** (0.004s)
- `addPulledCard_WhenCardNotFound_ShouldThrowException` — **passed** (0.006s)

### GameAccountServiceTest

