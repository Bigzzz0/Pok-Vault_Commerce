# 📘 PokéVault Commerce — Knowledge & Commit Log
> **ผู้รับผิดชอบ**: สมาชิกคนที่ 4 — นายแทนคุณ พันธ์นิกุล (673380301-0)  
> **บทบาท**: Trade Matching & State Pattern Specialist  
> **Branch**: `Tankun_6733803010_01`  

---

## 📌 ภาพรวมงานที่รับผิดชอบ (Role Overview)
สมาชิกคนที่ 4 รับผิดชอบ 3 ระบบหลักของแพลตฟอร์ม PokéVault Commerce:
1. **Global Exception Handling & API Error Contract**: จัดการข้อผิดพลาดทั้งระบบ คืนค่า Error Response ที่เป็นมาตรฐานเดียวกัน
2. **GoF State Pattern (Order Lifecycle Management)**: ควบคุมสถานะของคำสั่งซื้อ (`PENDING` ➔ `PAID` ➔ `SHIPPING` ➔ `COMPLETED` / `CANCELLED`) ป้องกันการกดยกเลิกในจังหวะที่ไม่ถูกต้อง และคืนสต็อกการ์ดอัตโนมัติเมื่อออเดอร์ถูกยกเลิก
3. **In-Game Trade Matching Algorithm**: อัลกอริทึมค้นหาไอดีเกมของร้านที่มีการ์ดใบที่ลูกค้าสั่งซื้อ และอยู่ในสถานะ `READY` เพื่อจับคู่อัตโนมัติ (`autoMatchBestAccount`) และจ่ายงานให้ `OrderItem`

---

## 📋 บันทึกประวัติและรายละเอียดของแต่ละ Commit

### ✅ Commit 1: Custom Exceptions
- **Commit Hash**: `2d9cc98`
- **Commit Message**: `feat: create custom exceptions for stock and invalid order states`
- **โฟลเดอร์หลัก**: `src/main/java/com/pokevault/common/exception/`

#### รายละเอียดและการทำงาน
สร้าง Unchecked Custom Exceptions (สืบทอดจาก `RuntimeException`) เพื่อใช้เฉพาะทางในโดเมน PokéVault:
1. **`ResourceNotFoundException.java`**: ใช้เมื่อค้นหาข้อมูลใน Database แล้วไม่พบ (เช่น หา OrderId, CardId ไม่เจอ)
2. **`InsufficientStockException.java`**: ใช้เมื่อสต็อกการ์ดในคลังไม่พอ โดยมี constructor พิเศษ `(cardName, requestedQty, availableQty)` ช่วยสร้าง error message ชัดเจน
3. **`InvalidOrderStateException.java`**: ใช้ใน State Pattern เมื่อมีการเรียก transition สถานะผิดกฎ (เช่น พยายามกดยกเลิกออเดอร์ที่ถูกส่ง `SHIPPING` ไปแล้ว)

---

### ✅ Commit 2: Global Exception Handler with RestControllerAdvice
- **Commit Hash**: `aebf676`
- **Commit Message**: `feat: implement GlobalExceptionHandler with RestControllerAdvice`
- **ไฟล์ที่สร้าง/แก้ไข**:
  1. `src/main/java/com/pokevault/common/exception/InvalidOrderStateException.java` (แก้ไขชื่อคลาสให้ตรงกับชื่อไฟล์)
  2. `src/main/java/com/pokevault/modules/trade/advice/GlobalExceptionHandler.java` (สร้างคลาส Centralized Exception Handler)

#### การทำงานของโค้ดใน Commit 2
- **`@RestControllerAdvice`**: เป็น Annotation ของ Spring Boot ที่นำหลักการ **AOP (Aspect-Oriented Programming)** มาใช้ โดยทำหน้าที่เป็นตัวดักจับ (Interceptor) ข้อยกเว้นที่ถูกโยน (`throw`) ออกมาจาก `@RestController` ทั้งหมดในระบบ
- **Handler Methods แต่ละตัว**:
  1. `@ExceptionHandler(ResourceNotFoundException.class)` ➔ คืนค่า HTTP 404 (Not Found)
  2. `@ExceptionHandler(InsufficientStockException.class)` ➔ คืนค่า HTTP 400 (Bad Request)
  3. `@ExceptionHandler(InvalidOrderStateException.class)` ➔ คืนค่า HTTP 400 (Bad Request)
  4. `@ExceptionHandler(MethodArgumentNotValidException.class)` ➔ ดักจับ Validation Error จาก `@Valid` ใน DTO
  5. `@ExceptionHandler(Exception.class)` ➔ Fallback ดักจับ Error อื่นๆ ที่ไม่คาดคิด คืนค่า HTTP 500 (Internal Server Error)

---

### ✅ Commit 3: Standard ErrorResponse DTO Format
- **Commit Hash**: `5060b86`
- **Commit Message**: `feat: define standard ErrorResponse format for API errors`
- **ไฟล์ที่สร้าง/แก้ไข**:
  1. `src/main/java/com/pokevault/common/response/ErrorResponse.java` (สร้าง DTO มาตรฐาน)
  2. `src/main/java/com/pokevault/modules/trade/advice/GlobalExceptionHandler.java` (Refactor ให้คืนค่า `ResponseEntity<ErrorResponse>`)

#### การทำงานและจุดเด่นของโค้ดใน Commit 3
- **Strongly-typed DTO**: สร้างคลาส `ErrorResponse` ที่ชัดเจน มีฟิลด์: `timestamp`, `status`, `error`, `message`, `path`, `details`
- **Lombok & Jackson Annotations**:
  - `@Builder`, `@Getter`, `@Setter`: ช่วยให้สร้าง Object ได้สะดวกและคลีนด้วย Builder Pattern
  - `@JsonInclude(JsonInclude.Include.NON_NULL)`: ซ่อนฟิลด์ที่มีค่าเป็น `null` ทำให้ Response สะอาดตา
- **Refactor `GlobalExceptionHandler`**: ปรับเปลี่ยน Return Type จาก `Map<String, Object>` เป็น `ResponseEntity<ErrorResponse>` ส่งผลให้ REST API ของระบบมีมาตรฐานเดียวกันทั้งระบบ

---

### ✅ Commit 4: OrderState Interface for GoF State Pattern
- **Commit Hash**: `daecdd9`
- **Commit Message**: `feat: create OrderState interface for State Pattern lifecycle`
- **ไฟล์ที่สร้าง/แก้ไข**:
  - `src/main/java/com/pokevault/modules/trade/state/OrderState.java`

#### การทำงานและสถาปัตยกรรม (Design Decision)
- **GoF State Pattern Interface**: กำหนดพฤติกรรมการเปลี่ยนสถานะของคำสั่งซื้อ ประกอบด้วย:
  - `pay(OrderContext context)`: ดำเนินการชำระเงิน
  - `ship(OrderContext context)`: ส่งมอบการ์ดในเกม (Trade Sent)
  - `complete(OrderContext context)`: ปิดออเดอร์เมื่อลูกค้ายืนยันรับการ์ดครบ
  - `cancel(OrderContext context)`: ขอยกเลิกคำสั่งซื้อ
  - `OrderStatus getStatus()`: ดึงค่าสถานะของ State ปัจจุบัน
- **Default Method Guard**: กำหนดให้ทุก Action ใน Interface มี `default implementation` ที่โยน `InvalidOrderStateException` ออกมาโดยอัตโนมัติ  
  **ประโยชน์**: คลาส State ย่อยที่จะสร้างในรอบหน้า (`PendingOrderState`, `PaidOrderState`, `ShippingOrderState` ฯลฯ) จะเลือก Override เฉพาะ Transition ที่ถูกต้องเท่านั้น หากมีการสั่งผิดเงื่อนไข จะถูกปฏิเสธทันทีและถูกจัดการด้วย `GlobalExceptionHandler` อย่างปลอดภัย

### ✅ Commit 5: OrderContext for State Pattern
- **Commit Hash**: `9b8d72e`
- **Commit Message**: `feat: implement OrderContext to manage current order state`
- **ไฟล์ที่สร้าง/แก้ไข**:
  - `src/main/java/com/pokevault/modules/trade/state/OrderContext.java`

#### การทำงานและสถาปัตยกรรม (Design Decision)
- **GoF State Pattern Context**: ทำหน้าที่เป็นตัวกลาง (Context) ในการถือครองข้อมูลคำสั่งซื้อ (`Order`) และสถานะปัจจุบัน (`OrderState currentState`)
- **Encapsulation & Delegation**:
  - เมธอด `setState(OrderState state)`: ใช้สำหรับอัปเดตสถานะของ Context
  - เมธอด `pay()`, `ship()`, `complete()`, `cancel()`: ทำการ Delegate (ส่งต่อ) การทำงานไปยัง `currentState.<action>(this)` ป้องกันการใช้คำสั่ง `if-else` หรือ `switch-case` ขนาดยาว
  - เมธอด `getStatus()`: ดึงค่า `OrderStatus` จาก `currentState` อำนวยความสะดวกให้ Service และ Controller เรียกดูสถานะได้ทันที
- **Flexible Constructors**: รองรับทั้งการสร้าง Context เปล่า, สร้างพร้อมกำหนด State เริ่มต้น, และสร้างแบบผูกกับ `Order`

---

### ✅ Commit 6: PendingOrderState and PaidOrderState
- **Commit Hash**: `730e21a` (และ `bd8db0c`)
- **Commit Message**: `feat: implement PendingOrderState and PaidOrderState`
- **ไฟล์ที่สร้าง/แก้ไข**:
  - `src/main/java/com/pokevault/domain/enums/OrderStatus.java` (เพิ่ม enum ครบ 5 สถานะ)
  - `src/main/java/com/pokevault/modules/trade/state/PendingOrderState.java`
  - `src/main/java/com/pokevault/modules/trade/state/PaidOrderState.java`
  - `src/main/java/com/pokevault/modules/trade/state/CancelledOrderState.java`
  - `src/main/java/com/pokevault/modules/trade/state/ShippingOrderState.java`
  - `src/main/java/com/pokevault/modules/trade/state/CompletedOrderState.java`

#### การทำงานและสถาปัตยกรรม (Design Decision)
- **PendingOrderState**: กำหนดให้ `pay(context)` สลับสถานะไปยัง `PaidOrderState` และ `cancel(context)` สลับสถานะไปยัง `CancelledOrderState` (การเรียก `ship()` หรือ `complete()` จะถูก default method ใน `OrderState` โยน Exception อัตโนมัติ)
- **PaidOrderState**: กำหนดให้ `ship(context)` สลับสถานะไปยัง `ShippingOrderState` และ `cancel(context)` สลับสถานะไปยัง `CancelledOrderState` เพื่อคืนเงิน
- **OrderStatus Enum**: ประกาศ enum ครบ 5 สถานะ (`PENDING`, `PAID`, `SHIPPING`, `COMPLETED`, `CANCELLED`) และ implement `getStatus()` ในทุก Concrete State คลาส เพื่อป้องกัน Build Failure

---

### ✅ Commit 7: ShippingOrderState with Cancel Rejection Guard
- **Commit Hash**: `067bac0`
- **Commit Message**: `feat: implement ShippingOrderState with cancel rejection guard`
- **ไฟล์ที่สร้าง/แก้ไข**:
  - `src/main/java/com/pokevault/modules/trade/state/ShippingOrderState.java`

#### การทำงานและสถาปัตยกรรม (Design Decision)
- **In-Game Trade State**: สถานะคำสั่งซื้อเมื่อร้านค้ากำลังส่งมอบการ์ดในเกม Pokémon Pocket (`SHIPPING`)
- **Action Transition**:
  - `complete(OrderContext context)`: ดำเนินการยืนยันการรับการ์ดสำเร็จ ➔ สลับ Context เป็น `CompletedOrderState`
- **Business Invariant (Cancel Rejection Guard)**:
  - Override `cancel(OrderContext context)` ให้โยน `InvalidOrderStateException("Cannot cancel order while cards are being shipped in game")`
  - **เหตุผลทางธุรกิจ**: เมื่อเริ่มส่งการ์ดในเกมแล้ว ระบบต้องปฏิเสธการขอยกเลิกคำสั่งซื้อทุกกรณี เพื่อป้องกันช่องโหว่การได้การ์ดฟรี (Free-card loss protection)

---

## ⏸️ Commit 8: CompletedOrderState and CancelledOrderState with Stock Restore (คิวงานถัดไป)
- **Roadmap Commit 8**: `feat: implement CompletedOrderState and CancelledOrderState with stock restore`
- **ไฟล์เป้าหมาย**:
  - `src/main/java/com/pokevault/modules/trade/state/CompletedOrderState.java`
  - `src/main/java/com/pokevault/modules/trade/state/CancelledOrderState.java`
- **สรุปสิ่งที่ต้องทำเมื่อกลับมา**:
  - `CompletedOrderState`: Terminal state ที่คำสั่งซื้อเสร็จสมบูรณ์ ไม่อนุญาตให้เปลี่ยนสถานะใดๆ ต่อ
  - `CancelledOrderState`: Terminal state สำหรับคำสั่งซื้อที่ยกเลิก โดยต้องคืนสต็อกการ์ดเข้าสู่คลัง (`inventory.restoreStock()`) อัตโนมัติ

---

## 🗺️ แผนผังความก้าวหน้า 15 Commits (Member 4 Tracker)
- [x] **Commit 1**: `feat: create custom exceptions for stock and invalid order states`
- [x] **Commit 2**: `feat: implement GlobalExceptionHandler with RestControllerAdvice`
- [x] **Commit 3**: `feat: define standard ErrorResponse format for API errors`
- [x] **Commit 4**: `feat: create OrderState interface for State Pattern lifecycle`
- [x] **Commit 5**: `feat: implement OrderContext to manage current order state`
- [x] **Commit 6**: `feat: implement PendingOrderState and PaidOrderState`
- [x] **Commit 7**: `feat: implement ShippingOrderState with cancel rejection guard`
- [ ] **Commit 8**: `feat: implement CompletedOrderState and CancelledOrderState with stock restore` *(คิวงานถัดไป)*
- [ ] **Commit 9**: `feat: implement state transition endpoint in OrderApiController`
- [ ] **Commit 10**: `feat: define TradeRecommendationResponse DTO`
- [ ] **Commit 11**: `feat: implement TradeMatchingService with account recommendation query`
- [ ] **Commit 12**: `feat: implement auto-match best account assignment algorithm`
- [ ] **Commit 13**: `feat: add endpoints for trade recommendations and account assignment`
- [ ] **Commit 14**: `test: add unit tests for OrderState transitions and guards`
- [ ] **Commit 15**: `test: add unit test for TradeMatchingServiceImpl auto-match logic`


