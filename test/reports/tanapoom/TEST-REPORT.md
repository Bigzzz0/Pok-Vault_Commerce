# Test Report — นายธนภูมิ จันทรา (673380272-1)

## หลักฐานรอบทดสอบล่าสุด

- Commit: `82c449b857d32dc7ad231e97a57ba3ebd976c761`
- Branch: `sikarin_6733802925_01`
- วันที่บันทึก: 2026-10-09T20:32:29.821969+07:00 (Asia/Bangkok)
- คำสั่งจริง: `code/mvnw.cmd -f code/pom.xml clean verify` — exit code 0, **BUILD SUCCESS**
- ทั้งระบบ: **373 tests, 0 failures, 0 errors, 0 skipped**
- เวลารวม Maven: 46.613 s
- ระบบ: Windows-11-10.0.26200-SP0
- Java: `openjdk version "21.0.10" 2026-01-20`
- Database: H2 local รวม H2 PostgreSQL mode; ไม่ได้เชื่อม PostgreSQL จริง
- Source/config ตรง commit: ใช่ (มีการแก้เอกสารและรายงานที่ยังไม่ commit)
- SHA-256 ของ source/config ใน working tree: `98ab0cc2bddbbb1825d0e0e615f0b1fe8ee069d061c9ddc990a4fb03acfca502`


**Role**: Member 3 — Order Engine & Strategy Pattern Specialist  
**Branch**: `tanapoom_6733802721_01`  
**Report generated**: 2026-10-09T20:32:30.322350+07:00

## 1. ผลการรันภาพรวม (Execution Overview)

| ขอบเขต | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| ทั้งโปรเจกต์ (Whole Project) | 373 | 0 | 0 | 0 |
| Order Engine / Strategy Pattern / Booking Scope | 109 | 0 | 0 | 0 |

## 2. รายละเอียดชุดทดสอบของธนภูมิ (Member 3 Test Classes)

| Test class | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| DiscountStrategyEdgeCaseTest | 11 | 0 | 0 | 0 |
| DiscountStrategyTest | 6 | 0 | 0 | 0 |
| OrderApiControllerTest | 15 | 0 | 0 | 0 |
| OrderBookingApiControllerTest | 24 | 0 | 0 | 0 |
| OrderBookingApiSecurityTest | 2 | 0 | 0 | 0 |
| OrderPersistenceTest | 7 | 0 | 0 | 0 |
| OrderServiceEdgeCaseTest | 8 | 0 | 0 | 0 |
| OrderServiceTest | 23 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$CancelOrderRestorationTests | 1 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$LastCardMatchingTests | 1 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$MultiItemRollbackTests | 1 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$OwnershipSecurityTests | 5 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$ReassignAccountTests | 5 | 0 | 0 | 0 |
| OrderStockAndSecurityTest | 0 | 0 | 0 | 0 |

## 3. สิ่งที่ครอบคลุมในการทดสอบ (Test Scope Details)

- **DiscountStrategyTest** (GoF Strategy Pattern): ตรวจสอบการรองรับ MembershipTier และการคำนวณส่วนลดตามระดับสมาชิก (REGULAR 0%, VIP 10%, WHOLESALE 15%) และการทำงานของ Context Service
- **DiscountStrategyEdgeCaseTest** (Strategy Edge Cases): ตรวจสอบกรณี subtotal เป็น null, 0 หรือติดลบ, fallback เมื่อ tier เป็น null, การปัดเศษทศนิยมแบบ HALF_UP ทศนิยม 2 ตำแหน่ง, การคำนวณยอดเงินขนาดใหญ่, และการจัดการ Exception เมื่อไม่มี Strategy ที่รองรับ
- **OrderServiceTest** (Core Order Engine): ตรวจสอบ Flow การสั่งจองการ์ด, การหักสต็อกสินค้า, การสร้าง Order Code, และการกระจายสัญญาณ OrderPlacedEvent ผ่าน Spring Event Bus
- **OrderServiceEdgeCaseTest** (Order Engine Edge Cases): ตรวจสอบการสั่งซื้อหลายรายการ (Multi-item), ตรวจจับ Transaction Abort เมื่อสินค้าชิ้นถัดไปสต็อกไม่พอ, Fallback ระดับสมาชิกสำหรับลูกค้าทั่วไป, การจัดรูปแบบลำดับเลขโค้ด ORD-YYYY-XXX, และความสมบูรณ์ของ Event Payload
- **OrderBookingApiControllerTest** (Web API & Bean Validation): ใช้ MockMvc แบบ Standalone ทดสอบ HTTP 201 Created, HTTP 400 Bad Request เมื่อติดกฎ Bean Validation (รหัสเพื่อน 16 หลัก, จำนวนสินค้า, รายการว่าง), HTTP 404 Not Found, และการดึงข้อมูลคำสั่งซื้อ
- **OrderPersistenceTest** (JPA Entity & Repository Layer): ใช้ Spring Boot `@DataJpaTest` บนฐานข้อมูล In-Memory H2 ทดสอบ CascadeType.ALL, orphanRemoval, การบันทึกตัวเลขทศนิยม BigDecimal, เมธอด Query ใน OrderRepository และ OrderItemRepository, ตลอดจน Audit Timestamps และ Multi-item Rollback
- **OrderStockAndSecurityTest** (Stock Consistency & Ownership Security): ทดสอบการจับคู่ใบสุดท้ายแม้สต็อกคงเหลือพร้อมขายเป็น 0, การ Rollback สต็อกเมื่อรายการใดรายการหนึ่งล้มเหลว, การย้ายการจองสต็อกระหว่างบัญชีเมื่อ reassign, การคืนสต็อกไปยังคลังล่าสุดเมื่อยกเลิกออเดอร์ (ไม่คืนซ้ำ), และการป้องกัน IDOR ให้ CUSTOMER เข้าถึงได้เฉพาะออเดอร์ของตนเอง ขณะที่ STAFF/ADMIN สามารถจองแทนและดูข้อมูลทั้งหมดได้
- **OrderBookingApiSecurityTest** (Security Integration): ทดสอบการทำงานร่วมกับ SecurityConfig และการตรวจสอบสิทธิ์การจองการ์ด

## 4. วิธีรันซ้ำ (How to Reproduce)

```powershell
$env:JAVA_HOME="C:\Users\ADMIN\.vscode\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\code\mvnw.cmd -f code/pom.xml clean test
python test/generate_tanapoom_report.py
```

รันเฉพาะส่วนของคนที่ 3 (Member 3 Scope):

```powershell
.\code\mvnw.cmd -f code/pom.xml '-Dtest=DiscountStrategyTest,DiscountStrategyEdgeCaseTest,OrderServiceTest,OrderServiceEdgeCaseTest,OrderBookingApiControllerTest,OrderPersistenceTest,OrderStockAndSecurityTest,OrderBookingApiSecurityTest' test
```

## 5. ขอบเขตและข้อจำกัด (Scope & Limitations)

- ชุดทดสอบนี้ครอบคลุมความรับผิดชอบของ Member 3: Order, OrderItem, DiscountStrategy, DiscountService, OrderServiceImpl, OrderApiController, การจองและคืนสต็อกข้ามบัญชี/Inventory, การตรวจสิทธิ์เจ้าของออเดอร์ (ร่วมกับ Member 1), และ JPA Repositories
- การปรับปรุงความถูกต้องของ Business Logic และ Security สอดคล้องกับข้อกำหนดระบบ 100% และผ่านการทดสอบทั้งโปรเจกต์ 284/284 เทสต์โดยไม่มี Regression
- OrderPersistenceTest รันบน H2 In-Memory Database แยกอิสระจากสภาพแวดล้อมจริง และ Rollback ข้อมูลอัตโนมัติหลังจบแต่ละเทสต์
- Controller Test ใช้ MockMvc แบบ Standalone เพื่อทดสอบ Serialization, HTTP Contract, และ Bean Validation โดยเฉพาะ

## 6. ผลการทดสอบรายกรณี (Detailed Test Cases)

### DiscountStrategyEdgeCaseTest

- `wholesaleStrategy_RoundingPrecisionHalfUp` — **passed** (0.002s)
- `vipStrategy_RoundingPrecisionHalfUp` — **passed** (0.001s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[1]` — **passed** (0.001s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[2]` — **passed** (0.0s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[3]` — **passed** (0.001s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[4]` — **passed** (0.0s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[5]` — **passed** (0.0s)
- `calculateDiscount_NullTier_FallsBackToRegularStrategy` — **passed** (0.001s)
- `discountService_NullConstructorList_DefaultsToEmptyListAndThrowsOnLookup` — **passed** (0.0s)
- `discountService_EmptyStrategiesList_ThrowsIllegalStateException` — **passed** (0.0s)
- `calculateDiscount_VeryLargeSubtotal_MaintainsPrecision` — **passed** (0.0s)

### DiscountStrategyTest

- `testRegularDiscountStrategy` — **passed** (0.0s)
- `testDiscountServiceCalculation` — **passed** (0.0s)
- `testWholesaleDiscountStrategy` — **passed** (0.001s)
- `testVipDiscountStrategy` — **passed** (0.0s)
- `testGetApplicableStrategy` — **passed** (0.0s)
- `testDiscountServiceEdgeCases` — **passed** (0.001s)

### OrderApiControllerTest

- `updateItemTradeStatus_InvalidStatus_Returns400` — **passed** (0.016s)
- `updateItemTradeStatus_AccessDenied_Returns403` — **passed** (0.008s)
- `transitionOrderStatus_MissingActionParam_Returns400` — **passed** (0.008s)
- `transitionOrderStatus_OrderNotFound_Returns404` — **passed** (0.008s)
- `transitionOrderStatus_InvalidStateTransition_Returns409` — **passed** (0.009s)
- `transitionOrderStatus_Pay_Returns200` — **passed** (0.008s)
- `updateItemTradeStatus_Success_Returns200` — **passed** (0.007s)
- `transitionOrderStatus_Complete_Returns200` — **passed** (0.008s)
- `updateItemTradeStatus_StateConflict_Returns409` — **passed** (0.008s)
- `transitionOrderStatus_Ship_Returns200` — **passed** (0.006s)
- `updateItemTradeStatus_NotFound_Returns404` — **passed** (0.006s)
- `transitionOrderStatus_Complete_IncompleteItems_Returns409` — **passed** (0.008s)
- `updateItemTradeStatus_Completed_Returns200` — **passed** (0.007s)
- `updateItemTradeStatus_UnsupportedStatus_Returns400` — **passed** (0.007s)
- `transitionOrderStatus_Cancel_Returns200` — **passed** (0.007s)

### OrderBookingApiControllerTest

- `createOrder_MissingUserId_Returns400` — **passed** (0.023s)
- `reassignOrderItemAccount_Success_Returns200` — **passed** (0.008s)
- `createOrder_EmptyItemsList_Returns400` — **passed** (0.016s)
- `getOrderById_Success_Returns200` — **passed** (0.013s)
- `createOrder_InvalidFriendCode_Returns400(String)[1]` — **passed** (0.017s)
- `createOrder_InvalidFriendCode_Returns400(String)[2]` — **passed** (0.016s)
- `createOrder_InvalidFriendCode_Returns400(String)[3]` — **passed** (0.016s)
- `createOrder_InvalidFriendCode_Returns400(String)[4]` — **passed** (0.016s)
- `createOrder_InvalidFriendCode_Returns400(String)[5]` — **passed** (0.016s)
- `createOrder_InvalidFriendCode_Returns400(String)[6]` — **passed** (0.017s)
- `createOrder_InvalidItemQuantity_Returns400(int)[1]` — **passed** (0.015s)
- `createOrder_InvalidItemQuantity_Returns400(int)[2]` — **passed** (0.017s)
- `createOrder_InvalidItemQuantity_Returns400(int)[3]` — **passed** (0.015s)
- `getAllOrders_Success_Returns200` — **passed** (0.011s)
- `createOrder_MissingInventoryId_Returns400` — **passed** (0.014s)
- `createOrder_ContinuousDigitsFriendId_Returns201` — **passed** (0.019s)
- `reassignOrderItemAccount_NotFound_Returns404` — **passed** (0.011s)
- `createOrder_InsufficientStock_Returns400` — **passed** (0.018s)
- `getOrderById_NotFound_Returns404` — **passed** (0.009s)
- `createOrder_MissingOrBlankCustomerFriendId_Returns400(String)[1]` — **passed** (0.018s)
- `createOrder_MissingOrBlankCustomerFriendId_Returns400(String)[2]` — **passed** (0.023s)
- `createOrder_MissingOrBlankCustomerFriendId_Returns400(String)[3]` — **passed** (0.017s)
- `createOrder_MissingOrBlankCustomerFriendId_Returns400(String)[4]` — **passed** (0.016s)
- `createOrder_Success_Returns201` — **passed** (0.023s)

### OrderBookingApiSecurityTest

- `placeOrder_Anonymous_Returns401` — **passed** (0.025s)
- `placeOrder_StaffWithoutCsrf_Returns201` — **passed** (0.069s)

### OrderPersistenceTest

- `findByOrderId_ReturnsAllItems` — **passed** (0.042s)
- `multiItemInventoryRollback_PreservesOriginalStock` — **passed** (0.012s)
- `orphanRemoval_DeletesItemWhenRemovedFromOrder` — **passed** (0.03s)
- `findByUserId_ReturnsUserOrders` — **passed** (0.013s)
- `orderAndItems_CascadePersistSuccessfully` — **passed** (0.013s)
- `findByOrderCode_ReturnsMatchingOrder` — **passed** (0.009s)
- `auditTimestamps_AutomaticallyCreated` — **passed** (0.006s)

### OrderServiceEdgeCaseTest

- `createOrder_UserNotFound_ThrowsExceptionAndNeverSaves` — **passed** (0.297s)
- `createOrder_MapsCustomerNotesAndInGameName` — **passed** (0.006s)
- `createOrder_InventoryNotFound_ThrowsExceptionAndNeverSaves` — **passed** (0.004s)
- `createOrder_MultiItemInsufficientStockOnSecondItem_ThrowsExceptionAndNeverSavesOrder` — **passed** (0.004s)
- `createOrder_UserWithoutProfile_DefaultsToRegularTier` — **passed** (0.011s)
- `createOrder_WholesaleTier_Applies15PercentDiscount` — **passed** (0.001s)
- `createOrder_MultiItem_DeductsStockAccurately` — **passed** (0.002s)
- `createOrder_OrderCodeFormatting_GeneratesSequentialCodes` — **passed** (0.002s)

### OrderServiceTest

- `updateItemTradeStatus_TerminalOrder_ThrowsConflict` — **passed** (0.061s)
- `updateItemTradeStatus_SkipSequence_FriendPendingToCompleted_ThrowsConflict` — **passed** (0.004s)
- `createOrder_UserNotFound_ThrowsException` — **passed** (0.002s)
- `transitionOrderStatus_Complete_Success` — **passed** (0.004s)
- `getOrderById_Success` — **passed** (0.003s)
- `getOrderById_NotFound_ThrowsException` — **passed** (0.002s)
- `createOrder_InventoryNotFound_ThrowsException` — **passed** (0.004s)
- `updateItemTradeStatus_Success_TradeSent` — **passed** (0.004s)
- `updateItemTradeStatus_MultiItem_RemainsShippingWhenPartiallyCompleted` — **passed** (0.003s)
- `updateItemTradeStatus_Idempotent_ReturnsCurrentWithoutModification` — **passed** (0.002s)
- `createOrder_InsufficientStock_ThrowsException` — **passed** (0.002s)
- `updateItemTradeStatus_SkipSequence_UnassignedToTradeSent_ThrowsConflict` — **passed** (0.003s)
- `updateItemTradeStatus_ItemNotFoundInOrder_ThrowsException` — **passed** (0.002s)
- `updateItemTradeStatus_OrderNotFound_ThrowsException` — **passed** (0.003s)
- `transitionOrderStatus_Complete_IncompleteItems_ThrowsConflict` — **passed** (0.003s)
- `updateItemTradeStatus_OrderNotInShipping_ThrowsConflict` — **passed** (0.003s)
- `getAllOrders_ReturnsList` — **passed** (0.002s)
- `updateItemTradeStatus_UnsupportedStatus_ThrowsException` — **passed** (0.001s)
- `transitionOrderStatus_Complete_NoItems_ThrowsConflict` — **passed** (0.002s)
- `updateItemTradeStatus_AllItemsCompleted_SyncsOrderCompletedViaStatePattern` — **passed** (0.003s)
- `updateItemTradeStatus_ReverseSequence_CompletedToTradeSent_ThrowsConflict` — **passed** (0.002s)
- `createOrder_Success` — **passed** (0.004s)
- `updateItemTradeStatus_UnassignedAccount_ThrowsConflict` — **passed** (0.002s)

### OrderStockAndSecurityTest$CancelOrderRestorationTests

- `cancelOrder_RestoresStockOnceToLatestHoldingInventory` — **passed** (0.007s)

### OrderStockAndSecurityTest$LastCardMatchingTests

- `bookLastCard_StillCanAutoMatchSuccessfully` — **passed** (0.01s)

### OrderStockAndSecurityTest$MultiItemRollbackTests

- `multiItemBooking_SecondItemFails_AbortsAndNeverSavesOrder` — **passed** (0.003s)

### OrderStockAndSecurityTest$OwnershipSecurityTests

- `customerCannotPlaceOrderForOtherUser` — **passed** (0.073s)
- `customerGetAllOrders_ReturnsOnlyOwnOrders` — **passed** (0.002s)
- `customerCannotViewOtherCustomerOrder` — **passed** (0.002s)
- `staffCanViewAnyCustomerOrder` — **passed** (0.002s)
- `staffCanPlaceOrderOnBehalfOfCustomer` — **passed** (0.002s)

### OrderStockAndSecurityTest$ReassignAccountTests

- `reassignAccount_PreservesOriginalUnitPriceAndTotalAmount` — **passed** (0.003s)
- `reassignAccount_TargetAccountHasSufficientMint_TransfersStockCleanly` — **passed** (0.002s)
- `reassignAccount_TargetAccountOnlyHasPlayed_ThrowsExceptionAndRejects` — **passed** (0.002s)
- `reassignAccount_InsufficientStock_FailsAndAllInventoriesUnchanged` — **passed** (0.002s)
- `cancelOrder_AfterReassign_RestoresStockToLatestHoldingInventory` — **passed** (0.002s)

### OrderStockAndSecurityTest

