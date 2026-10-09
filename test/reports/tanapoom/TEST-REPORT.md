# Test Report — นายธนภูมิ จันทรา (673380272-1)

**Role**: Member 3 — Order Engine & Strategy Pattern Specialist  
**Branch**: `tanapoom_6733802721_01`  
**Report generated**: 2026-10-09T17:56:22.959502+07:00

## 1. ผลการรันภาพรวม (Execution Overview)

| ขอบเขต | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| ทั้งโปรเจกต์ (Whole Project) | 284 | 0 | 0 | 0 |
| Order Engine / Strategy Pattern / Booking Scope | 99 | 0 | 0 | 0 |

## 2. รายละเอียดชุดทดสอบของธนภูมิ (Member 3 Test Classes)

| Test class | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| DiscountStrategyEdgeCaseTest | 11 | 0 | 0 | 0 |
| DiscountStrategyTest | 6 | 0 | 0 | 0 |
| OrderApiControllerTest | 13 | 0 | 0 | 0 |
| OrderBookingApiControllerTest | 24 | 0 | 0 | 0 |
| OrderBookingApiSecurityTest | 1 | 0 | 0 | 0 |
| OrderPersistenceTest | 7 | 0 | 0 | 0 |
| OrderServiceEdgeCaseTest | 8 | 0 | 0 | 0 |
| OrderServiceTest | 20 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$CancelOrderRestorationTests | 1 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$LastCardMatchingTests | 1 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$MultiItemRollbackTests | 1 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$OwnershipSecurityTests | 5 | 0 | 0 | 0 |
| OrderStockAndSecurityTest$ReassignAccountTests | 1 | 0 | 0 | 0 |
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
.\mvnw.cmd clean test
python test/generate_tanapoom_report.py
```

รันเฉพาะส่วนของคนที่ 3 (Member 3 Scope):

```powershell
.\mvnw.cmd '-Dtest=DiscountStrategyTest,DiscountStrategyEdgeCaseTest,OrderServiceTest,OrderServiceEdgeCaseTest,OrderBookingApiControllerTest,OrderPersistenceTest,OrderStockAndSecurityTest,OrderBookingApiSecurityTest' test
```

## 5. ขอบเขตและข้อจำกัด (Scope & Limitations)

- ชุดทดสอบนี้ครอบคลุมความรับผิดชอบของ Member 3: Order, OrderItem, DiscountStrategy, DiscountService, OrderServiceImpl, OrderApiController, การจองและคืนสต็อกข้ามบัญชี/Inventory, การตรวจสิทธิ์เจ้าของออเดอร์ (ร่วมกับ Member 1), และ JPA Repositories
- การปรับปรุงความถูกต้องของ Business Logic และ Security สอดคล้องกับข้อกำหนดระบบ 100% และผ่านการทดสอบทั้งโปรเจกต์ 284/284 เทสต์โดยไม่มี Regression
- OrderPersistenceTest รันบน H2 In-Memory Database แยกอิสระจากสภาพแวดล้อมจริง และ Rollback ข้อมูลอัตโนมัติหลังจบแต่ละเทสต์
- Controller Test ใช้ MockMvc แบบ Standalone เพื่อทดสอบ Serialization, HTTP Contract, และ Bean Validation โดยเฉพาะ

## 6. ผลการทดสอบรายกรณี (Detailed Test Cases)

### DiscountStrategyEdgeCaseTest

- `wholesaleStrategy_RoundingPrecisionHalfUp` — **passed** (0.004s)
- `vipStrategy_RoundingPrecisionHalfUp` — **passed** (0.001s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[1]` — **passed** (0.001s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[2]` — **passed** (0.0s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[3]` — **passed** (0.001s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[4]` — **passed** (0.001s)
- `calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String)[5]` — **passed** (0.0s)
- `calculateDiscount_NullTier_FallsBackToRegularStrategy` — **passed** (0.001s)
- `discountService_NullConstructorList_DefaultsToEmptyListAndThrowsOnLookup` — **passed** (0.0s)
- `discountService_EmptyStrategiesList_ThrowsIllegalStateException` — **passed** (0.0s)
- `calculateDiscount_VeryLargeSubtotal_MaintainsPrecision` — **passed** (0.0s)

### DiscountStrategyTest

- `testRegularDiscountStrategy` — **passed** (0.001s)
- `testDiscountServiceCalculation` — **passed** (0.0s)
- `testWholesaleDiscountStrategy` — **passed** (0.0s)
- `testVipDiscountStrategy` — **passed** (0.001s)
- `testGetApplicableStrategy` — **passed** (0.001s)
- `testDiscountServiceEdgeCases` — **passed** (0.001s)

### OrderApiControllerTest

- `updateItemTradeStatus_InvalidStatus_Returns400` — **passed** (0.073s)
- `updateItemTradeStatus_AccessDenied_Returns403` — **passed** (0.016s)
- `transitionOrderStatus_MissingActionParam_Returns400` — **passed** (0.016s)
- `transitionOrderStatus_OrderNotFound_Returns404` — **passed** (0.013s)
- `transitionOrderStatus_InvalidStateTransition_Returns409` — **passed** (0.013s)
- `transitionOrderStatus_Pay_Returns200` — **passed** (0.016s)
- `updateItemTradeStatus_Success_Returns200` — **passed** (0.017s)
- `updateItemTradeStatus_StateConflict_Returns409` — **passed** (0.012s)
- `transitionOrderStatus_Ship_Returns200` — **passed** (0.015s)
- `updateItemTradeStatus_NotFound_Returns404` — **passed** (0.013s)
- `updateItemTradeStatus_Completed_Returns200` — **passed** (0.012s)
- `updateItemTradeStatus_UnsupportedStatus_Returns400` — **passed** (0.011s)
- `transitionOrderStatus_Cancel_Returns200` — **passed** (0.013s)

### OrderBookingApiControllerTest

- `createOrder_MissingUserId_Returns400` — **passed** (0.06s)
- `reassignOrderItemAccount_Success_Returns200` — **passed** (0.015s)
- `createOrder_EmptyItemsList_Returns400` — **passed** (0.031s)
- `getOrderById_Success_Returns200` — **passed** (0.016s)
- `createOrder_InvalidFriendCode_Returns400(String)[1]` — **passed** (0.05s)
- `createOrder_InvalidFriendCode_Returns400(String)[2]` — **passed** (0.036s)
- `createOrder_InvalidFriendCode_Returns400(String)[3]` — **passed** (0.033s)
- `createOrder_InvalidFriendCode_Returns400(String)[4]` — **passed** (0.038s)
- `createOrder_InvalidFriendCode_Returns400(String)[5]` — **passed** (0.039s)
- `createOrder_InvalidFriendCode_Returns400(String)[6]` — **passed** (0.036s)
- `createOrder_InvalidItemQuantity_Returns400(int)[1]` — **passed** (0.027s)
- `createOrder_InvalidItemQuantity_Returns400(int)[2]` — **passed** (0.038s)
- `createOrder_InvalidItemQuantity_Returns400(int)[3]` — **passed** (0.036s)
- `getAllOrders_Success_Returns200` — **passed** (0.027s)
- `createOrder_MissingInventoryId_Returns400` — **passed** (0.022s)
- `createOrder_ContinuousDigitsFriendId_Returns201` — **passed** (0.026s)
- `reassignOrderItemAccount_NotFound_Returns404` — **passed** (0.014s)
- `createOrder_InsufficientStock_Returns400` — **passed** (0.026s)
- `getOrderById_NotFound_Returns404` — **passed** (0.015s)
- `createOrder_MissingOrBlankCustomerFriendId_Returns400(String)[1]` — **passed** (0.041s)
- `createOrder_MissingOrBlankCustomerFriendId_Returns400(String)[2]` — **passed** (0.024s)
- `createOrder_MissingOrBlankCustomerFriendId_Returns400(String)[3]` — **passed** (0.021s)
- `createOrder_MissingOrBlankCustomerFriendId_Returns400(String)[4]` — **passed** (0.028s)
- `createOrder_Success_Returns201` — **passed** (0.028s)

### OrderBookingApiSecurityTest

- `placeOrder_PublicWithoutCsrf_Returns201` — **passed** (0.111s)

### OrderPersistenceTest

- `findByOrderId_ReturnsAllItems` — **passed** (0.077s)
- `multiItemInventoryRollback_PreservesOriginalStock` — **passed** (0.013s)
- `orphanRemoval_DeletesItemWhenRemovedFromOrder` — **passed** (0.046s)
- `findByUserId_ReturnsUserOrders` — **passed** (0.012s)
- `orderAndItems_CascadePersistSuccessfully` — **passed** (0.01s)
- `findByOrderCode_ReturnsMatchingOrder` — **passed** (0.008s)
- `auditTimestamps_AutomaticallyCreated` — **passed** (0.006s)

### OrderServiceEdgeCaseTest

- `createOrder_UserNotFound_ThrowsExceptionAndNeverSaves` — **passed** (0.236s)
- `createOrder_MapsCustomerNotesAndInGameName` — **passed** (0.007s)
- `createOrder_InventoryNotFound_ThrowsExceptionAndNeverSaves` — **passed** (0.002s)
- `createOrder_MultiItemInsufficientStockOnSecondItem_ThrowsExceptionAndNeverSavesOrder` — **passed** (0.003s)
- `createOrder_UserWithoutProfile_DefaultsToRegularTier` — **passed** (0.003s)
- `createOrder_WholesaleTier_Applies15PercentDiscount` — **passed** (0.002s)
- `createOrder_MultiItem_DeductsStockAccurately` — **passed** (0.004s)
- `createOrder_OrderCodeFormatting_GeneratesSequentialCodes` — **passed** (0.004s)

### OrderServiceTest

- `updateItemTradeStatus_TerminalOrder_ThrowsConflict` — **passed** (0.054s)
- `updateItemTradeStatus_SkipSequence_FriendPendingToCompleted_ThrowsConflict` — **passed** (0.003s)
- `createOrder_UserNotFound_ThrowsException` — **passed** (0.002s)
- `getOrderById_Success` — **passed** (0.002s)
- `getOrderById_NotFound_ThrowsException` — **passed** (0.002s)
- `createOrder_InventoryNotFound_ThrowsException` — **passed** (0.002s)
- `updateItemTradeStatus_Success_TradeSent` — **passed** (0.004s)
- `updateItemTradeStatus_MultiItem_RemainsShippingWhenPartiallyCompleted` — **passed** (0.005s)
- `updateItemTradeStatus_Idempotent_ReturnsCurrentWithoutModification` — **passed** (0.002s)
- `createOrder_InsufficientStock_ThrowsException` — **passed** (0.013s)
- `updateItemTradeStatus_SkipSequence_UnassignedToTradeSent_ThrowsConflict` — **passed** (0.003s)
- `updateItemTradeStatus_ItemNotFoundInOrder_ThrowsException` — **passed** (0.003s)
- `updateItemTradeStatus_OrderNotFound_ThrowsException` — **passed** (0.002s)
- `updateItemTradeStatus_OrderNotInShipping_ThrowsConflict` — **passed** (0.003s)
- `getAllOrders_ReturnsList` — **passed** (0.002s)
- `updateItemTradeStatus_UnsupportedStatus_ThrowsException` — **passed** (0.002s)
- `updateItemTradeStatus_AllItemsCompleted_SyncsOrderCompletedViaStatePattern` — **passed** (0.007s)
- `updateItemTradeStatus_ReverseSequence_CompletedToTradeSent_ThrowsConflict` — **passed** (0.002s)
- `createOrder_Success` — **passed** (0.004s)
- `updateItemTradeStatus_UnassignedAccount_ThrowsConflict` — **passed** (0.002s)

### OrderStockAndSecurityTest$CancelOrderRestorationTests

- `cancelOrder_RestoresStockOnceToLatestHoldingInventory` — **passed** (0.007s)

### OrderStockAndSecurityTest$LastCardMatchingTests

- `bookLastCard_StillCanAutoMatchSuccessfully` — **passed** (0.023s)

### OrderStockAndSecurityTest$MultiItemRollbackTests

- `multiItemBooking_SecondItemFails_AbortsAndNeverSavesOrder` — **passed** (0.005s)

### OrderStockAndSecurityTest$OwnershipSecurityTests

- `customerCannotPlaceOrderForOtherUser` — **passed** (0.063s)
- `customerGetAllOrders_ReturnsOnlyOwnOrders` — **passed** (0.002s)
- `customerCannotViewOtherCustomerOrder` — **passed** (0.002s)
- `staffCanViewAnyCustomerOrder` — **passed** (0.002s)
- `staffCanPlaceOrderOnBehalfOfCustomer` — **passed** (0.003s)

### OrderStockAndSecurityTest$ReassignAccountTests

- `reassignAccount_TransfersStockBetweenInventoriesCorrectly` — **passed** (0.008s)

### OrderStockAndSecurityTest

