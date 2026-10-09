# 📑 แผนการนำเสนอโครงงานฉบับสมบูรณ์ (Master Presentation Outline & Defense Guide)
## โครงการ: PokéVault Commerce (Pokémon TCG Pocket Vault & Chat Commerce)
**วิชา**: CP353002 Principles of Software Design and Development (Spring Boot 3.4.3 / Java 17)  
**เกณฑ์ประเมิน**: 100 คะแนนเต็ม (ประเมินรายบุคคล 20 คะแนน + สถาปัตยกรรมและการออกแบบ 80 คะแนน)  
**ข้อจำกัดสำคัญ**: 
- **เวลาในการนำเสนอรวม**: **ไม่เกิน 10 นาที** (จัดสรร 9 นาที 45 วินาที เผื่อสลับผู้พูด)
- **จำนวนสไลด์รวมทั้งกลุ่ม**: **ไม่เกิน 15 สไลด์** (กำหนดที่ **14 สไลด์พอดี**)
- **จำนวนสมาชิก**: 5 คน (คนละ ~1.5 – 2.0 นาที)

---

## 👥 ตารางสรุปเวลา สไลด์ และบทบาทสมาชิกทุกคน (Presenter Matrix)

| สไลด์ที่ | หัวข้อสไลด์ (Slide Title) | ผู้รับผิดชอบนำเสนอ (Presenter) | เวลาที่ใช้ | เกณฑ์ Rubric ที่ครอบคลุม |
| :---: | :--- | :--- | :---: | :--- |
| **1** | หน้าปก, ที่มาของระบบ & Business Value (Use Case) | **คนที่ 1**: นายศิฆรินทร์ อุปจันทร์ (673380292-5) | 0:45 นาที | Use Case Diagram, Business Problem |
| **2** | 4-Tier Layered Architecture & Component / Deployment | **คนที่ 1**: นายศิฆรินทร์ อุปจันทร์ (673380292-5) | 0:45 นาที | Layered Architecture, Component & Deployment Diagram |
| **3** | Database Design (ER 8 ตาราง, 1:1, 1:N & Security) | **คนที่ 1**: นายศิฆรินทร์ อุปจันทร์ (673380292-5) | 1:00 นาที | Database Design (10 คะแนน), Spring Security |
| **4** | Game Account Vault & Inventory Management | **คนที่ 2**: นายสัพพัญญู คำตุ้ม (673380066-4) | 0:45 นาที | Domain Model, Business Invariants |
| **5** | **GoF Observer Pattern** & Sequence Scenario 1 | **คนที่ 2**: นายสัพพัญญู คำตุ้ม (673380066-4) | 1:00 นาที | GoF Observer Pattern, Sequence Diagram 1 |
| **6** | Order Engine, Transactional Booking & Activity Flow | **คนที่ 3**: นายธนภูมิ จันทรา (673380272-1) | 0:45 นาที | Activity Diagram, Concurrency Invariants |
| **7** | **GoF Strategy Pattern** & Sequence Scenario 2 | **คนที่ 3**: นายธนภูมิ จันทรา (673380272-1) | 1:00 นาที | GoF Strategy Pattern, Class Diagram, Sequence 2 |
| **8** | **GoF State Pattern** & Anti-Fraud / Completion Guard | **คนที่ 4**: นายแทนคุณ พันธ์นิกุล (673380301-0) | 1:00 นาที | GoF State Pattern, State Diagram, Invariants |
| **9** | In-Game Trade Matching Engine & Sequence Scenario 3 | **คนที่ 4**: นายแทนคุณ พันธ์นิกุล (673380301-0) | 0:45 นาที | Sequence Diagram 3, Two-Tier Candidate DTO |
| **10** | Centralized Global Exception Handler (RFC 7231 / 409) | **คนที่ 4**: นายแทนคุณ พันธ์นิกุล (673380301-0) | 0:45 นาที | REST API (Exception Handling, Status Codes) |
| **11** | Frontend Web UI, 3D Holo Cards & Chat Handshake | **คนที่ 5**: นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1) | 0:45 นาที | Presentation Layer, UI/UX, Chat Commerce |
| **12** | REST API & Swagger UI (CRUD 2 Resources, Page/Sort) | **คนที่ 5**: นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1) | 0:45 นาที | REST API (10 คะแนน), Swagger UI, Validation |
| **13** | SOLID Principles (5 ข้อ) & Enterprise Patterns (6 แบบ) | **คนที่ 5**: นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1) | 0:45 นาที | SOLID Analysis (15 คะแนน), Enterprise Patterns (15 คะแนน) |
| **14** | Automated Testing (355 Passes), Git & Live Deployment | **คนที่ 5**: นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1) | 0:45 นาที | Testing (5 คะแนน), Git (5 คะแนน), Deploy (5 คะแนน) |
| — | **รวมเวลาการนำเสนอทั้งหมด** | **ครบทั้ง 5 คน (14 สไลด์)** | **9:45 นาที** | **ครอบคลุมเกณฑ์ 100 คะแนนเต็ม** |

---

## 🎯 สรุปการครอบคลุมเกณฑ์การประเมิน (Rubric Compliance Checklist)

- [x] **ความเข้าใจระบบและการตอบคำถาม (20 คะแนน)**: มีคู่มือตอบคำถามเจาะลึกรายบุคคล (Section 4) ครบทั้ง 5 คน ชี้ไฟล์และบรรทัดโค้ดจริง
- [x] **ความถูกต้องและครบถ้วนของ Diagram (15 คะแนน)**: ครบ 8 รูปแบบ
  - Use Case Diagram (สไลด์ 1)
  - Component & Deployment Diagram (สไลด์ 2, 14)
  - ER Diagram (สไลด์ 3)
  - Domain Model (สไลด์ 4)
  - Class Diagram แสดงตำแหน่ง Pattern (สไลด์ 5, 7, 8)
  - Activity Diagram (สไลด์ 6)
  - State Diagram (สไลด์ 8)
  - Sequence Diagram อย่างน้อย 3 Scenarios (สไลด์ 5, 7, 9)
- [x] **Layered Architecture และ SOLID Principles (15 คะแนน)**: แยก 4 ชั้นเด็ดขาด ไม่ข้าม Layer ใช้ Constructor Injection 100% พร้อมชี้บรรทัดตาม `doc/solid-analysis.md` (สไลด์ 2, 13)
- [x] **Design Patterns (15 คะแนน)**:
  - Enterprise Patterns ครบ 6 รายการ: Layered Architecture, MVC, Repository, Service Layer, DTO+Mapper, Dependency Injection (สไลด์ 13)
  - GoF Behavioral Patterns ครบ 3 แบบในกลุ่มเดียวกัน: Strategy (สไลด์ 7), State (สไลด์ 8), Observer (สไลด์ 5) พร้อมเหตุผลทางธุรกิจจริง
- [x] **Database Design (10 คะแนน)**: 8 ตาราง, มี 1:1 (`users` $\leftrightarrow$ `user_profiles`) และ 1:N ครบ, FK, Index, Cascade, FetchType ชัดเจน, มี Migration Script (สไลด์ 3)
- [x] **REST API และ Swagger (10 คะแนน)**: CRUD ครบ $\ge 2$ Resources (`/api/v1/cards`, `/api/v1/accounts`), Status Code ถูกต้อง, Global Exception Handler, Pagination/Sorting, Validation, Swagger UI ใช้งานได้จริง (สไลด์ 10, 12)
- [x] **Testing (5 คะแนน)**: JUnit 5 + Mockito + Spring Boot Test ผ่าน **355/355 ข้อ (100% BUILD SUCCESS)** มี Test Report แยกรายบุคคล (สไลด์ 14)
- [x] **Git Workflow & Structure (5 คะแนน)**: รูปแบบ Branch `ชื่อ_รหัส_section`, Merge ผ่าน PR มี Reviewer ครบ โฟลเดอร์ `src/`, `test/`, `doc/`, `doc/screenshots/` สมบูรณ์ (สไลด์ 14)
- [x] **Deployment (5 คะแนน)**: Docker Multi-Stage + Public Cloud URL เข้าถึงได้จริง (สไลด์ 14)

---

## 📽️ รายละเอียดสไลด์ทีละหน้า (Slide-by-Slide Detailed Content & Scripts)

---

### สไลด์ที่ 1: หน้าปก, ที่มาของโปรเจกต์ & ปัญหาทางธุรกิจ (Problem & Business Value)
* **ผู้พูด**: **คนที่ 1 — นายศิฆรินทร์ อุปจันทร์ (673380292-5)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: Use Case Diagram, Business Understanding
* **องค์ประกอบบนสไลด์**:
  - โลโก้ PokéVault Commerce, รายชื่อสมาชิก 5 คนและรหัสนักศึกษา
  - ไดอะแกรม: **Use Case Diagram** (`doc/diagrams/use-case-diagram.md`) แสดงบทบาท Customer, Staff/Admin, และ System Background
  - ปัญหา (Pain Points) ของเกม Pokémon TCG Pocket vs ทางออกของระบบ (Solution)
* **ประเด็นที่ต้องอธิบาย**:
  1. เกมจำกัดโควต้าการเทรดรายวัน และต้องอาศัย Friend ID 16 หลักในการส่งการ์ดในเกม
  2. ปัญหาของผู้เล่นคือหาการ์ดใบที่ต้องการได้ยาก และร้านค้าประสบปัญหาการบริหารจัดการไอดีเกมบอทจำนวนมากที่ถือสต็อกการ์ดกระจัดกระจาย
  3. PokéVault คือระบบ Web & Chat Commerce จัดการคลังไอดีเกม (Vault) และช่วยส่งมอบการ์ดให้ลูกค้าอัตโนมัติ
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"กราบเรียนอาจารย์ที่เคารพครับ กลุ่มของพวกเราขอเสนอนำเสนอโครงการ **PokéVault Commerce** ระบบบริหารคลังและการจำหน่ายการ์ดเกม Pokémon TCG Pocket แบบ Chat Commerce ครับ  
  > 
  > ปัญหาหลักของเกมนี้คือ ลูกค้าไม่สามารถส่งไอเทมผ่าน API ของเกมได้โดยตรง แต่ต้องอาศัยการเพิ่มเพื่อนด้วย Friend ID 16 หลัก และร้านค้ามีไอดีเกมหลายบัญชีเพื่อเปิดซองสะสมการ์ด ทำให้การจับคู่ไอดีเพื่อส่งการ์ดทำได้ช้าและเสี่ยงต่อการผิดพลาด  
  > 
  > จาก **Use Case Diagram** ระบบของเราแบ่ง Actor เป็น 2 กลุ่มหลักคือ ลูกค้า ที่สามารถเลือกดูแกลเลอรี สั่งจองการ์ด และส่งสลิปผ่าน Chat Commerce และ พนักงานร้าน ที่มีหน้าที่จัดการคลังการ์ด จัดการไอดีเกม และกดยืนยันการส่งมอบการ์ดในเกมครับ"*

---

### สไลด์ที่ 2: สถาปัตยกรรม 4-Tier Layered Architecture & Component / Deployment Diagram
* **ผู้พูด**: **คนที่ 1 — นายศิฆรินทร์ อุปจันทร์ (673380292-5)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: Layered Architecture, Component & Deployment Diagram
* **องค์ประกอบบนสไลด์**:
  - ไดอะแกรม: **Component Diagram & Deployment Diagram** (`doc/diagrams/component-deployment-diagram.md`)
  - แผนผัง 4-Tier Layered Architecture: Presentation (Thymeleaf/REST) $\rightarrow$ Service Layer $\rightarrow$ Repository Layer $\rightarrow$ Domain/Database
  - จุดเด่น: การห้ามข้าม Layer เด็ดขาด และการใช้ Constructor Injection 100%
* **ประเด็นที่ต้องอธิบาย**:
  1. การจัดโครงสร้างโปรเจกต์แบบ Package by Feature/Module (`catalog`, `vault`, `order`, `trade`, `web`)
  2. Controller เรียกผ่าน Service Interface เท่านั้น ไม่เรียก Repository โดยตรง
  3. สถาปัตยกรรมทำงานบน Docker Container เชื่อมต่อไปยัง PostgreSQL 16 (Production) และ H2 In-Memory (Testing)
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"ในด้านสถาปัตยกรรมระบบ เราออกแบบตามหลัก **Layered Architecture 4 ชั้น** อย่างเคร่งครัดครับ  
  > 
  > จาก Component Diagram: Presentation Layer ประกอบด้วย Thymeleaf Controllers และ REST API Controllers ส่งต่อคำขอลงมายัง Service Layer ซึ่งเป็นที่รวมของ Business Invariants ทั้งหมด โดยห้าม Controller เรียก Repository โดยตรงเด็ดขาด  
  > 
  > ทุก Service และ Controller สื่อสารผ่าน Interface และใช้ **Constructor Injection ผ่าน Lombok `@RequiredArgsConstructor` 100%** ไม่มีการใช้ Field Injection ทำให้ระบบ Loose Coupling และทำ Unit Test ได้ง่ายมากครับ"*

---

### สไลด์ที่ 3: Database Design (ER Diagram 8 ตาราง, ความสัมพันธ์ 1:1, 1:N & Security Entities)
* **ผู้พูด**: **คนที่ 1 — นายศิฆรินทร์ อุปจันทร์ (673380292-5)** (เวลา: 1:00 นาที)
* **เกณฑ์ Rubric**: Database Design (10 คะแนนเต็ม), ER Diagram, Security Integration
* **องค์ประกอบบนสไลด์**:
  - ไดอะแกรม: **ER Diagram** (`doc/diagrams/er-diagram.md`) แสดง 8 ตารางสมบูรณ์
  - ไฮไลท์ความสัมพันธ์:
    - **One-to-One (1:1)**: `users` $\leftrightarrow$ `user_profiles` (แยก Security credentials ออกจาก Business profile)
    - **One-to-Many (1:N)**: `card_expansions` $\rightarrow$ `cards`, `cards` $\rightarrow$ `card_inventories`, `game_accounts` $\rightarrow$ `card_inventories`, `users` $\rightarrow$ `orders`, `orders` $\rightarrow$ `order_items`
  - ตารางสรุป Index, Foreign Keys, Cascade Types, และ FetchType (Lazy Loading)
* **ประเด็นที่ต้องอธิบาย**:
  1. การออกแบบผ่าน Normalization BCNF ปราศจาก Data Redundancy
  2. การแยก `User` และ `UserProfile` ตามหลัก Single Responsibility ป้องกันข้อมูลรหัสผ่านรั่วไหล
  3. Migration Scripts: `schema.sql`, `data.sql` และ `core-schema-alignment.sql` สำหรับ PostgreSQL
  4. Spring Security: Role-Based Access Control (`CUSTOMER`, `STAFF`, `ADMIN`) ป้องกัน IDOR ด้วย `OrderAccessPolicy`
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"ถัดมาคือการออกแบบฐานข้อมูลครับ ระบบของเรามีทั้งหมด **8 ตารางหลัก** ตามเกณฑ์มาตรฐานวิชาครับ  
  > 
  > ไฮไลท์สำคัญคือความสัมพันธ์ **1:1** ระหว่าง `users` และ `user_profiles` ซึ่งเราจงใจแยกตารางความปลอดภัยออกจากข้อมูลโปรไฟล์ธุรกิจ และความสัมพันธ์ **1:N** เช่น 1 คำสั่งซื้อมีได้หลาย `order_items`, การ์ด 1 ใบกระจายอยู่ในหลาย `card_inventories` ของแต่ละไอดีเกม  
  > 
  > เรากำหนด Index บน Foreign Key ทุกตัว, ใช้ `FetchType.LAZY` เพื่อป้องกันปัญหา N+1 Query และมี DDL Migration Script รองรับทั้ง H2 และ PostgreSQL พร้อมระบบ Spring Security ตรวจสอบสิทธิ์ผ่าน `OrderAccessPolicy` ป้องกันลูกค้าแอบเข้าถึงออเดอร์ของผู้อื่นครับ"*

---

### สไลด์ที่ 4: Game Account Vault & Inventory Management (Domain Model & Invariants)
* **ผู้พูด**: **คนที่ 2 — นายสัพพัญญู คำตุ้ม (673380066-4)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: Domain Model, Business Invariants Rules
* **องค์ประกอบบนสไลด์**:
  - ไดอะแกรม: **Domain Model** (`doc/diagrams/domain-model.md`) เฉพาะส่วน Vault & Inventory
  - โมเดล Entity: `GameAccount`, `AccountTradeStatus` (`READY`, `BUSY`, `COOLDOWN`, `SUSPENDED`), และ `CardInventory`
  - กฎทางธุรกิจระดับ Invariant (Business Invariants):
    - `deductStock()`: สต็อกห้ามติดลบ
    - `restoreStock()`: คืนสต็อกเข้าคลังอย่างถูกต้อง
    - ตรวจสอบ Friend ID รูปแบบ 16 หลัก
* **ประเด็นที่ต้องอธิบาย**:
  1. บัญชีเกมร้านค้าทำหน้าที่เป็น "ตู้เซฟเก็บการ์ด" (Game Account Vault)
  2. ฟังก์ชัน `addPulledCard()` เมื่อพนักงานเปิดซองในเกมแล้วนำการ์ดมาบันทึกเข้าไอดี
  3. การรับประกันว่าสต็อกคงคลังในแต่ละไอดีจะไม่มีวันติดลบเด็ดขาด (Fail-Safe Invariant)
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"กราบเรียนอาจารย์ครับ กระผม สัพพัญญู สมาชิกคนที่ 2 รับผิดชอบโมดูล **Game Account Vault และ Inventory Management** ครับ  
  > 
  > หัวใจของร้านเราคือไอดีเกมที่ทำหน้าที่เก็บการ์ด โดย `GameAccount` แต่ละไอดีจะมีสถานะการเทรด เช่น `READY` หรือติด `COOLDOWN` และถือครอง `CardInventory` ของการ์ดแต่ละใบ  
  > 
  > ในระดับ Domain Entity กระผมได้ออกแบบ **Business Invariant Guard** ภายในเมธอด `deductStock()` และ `restoreStock()` เพื่อรับประกันว่าจำนวนสต็อกในระบบจะไม่มีวันติดลบเด็ดขาด หากพยายามหักเกินจะโยน `InsufficientStockException` ทันที และมีระบบบันทึกผลการเปิดซองการ์ดเพิ่มเข้าคลังได้อย่างถูกต้องครับ"*

---

### สไลด์ที่ 5: GoF Behavioral Pattern 1: Observer Pattern (LowStockObserver แจ้งเตือนคลังสต็อกต่ำ)
* **ผู้พูด**: **คนที่ 2 — นายสัพพัญญู คำตุ้ม (673380066-4)** (เวลา: 1:00 นาที)
* **เกณฑ์ Rubric**: GoF Observer Pattern, Sequence Diagram Scenario 1, Class Diagram
* **องค์ประกอบบนสไลด์**:
  - ไดอะแกรม: **Class Diagram (Observer Pattern)** + **Sequence Diagram Scenario 1 (Low Stock Alert)** (`doc/diagrams/sequence-diagrams.md#scenario-2`)
  - โครงสร้างคลาส:
    - **Subject / Publisher**: `ApplicationEventPublisher` (Spring Event Bus)
    - **Event Object**: `OrderPlacedEvent`
    - **Concrete Observer**: `LowStockObserver` (`@EventListener`)
  - เหตุผลทางธุรกิจ: เมื่อมีคำสั่งซื้อและสต็อกลดลงเหลือ $\le 2$ ใบ ระบบต้องแจ้งเตือนฝ่ายจัดซื้อทันที โดยโมดูล Order ต้องไม่ผูกติดแน่นกับ Vault
* **ประเด็นที่ต้องอธิบาย**:
  1. ปัญหาถ้าไม่ใช้ Observer: `OrderService` จะต้อง Inject `InventoryService` หรือ Notification เข้ามา ทำให้เกิด Tight Coupling และขัดหลัก SRP
  2. การแก้ปัญหาด้วย Spring Event Publisher: `OrderServiceImpl` ส่ง `OrderPlacedEvent` ออกไปแบบ Decoupled
  3. `LowStockObserver` ดักจับ Event ตรวจสอบเงื่อนไข threshold $\le 2$ และส่งข้อความเตือน Warning
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"สำหรับ Design Pattern แรกของกลุ่มเรา คือ **GoF Observer Pattern** ครับ  
  > 
  > ปัญหาทางธุรกิจคือ เมื่อลูกค้าสั่งซื้อการ์ดจนสต็อกเหลือน้อย ร้านต้องรีบนำไอดีไปเปิดซองหาการ์ดมาเติม แต่ถ้าให้ `OrderService` เรียกโค้ดแจ้งเตือนโดยตรง จะทำให้โมดูล Order ผูกติดแน่นกับโมดูลคลังสินค้า ขัดต่อหลัก SRP  
  > 
  > กระผมจึงแก้ปัญหาด้วย Observer Pattern โดยใช้ Spring Event Bus ครับ เมื่อมีคำสั่งซื้อเกิดขึ้น `OrderService` จะยิง `OrderPlacedEvent` ออกไป และมี `LowStockObserver` ทำหน้าที่เป็น Observer คอยดักฟัง หากพบว่าการ์ดในคลังเหลือเท่ากับหรือน้อยกว่า 2 ใบ จะสั่งยิงแจ้งเตือนทันที ทำให้ทั้งสองโมดูลแยกขาดจากกันโดยสิ้นเชิงครับ"*

---

### สไลด์ที่ 6: Order Engine & Chat Commerce Handshake (Activity Flow & Concurrency)
* **ผู้พูด**: **คนที่ 3 — นายธนภูมิ จันทรา (673380272-1)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: Activity Diagram, Concurrency & Stock Reservation Invariant
* **องค์ประกอบบนสไลด์**:
  - ไดอะแกรม: **Activity Diagram** (`doc/diagrams/activity-diagram.md`) แสดงวงจรคำสั่งซื้อ Chat Commerce
  - ตรรกะ Transactional Booking: การจองการ์ด ตัดสต็อกทันทีในระดับ Database Transaction
  - รองรับการจองแทนลูกค้า (Staff Booking Delegation) และลูกค้าทั่วไป
  - กลไก Rollback อัตโนมัติเมื่อสั่งซื้อหลายใบแล้วมีใบใดใบหนึ่งสต็อกไม่พอ
* **ประเด็นที่ต้องอธิบาย**:
  1. การเชื่อมต่อระหว่างหน้าเว็บกับ Facebook Chat Commerce (Handshake via Order Code & Friend ID)
  2. การทำงานของ `OrderServiceImpl.createOrder()` ภายใต้ `@Transactional`
  3. การจองการ์ดใบสุดท้ายในคลัง (สต็อกเหลือ 0) ยังสามารถคงสิทธิ์และจับคู่ส่งมอบได้ถูกต้อง
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"กราบเรียนอาจารย์ครับ กระผม ธนภูมิ สมาชิกคนที่ 3 รับผิดชอบ **Order Engine และ Strategy Pattern** ครับ  
  > 
  > ในส่วนของระบบสั่งซื้อ จาก **Activity Diagram** เมื่อลูกค้ากดจองการ์ด ระบบจะสร้างรหัสออเดอร์ เช่น `ORD-2026-001` พร้อมสร้างข้อความสรุปยอดเงินและเลขคำสั่งซื้อให้ลูกค้านำไปส่งต่อใน Facebook Messenger ได้ทันที  
  > 
  > ในเบื้องหลัง ระบบทำงานแบบ `@Transactional` หักสำรองสต็อกการ์ดทันทีเพื่อป้องกันปัญหา Overselling หากลูกค้าสั่งการ์ดหลายใบแล้วมีเพียงใบเดียวที่สต็อกไม่พอ ระบบจะทำการ Rollback ทั้งหมดทันที และมีระบบรองรับพนักงานจองแทนลูกค้าที่สั่งผ่านแชทได้อย่างปลอดภัยครับ"*

---

### สไลด์ที่ 7: GoF Behavioral Pattern 2: Strategy Pattern (เครื่องยนต์คำนวณส่วนลดตามระดับสมาชิก)
* **ผู้พูด**: **คนที่ 3 — นายธนภูมิ จันทรา (673380272-1)** (เวลา: 1:00 นาที)
* **เกณฑ์ Rubric**: GoF Strategy Pattern, Class Diagram, Sequence Scenario 2
* **องค์ประกอบบนสไลด์**:
  - ไดอะแกรม: **Class Diagram (Strategy Pattern)** + **Sequence Diagram Scenario 2 (Discount Calculation)** (`doc/diagrams/sequence-diagrams.md#scenario-1`)
  - โครงสร้างคลาส:
    - **Strategy Interface**: `DiscountStrategy`
    - **Concrete Strategies**: `RegularDiscountStrategy` (0%), `VipDiscountStrategy` (10%), `WholesaleDiscountStrategy` (15%)
    - **Context Class**: `DiscountService` ทำการรวบรวมผ่าน Spring DI `List<DiscountStrategy>`
  - จุดเด่น OCP: เพิ่มโปรโมชันใหม่ (เช่น เทศกาล 20%) ได้โดยการสร้างคลาสใหม่ ไม่แตะต้องโค้ดเดิม
* **ประเด็นที่ต้องอธิบาย**:
  1. ปัญหาถ้าใช้ if-else หรือ switch-case ใน Service: โค้ดจะยาว ซับซ้อน และเสี่ยงพังเมื่อเพิ่มเงื่อนไขใหม่ (ขัด OCP)
  2. การใช้ Strategy Pattern ร่วมกับ Polymorphism และ Spring Autowiring
  3. การทดสอบครอบคลุมทุก Strategy รวมถึง Edge Case ยอดซื้อเป็นศูนย์หรือทศนิยม
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"สำหรับ Design Pattern ที่สอง คือ **GoF Strategy Pattern** ในระบบคำนวณส่วนลดครับ  
  > 
  > ร้านของเรามีลูกค้า 3 ระดับ คือ Regular ลด 0%, VIP ลด 10%, และ Wholesale ลด 15% หากเราเขียน if-else ดักใน Service ทุกครั้งที่มีระดับสมาชิกหรือโปรโมชันใหม่ เราจะต้องกลับมารื้อโค้ดเดิม ซึ่งละเมิดหลัก Open/Closed Principle  
  > 
  > กระผมจึงแยกอัลกอริทึมการคำนวณออกเป็น 3 คลาสตาม Strategy Pattern โดยมี `DiscountService` เป็น Context ที่รับ `List<DiscountStrategy>` เข้ามาอัตโนมัติผ่าน Spring DI เมื่อคำนวณส่วนลด ระบบจะเลือกกลยุทธ์ที่ตรงกับระดับสมาชิกของลูกค้าอย่างถูกต้อง หากในอนาคตต้องการเพิ่มโปรโมชันใหม่ ก็เพียงแค่สร้างคลาสใหม่ขึ้นมาโดยไม่ต้องแตะต้อง Service เดิมแม้แต่บรรทัดเดียวครับ"*

---

### สไลด์ที่ 8: GoF Behavioral Pattern 3: State Pattern (วงจรชีวิตคำสั่งซื้อ & Anti-Fraud / Completion Guard)
* **ผู้พูด**: **คนที่ 4 — นายแทนคุณ พันธ์นิกุล (673380301-0)** (เวลา: 1:00 นาที)
* **เกณฑ์ Rubric**: GoF State Pattern, State Diagram, Business Invariants Protection
* **องค์ประกอบบนสไลด์**:
  - ไดอะแกรม: **State Diagram (Order Lifecycle)** (`doc/diagrams/state-diagram.md`)
  - โครงสร้างคลาส: `OrderState` Interface, `OrderContext`, และ 5 Concrete States:
    `PendingOrderState` $\rightarrow$ `PaidOrderState` $\rightarrow$ `ShippingOrderState` $\rightarrow$ `CompletedOrderState` / `CancelledOrderState`
  - ไฮไลท์ความปลอดภัย (Business Invariants):
    - **Anti-Fraud Guard**: ในสถานะ `SHIPPING` ห้ามกดยกเลิก (`cancel()`) เด็ดขาด ป้องกันการโกงการ์ด
    - **Automatic Stock Restoration**: เมื่อยกเลิกใน `PENDING`/`PAID` ตัว `CancelledOrderState` คืนสต็อกเข้าคลังให้อัตโนมัติทันที
    - **Completion Guard**: ในสถานะ `SHIPPING` การจะ `complete()` ได้ สินค้าทุกรายการต้องเป็น `COMPLETED` ก่อน หากเทรดไม่ครบจะโยน 409 Conflict ทันที
* **ประเด็นที่ต้องอธิบาย**:
  1. การหลีกเลี่ยง Spaghetti Code จากการเช็คสถานะออเดอร์ด้วย Enum flag
  2. ความปลอดภัยระดับองค์กร: ป้องกันร้านเสียการ์ดฟรี และป้องกันปิดออเดอร์ก่อนส่งมอบครบ
  3. สอดคล้องกับหลัก Single Responsibility และ Liskov Substitution Principle
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"กราบเรียนอาจารย์ครับ กระผม แทนคุณ สมาชิกคนที่ 4 รับผิดชอบ **GoF State Pattern, Trade Matching Engine และ Global Exception Handler** ครับ  
  > 
  > เริ่มต้นที่ **GoF State Pattern** เรานำมาใช้ควบคุมวงจรชีวิตของคำสั่งซื้อ 5 สถานะครับ ในระบบเทรดการ์ด ปัญหาใหญ่คือ 'ลูกค้ายกเลิกคำสั่งซื้อหลังจากที่ร้านกดยื่นข้อเสนอเทรดในเกมไปแล้ว' ซึ่งจะทำให้ร้านสูญเสียการ์ดฟรี  
  > 
  > กระผมจึงออกแบบ State Machine แยกคลาสสถานะอย่างเป็นอิสระ โดยมี **Anti-Fraud Guard** ใน `ShippingOrderState` สั่งห้ามยกเลิกออเดอร์เด็ดขาด, มีระบบ **Automatic Stock Restoration** ใน `CancelledOrderState` คืนการ์ดเข้าคลังให้อัตโนมัติ และมี **Completion Guard** ตรวจสอบว่าสินค้าทุกรายการต้องเทรดเสร็จสิ้นก่อน จึงจะอนุญาตให้ปิดคำสั่งซื้อได้ หากยังเทรดไม่ครบจะคืนค่า 409 Conflict ทันทีครับ"*

---

### สไลด์ที่ 9: In-Game Trade Matching Engine (Greedy Stock-Maximization & Auto-Sync)
* **ผู้พูด**: **คนที่ 4 — นายแทนคุณ พันธ์นิกุล (673380301-0)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: Sequence Diagram Scenario 3, Trade Lifecycle Sequence
* **องค์ประกอบบนสไลด์**:
  - ไดอะแกรม: **Sequence Diagram Scenario 3 (Trade Matching & Auto-Sync)** (`doc/diagrams/sequence-diagrams.md#scenario-3`)
  - อัลกอริทึม Greedy Stock-Maximization ใน `TradeMatchingServiceImpl`:
    - กรองไอดีเกมสถานะ `READY` (ไม่ติด Cooldown)
    - เลือกไอดีที่มีสต็อกการ์ดใบนั้นสูงสุดก่อน (`Max Quantity First`)
  - โมเดล Two-Tier Recommendation: Best Match Candidate + Alternative Candidates
  - กลไก **Auto-Sync Order Completed**: เมื่อพนักงานส่งมอบการ์ดในเกมครบทุกรายการ ระบบจะสั่ง State Pattern ปิดออเดอร์หลักเป็น `COMPLETED` อัตโนมัติ
* **ประเด็นที่ต้องอธิบาย**:
  1. เหตุผลที่เลือกไอดีสต็อกสูงสุด: รวมศูนย์การตัดของออกจากไอดีที่มีการ์ดหนาแน่น ลดภาระการสลับล็อกอิน
  2. การซิงค์สต็อกเมื่อสลับบัญชี (Stock Synchronization Transfer)
  3. ลำดับสถานะเทรดรายชิ้น: `UNASSIGNED` $\rightarrow$ `FRIEND_PENDING` $\rightarrow$ `TRADE_SENT` $\rightarrow$ `COMPLETED`
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"ถัดมาคือ **In-Game Trade Matching Engine** ครับ  
  > 
  > จาก **Sequence Diagram Scenario 3** เพื่อแก้ปัญหาพนักงานไม่รู้ว่าจะหยิบการ์ดจากไอดีเกมไหนส่งให้ลูกค้า กระผมพัฒนาอัลกอริทึม **Greedy Stock-Maximization** คัดเลือกเฉพาะไอดีสถานะ `READY` และดึงการ์ดออกจากไอดีที่มีสต็อกสูงสุดก่อนเพื่อลดการกระจายตัวของคลัง พร้อมส่งรายชื่อไอดีสำรองผ่าน Two-Tier DTO ให้เลือกสลับได้  
  > 
  > และเมื่อพนักงานเทรดการ์ดในเกมเสร็จจนครบทุกใบ ระบบมีกลไก **Auto-Sync** เรียก State Pattern เปลี่ยนสถานะออเดอร์หลักเป็น `COMPLETED` ให้อัตโนมัติ ช่วยลดข้อผิดพลาดของพนักงานได้ 100% ครับ"*

---

### สไลด์ที่ 10: Centralized Global Exception Handler (AOP @RestControllerAdvice & RFC 7231)
* **ผู้พูด**: **คนที่ 4 — นายแทนคุณ พันธ์นิกุล (673380301-0)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: REST API (Global Exception Handler, Standard Status Codes & ErrorResponse)
* **องค์ประกอบบนสไลด์**:
  - แผนผัง Spring AOP `@RestControllerAdvice` ใน `GlobalExceptionHandler.java`
  - ตาราง Mapping HTTP Status Codes ตามมาตรฐาน RFC 7231:
    - `400 BAD_REQUEST`: พารามิเตอร์ผิดพลาด, Validation ไม่ผ่าน
    - `403 FORBIDDEN`: ป้องกันลูกค้ายิง API ข้ามสิทธิ์ของพนักงาน
    - `404 NOT_FOUND`: หา Resource, Order หรือ Card ไม่พบ
    - `409 CONFLICT`: ความขัดแย้งเชิงสถานะ (`TradeStateConflictException`, `InvalidOrderStateException`)
    - `500 INTERNAL_SERVER_ERROR`: Unhandled Error พร้อมซ่อน Stacktrace
  - ตัวอย่าง JSON `ErrorResponse` สากล (`timestamp`, `status`, `errorCode`, `message`, `path`)
* **ประเด็นที่ต้องอธิบาย**:
  1. การรวมศูนย์จัดการ Error ไว้ที่จุดเดียว Controller ไม่ต้องเขียน try-catch ซ้ำซ้อน
  2. การเลือกใช้ HTTP 409 Conflict สำหรับข้อผิดพลาดด้านสถานะธุรกิจอย่างถูกต้องตามมาตรฐานสากล
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"ในส่วนสุดท้ายของกระผมคือ **Centralized Global Exception Handler** ครับ  
  > 
  > กระผมใช้ Spring AOP `@RestControllerAdvice` ดักจับข้อผิดพลาดทั้งระบบไว้ที่จุดเดียว ทำให้ใน Controller ไม่ต้องมี try-catch ซ้ำซ้อน และแปลง Exception เป็นมาตรฐาน JSON `ErrorResponse` สากล  
  > 
  > โดยเฉพาะอย่างยิ่ง เราเลือกใช้ **HTTP 409 Conflict** เมื่อเกิดข้อขัดแย้งเชิงสถานะ เช่น พยายามยกเลิกขณะขนส่ง หรือเทรดการ์ดยังไม่ครบแต่พยายามปิดออเดอร์ ซึ่งถูกต้องตามมาตรฐาน RFC 7231 ทำให้ Frontend และผู้ใช้งานได้รับข้อความแจ้งเตือนที่ชัดเจนและปลอดภัยครับ"*

---

### สไลด์ที่ 11: Frontend Web UI, 3D Holo Cards & Chat Commerce Handshake
* **ผู้พูด**: **คนที่ 5 — นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: Presentation Layer, UI/UX Design, Chat Commerce Integration
* **องค์ประกอบบนสไลด์**:
  - ภาพหน้าจอระบบจริง (Screenshots):
    - แกลเลอรีการ์ด 3D พร้อมเอฟเฟกต์โฮโลแกรมแสงสะท้อน (Interactive Holographic Foil Shader)
    - ตัวกรองการ์ดตามธาตุ (Element), ระดับความหายาก (Rarity), และ Expansion Pack
    - หน้าจัดการคลังสินค้า (Inventory Dashboard) และหน้ารายการสั่งซื้อของพนักงาน
  - กลไกการเชื่อมต่อ Chat Commerce Handshake: คัดลอกข้อความสรุปและพาไปยัง Facebook Messenger
* **ประเด็นที่ต้องอธิบาย**:
  1. พัฒนาด้วย Thymeleaf และ Vanilla Modern CSS/JS ปราศจาก Framework หนัก ทำให้เว็บโหลดเร็วมาก
  2. การสร้างประสบการณ์เสมือนถือการ์ดจริงด้วย CSS 3D Transforms และ Gyroscope Pointer Effect
  3. Flow การสั่งซื้อแบบ Chat Commerce ที่ออกแบบมาให้ลูกค้าซื้อง่ายและร้านปิดการขายได้เร็ว
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"กราบเรียนอาจารย์ครับ กระผม สรวิชญ์ สมาชิกคนที่ 5 รับผิดชอบ **Frontend UI, REST API Integration, Docker CI/CD และ Deployment** ครับ  
  > 
  > ในส่วนของหน้าบ้าน เราพัฒนาด้วย Thymeleaf ร่วมกับ Vanilla CSS/JS โดยสร้างจุดเด่นคือ **แกลเลอรีการ์ด 3D Holographic** ที่มีแสงสะท้อนฟอยล์ตามทิศทางเมาส์ มอบประสบการณ์พรีเมียมให้ลูกค้า พร้อมตัวกรองค้นหาการ์ดตามธาตุและความหายาก  
  > 
  > เมื่อลูกค้าเลือกการ์ดและกรอก Friend ID ระบบจะทำการคำนวณราคาและสร้างข้อความสรุปคำสั่งซื้อพร้อมคัดลอกลง Clipboard ให้ลูกค้านำไปส่งต่อใน Messenger เพื่อชำระเงินกับแอดมินร้านได้ในคลิกเดียวครับ"*

---

### สไลด์ที่ 12: REST API & Swagger UI Integration (CRUD 2 Resources, Pagination & Validation)
* **ผู้พูด**: **คนที่ 5 — นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: REST API (10 คะแนนเต็ม), Swagger UI, Bean Validation, Pagination/Sorting
* **องค์ประกอบบนสไลด์**:
  - ภาพถ่ายหน้าจอ **Swagger UI** (`/swagger-ui.html`)
  - ตารางสรุป Resource-based REST Endpoints:
    - **Resource 1: Cards (`/api/v1/cards`)**: ครบ CRUD (GET ทั้งหมดพร้อม Pagination/Sort, GET by id, POST, PUT, DELETE)
    - **Resource 2: Game Accounts (`/api/v1/accounts`)**: ครบ CRUD (GET ทั้งหมดพร้อม Page/Sort, GET by id, POST, PUT, DELETE)
    - **Orders & Trades (`/api/v1/orders`, `/api/v1/trades`)**: State Transitions, Auto-Match, Trade Status
  - การใช้งาน Bean Validation (`@Valid`, `@NotBlank`, `@Min`) และ Pagination (`page`, `size`, `sort`)
* **ประเด็นที่ต้องอธิบาย**:
  1. การออกแบบ API ตามหลัก Resource-Based Naming และใช้ HTTP Methods (GET, POST, PUT, PATCH, DELETE) ถูกต้อง
  2. การตั้งค่า SpringDoc OpenAPI ให้แสดงตัวอย่าง Schema และ ErrorResponse ใน Swagger UI อย่างครบถ้วน
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"ในด้าน REST API ระบบของเราออกแบบตามมาตรฐาน RESTful อย่างสมบูรณ์ มีการทำ **CRUD ครบถ้วนเกิน 2 Resources หลัก** คือระบบจัดการการ์ด `/api/v1/cards` และระบบจัดการไอดีเกม `/api/v1/accounts` ครับ  
  > 
  > ทุก Endpoint มีการตรวจสอบข้อมูลด้วย **Jakarta Bean Validation** เช่น ตรวจสอบราคาห้ามติดลบ, มีระบบ **Pagination และ Sorting** ผ่าน Spring Data Pageable และมีเอกสารสเปก **Swagger UI** ที่เข้าทดสอบผ่าน Browser ได้จริง ณ วันนี้ครับ"*

---

### สไลด์ที่ 13: การวิเคราะห์หลักการ SOLID (ครบ 5 ข้อ) & Enterprise Architecture Patterns (ครบ 6 แบบ)
* **ผู้พูด**: **คนที่ 5 — นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: SOLID Principles (15 คะแนนเต็ม), Enterprise Patterns (15 คะแนนเต็ม)
* **องค์ประกอบบนสไลด์**:
  - ตารางสรุป **SOLID Principles Analysis** อ้างอิง `doc/solid-analysis.md`:
    - **S (SRP)**: แยก Service, Repository, Entity, DTO ไม่ปะปนหน้าที่
    - **O (OCP)**: ขยาย `DiscountStrategy` และ `OrderState` โดยไม่แก้ไขโค้ดเดิม
    - **L (LSP)**: State ทุกตัวสลับแทนที่ใน `OrderContext` ได้อย่างสมบูรณ์
    - **I (ISP)**: แยก Interface เฉพาะทาง ไม่สร้าง Fat Interface
    - **D (DIP)**: พึ่งพา Interface และใช้ Constructor Injection 100%
  - ตารางสรุป **Enterprise Patterns ครบ 6 แบบ** อ้างอิง `doc/design-patterns.md`:
    Layered Architecture, MVC, Repository, Service Layer, DTO+Mapper, Dependency Injection
* **ประเด็นที่ต้องอธิบาย**:
  1. การนำทฤษฎีการออกแบบเชิงวัตถุมาประยุกต์ใช้จริงในโค้ดทุกไฟล์
  2. การมีเอกสารอ้างอิงระบุไฟล์และบรรทัดอย่างโปร่งใสใน `doc/solid-analysis.md`
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"ในด้านคุณภาพการออกแบบ สถาปัตยกรรมของเราสะท้อนหลักการ **SOLID ครบทั้ง 5 ข้อ** อย่างเคร่งครัดครับ  
  > 
  > ไม่ว่าจะเป็น **SRP** ที่แยกหน้าที่ชัดเจน, **OCP** ในระบบส่วนลด, **LSP** ที่คลาส State ทุกตัวสามารถทดแทนกันได้, **ISP** ที่แยก Interface เฉพาะทาง และ **DIP** ที่ทุกคลาสใช้ Constructor Injection โดยเราได้ทำรายงานวิเคราะห์ระบุไฟล์และบรรทัดโค้ดไว้อย่างละเอียดใน `doc/solid-analysis.md`  
  > 
  > พร้อมทั้งปฏิบัติตาม **Enterprise Architecture Patterns ครบทั้ง 6 รูปแบบ** ทั้ง Layered Architecture, MVC, Repository, Service Layer, DTO และ DI ครับ"*

---

### สไลด์ที่ 14: ผลการทดสอบ Unit Testing (355/355 Passes), Git Workflow & การ Deploy บน Cloud
* **ผู้พูด**: **คนที่ 5 — นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1)** (เวลา: 0:45 นาที)
* **เกณฑ์ Rubric**: Testing (5 คะแนน), Git Workflow (5 คะแนน), Deployment (5 คะแนน)
* **องค์ประกอบบนสไลด์**:
  - สถิติผลการรันชุดทดสอบ: **355 / 355 Tests ผ่านทั้งหมด 100% (0 Failures, 0 Errors)** เวลา ~27 วินาที
  - รายงาน Test Reports แยกรายบุคคลในโฟลเดอร์ `test/reports/` (`sikarin`, `sapphanyu`, `tanapoom`, `tankun`)
  - โครงสร้าง Git Workflow: Branch รูปแบบ `ชื่อ_รหัส_section`, การ Merge ผ่าน Pull Request มี Reviewer ครบถ้วน
  - การ Deploy: Multi-Stage Docker Build, GitHub Actions CI/CD Pipeline, และ **Public Cloud URL สำหรับทดสอบสด**
* **ประเด็นที่ต้องอธิบาย**:
  1. การทดสอบครอบคลุมทั้ง Unit Test (Mockito) และ Web Slice Test (MockMvc)
  2. ความพร้อมของระบบบนสภาพแวดล้อมจริง พร้อมเปิด Live Demo ให้คณะกรรมการชม
* **🎙️ บทพูดนำเสนอ (Script)**:
  > *"สไลด์สุดท้ายคือหลักฐานความถูกต้องและการส่งมอบงานครับ  
  > 
  > เรามีชุดทดสอบอัตโนมัติด้วย JUnit 5 และ Mockito ครอบคลุมทั้งโปรเจกต์ **355 Test Cases รันผ่าน 100% BUILD SUCCESS** โดยมี Test Report รับรองผลการทดสอบของสมาชิกทุกคน  
  > 
  > ในด้าน Git Workflow ทุกคนทำงานบน Branch ตามรูปแบบที่กำหนดและ Merge ผ่าน Pull Request โดยมีเพื่อนร่วมทีม Review ก่อนเสมอ และโปรเจกต์นี้ถูก Build ผ่าน Multi-Stage Docker พร้อม Deploy ขึ้น Cloud สาธารณะที่พร้อมใช้งานได้จริงในขณะนี้ครับ กลุ่ม PokéVault Commerce ขอขอบพระคุณอาจารย์ครับ และพร้อมสำหรับการตอบข้อซักถามครับ"*

---

## 🛡️ คลังข้อมูลเจาะลึกและการตอบคำถามรายบุคคล (Individual Q&A Defense Guide)
*(หัวใจสำคัญของ **เกณฑ์ข้อที่ 1: 20 คะแนนเต็ม** — ประเมินรายบุคคล ตอบไม่ได้ได้ 0)*

---

### 1. สมาชิกคนที่ 1: นายศิฆรินทร์ อุปจันทร์ (673380292-5)
* **บทบาท**: Core Entity, Card Catalog, Schema/Database, Spring Security Specialist
* **ไฟล์ที่รับผิดชอบหลัก**:
  - `Card.java`, `CardExpansion.java`, `User.java`, `UserProfile.java`
  - `CardServiceImpl.java`, `CardApiController.java`, `CardRepository.java`
  - `schema.sql`, `data.sql`, `core-schema-alignment.sql`
  - `SecurityConfig.java`, `CustomUserDetailsService.java`, `OrderAccessPolicy.java`
* **คำถามเก็ง 1**: *ทำไมต้องแยกตาราง `users` กับ `user_profiles` ออกจากกันเป็นความสัมพันธ์ 1:1?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"เพื่อปฏิบัติตามหลัก **Single Responsibility Principle (SRP)** ครับ ตาราง `users` มีหน้าที่ดูแลความปลอดภัย การยืนยันตัวตน (Authentication) และรหัสผ่านที่เข้ารหัสด้วย BCrypt ส่วน `user_profiles` มีหน้าที่เก็บข้อมูลธุรกิจของลูกค้า เช่น ชื่อ ที่อยู่จัดส่ง และระดับสมาชิก การแยกกันช่วยป้องกันไม่ให้การอัปเดตข้อมูลลูกค้าไปกระทบกับตารางความปลอดภัย และป้องกันข้อมูล Credential รั่วไหลเมื่อส่ง DTO ของโปรไฟล์ครับ"*
* **คำถามเก็ง 2**: *ป้องกันปัญหา IDOR (Insecure Direct Object References) ในการเข้าถึงคำสั่งซื้ออย่างไร?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"เราสร้าง Service ชื่อ `OrderAccessPolicy` ตรวจสอบ Username จาก `SecurityContext` เทียบกับเจ้าของ Order ในฐานข้อมูลจริง ไม่เชื่อถือ `userId` ที่ส่งมาจาก Client โดยใช้ `@PreAuthorize` และ `@PostAuthorize` ในระดับ Controller หากไม่ใช่เจ้าของหรือไม่มีสิทธิ์ Staff จะถูกปฏิเสธทันทีด้วย HTTP 403 Forbidden ครับ"*

---

### 2. สมาชิกคนที่ 2: นายสัพพัญญู คำตุ้ม (673380066-4)
* **บทบาท**: Game Account Vault, Inventory Management, Observer Pattern Specialist
* **ไฟล์ที่รับผิดชอบหลัก**:
  - `GameAccount.java`, `CardInventory.java`
  - `GameAccountServiceImpl.java`, `GameAccountApiController.java`
  - `LowStockObserver.java`, `OrderPlacedEvent.java`
* **คำถามเก็ง 1**: *ทำไมจึงเลือกใช้ GoF Observer Pattern ในการแจ้งเตือนสต็อกต่ำ แทนที่จะให้ `OrderService` สั่งแจ้งเตือนตรงๆ?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"เพื่อลด **Tight Coupling** และรักษาหลัก **Single Responsibility Principle** ครับ หน้าที่ของ `OrderService` คือการประมวลผลคำสั่งซื้อ ไม่ควรต้องมารับรู้เรื่อง Notification หรือการติดตามคลังการ์ด การใช้ Observer Pattern ผ่าน Spring `ApplicationEventPublisher` ทำให้ `OrderService` เพียงยิง `OrderPlacedEvent` ออกไป และปล่อยให้ `LowStockObserver` ดักฟังและตรวจสอบ threshold $\le 2$ ได้อย่างอิสระ หากอนาคตต้องการเพิ่มการแจ้งเตือนผ่าน Discord หรือ LINE ก็สร้าง Observer ใหม่มาดักฟัง Event เดิมได้ทันทีโดยไม่ต้องแก้โค้ดฝั่ง Order ครับ"*
* **คำถามเก็ง 2**: *ในระดับ Entity มีการป้องกันไม่ให้สต็อกการ์ดติดลบอย่างไร?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"ในคลาส `CardInventory` เมธอด `deductStock(int qty)` มี Business Invariant Guard ดักว่า ถ้าจำนวนที่ขอตัดมากกว่าสต็อกที่มี หรือค่า `qty <= 0` จะโยน `InsufficientStockException` ทันที และบันทึกผ่าน Transaction ทำให้ไม่เกิดภาวะ Race Condition หรือสต็อกติดลบครับ"*

---

### 3. สมาชิกคนที่ 3: นายธนภูมิ จันทรา (673380272-1)
* **บทบาท**: Order Engine, Transactional Booking, Strategy Pattern Specialist
* **ไฟล์ที่รับผิดชอบหลัก**:
  - `Order.java`, `OrderItem.java`
  - `OrderServiceImpl.java` (createOrder, cancelOrder, reassignAccount)
  - `DiscountStrategy.java`, `RegularDiscountStrategy.java`, `VipDiscountStrategy.java`, `WholesaleDiscountStrategy.java`, `DiscountService.java`
* **คำถามเก็ง 1**: *การนำ Strategy Pattern มาใช้ในระบบส่วนลด สอดคล้องกับ Open/Closed Principle (OCP) อย่างไร?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"สอดคล้อง 100% ครับ เราสร้าง Interface `DiscountStrategy` และแยกคลาสคำนวณส่วนลดของแต่ละระดับสมาชิกออกเป็น `RegularDiscountStrategy`, `VipDiscountStrategy`, `WholesaleDiscountStrategy` โดย `DiscountService` ทำหน้าที่เป็น Context รับ `List<DiscountStrategy>` ผ่าน Spring DI เมื่อธุรกิจต้องการเพิ่มโปรโมชันใหม่ เช่น สมาชิก VIP+ ลด 20% เราเพียงสร้างคลาสใหม่ที่ Implement `DiscountStrategy` โดยไม่ต้องแก้ไขหรือเขียน if-else เพิ่มใน `DiscountService` เดิมแม้แต่บรรทัดเดียว เป็นการเปิดรับการขยายแต่ปิดการแก้ไขครับ"*
* **คำถามเก็ง 2**: *ถ้าลูกค้าสั่งซื้อการ์ด 3 รายการพร้อมกัน แต่รายการสุดท้ายสต็อกไม่พอ ระบบมีวิธีจัดการอย่างไร?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"เมธอด `createOrder` กำกับด้วย `@Transactional` เมื่อวนลูปตรวจสอบสต็อกและพบว่ารายการสุดท้ายไม่พอ จะโยน `InsufficientStockException` ทันที ซึ่งจะสั่ง Rollback Database Transaction ทั้งหมด ทำให้การ์ด 2 รายการแรกที่ถูกหักไปได้รับการคืนสต็อกทันที และไม่มีการบันทึก Record ของ Order ลงในฐานข้อมูล เพื่อรักษา Data Consistency ครับ"*

---

### 4. สมาชิกคนที่ 4: นายแทนคุณ พันธ์นิกุล (673380301-0)
* **บทบาท**: GoF State Pattern, Trade Matching Engine, Global Exception Handler Specialist
* **ไฟล์ที่รับผิดชอบหลัก**:
  - `OrderState.java`, `OrderContext.java`, `PendingOrderState.java`, `PaidOrderState.java`, `ShippingOrderState.java`, `CompletedOrderState.java`, `CancelledOrderState.java`
  - `TradeMatchingServiceImpl.java`, `TradeMatchingApiController.java`, `TradeRecommendationResponse.java`
  - `GlobalExceptionHandler.java`, `InvalidOrderStateException.java`, `TradeStateConflictException.java`
* **ไฟล์อ้างอิงเจาะลึกเฉพาะบุคคล**: ดูคู่มือฉบับเต็มใน [`doc/slide/slide MEMBER 4.md`](file:///e:/Coding/Pok-Vault_Commerce/doc/slide/slide%20MEMBER%204.md)
* **คำถามเก็ง 1**: *Anti-Fraud Guard และ Completion Guard ใน `ShippingOrderState` ทำงานอย่างไรและแก้ปัญหาอะไร?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"ในสถานะ `SHIPPING` พนักงานได้ส่งข้อเสนอเทรดการ์ดในเกมให้ลูกค้าแล้ว หากปล่อยให้ลูกค้ายกเลิกคำสั่งซื้อ ลูกค้าจะได้ทั้งเงินคืนและได้การ์ดในเกมฟรี เราจึงมี **Anti-Fraud Guard** ให้เมธอด `cancel()` โยน `InvalidOrderStateException` ทันที และมี **Completion Guard** ตรวจสอบว่าสินค้าทุกรายการในออเดอร์ต้องมีสถานะเป็น `COMPLETED` ก่อนเท่านั้นจึงจะอนุญาตให้ปิดออเดอร์ (`complete()`) หากยังเทรดไม่ครบจะโยน `TradeStateConflictException` (HTTP 409 Conflict) ทันทีทั้งแบบกดปิดเองและแบบ Auto-Sync ครับ"*
* **คำถามเก็ง 2**: *อัลกอริทึม Greedy Stock-Maximization ใน Trade Matching ทำงานอย่างไร และมีเหตุผลใดที่เลือกไอดีที่มีสต็อกสูงสุด?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"ระบบจะดึง `CardInventory` ที่ถือการ์ดใบที่สั่งซื้อ กรองเฉพาะไอดีเกมที่มีสถานะ `READY` (ไม่ติด Cooldown) และคัดเลือกไอดีที่มีสต็อกสูงสุดขึ้นมาเป็น `Best Match` เหตุผลคือเพื่อรวมศูนย์การตัดการ์ดออกจากไอดีที่มีสต็อกหนาแน่น ลดการกระจายตัวของคลัง และลดภาระพนักงานในการสลับล็อกอินหลายไอดี พร้อมทั้งส่ง Two-Tier DTO ที่มี Alternative Candidates ให้เลือกสำรองกรณีไอดีหลักมีปัญหาครับ"*
* **คำถามเก็ง 3**: *ทำไมถึงเลือกใช้ HTTP Status 409 Conflict ใน GlobalExceptionHandler?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"ตามมาตรฐาน RFC 7231 ของ HTTP ข้อผิดพลาดที่เกิดจากการฝ่าฝืน Business State หรือเกิดความขัดแย้งของสถานะปัจจุบันกับคำขอ (State Conflict) ควรใช้ **409 Conflict** ไม่ควรใช้ 400 Bad Request หรือ 500 Server Error เพราะไม่ใช่ไวยากรณ์ผิดและไม่ใช่เซิร์ฟเวอร์พัง แต่เป็นข้อขัดแย้งเชิงสถานะทางธุรกิจครับ"*

---

### 5. สมาชิกคนที่ 5: นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1)
* **บทบาท**: Web Frontend, REST API & Swagger, DevOps, Docker CI/CD & Deployment Specialist
* **ไฟล์ที่รับผิดชอบหลัก**:
  - `WebViewController.java`, `WebPageService.java`, `templates/*.html`, `static/css/`, `static/js/`
  - `Dockerfile`, `docker-compose.yml`, `.github/workflows/ci-cd.yml`
  - Swagger UI Configuration, `README.md`
* **คำถามเก็ง 1**: *ทำไมถึงเลือกทำ Frontend ด้วย Thymeleaf + Vanilla Modern CSS แทนที่จะใช้ React หรือ Vue?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"เนื่องจากโปรเจกต์นี้เป็น Spring Boot Monolith สถาปัตยกรรม SSR (Server-Side Rendering) ด้วย Thymeleaf ช่วยให้เชื่อมต่อกับ Model และ Security Context ได้อย่างราบรื่น ไม่ต้องตั้งค่า CORS หรือจัดการ Auth Token ที่ซับซ้อน และการใช้ Vanilla Modern CSS ร่วมกับ 3D Transforms ช่วยให้หน้าเว็บโหลดได้เร็วกว่า มี Performance สูง และสามารถสร้างเอฟเฟกต์ Holographic Foil ได้อย่างลื่นไหลโดยไม่ต้องพึ่งพาไลบรารีภายนอกครับ"*
* **คำถามเก็ง 2**: *กระบวนการ CI/CD และ Docker Multi-Stage Build ช่วยเพิ่มประสิทธิภาพในการส่งมอบงานอย่างไร?*
  - **แนวทางตอบ (ได้คะแนนเต็ม)**: *"ใน `Dockerfile` เราใช้ **Multi-Stage Build**: Stage แรกใช้ Maven Image สำหรับคอมไพล์โค้ดและรันเทส และ Stage ที่สองคัดลอกเฉพาะไฟล์ `.jar` ขนาดกะทัดรัดมาใส่ใน JRE Base Image น้ำหนักเบา ทำให้ขนาดของ Docker Image สุดท้ายมีขนาดเล็กมาก และปลอดภัยเพราะไม่มี Source Code หรืองาน Build หลงเหลืออยู่ และเชื่อมโยงกับ GitHub Actions เมื่อมี PR เข้าสู่ branch `develop` ระบบจะทำการรันเทสและ Build Image อัตโนมัติทันทีครับ"*

---

## 🛠️ คำสั่งสาธิตสดประกอบการนำเสนอ (Live Demo Commands)

หากอาจารย์สั่งให้รันคำสั่งสดเพื่อพิสูจน์ระบบในวันนำเสนอ ให้ใช้คำสั่งมาตรฐานเหล่านี้:

1. **รัน Unit Test ทั้งหมดของโปรเจกต์ (355 ข้อ)**:
   ```powershell
   .\mvnw.cmd test
   ```
2. **รัน Unit Test เฉพาะของสมาชิกคนที่ 4 (87 ข้อ)**:
   ```powershell
   .\mvnw.cmd test "-Dtest=OrderStateTest,TradeMatchingServiceTest,TradeMatchingApiControllerTest,OrderApiControllerTest,GlobalExceptionHandlerTest,TradeRecommendationResponseTest,CustomExceptionTest"
   ```
3. **รันแอปพลิเคชัน Spring Boot**:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. **เปิดหน้า Swagger UI**:
   - URL: `http://localhost:8080/swagger-ui.html`
5. **เปิดหน้าเว็บหน้าร้าน (Storefront)**:
   - URL: `http://localhost:8080/`
