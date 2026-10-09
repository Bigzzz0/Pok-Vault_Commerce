# 📘 Knowledge 05: Data Transfer Objects (DTO) และ Bean Validation ของโมดูล Vault

> **Commit Reference**: `feat: create DTOs for GameAccount and AddPulledCard requests`  
> **ผู้รับผิดชอบ**: นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2: Game Account Vault & Inventory Manager  
> **โมดูล**: Application Layer / DTOs (`com.pokevault.modules.vault.dto`)  
> **สถานะ**: Implemented

---

## 1. 🎯 วัตถุประสงค์และภาพรวม (Overview & Objective)

ในการออกแบบสถาปัตยกรรมซอฟต์แวร์ระดับ Enterprise ด้วย Spring Boot การสื่อสารระหว่าง **Client/Controller** และ **Domain Model** จำเป็นต้องผ่าน **Data Transfer Objects (DTO)** เพื่อ:
1. **Information Hiding & Security**: ไม่เปิดเผยโครงสร้างตารางฐานข้อมูลหรือ Internal State ของ Entity (`GameAccount`, `CardInventory`) สู่ภายนอกโดยตรง
2. **Defensive Validation (Fail-Fast)**: ตรวจสอบความถูกต้องของข้อมูลที่ส่งเข้ามาตั้งแต่ชั้น Controller ด้วย Jakarta Bean Validation (`@Valid`) ป้องกันไม่ให้ข้อมูลที่ไม่สมบูรณ์เล็ดลอดไปยัง Service Layer
3. **Decoupled API Contract**: แยกรูปแบบ JSON ที่ Client ต้องการ ออกจาก Schema ของ Entity ทำให้สามารถปรับเปลี่ยนฐานข้อมูลได้โดยไม่กระทบ Frontend

ในขั้นตอนนี้ โมดูล Vault จึงได้จัดเตรียม DTO ครบทั้ง 4 คลาสตาม Class Diagram และ Sequence Diagram ของระบบ

---

## 2. 🧩 โครงสร้าง DTO ทั้ง 4 คลาสในโมดูล Vault

### 2.1 `GameAccountRequest.java` (ลงทะเบียน/แก้ไขไอดีร้าน)
- **การนำไปใช้**: รับข้อมูลสำหรับ `POST /api/v1/accounts`
- **Validation Rules**:
  - `@NotBlank` รหัสบัญชี (`accountCode`) และชื่อเทรนเนอร์ (`inGameName`)
  - `@Pattern(regexp = "^\\d{4}-?\\d{4}-?\\d{4}-?\\d{4}$")`: บังคับรูปแบบ Friend ID ในเกม Pokémon Pocket ให้เป็นตัวเลข 16 หลัก (เช่น `1234-5678-9012-3456`)
  - `@DecimalMin("0.00")`: ป้องกันค่าใช้จ่ายต้นทุนซื้อไอดี (`buyInCost`) ติดลบ

### 2.2 `GameAccountResponse.java` (ส่งข้อมูลไอดีร้านกลับไปยัง UI)
- **การนำไปใช้**: ตอบกลับผลลัพธ์ของ `GET /api/v1/accounts` และ `GET /api/v1/accounts/{id}`
- **Static Mapper**: มีเมธอด `fromEntity(GameAccount)` และ `fromEntity(GameAccount, totalCards)` เพื่อแปลงข้อมูล Entity เป็น DTO พร้อมคำนวณจำนวนการ์ดรวมในไอดีนั้น

### 2.3 `AddPulledCardRequest.java` (บันทึกการเปิดซองการ์ด)
- **การนำไปใช้**: รับข้อมูลสำหรับ `POST /api/v1/accounts/{id}/pulls` (+ Add Pull)
- **Validation Rules**:
  - `@NotNull` บน `cardId` และ `condition` (ต้องระบุรหัสการ์ดและสภาพการ์ด)
  - `@Min(1)` บน `quantity`: จำนวนการ์ดที่เปิดได้ต้องอย่างน้อย 1 ใบ
  - `@DecimalMin("0.00")` บน `buyInPrice` และ `sellingPrice`: ป้องกันราคาซื้อ/ขายติดลบ

### 2.4 `AccountCardResponse.java` (แสดงรายการการ์ดในไอดีเกม)
- **การนำไปใช้**: ตอบกลับผลลัพธ์ของ `GET /api/v1/accounts/{id}/cards` และคืนค่าหลังเปิดซองสำเร็จ (ตาม Sequence Diagram ขั้นตอนที่ 14)
- **Static Mapper**: มี `fromEntity(CardInventory)` พร้อม Null Safety Handling สำหรับดึงข้อมูลชื่อการ์ด, หมายเลขการ์ด, รูปภาพ, และชุด Expansion มาแสดงผลได้อย่างสมบูรณ์

---

## 3. 🔍 การประยุกต์ใช้ Bean Validation และความปลอดภัยของข้อมูล

| Validation Annotation | จุดที่นำไปใช้ | เหตุผลทางธุรกิจและเทคนิค |
| :--- | :--- | :--- |
| `@NotBlank` | `accountCode`, `inGameName`, `friendId` | ป้องกัน String ว่างเปล่าหรือช่องว่าง (Whitespace-only injection) |
| `@Pattern` | `friendId` | กรองรูปแบบรหัสเพื่อน 16 หลักตามรูปแบบของ Pokémon TCG Pocket Client |
| `@DecimalMin("0.00")` | `buyInCost`, `buyInPrice`, `sellingPrice` | บังคับ Business Invariant ว่าราคาและต้นทุนทางการเงินห้ามติดลบ |
| `@Min(1)` | `quantity` | การเปิดซองการ์ดต้องได้รับอย่างน้อย 1 ใบขึ้นไป |
| `@Size` | `accountCode`, `notes`, `storageSlot` | ป้องกัน Buffer Overflow และตัดปัญหาข้อมูลยาวเกินขนาดคอลัมน์ใน SQL |

---

## 4. 🏛️ การสอดคล้องกับหลักการออกแบบซอฟต์แวร์ (SOLID Alignment)

| หลักการ | การประยุกต์ใช้ใน DTO Layer |
| :--- | :--- |
| **SRP (Single Responsibility)** | แต่ละ DTO รับผิดชอบโครงสร้างข้อมูลเพียง Use Case เดียว (เช่น Request แยกจาก Response) |
| **OCP (Open/Closed Principle)** | Entity ภายในสามารถเพิ่มฟิลด์ทางเทคนิค (เช่น JPA Version, Audit Columns) ได้โดยไม่กระทบ API Contract ของภายนอก |
| **Defensive Design** | ใช้ `@Builder.Default` และ Validation Constraints ป้องกัน State ที่ไม่ถูกต้องตั้งแต่ด่านหน้า (Controller Boundary) |

---

## 5. 🧪 ผลการทดสอบ (Verification)
- Compile ผ่านด้วย `./mvnw test-compile` (BUILD SUCCESS)
- Automated Unit Tests ผ่านครบ 100% ด้วย `./mvnw test` (18/18 tests passed)
