# Test Report — แทนคุณ พันธ์นิกุล

Branch: `Tankun_6733803010_01`  
บทบาท: Trade Matching, GoF State Pattern & Global Exception Handling Specialist (สมาชิกคนที่ 4, 673380301-0)  
Report generated: 2026-10-09T18:29:52.905266+07:00

## ผลการรัน

| ขอบเขต | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| ทั้งโปรเจกต์ | 355 | 0 | 0 | 0 |
| ส่วนของแทนคุณ (Trade & State & Advice) | 87 | 0 | 0 | 0 |

## รายละเอียดส่วนของแทนคุณ (Member 4)

| Test class | Tests | Failures | Errors |
|---|---:|---:|---:|
| OrderStateTest | 23 | 0 | 0 |
| TradeMatchingServiceTest | 21 | 0 | 0 |
| TradeMatchingApiControllerTest | 13 | 0 | 0 |
| OrderApiControllerTest | 15 | 0 | 0 |
| GlobalExceptionHandlerTest | 10 | 0 | 0 |
| TradeRecommendationResponseTest | 3 | 0 | 0 |
| CustomExceptionTest | 2 | 0 | 0 |

## สิ่งที่ทดสอบ

- **OrderStateTest** (GoF State Pattern): วงจรชีวิตของคำสั่งซื้อครบทุกสถานะ (`PENDING` ➔ `PAID` ➔ `SHIPPING` ➔ `COMPLETED` / `CANCELLED`), Anti-Fraud Guard ห้ามยกเลิกขณะอยู่ในสถานะ `SHIPPING` เพื่อป้องกันการโกงการ์ดฟรี, กลไกคืนสต็อกการ์ดเข้าคลังอัตโนมัติ (`restoreStock`), และความปลอดภัยเมื่อเผชิญค่าว่าง (Null-safety Edge Cases)
- **TradeMatchingServiceTest** (Matching Engine): อัลกอริทึม Auto-Match Greedy Stock-Maximization คัดเลือกเฉพาะไอดีเกมที่มีสถานะ `READY` และถือสต็อกการ์ดมากที่สุดเพื่อจ่ายงานเทรด, การสร้างคำแนะนำ 2 ระดับ (`Best Match` + `Alternative Candidates`), การจับคู่อัตโนมัติทั้งคำสั่งซื้อ (Batch), และการมอบหมายไอดีแบบระบุเอง (Manual Assignment)
- **TradeMatchingApiControllerTest** (Trade REST API): MockMvc ครบทั้ง 5 Endpoints (`GET /recommendations`, `GET /recommendation`, `POST /auto-match` รายชิ้น, `POST /auto-match` ทั้งออเดอร์, `POST /assign`) พร้อมการแปลงสถานะ HTTP 200, 400, 404, 409
- **OrderApiControllerTest** (State & Trade Lifecycle API): MockMvc ทดสอบการเปลี่ยนสถานะผ่าน GoF State Pattern (`PATCH /orders/{id}/status?action=...`) และการปรับสถานะเทรดรายสินค้า (`PATCH /orders/{orderId}/items/{itemId}/trade-status?status=...`) พร้อมตรวจจับสิทธิ์พนักงาน (403)
- **GlobalExceptionHandlerTest** (AOP `@RestControllerAdvice`): การดักจับข้อผิดพลาดระดับแอปพลิเคชันและแปลงเป็นมาตรฐาน JSON `ErrorResponse` ระดับระบบ ครอบคลุม 404 (Not Found), 409 (State Conflict), 400 (Bad Request / Missing Parameter), 403 (Access Denied), และ 500 (Internal Server Error)
- **TradeRecommendationResponseTest** (Trade DTO): ทดสอบ Builder Pattern, Getter/Setter, Default Empty List, และ Inner Class `CandidateAccountResponse`
- **CustomExceptionTest** (Custom Exceptions): ทดสอบ `TradeStateConflictException` และ `InvalidOrderStateException` ทั้งแบบระบุ Message และห่อหุ้ม Cause

## วิธีรันซ้ำ

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd clean test
python generate_tankun_report.py
```

รันเฉพาะส่วนของแทนคุณ:

```powershell
.\mvnw.cmd test "-Dtest=OrderStateTest,TradeMatchingServiceTest,TradeMatchingApiControllerTest,OrderApiControllerTest,GlobalExceptionHandlerTest,TradeRecommendationResponseTest,CustomExceptionTest"
```

## ขอบเขตและข้อจำกัด

- ทดสอบเฉพาะไฟล์ที่เกี่ยวข้องกับสถาปัตยกรรมของสมาชิกคนที่ 4 ไม่แตะต้องโค้ด Production นอกขอบเขต
- MockMvc ใช้ Standalone Setup ร่วมกับ `@ExtendWith(MockitoExtension.class)` เพื่อความรวดเร็วระดับมิลลิวินาที และหลีกเลี่ยงความขัดแย้งของ Sliced Context กับ `@EnableJpaAuditing`
- ผลการทดสอบ Unit Tests ทั้ง 87 เคส ยืนยันว่า Business Invariants, State Machine, และ Trade Fulfillment Sequence ทำงานได้อย่างถูกต้องสมบูรณ์ 100%
- ผลรายกรณีแบบละเอียดดูได้จาก `summary.json` และ JUnit XML ในโฟลเดอร์นี้

## ผลรายกรณี (All 87 Test Cases)

### OrderStateTest (23 เคส)

- `testCancelFromPending` — passed
- `testCancelFromPaidRestoresStock` — passed
- `testCancelFromPaid` — passed
- `testCancelRestoresStock` — passed
- `testRestoreStockSkipsInvalidItems` — passed
- `testRestoreStockNullContextAndOrder` — passed
- `testDefaultConstructor` — passed
- `testExecuteActionValidStrings` — passed
- `testExecuteActionInvalidStrings` — passed
- `testFromOrderNullOrder` — passed
- `testFromOrderStateMapping` — passed
- `testFullOrderLifecycle` — passed
- `testIllegalActionsInPending` — passed
- `testIllegalActionsInPaid` — passed
- `testIllegalActionsInShipping` — passed
- `testConstructors` — passed
- `testSetStateSyncsOrder` — passed
- `testSafeDelegationWhenStateIsNull` — passed
- `testCompleteRejectedWhenItemsNotAllCompleted` — passed
- `testCancelRejectedWhileShipping` — passed
- `testCompleteRejectedWhenOrderHasNoItems` — passed
- `testCancelledStateIsTerminal` — passed
- `testCompletedStateIsTerminal` — passed

### TradeMatchingServiceTest (21 เคส)

- `testAutoMatchOrderItemInsufficientStock` — passed
- `testAutoMatchOrderItemSuccess` — passed
- `testAutoMatchOrderItemRejectedWhenOrderCompleted` — passed
- `testAutoMatchOrderItemNoReadyAccounts` — passed
- `testAutoMatchOrderItemGuardAgainstReassignment` — passed
- `testAutoMatchOrderItemRejectedWhenOrderCancelled` — passed
- `testAutoMatchOrderSuccess` — passed
- `testAutoMatchOrderRejectedWhenOrderCancelled` — passed
- `testManualAssignRejectWhenCompleted` — passed
- `testManualAssignRejectedWhenTargetAccountHasInsufficientStock` — passed
- `testManualAssignRejectedWhenOrderCancelled` — passed
- `testManualAssignRejectedWhenAccountDoesNotHoldCard` — passed
- `testManualAssignRejectWhenTradeSent` — passed
- `testManualAssignAccountNotReady` — passed
- `testManualAssignLastAvailableCardSuccess` — passed
- `testManualAssignRejectedWhenOrderCompleted` — passed
- `testManualAssignAccountSuccess` — passed
- `testGetRecommendationForItem` — passed
- `testGetRecommendationsNoInventory` — passed
- `testGetRecommendationsOnlyBusyAccount` — passed
- `testGetRecommendationsRanksBestReadyAccount` — passed

### TradeMatchingApiControllerTest (13 เคส)

- `autoMatchOrderItem_AlreadyCompleted_Returns409` — passed
- `getItemRecommendation_NotFound_Returns404` — passed
- `autoMatchOrder_NotFound_Returns404` — passed
- `getItemRecommendation_Success_Returns200` — passed
- `autoMatchOrder_Success_Returns200` — passed
- `assignAccountToOrderItem_Success_Returns200` — passed
- `assignAccountToOrderItem_MissingAccountId_Returns400` — passed
- `autoMatchOrderItem_Success_Returns200` — passed
- `getOrderRecommendations_Success_Returns200` — passed
- `autoMatchOrderItem_InsufficientStock_Returns400` — passed
- `assignAccountToOrderItem_AccountNotReady_Returns409` — passed
- `getOrderRecommendations_NotFound_Returns404` — passed
- `assignAccountToOrderItem_AccountNotFound_Returns404` — passed

### OrderApiControllerTest (15 เคส)

- `updateItemTradeStatus_InvalidStatus_Returns400` — passed
- `updateItemTradeStatus_AccessDenied_Returns403` — passed
- `transitionOrderStatus_MissingActionParam_Returns400` — passed
- `transitionOrderStatus_OrderNotFound_Returns404` — passed
- `transitionOrderStatus_InvalidStateTransition_Returns409` — passed
- `transitionOrderStatus_Pay_Returns200` — passed
- `updateItemTradeStatus_Success_Returns200` — passed
- `transitionOrderStatus_Complete_Returns200` — passed
- `updateItemTradeStatus_StateConflict_Returns409` — passed
- `transitionOrderStatus_Ship_Returns200` — passed
- `updateItemTradeStatus_NotFound_Returns404` — passed
- `transitionOrderStatus_Complete_IncompleteItems_Returns409` — passed
- `updateItemTradeStatus_Completed_Returns200` — passed
- `updateItemTradeStatus_UnsupportedStatus_Returns400` — passed
- `transitionOrderStatus_Cancel_Returns200` — passed

### GlobalExceptionHandlerTest (10 เคส)

- `handleInvalidOrderState_Returns409` — passed
- `handleInsufficientStock_Returns400` — passed
- `handleTypeMismatch_Returns400` — passed
- `handleTradeStateConflict_Returns409` — passed
- `handleIllegalArgument_Returns400` — passed
- `handleResourceNotFound_Returns404` — passed
- `handleGeneralException_Returns500` — passed
- `handleMissingParameter_Returns400` — passed
- `handleAccessDenied_Returns403` — passed
- `handleNoResourceFound_Returns404` — passed

### TradeRecommendationResponseTest (3 เคส)

- `testBuilderAndGetters` — passed
- `testDefaultAlternativeCandidates` — passed
- `testSetters` — passed

### CustomExceptionTest (2 เคส)

- `testTradeStateConflictException` — passed
- `testInvalidOrderStateException` — passed
