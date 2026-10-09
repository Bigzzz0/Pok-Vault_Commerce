# 🧪 PokéVault Commerce — Member 4 Unit Testing Guide (README-TEST.md)

> **ผู้รับผิดชอบ**: สมาชิกคนที่ 4 — นายแทนคุณ พันธ์นิกุล (673380301-0)  
> **บทบาท**: Trade Matching, GoF State Pattern & Global Exception Handling Specialist  
> **Branch**: `Tankun_6733803010_01`  
> **วิชา**: CP353002 Principles of Software Design and Development (Spring Boot 3.4.3 / Java 17)  
> **ผลการทดสอบทั้งหมดของคนที่ 4**: ✅ **7 Test Classes / 74 Test Scenarios (100% BUILD SUCCESS — 0 Failures, 0 Errors)**  
> **เวลาประมวลผลเฉลี่ย**: ~2-4 วินาที  

---

## 📑 สารบัญ (Table of Contents)

1. [ขอบเขตงานและคลาสที่สมาชิกคนที่ 4 รับผิดชอบ](#1-ขอบเขตงานและคลาสที่สมาชิกคนที่-4-รับผิดชอบ)
2. [สรุปผลและตารางคลาสทดสอบทั้งหมดของคนที่ 4 (74 เคส)](#2-สรุปผลและตารางคลาสทดสอบทั้งหมดของคนที่-4-74-เคส)
3. [คำสั่งและวิธีรันเทสเฉพาะของสมาชิกคนที่ 4](#3-คำสั่งและวิธีรันเทสเฉพาะของสมาชิกคนที่-4)
4. [เจาะลึกรายละเอียดแต่ละ Test Suite ของสมาชิกคนที่ 4](#4-เจาะลึกรายละเอียดแต่ละ-test-suite-ของสมาชิกคนที่-4)
   - [4.1 OrderStateTest — GoF State Pattern Lifecycle & Guards (21 เคส)](#41-orderstatetest--gof-state-pattern-lifecycle--guards-21-เคส)
   - [4.2 TradeMatchingServiceTest — Auto-Match & Two-Tier Recommendation (12 เคส)](#42-tradematchingservicetest--auto-match--two-tier-recommendation-12-เคส)
   - [4.3 TradeMatchingApiControllerTest — Trade REST API Endpoints (13 เคส)](#43-tradematchingapicontrollertest--trade-rest-api-endpoints-13-เคส)
   - [4.4 OrderApiControllerTest — State Transitions & Trade Status Lifecycle (13 เคส)](#44-orderapicontrollertest--state-transitions--trade-status-lifecycle-13-เคส)
   - [4.5 GlobalExceptionHandlerTest — Centralized HTTP Error Handling (10 เคส)](#45-globalexceptionhandlertest--centralized-http-error-handling-10-เคส)
   - [4.6 TradeRecommendationResponseTest — Trade DTO & Candidate Model (3 เคส)](#46-traderecommendationresponsetest--trade-dto--candidate-model-3-เคส)
   - [4.7 CustomExceptionTest — Custom Domain Exceptions (2 เคส)](#47-customexceptiontest--custom-domain-exceptions-2-เคส)
5. [การตรวจสอบกฎความปลอดภัยทางธุรกิจและ Design Patterns (Business Invariants & Patterns)](#5-การตรวจสอบกฎความปลอดภัยทางธุรกิจและ-design-patterns-business-invariants--patterns)
6. [แนวทางการเขียนเทสและสถาปัตยกรรมทดสอบที่เลือกใช้ (Testing Architecture & Practices)](#6-แนวทางการเขียนเทสและสถาปัตยกรรมทดสอบที่เลือกใช้-testing-architecture--practices)
7. [การแก้ปัญหาทั่วไปเมื่อรันเทส (Troubleshooting)](#7-การแก้ปัญหาทั่วไปเมื่อรันเทส-troubleshooting)

---

## 1. ขอบเขตงานและคลาสที่สมาชิกคนที่ 4 รับผิดชอบ

สมาชิกคนที่ 4 (**นายแทนคุณ พันธ์นิกุล**) พัฒนาและเป็นเจ้าของคลาสหลักในระบบดังต่อไปนี้:

| เลเยอร์ / โมดูล | คลาสที่พัฒนาขึ้นใน Branch นี้ | ความรับผิดชอบทางธุรกิจ |
| :--- | :--- | :--- |
| **GoF State Pattern** | `OrderState`, `OrderContext`, `PendingOrderState`, `PaidOrderState`, `ShippingOrderState`, `CompletedOrderState`, `CancelledOrderState` | ควบคุม State Machine ของคำสั่งซื้อ, ป้องกันการข้ามสถานะ, ป้องกันการยกเลิกระหว่างขนส่ง (Anti-Fraud Guard), และคืนสต็อกอัตโนมัติ |
| **Trade Matching Service** | `TradeMatchingService`, `TradeMatchingServiceImpl` | อัลกอริทึม Auto-Match Stock-Maximization ค้นหาไอดีเกมสถานะ `READY` ที่มีสต็อกการ์ดสูงสุด, คำแนะนำ 2 ระดับ (Best Match + Alternative Candidates), และการจ่ายงานเทรด |
| **REST Controllers** | `TradeMatchingApiController`, `OrderApiController` (ส่วน State Transition และ Trade Status Lifecycle) | REST APIs สำหรับจับคู่ไอดีเกม (`/api/v1/trades/**`), การเปลี่ยนสถานะคำสั่งซื้อ (`PATCH /orders/{id}/status`), และการเปลี่ยนสถานะเทรดรายสินค้า (`PATCH /orders/{orderId}/items/{itemId}/trade-status`) |
| **Exception & Advice** | `GlobalExceptionHandler`, `TradeStateConflictException`, `InvalidOrderStateException` | ตัวดักจับข้อผิดพลาดรวมศูนย์ระดับระบบ (AOP `@RestControllerAdvice`) และแปลงเป็นมาตรฐาน JSON `ErrorResponse` (400, 403, 404, 409, 500) |
| **Data Transfer Objects** | `TradeRecommendationResponse`, `CandidateAccountResponse` | DTO สำหรับส่งข้อมูลผลลัพธ์คำแนะนำไอดีเกมและตัวเลือกสำรองให้ Frontend |

---

## 2. สรุปผลและตารางคลาสทดสอบทั้งหมดของคนที่ 4 (74 เคส)

คลาสทดสอบทั้งหมดด้านล่างนี้ถูกสร้างขึ้นเพื่อทดสอบคลาสของ**นายแทนคุณ พันธ์นิกุล**โดยเฉพาะ:

```
src/test/java/com/pokevault/
├── common/
│   └── exception/
│       └── CustomExceptionTest.java                  [ 2 เคส ] ✅ Passed
├── modules/
│   ├── order/
│   │   └── OrderApiControllerTest.java               [ 13 เคส ] ✅ Passed
│   └── trade/
│       ├── advice/
│       │   └── GlobalExceptionHandlerTest.java       [ 10 เคส ] ✅ Passed
│       ├── controller/
│       │   └── TradeMatchingApiControllerTest.java   [ 13 เคส ] ✅ Passed
│       ├── dto/
│       │   └── TradeRecommendationResponseTest.java  [  3 เคส ] ✅ Passed
│       ├── service/
│       │   └── TradeMatchingServiceTest.java         [ 12 เคส ] ✅ Passed
│       └── state/
│           └── OrderStateTest.java                   [ 21 เคส ] ✅ Passed
```

| # | Test Suite Class | แพ็กเกจ (Package) | ขอบเขตการทดสอบที่รับผิดชอบ | จำนวนเคส | ผลลัพธ์ |
| :---: | :--- | :--- | :--- | :---: | :---: |
| 1 | **`OrderStateTest`** | `com.pokevault.modules.trade.state` | วงจรชีวิต GoF State Machine ทุก Transition, Anti-Fraud Guard ป้องกันยกเลิกระหว่างส่งมอบ, ระบบคืนสต็อกการ์ดอัตโนมัติ (Stock Restoration), และ Null-safety Edge Cases | **21 เคส** | ✅ PASS |
| 2 | **`TradeMatchingServiceTest`** | `com.pokevault.modules.trade.service` | อัลกอริทึม Auto-Match Stock-Maximization, การสร้าง Recommendation 2 ระดับ, การจ่ายงานทั้งคำสั่งซื้อ (Batch), และการมอบหมายไอดีแบบ Manual | **12 เคส** | ✅ PASS |
| 3 | **`TradeMatchingApiControllerTest`** | `com.pokevault.modules.trade.controller` | MockMvc ครบทั้ง 5 REST API Endpoints ของ Trade Matching, การตรวจสอบ Parameter, และการแปลงสถานะ HTTP (200, 400, 404, 409) | **13 เคส** | ✅ PASS |
| 4 | **`OrderApiControllerTest`** | `com.pokevault.modules.order` | MockMvc ทดสอบ State Transition API (`PATCH /status?action=...`) และ Trade Status Lifecycle API (`PATCH /trade-status?status=...`) พร้อมตรวจจับสิทธิ์ (403) | **13 เคส** | ✅ PASS |
| 5 | **`GlobalExceptionHandlerTest`** | `com.pokevault.modules.trade.advice` | การดักจับ Exception รวมศูนย์และแปลงเป็น JSON `ErrorResponse` ระดับระบบ ครอบคลุม 404, 409, 400, 403, 500 ครบถ้วน | **10 เคส** | ✅ PASS |
| 6 | **`TradeRecommendationResponseTest`** | `com.pokevault.modules.trade.dto` | ทดสอบ DTO, Builder Pattern, Default Empty List และ Inner Class `CandidateAccountResponse` | **3 เคส** | ✅ PASS |
| 7 | **`CustomExceptionTest`** | `com.pokevault.common.exception` | ทดสอบ Custom Exception Classes: `TradeStateConflictException` และ `InvalidOrderStateException` ทั้งแบบระบุ Message และแบบห่อหุ้ม Cause | **2 เคส** | ✅ PASS |
| **รวม** | **7 Test Classes** | — | **ชุดทดสอบคลาสของสมาชิกคนที่ 4 ทั้งหมด** | **74 เคส** | **100% PASS** |

---

## 3. คำสั่งและวิธีรันเทสเฉพาะของสมาชิกคนที่ 4

### 3.1 คำสั่งรันผ่าน PowerShell (Windows)

```powershell
# ตั้งค่าชี้ไปยัง Java 17 LTS (Eclipse Temurin)
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

# รันชุดเทสของสมาชิกคนที่ 4 ทั้ง 7 คลาส (74 เคส)
.\mvnw.cmd test "-Dtest=OrderStateTest,TradeMatchingServiceTest,TradeMatchingApiControllerTest,OrderApiControllerTest,GlobalExceptionHandlerTest,TradeRecommendationResponseTest,CustomExceptionTest"
```

> **ผลลัพธ์ที่ได้จากการรันจริง**:
> ```
> [INFO] Running com.pokevault.modules.trade.controller.TradeMatchingApiControllerTest
> [INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
> [INFO] Running com.pokevault.modules.order.OrderApiControllerTest
> [INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
> [INFO] Running com.pokevault.modules.trade.advice.GlobalExceptionHandlerTest
> [INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
> [INFO] Running com.pokevault.modules.trade.service.TradeMatchingServiceTest
> [INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
> [INFO] Running com.pokevault.modules.trade.state.OrderStateTest
> [INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
> [INFO] Running com.pokevault.modules.trade.dto.TradeRecommendationResponseTest
> [INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
> [INFO] Running com.pokevault.common.exception.CustomExceptionTest
> [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
> [INFO] 
> [INFO] Results:
> [INFO] Tests run: 74, Failures: 0, Errors: 0, Skipped: 0
> [INFO] ------------------------------------------------------------------------
> [INFO] BUILD SUCCESS
> [INFO] ------------------------------------------------------------------------
> ```

---

### 3.2 คำสั่งรันแยกรายคลาสของคนที่ 4

* **รันเฉพาะ State Pattern**:
  ```powershell
  .\mvnw.cmd test -Dtest=OrderStateTest
  ```
* **รันเฉพาะ Trade Matching Service**:
  ```powershell
  .\mvnw.cmd test -Dtest=TradeMatchingServiceTest
  ```
* **รันเฉพาะ Trade Matching REST Controller**:
  ```powershell
  .\mvnw.cmd test -Dtest=TradeMatchingApiControllerTest
  ```
* **รันเฉพาะ Order REST Controller (State & Trade Endpoints)**:
  ```powershell
  .\mvnw.cmd test -Dtest=OrderApiControllerTest
  ```
* **รันเฉพาะ Global Exception Handler**:
  ```powershell
  .\mvnw.cmd test -Dtest=GlobalExceptionHandlerTest
  ```

---

## 4. เจาะลึกรายละเอียดแต่ละ Test Suite ของสมาชิกคนที่ 4

### 4.1 `OrderStateTest` — GoF State Pattern Lifecycle & Guards (21 เคส)
* **ไฟล์**: [`src/test/java/com/pokevault/modules/trade/state/OrderStateTest.java`](file:///e:/Coding/Pok-Vault_Commerce/src/test/java/com/pokevault/modules/trade/state/OrderStateTest.java)
* **คลาสที่ถูกทดสอบ**: `OrderContext`, `OrderState`, `PendingOrderState`, `PaidOrderState`, `ShippingOrderState`, `CompletedOrderState`, `CancelledOrderState`
* **ประเด็นที่ทดสอบ (9 Nested Classes)**:
  1. `HappyPathTests`: วงจรชีวิตปกติครบกระบวนการ `PENDING` ➔ `PAID` ➔ `SHIPPING` ➔ `COMPLETED`
  2. `CancellationTests`: การยกเลิกจาก `PENDING` และ `PAID` ➔ สั่งคืนสต็อกเข้าคลังอัตโนมัติ (`restoreStock`)
  3. `ShippingGuardTests` (**Anti-Fraud Guard**): ปฏิเสธการยกเลิกคำสั่งซื้อเมื่ออยู่ในสถานะ `SHIPPING` ป้องกันการขอยกเลิกขณะการ์ดถูกส่งในเกมไปแล้ว
  4. `TerminalStateTests`: สถานะ `COMPLETED` และ `CANCELLED` ปฏิเสธทุก Action ซ้ำซ้อน
  5. `IllegalTransitionTests`: ปฏิเสธการข้ามสถานะที่ไม่ได้รับอนุญาต เช่น สั่ง `ship()` ขณะเป็น `PENDING`
  6. `ExecuteActionStringTests`: ทดสอบการรัน Action ผ่านข้อความตัวอักษร Case-insensitive ("PAY", "ship", "complete")
  7. `FactoryMethodTests`: การสร้าง `OrderContext.fromOrder(order)` แปลง Entity เป็น Concrete State อย่างแม่นยำ
  8. `OrderContextMutatorTests`: ทดสอบ Constructor, Setter และการซิงค์สถานะเข้าสู่ Entity
  9. `CancelledOrderStateEdgeCaseTests`: ความปลอดภัยในการทำงานเมื่อเผชิญข้อมูลที่เป็น Null (Defensive Null-safety)

---

### 4.2 `TradeMatchingServiceTest` — Auto-Match & Two-Tier Recommendation (12 เคส)
* **ไฟล์**: [`src/test/java/com/pokevault/modules/trade/service/TradeMatchingServiceTest.java`](file:///e:/Coding/Pok-Vault_Commerce/src/test/java/com/pokevault/modules/trade/service/TradeMatchingServiceTest.java)
* **คลาสที่ถูกทดสอบ**: `TradeMatchingServiceImpl` (Implementing `TradeMatchingService`)
* **ประเด็นที่ทดสอบ (4 Nested Classes)**:
  1. `RecommendationQueryTests`:
     - สร้างคำแนะนำ 2 ระดับ: อันดับ 1 สต็อกสูงสุด (`Best Match`) และทางเลือกสำรอง (`Alternative Candidates`)
     - กรองเฉพาะบัญชีที่มีสถานะ `READY` เท่านั้น บัญชี `BUSY_TRADING` ถูกกันออก
  2. `AutoMatchAlgorithmTests`:
     - จับคู่บัญชีที่มีสต็อกมากที่สุดให้อัตโนมัติและปรับสถานะ `OrderItem.tradeStatus` เป็น `FRIEND_PENDING`
     - หากไม่มีไอดีเกมใดมีสต็อกเพียงพอ โยน `InsufficientStockException`
     - หากสินค้าเทรดเสร็จสิ้นแล้ว (`COMPLETED`) ปฏิเสธการจับคู่ใหม่ (`InvalidOrderStateException`)
  3. `BatchAutoMatchTests`:
     - ฟังก์ชัน `autoMatchOrder(orderId)` ทำการจับคู่อัตโนมัติครบทุกรายการสินค้าในคำสั่งซื้อ
  4. `ManualAssignmentTests`:
     - พนักงานเลือกกำหนดไอดีเกมเองผ่าน `assignAccountToOrderItem(orderItemId, accountId)`
     - ปฏิเสธการมอบหมายหากบัญชีไม่ได้อยู่ในสถานะ `READY`

---

### 4.3 `TradeMatchingApiControllerTest` — Trade REST API Endpoints (13 เคส)
* **ไฟล์**: [`src/test/java/com/pokevault/modules/trade/controller/TradeMatchingApiControllerTest.java`](file:///e:/Coding/Pok-Vault_Commerce/src/test/java/com/pokevault/modules/trade/controller/TradeMatchingApiControllerTest.java)
* **คลาสที่ถูกทดสอบ**: `TradeMatchingApiController`
* **ประเด็นที่ทดสอบ**:
  - `GET /api/v1/trades/orders/{orderId}/recommendations` (200 OK ดึงสำเร็จ, 404 ไม่พบ Order)
  - `GET /api/v1/trades/items/{orderItemId}/recommendation` (200 OK ดึงสำเร็จ, 404 ไม่พบ Item)
  - `POST /api/v1/trades/items/{orderItemId}/auto-match` (200 OK สำเร็จ, 400 สต็อกไม่พอ, 409 ซ้ำซ้อนเมื่อ Completed)
  - `POST /api/v1/trades/orders/{orderId}/auto-match` (200 OK จับคู่ทั้งออเดอร์, 404 ไม่พบ Order)
  - `POST /api/v1/trades/items/{orderItemId}/assign?accountId={}` (200 OK มอบหมายสำเร็จ, 409 ไอดีไม่ Ready, 404 ไม่พบไอดี, 400 ไม่ส่ง accountId)

---

### 4.4 `OrderApiControllerTest` — State Transitions & Trade Status Lifecycle (13 เคส)
* **ไฟล์**: [`src/test/java/com/pokevault/modules/order/OrderApiControllerTest.java`](file:///e:/Coding/Pok-Vault_Commerce/src/test/java/com/pokevault/modules/order/OrderApiControllerTest.java)
* **คลาสที่ถูกทดสอบ**: `OrderApiController` (ส่วนที่สมาชิกคนที่ 4 รับผิดชอบ)
* **ประเด็นที่ทดสอบ**:
  - **State Pattern Transition Endpoints**:
    - `PATCH /api/v1/orders/{id}/status?action=pay` ➔ 200 OK (สถานะกลายเป็น `PAID`)
    - `PATCH /api/v1/orders/{id}/status?action=ship` ➔ 200 OK (สถานะกลายเป็น `SHIPPING`)
    - `PATCH /api/v1/orders/{id}/status?action=cancel` ➔ 200 OK (สถานะกลายเป็น `CANCELLED`)
    - 409 CONFLICT: เมื่อขัดแย้งกับกฎสถานะ (เช่น ขอยกเลิกระหว่างขนส่ง)
    - 404 NOT FOUND: เมื่อไม่พบออเดอร์
    - 400 BAD REQUEST: เมื่อไม่ระบุ Parameter `action`
  - **Trade Status Lifecycle Endpoints**:
    - `PATCH /api/v1/orders/{orderId}/items/{itemId}/trade-status?status=TRADE_SENT` ➔ 200 OK
    - `PATCH /api/v1/orders/{orderId}/items/{itemId}/trade-status?status=COMPLETED` ➔ 200 OK
    - 403 FORBIDDEN: ตรวจจับเมื่อผู้ใช้ไม่มีสิทธิ์ (`AccessDeniedException`)
    - 400 BAD REQUEST: ค่าสถานะผิดพลาด หรือสถานะที่ไม่รองรับ
    - 409 CONFLICT: ออเดอร์ยังไม่เข้าสู่ `SHIPPING` หรือยังไม่ได้จับคู่ไอดีเกม (`TradeStateConflictException`)

---

### 4.5 `GlobalExceptionHandlerTest` — Centralized HTTP Error Handling (10 เคส)
* **ไฟล์**: [`src/test/java/com/pokevault/modules/trade/advice/GlobalExceptionHandlerTest.java`](file:///e:/Coding/Pok-Vault_Commerce/src/test/java/com/pokevault/modules/trade/advice/GlobalExceptionHandlerTest.java)
* **คลาสที่ถูกทดสอบ**: `GlobalExceptionHandler`
* **ประเด็นที่ทดสอบ (HTTP Status Code Contract)**:
  - `404 NOT_FOUND`: `ResourceNotFoundException`, `NoResourceFoundException`
  - `409 STATE_CONFLICT`: `TradeStateConflictException`, `InvalidOrderStateException`
  - `400 BAD_REQUEST`:
    - `MethodArgumentTypeMismatchException` (ส่งค่าตัวแปรไม่ตรงประเภท)
    - `MissingServletRequestParameterException` (ไม่ส่ง Query Parameter ที่บังคับ)
    - `IllegalArgumentException` (ส่งอาร์กิวเมนต์ที่ผิดกฎ)
    - `InsufficientStockException` (สต็อกการ์ดไม่พอ)
  - `403 ACCESS_DENIED`: `AccessDeniedException` (สิทธิ์ไม่เพียงพอ)
  - `500 INTERNAL_SERVER_ERROR`: `Exception` (ข้อผิดพลาดระบบที่ไม่คาดคิด)

---

### 4.6 `TradeRecommendationResponseTest` — Trade DTO & Candidate Model (3 เคส)
* **ไฟล์**: [`src/test/java/com/pokevault/modules/trade/dto/TradeRecommendationResponseTest.java`](file:///e:/Coding/Pok-Vault_Commerce/src/test/java/com/pokevault/modules/trade/dto/TradeRecommendationResponseTest.java)
* **คลาสที่ถูกทดสอบ**: `TradeRecommendationResponse`, `CandidateAccountResponse`
* **ประเด็นที่ทดสอบ**:
  - การทำงานของ Builder Pattern และ Getter/Setter ครบทุกตัวแปร
  - ค่า Default ของ `alternativeCandidates` ต้องเป็น List ที่ไม่เป็น null ป้องกัน NullPointerException
  - การทำงานของ Inner Class `CandidateAccountResponse` สำหรับส่งรายชื่อบัญชีสำรอง

---

### 4.7 `CustomExceptionTest` — Custom Domain Exceptions (2 เคส)
* **ไฟล์**: [`src/test/java/com/pokevault/common/exception/CustomExceptionTest.java`](file:///e:/Coding/Pok-Vault_Commerce/src/test/java/com/pokevault/common/exception/CustomExceptionTest.java)
* **คลาสที่ถูกทดสอบ**: `TradeStateConflictException`, `InvalidOrderStateException`
* **ประเด็นที่ทดสอบ**:
  - `TradeStateConflictException`: Constructor แบบระบุ Message และแบบห่อหุ้ม Cause
  - `InvalidOrderStateException`: Constructor แบบระบุ Message และแบบห่อหุ้ม Cause

---

## 5. การตรวจสอบกฎความปลอดภัยทางธุรกิจและ Design Patterns (Business Invariants & Patterns)

| หมวดหมู่การทดสอบ | กฎทางธุรกิจและพฤติกรรมที่ตรวจสอบ | คลาสเทสต์ที่ยืนยัน |
| :--- | :--- | :--- |
| **GoF State Pattern** | วงจรชีวิตของคำสั่งซื้อต้องเปลี่ยนสถานะผ่าน Concrete State Class เท่านั้น ปราศจาก Spaghetti if-else ซ้อนกัน | `OrderStateTest`, `OrderApiControllerTest` |
| **Anti-Fraud Shipping Guard** | **ห้ามยกเลิกคำสั่งซื้อขณะอยู่ในสถานะ `SHIPPING`** เพื่อป้องกันการโกงการ์ดฟรี (Free-Card Loss Protection) | `OrderStateTest.ShippingGuardTests` |
| **Automatic Stock Restoration** | เมื่อคำสั่งซื้อถูกยกเลิก (`CANCELLED`) ระบบต้องคืนจำนวนการ์ดกลับเข้าคลัง `CardInventory` โดยอัตโนมัติ | `OrderStateTest.CancellationTests` |
| **Greedy Stock-Maximization** | คัดเลือกเฉพาะไอดีเกมที่มีสถานะ **`READY`** และมีสต็อกการ์ดสูงสุดเพื่อกระจายภาระการส่งมอบ | `TradeMatchingServiceTest.AutoMatchAlgorithmTests` |
| **Two-Tier Recommendation** | ระบบส่งทั้ง Best Match และ Alternative Candidates ให้พนักงานสลับบัญชีได้ตามสถานการณ์จริง | `TradeMatchingServiceTest.RecommendationQueryTests` |
| **Strict Trade Sequence** | ลำดับสถานะเทรดต้องเป็น `UNASSIGNED` ➔ `FRIEND_PENDING` ➔ `TRADE_SENT` ➔ `COMPLETED` ห้ามกระโดดข้ามขั้น | `OrderApiControllerTest`, `GlobalExceptionHandlerTest` |

---

## 6. แนวทางการเขียนเทสและสถาปัตยกรรมทดสอบที่เลือกใช้ (Testing Architecture & Practices)

1. **Mockito Isolation (ไม่มี Spring Context Overhead)**:
   - ใช้ `@ExtendWith(MockitoExtension.class)` สำหรับ Service และ Domain Unit Tests ทำให้รันเทสได้เร็วระดับมิลลิวินาที (ไม่ต้องรอ Spring Boot โหลด Context)
2. **MockMvc Standalone Setup**:
   ```java
   mockMvc = MockMvcBuilders.standaloneSetup(controller)
           .setControllerAdvice(new GlobalExceptionHandler())
           .build();
   ```
   - **เหตุผล**: หลีกเลี่ยงปัญหา sliced `@WebMvcTest` กับคลาสหลักที่มี `@EnableJpaAuditing` และสามารถทดสอบ `GlobalExceptionHandler` ร่วมกับ Controller ได้อย่างเป็นอิสระและสมบูรณ์
3. **AAA Pattern (Arrange - Act - Assert)**:
   - โครงสร้างเทสแบ่งเป็น 3 ส่วนชัดเจน พร้อมใช้ `@DisplayName` ภาษาไทยที่อธิบายเงื่อนไขและผลลัพธ์ชัดเจน
4. **AssertJ Fluent Assertions**:
   - ใช้ `assertThat(...)` และ `assertThatThrownBy(...)` เพื่อให้โค้ดทดสอบอ่านง่ายเหมือนประโยคภาษาอังกฤษ

---

## 7. การแก้ปัญหาทั่วไปเมื่อรันเทส (Troubleshooting)

### ❓ ปัญหาที่ 1: `mvnw : The term 'mvnw' is not recognized` บน Windows PowerShell
* **วิธีแก้**: บน PowerShell ให้พิมพ์ **`.\mvnw.cmd`** นำหน้าเสมอ:
  ```powershell
  .\mvnw.cmd test
  ```

### ❓ ปัญหาที่ 2: รันคำสั่ง `-Dtest=A,B` แล้วเกิดข้อผิดพลาด `Missing argument in parameter list`
* **วิธีแก้**: บน PowerShell ให้ใส่เครื่องหมายคำพูดรอบพารามิเตอร์เสมอ:
  ```powershell
  .\mvnw.cmd test "-Dtest=OrderStateTest,TradeMatchingServiceTest"
  ```

### ❓ ปัญหาที่ 3: คอมไพล์ไม่ผ่านจากข้อผิดพลาด Lombok / Java Internal API
* **วิธีแก้**: ตรวจสอบว่า `JAVA_HOME` ชี้ไปยัง **Java 17 LTS**:
  ```powershell
  $env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
  $env:Path="$env:JAVA_HOME\bin;$env:Path"
  ```

---

> 📝 **เอกสารนี้จัดทำขึ้นสำหรับ**: สมาชิกคนที่ 4 — นายแทนคุณ พันธ์นิกุล (673380301-0)  
> Branch: `Tankun_6733803010_01`  
> อัปเดตล่าสุด: ตุลาคม 2026
