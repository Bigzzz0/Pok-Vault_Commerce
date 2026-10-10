# 📑 คู่มือนำเสนอและตอบคำถามเดี่ยว สมาชิกคนที่ 4: นายแทนคุณ พันธ์นิกุล
## โครงการ: PokéVault Commerce (Pokémon TCG Pocket Vault & Chat Commerce)
**วิชา**: CP353002 Principles of Software Design and Development (Spring Boot 3.4.3 / Java 17)  
**ผู้นำเสนอ**: **สมาชิกคนที่ 4 — นายแทนคุณ พันธ์นิกุล (รหัสนักศึกษา: 673380301-0)**  
**บทบาทหน้าที่**: Trade Matching Engine, GoF State Pattern, Auto-Sync Lifecycle & Centralized Global Exception Handler  
**Git Branch**: `Tankun_6733803010_01` (ตรงตามรูปแบบ `ชื่อ_รหัสนักศึกษา_section`)  
**ผลงานการทดสอบเฉพาะตัว**: ✅ **7 Test Classes / 94 Test Cases (100% BUILD SUCCESS — 0 Failures, 0 Errors, ~2.5 วินาที)**  
**ลิงก์สไลด์ Canva (49 หน้า)**: [เปิดสไลด์ Canva ชุดปัจจุบัน](https://canva.link/65ccer7e31k2afm)

---

## 🧭 สารบัญเอกสาร (Table of Contents)
1. [ภาพรวมการนำเสนอ 5 คน / 9 นาที และบทบาทของแทนคุณ](#1-ภาพรวมการนำเสนอ-5-คน--9-นาที-และบทบาทของแทนคุณ)
2. [ตารางวิเคราะห์ความสอดคล้องกับเกณฑ์การประเมิน 100 คะแนน (Rubric Scoring Guide)](#2-ตารางวิเคราะห์ความสอดคล้องกับเกณฑ์การประเมิน-100-คะแนน-rubric-scoring-guide)
3. [บทพูดและคิวการนำเสนอของแทนคุณ (เวลา 1:35 นาที + Demo 3)](#3-บทพูดและคิวการนำเสนอของแทนคุณ-เวลา-135-นาที--demo-3)
   - [ช่วงที่ 1: หน้า 13–14 (3:35–4:00) — State Diagram & โค้ด State Pattern](#ช่วงที่-1-หน้า-1314-335400--state-diagram--โค้ด-state-pattern)
   - [ช่วงที่ 2: หน้า 17–18 (4:30–4:50) — Sequence (3 Scenarios) & Activity Diagram](#ช่วงที่-2-หน้า-1718-430450--sequence-3-scenarios--activity-diagram)
   - [ช่วงที่ 3: Live Demo 3 (6:40–7:30) — จับคู่และบันทึกเทรดครบ บนเว็บ `/orders`](#ช่วงที่-3-live-demo-3-640730--จับคู่และบันทึกเทรดครบ-บนเว็บ-orders)
4. [สิ่งที่แทนคุณต้องรู้จากประวัติ Commit ของตนเอง (Commits Deep-Dive)](#4-สิ่งที่แทนคุณต้องรู้จากประวัติ-commit-ของตนเอง-commits-deep-dive)
5. [คลังความรู้เชิงลึก: Strategy และ SOLID Principles ในโค้ดของแทนคุณ (Master Knowledge Synthesis)](#5-คลังความรู้เชิงลึก-strategy-และ-solid-principles-ในโค้ดของแทนคุณ-master-knowledge-synthesis)
   - [5.1 กลยุทธ์การเขียนโค้ดและ Strategy ทั้ง 6 รูปแบบที่แทนคุณใช้จริง (Coding Strategies & Strategy Patterns)](#51-กลยุทธ์การเขียนโค้ดและ-strategy-ทั้ง-6-รูปแบบที่แทนคุณใช้จริง-coding-strategies--strategy-patterns)
   - [5.2 การวิเคราะห์ SOLID Principles ทั้ง 5 ข้อ (S, O, L, I, D) ในโค้ดของแทนคุณอย่างละเอียด](#52-การวิเคราะห์-solid-principles-ทั้ง-5-ข้อ-s-o-l-i-d-ในโค้ดของแทนคุณอย่างละเอียด)
   - [5.3 กฎความปลอดภัยทางธุรกิจและ Invariants (Anti-Fraud, Auto-Restore, Completion Guard)](#53-กฎความปลอดภัยทางธุรกิจและ-invariants-anti-fraud-auto-restore-completion-guard)
   - [5.4 อัลกอริทึม In-Game Trade Matching (Greedy Stock-Maximization & Condition Matching)](#54-อัลกอริทึม-in-game-trade-matching-greedy-stock-maximization--condition-matching)
   - [5.5 วงจรสถานะการเทรด (Trade Status Lifecycle) และระบบ Auto-Sync](#55-วงจรสถานะการเทรด-trade-status-lifecycle-และระบบ-auto-sync)
   - [5.6 มาตรฐาน REST API และการจัดการข้อยกเว้นด้วย AOP (ทำไมต้อง HTTP 409 Conflict)](#56-มาตรฐาน-rest-api-และการจัดการข้อยกเว้นด้วย-aop-ทำไมต้อง-http-409-conflict)
   - [5.7 ข้อเท็จจริงที่ต้องตอบให้ตรง (ห้ามผิดเด็ดขาด)](#57-ข้อเท็จจริงที่ต้องตอบให้ตรง-ห้ามผิดเด็ดขาด)
6. [แนวทางการตอบคำถามช่วง Q&A (3 นาทีหลัง 9:00) และการเปิดหลักฐาน (หน้า 23–49)](#6-แนวทางการตอบคำถามช่วง-qa-3-นาทีหลัง-900-และการเปิดหลักฐาน-หน้า-2349)
   - [6.1 กฎเหล็กการตอบคำถาม 20–30 วินาที](#61-กฎเหล็กการตอบคำถาม-2030-วินาที)
   - [6.2 เก็ง 12 คำถามเชือดของอาจารย์พร้อมแนวทางตอบแบบคะแนนเต็ม (เน้น Strategy & SOLID)](#62-เก็ง-12-คำถามเชือดของอาจารย์พร้อมแนวทางตอบแบบคะแนนเต็ม-เน้น-strategy--solid)
   - [6.3 คำสั่งรันเทสสด 94 เคสแสดงหน้าห้อง (Live Verification)](#63-คำสั่งรันเทสสด-94-เคสแสดงหน้าห้อง-live-verification)

---

## 1. ภาพรวมการนำเสนอ 5 คน / 9 นาที และบทบาทของแทนคุณ

* **ชุดสไลด์ Canva ปัจจุบัน**: **49 หน้า** ([เปิดสไลด์](https://canva.link/65ccer7e31k2afm))
  - **หน้า 1–22**: สไลด์เนื้อหาหลักและคิวการ Demo (เดินหน้าเรียง 1 ➔ 22 ตามลำดับ)
  - **หน้า 23–49**: ภาคผนวกหลักฐานเชิงลึกสำหรับเปิดซัพพอร์ตช่วงถามตอบ (Q&A Defense)
  - **หน้า 49**: ป้าย Live Demo สำรอง (ไม่ต้องเปิดถ้าเว็บทำงานปกติ)
* **ผู้คุมจอ**: **สรวิชญ์ตลอดการนำเสนอ** เปลี่ยนสไลด์และเปิดเว็บตามคิว โดยใช้เครื่องและเบราว์เซอร์เดิม ทุกคนยืนพร้อม ไม่เดินสลับมาคุมเมาส์
* **เวลา 0:00 เริ่มเมื่อเริ่มพูด**: เวลาทั้งหมดรวมการเปลี่ยนสไลด์ ส่งต่อผู้พูด และกดคลิก Demo แล้ว เป็นเป้าหมายสำหรับซ้อมจับเวลา

### ตารางภาพรวมผู้พูดทั้ง 5 คน (เป้าหมาย 9:00 นาที)

| ผู้พูด | หน้าสไลด์ / ช่วงงาน | เนื้อหาที่รับผิดชอบ | เวลารวม |
| :--- | :--- | :--- | :---: |
| **ศิฆรินทร์ อุปจันทร์** | 1, 3–8, 20–21 | เปิดเรื่อง, Use Case, Domain, Layered + โค้ด Catalog, SOLID, Database, Testing + Security | 2:30 |
| **สัพพัญญู คำตุ้ม** | 2, 15 และ Demo เพิ่มสต็อก | ปัญหาของร้าน, Observer + โค้ด, บัญชีเกมและ Inventory | 1:30 |
| **ธนภูมิ จันทรา** | 11–12 และ Demo จองการ์ด | Class Diagram / GoF, Strategy + โค้ด, Order และส่วนลด | 1:35 |
| **แทนคุณ พันธ์นิกุล (คุณ)** | **13–14, 17–18 และ Demo เทรด** | **State + โค้ด, Sequence / Activity, Matching และการปิดออเดอร์** | **1:35** |
| **สรวิชญ์ ศาสนสุพินธุ์** | 9–10, 16, 19, 22 และ Demo Swagger | Enterprise Patterns, Web / Deploy / Git, REST API, บทเรียนและปิดเรื่อง (ผู้คุมจอ) | 1:50 |
| **รวมเวลาพูดหลัก** | **หน้า 1–22 + 3 Web Demos + Swagger** | **แบ่งตามแนวทาง: Problem 1:30, Design 3:00, Demo 3:30, สรุป 1:00** | **9:00** |
| **ช่วงถาม-ตอบ (Q&A)** | **หน้า 23–49 (ภาคผนวก)** | **ถามตอบรายบุคคล ตอบกระชับ 20–30 วินาที ชี้ Diagram และโค้ด** | **3:00** |

### ลำดับเวลาหลัก 9 นาที (Master Timeline)

| เวลา | สิ่งที่อยู่บนจอ | ผู้พูด | เป้าหมายการนำเสนอ |
| :---: | :---: | :---: | :--- |
| 0:00–0:10 | หน้า 1 | ศิฆรินทร์ | ชื่อและคุณค่าของระบบ PokéVault Commerce |
| 0:10–0:45 | หน้า 2 | สัพพัญญู | ปัญหาที่ร้านเจอ (การ์ดกระจายหลายไอดี, คุมสต็อกยาก) |
| 0:45–1:10 | หน้า 3 | ศิฆรินทร์ | ผู้ใช้, สิทธิ์ (Customer vs Staff), ขอบเขตระบบ |
| 1:10–1:30 | หน้า 4 | ศิฆรินทร์ | Domain Model และความเชื่อมโยงของข้อมูล |
| 1:30–2:40 | หน้า 5–8 | ศิฆรินทร์ | Layered Architecture, โค้ด Catalog, SOLID, ER 8 ตาราง |
| 2:40–3:10 | หน้า 9–10 | สรวิชญ์ | Enterprise Patterns ครบ 6 รายการ (Layered, MVC, Repo, Service, DTO, DI) |
| 3:10–3:35 | หน้า 11–12 | ธนภูมิ | Class Diagram รวม GoF 3 แบบ และ GoF Strategy คิดส่วนลด |
| **3:35–4:00** | **หน้า 13–14** | **แทนคุณ** | **GoF State Diagram, โค้ด ShippingOrderState, Guard & HTTP 409** |
| 4:00–4:15 | หน้า 15 | สัพพัญญู | GoF Observer Pattern ตรวจสต็อกต่ำผ่าน Event Listener |
| 4:15–4:30 | หน้า 16 | สรวิชญ์ | Component, Deployment (Render + Neon), Git Actions CI |
| **4:30–4:50** | **หน้า 17–18** | **แทนคุณ** | **Sequence Diagram 3 Scenarios และ Activity Diagram (ส่งเข้า Demo)** |
| 4:50–6:00 | เว็บ `/inventory` ➔ `/orders` | ธนภูมิ | Happy Path 1: จองการ์ด (VIP ส่วนลด 10%, จองสต็อก) |
| 6:00–6:40 | เว็บ `/accounts` | สัพพัญญู | Happy Path 2: บันทึกเปิดซองเพิ่มสต็อกเข้าไอดีเกม |
| **6:40–7:30** | **เว็บ `/orders`** | **แทนคุณ** | **Happy Path 3: จ่ายเงิน ➔ Auto-Match เทรด ➔ Sync ปิดออเดอร์** |
| 7:30–8:00 | หน้า 19 ➔ Swagger | สรวิชญ์ | CRUD 2 Resources, ลองเรียก API สดบน Swagger UI |
| 8:00–8:25 | หน้า 20–21 | ศิฆรินทร์ | ผลทดสอบรวม (373 Tests) และตัวอย่าง Security Test (403 Forbidden) |
| 8:25–9:00 | หน้า 22 | สรวิชญ์ | บทเรียน (UI State sync, WebP), แผนงานต่อไป และปิดเรื่อง |

---

## 2. ตารางวิเคราะห์ความสอดคล้องกับเกณฑ์การประเมิน 100 คะแนน (Rubric Scoring Guide)

ตารางนี้ถอดแบบมาจาก **"เกณฑ์การประเมินวันนำเสนอ CP353002 Principles of Software Design and Development"** เพื่อชี้ชัดว่าในแต่ละหัวข้อ **แทนคุณ พันธ์นิกุล** ได้สร้างผลงานรองรับไว้อย่างไร เพื่อการันตีคะแนนเต็ม:

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                            เกณฑ์การประเมินวันนำเสนอ CP353002 (100 คะแนนเต็ม) vs ผลงานของแทนคุณ                   │
├─────────────────────────────────────────┬────────┬───────────────────────────────────────────────────────────────┤
│ รายการประเมิน                           │ คะแนน  │ การรองรับและหลักฐานในส่วนงานของแทนคุณ (นายแทนคุณ พันธ์นิกุล)  │
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 1. ความเข้าใจระบบและการตอบคำถาม         │ 20     │ • อธิบายโค้ดที่ตนเองเขียนได้ทุกบรรทัด (ประเมินรายบุคคล หากตอบไม่ได้ได้ 0) │
│    (ประเมินรายบุคคล ตอบคำถามเชิงออกแบบ) │        │ • ถือครอง GoF State Pattern, Trade Matching Engine, Exception Advice   │
│                                         │        │ • มีคู่มือ Code Defense และเก็งคำถามเจาะลึก Strategy & SOLID ในหมวดที่ 6│
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 2. ความถูกต้องและครบถ้วนของ Diagram     │ 15     │ • นำเสนอ State Diagram (หน้า 13): วงจร 5 สถานะ + Guards                 │
│    (Use Case, Domain, Class, Sequence   │        │ • นำเสนอ Sequence Diagram 3 Scenarios (หน้า 17): จองการ์ด, เปิดซอง, เทรด│
│     3 Scenarios, Activity, ER, State)   │        │ • นำเสนอ Activity Diagram (หน้า 18): Decision จุดตรวจเงื่อนไขและปิดงาน  │
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 3. Layered Architecture & SOLID         │ 15     │ • แยก Layer ชัดเจน: Controller ➔ Service ➔ Repository ไม่ข้าม Layer     │
│    (ไม่ข้าม Layer, Constructor Inj.,    │        │ • ใช้ Constructor Injection ผ่าน Lombok @RequiredArgsConstructor 100% │
│     doc/solid-analysis.md ระบุไฟล์/บรรทัด)│      │ • ปฏิบัติตาม SOLID ครบ 5 ข้อ (S, O, L, I, D) เจาะลึกตามหมวดที่ 5.2    │
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 4. Design Patterns และเหตุผลเลือกใช้    │ 15     │ • ถือครอง GoF State Pattern (กลุ่ม Behavioral ร่วมกับ Strategy/Observer)│
│    (Enterprise 6 แบบ, GoF 3 แบบกลุ่มเดียว│        │ • มีเหตุผลทางธุรกิจชัดเจน (Anti-Fraud Guard ป้องกันลูกค้าโกงเอาการ์ดฟรี)│
│     มี doc/design-patterns.md ไม่ยัดแพทเทิร์น)│   │ • ประยุกต์ใช้ Strategy 6 รูปแบบ ทั้ง Dynamic Behavioral & Greedy Match│
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 5. Database Design                      │ 10     │ • ควบคุมความสัมพันธ์ 1:N ของ Order ➔ OrderItem และ OrderItem ➔ Inventory │
│    (≥6 ตาราง, 1:1, 1:N, FK, Cascade,    │        │ • ควบคุม ACID Transactional Boundary ในการสลับสต็อกและคืนสต็อก         │
│     FetchType, Migration Script)        │        │ • Auto Stock Restoration คืนการ์ดเข้า CardInventory เมื่อ Order ถูกยกเลิก│
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 6. REST API และ Swagger                 │ 10     │ • พัฒนา Centralized GlobalExceptionHandler (AOP @RestControllerAdvice) │
│    (CRUD ≥2 Resources, Status Codes,    │        │ • จัดการ HTTP Status ถูกต้องตาม RFC 7231 โดยเฉพาะ 409 Conflict และ 400│
│     Global Exception, Validation,       │        │ • พัฒนา API Trade Matching (5 Endpoints) และ Order Status Transition  │
│     Pagination, Sorting, Swagger UI)    │        │ • เอกสาร OpenAPI / Swagger Annotations ครบถ้วน                         │
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 7. Testing                              │ 5      │ • พัฒนาชุดทดสอบเดี่ยว: 7 Test Classes / 94 Test Cases (100% ผ่าน)      │
│    (JUnit 5, Mockito, Spring Boot Test, │        │ • ใช้ Mockito Standalone สปีดเร็ว ~2.5 วินาที ไร้ Failure และ Error   │
│     Test Report ครบ)                    │        │ • มีบันทึกผลการทดสอบอย่างเป็นทางการใน README-TEST.md                  │
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 8. Git Workflow, README & Repository    │ 5      │ • Branch ชื่อ: Tankun_6733803010_01 ตรงตามรูปแบบที่กำหนด               │
│    (Branch ชื่อ_รหัส_sec, Merge ผ่าน PR, │        │ • Merge ผ่าน Pull Request มี Reviewer ตรวจสอบครบถ้วน                   │
│     มี Reviewer, โฟลเดอร์ code/test/doc)│        │ • มีประวัติ Commit ชัดเจนตั้งแต่ Milestone 1 ถึง Milestone 5           │
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ 9. Deployment                           │ 5      │ • มีส่วนร่วมในระบบที่ Deploy จริงบน Render (Web) + Neon (PostgreSQL)  │
│    (เข้าถึงผ่าน Public URL ได้จริง ณ วัน)│        │ • สาธิต Live Demo Happy Path 3 (จับคู่และบันทึกเทรดครบ) บนระบบ Deploy │
├─────────────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────┤
│ รวมคะแนนเต็ม                             │ 100    │ พร้อมตอบและสาธิตอย่างมั่นใจทุกประเด็น                                 │
└─────────────────────────────────────────┴────────┴───────────────────────────────────────────────────────────────┘
```

---

## 3. บทพูดและคิวการนำเสนอของแทนคุณ (เวลา 1:35 นาที + Demo 3)

แทนคุณมีคิวพูดในสไลด์ 2 ช่วง และขึ้น Demo 1 ช่วง รวมเวลาทั้งสิ้น **2 นาที 25 วินาที**:
- **สไลด์หน้า 13–14**: 25 วินาที (3:35–4:00)
- **สไลด์หน้า 17–18**: 20 วินาที (4:30–4:50)
- **Live Demo 3 บนเว็บ**: 50 วินาที (6:40–7:30)

---

### ช่วงที่ 1: หน้า 13–14 (3:35–4:00) — State Diagram & โค้ด State Pattern

#### สไลด์หน้า 13 (เวลา 3:35–3:50 / 15 วินาที)
* **สิ่งที่อยู่บนจอ**: State Transition Diagram ของ Order และความสัมพันธ์กับ OrderItem
* **🎙️ บทพูดของแทนคุณ**:
  > *"สถานะ Order กับสถานะเทรดของแต่ละ Item แยกกันครับ Order ปิดได้เมื่อทุก Item เป็น Completed และเมื่ออยู่ Shipping จะยกเลิกไม่ได้"*
* **👆 ท่าทางและการชี้ (Action Cues)**:
  - ชี้แถวสถานะ Order (`PENDING` ➔ `PAID` ➔ `SHIPPING` ➔ `COMPLETED`) เทียบกับแถวสถานะของ `OrderItem` (`UNASSIGNED` ➔ `FRIEND_PENDING` ➔ `TRADE_SENT` ➔ `COMPLETED`)
  - ชี้จุดตรวจครบ (All Items Completed) ก่อนที่คำสั่งซื้อจะเปลี่ยนเข้าสู่ `COMPLETED`

#### สไลด์หน้า 14 (เวลา 3:50–4:00 / 10 วินาที)
* **สิ่งที่อยู่บนจอ**: ซอร์สโค้ด `ShippingOrderState.java`
* **🎙️ บทพูดของแทนคุณ**:
  > *"ShippingOrderState ใช้ allMatch ตรวจว่าทุก Item Completed ถ้ายังไม่ครบจะโยนข้อผิดพลาดที่ตอบ HTTP 409 ครับ"*
* **👆 ท่าทางและการชี้ (Action Cues)**:
  - ชี้บรรทัด `allItemsCompleted = order.getItems().stream().allMatch(...)`
  - ชี้จุดที่โยน `TradeStateConflictException`
  - ชี้คำสั่งเปลี่ยนสถานะสู่ `context.setState(new CompletedOrderState())`

---

### ช่วงที่ 2: หน้า 17–18 (4:30–4:50) — Sequence (3 Scenarios) & Activity Diagram

#### สไลด์หน้า 17 (เวลา 4:30–4:40 / 10 วินาที)
* **สิ่งที่อยู่บนจอ**: Sequence Diagram แสดง 3 Scenarios
* **🎙️ บทพูดของแทนคุณ**:
  > *"Sequence แสดงสาม Use Case คือจองการ์ด บันทึกเปิดซอง และเทรดครับ เห็นลำดับการเรียก Controller, Service และบันทึกข้อมูล"*
* **👆 ท่าทางและการชี้ (Action Cues)**:
  - กวาดมือชี้ลูกศรจาก Actor ไปยัง Controller และ Service ในแต่ละ Scenario ทั้ง 3 แถบ
  - ไม่อ่านชื่อทุก Message บนลูกศรเพื่อประหยัดเวลา

#### สไลด์หน้า 18 (เวลา 4:40–4:50 / 10 วินาที)
* **สิ่งที่อยู่บนจอ**: Activity Diagram แสดงเวิร์กโฟลว์การทำงาน
* **🎙️ บทพูดของแทนคุณ**:
  > *"Activity แสดงทางเลือกของงาน เช่นข้อมูลผ่านหรือไม่ และเทรดครบหรือยัง ต่อไปสาธิต Happy Path ของสามงานนี้บนเว็บที่ Deploy ครับ"*
* **👆 ท่าทางและการชี้ (Action Cues)**:
  - ชี้จุด Decision (รูปข้าวหลามตัด) เงื่อนไขความถูกต้อง และทางแยกไปสู่จุดสิ้นสุดงาน (End Node)
  - พยักหน้าส่งสัญญาณให้ **สรวิชญ์** สลับหน้าจอเบราว์เซอร์ไปยังแท็บระบบที่ Deploy ไว้ เพื่อเริ่ม Demo 1

---

### ช่วงที่ 3: Live Demo 3 (6:40–7:30) — จับคู่และบันทึกเทรดครบ บนเว็บ `/orders`

* **เส้นทาง**: เข้าสู่หน้า `/orders` ➔ เลือกคำสั่งซื้อใหม่ที่เพิ่งถูกสร้างขึ้นจาก Demo 1 (ของธนภูมิ)
* **อุปกรณ์และผู้ควบคุม**: สรวิชญ์เป็นผู้คลิกเมาส์ตามคิว แทนคุณยืนข้างๆ บรรยายประสานจังหวะการกด

| เวลา | ผู้คุมจอ (สรวิชญ์) กดอะไร | แทนคุณพูดอะไร |
| :---: | :--- | :--- |
| **6:40–6:50** (10s) | กดปุ่ม **"ชำระเงินแล้ว"** และกดยืนยันในกล่องข้อความ | *“หลังร้านตรวจยอดโอน พนักงานบันทึกว่าชำระแล้ว ออเดอร์จะเป็น Paid ครับ”* |
| **6:50–7:05** (15s) | 1. กดปุ่ม **"จัดการเทรด"** (เปิด Modal)<br>2. กดปุ่ม **"จับคู่อัตโนมัติ"**<br>3. ชี้ไอดีเกมที่ถูกเลือกและ Friend Code<br>4. ปิด Modal ชั่วคราว<br>5. กด **"เริ่มเทรด"** และยืนยันที่แถว Order (สถานะกลายเป็น SHIPPING)<br>6. เปิด Modal จัดการเทรดกลับขึ้นมา | *“ระบบเลือกบัญชีที่มีการ์ดตรงสภาพและพร้อมเทรดครับ เมื่อเริ่มงาน ออเดอร์เป็น Shipping”* |
| **7:05–7:20** (15s) | เลื่อนสถานะของ OrderItem ตามลำดับ:<br>`FRIEND_PENDING` ➔ `TRADE_SENT` ➔ `COMPLETED` | *“การส่งจริงทำในเกมครับ Demo นี้จำลองว่าดำเนินการแล้ว และบันทึกจากส่งเทรดไปเป็นรับการ์ดครบตามลำดับ”* |
| **7:20–7:30** (10s) | ปิด Modal จัดการเทรด แล้วใช้เมาส์ชี้สถานะของ Order หลักที่เปลี่ยนเป็น **COMPLETED** | *“Item สุดท้ายครบแล้ว Order จึงปิดอัตโนมัติครับ เราไม่ต้องกดปิดออเดอร์ซ้ำ”* |

#### ⚠️ ข้อควรระวังขั้นวิกฤตสำหรับ Demo 3 (กฎเหล็ก):
1. **อย่ากดปุ่ม "เทรดสำเร็จ" ระดับ Order ซ้ำ**: เมื่อ Item ชิ้นสุดท้ายถูกปรับเป็น `COMPLETED` ตัวระบบ Auto-Sync ได้เรียก `context.complete()` ปิดออเดอร์ให้อัตโนมัติแล้ว ให้ชี้ที่หน้าจอว่าสถานะเปลี่ยนเป็น COMPLETED แล้วทันที หากเผลอกดซ้ำจะเกิด Error หรือดูไม่เป็นมืออาชีพ
2. **ใช้ออเดอร์ที่มีสินค้า 1 รายการ**: เพื่อความรวดเร็วและลดจำนวนคลิก ให้ใช้ออเดอร์ที่ธนภูมิจองไว้ใน Demo 1 ที่มีสินค้า 1 ชิ้น
3. **ผลลัพธ์ที่ต้องปรากฏชัดบนจอ**:
   - บัญชีเกมถูกจับคู่อัตโนมัติ (Assigned Account & Friend Code)
   - สถานะ Order กลายเป็น `SHIPPING`
   - สถานะ Item กลายเป็น `COMPLETED`
   - สถานะ Order หลักกลายเป็น `COMPLETED` โดยอัตโนมัติ

---

## 4. สิ่งที่แทนคุณต้องรู้จากประวัติ Commit ของตนเอง (Commits Deep-Dive)

แทนคุณพัฒนาโค้ดภายใต้ Branch `Tankun_6733803010_01` โดยมีผลงานตั้งแต่เริ่มต้น 15 Commits จนถึงการต่อยอดระบบขั้นสูงใน Commit ปัจจุบัน ดังนี้:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        แผนผัง Milestones ประวัติ Commit ของแทนคุณ                      │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ Milestone 1: Centralized Exception Architecture                                        │
│ • Commit 1 (2d9cc98): สร้าง Custom Domain Exceptions (ResourceNotFound, Insufficient)  │
│ • Commit 2 (aebf676): พัฒนา GlobalExceptionHandler ด้วย Spring AOP @RestControllerAdvice│
│ • Commit 3 (5060b86): กำหนดมาตรฐาน JSON ErrorResponse DTO ตาม RFC 7231                 │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ Milestone 2: GoF State Pattern Lifecycle Engine                                        │
│ • Commit 4 (daecdd9): นิยามอินเทอร์เฟซ OrderState พร้อม Default Methods Guard          │
│ • Commit 5 (9b8d72e): สร้าง OrderContext ห่อหุ้ม Order Entity และคุมการเปลี่ยน State  │
│ • Commit 6 (730e21a, bd8db0c): พัฒนา PendingOrderState และ PaidOrderState              │
│ • Commit 7 (067bac0): พัฒนา ShippingOrderState พร้อม Anti-Fraud Guard ห้าม Cancel      │
│ • Commit 8 (2831a5f): พัฒนา CompletedOrderState และ CancelledOrderState (Auto-Restore) │
│ • Commit 9 (b45a9c4): ผูก State Pattern เข้ากับ REST API PATCH /orders/{id}/status     │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ Milestone 3: In-Game Trade Matching System                                             │
│ • Commit 10 (35cd653): ออกแบบ TradeRecommendationResponse DTO สองระดับ                 │
│ • Commit 11 (6118057): สร้าง TradeMatchingService แนะนำไอดีเกมที่พร้อมเทรด             │
│ • Commit 12 (9fcac5f): อัลกอริทึม Auto-Match Greedy Stock-Maximization                 │
│ • Commit 13 (ebc8794): พัฒนา 5 REST Endpoints ใน TradeMatchingApiController           │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ Milestone 4: Comprehensive Unit Testing Suite                                          │
│ • Commit 14 (c6b6942): พัฒนาชุดทดสอบ OrderStateTest (State Transitions & Guards)       │
│ • Commit 15 (86dcc4d): พัฒนาชุดทดสอบ TradeMatchingServiceTest (Auto-Match Logic)       │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ Milestone 5 (Advanced Hardening & Integration):                                        │
│ • Commit 1a8baf6: สร้าง Trade Status Lifecycle API พร้อมระบบ Auto-Sync Order Completed │
│ • Commit cade2c2: ขยายชุดทดสอบเต็มรูปแบบ 7 Test Classes                                │
│ • Commit a34fae8: เสริม Double Completion Guard และเข้มงวด Sequence การเทรด           │
│ • Commit 3c72fee: บังคับใช้ Condition Matching (MINT vs PLAYED) และ Stock Reservation   │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. คลังความรู้เชิงลึก: Strategy และ SOLID Principles ในโค้ดของแทนคุณ (Master Knowledge Synthesis)

---

### 5.1 กลยุทธ์การเขียนโค้ดและ Strategy ทั้ง 6 รูปแบบที่แทนคุณใช้จริง (Coding Strategies & Strategy Patterns)

คำว่า **"Strategy"** ในงานที่แทนคุณรับผิดชอบ สามารถอธิบายได้ทั้งในมิติของ **GoF Design Patterns** และ **Algorithmic Selection Strategies (กลยุทธ์เชิงขั้นตอนวิธี)** ครบทั้ง 6 รูปแบบ ดังนี้:

#### 1. GoF State Pattern as a Dynamic Behavioral Strategy (ทฤษฎีคู่แฝด Strategy vs State)
* **ความสัมพันธ์ทางทฤษฎี**: ในตำรา GoF ระบุว่า *State pattern is a dynamic extension of the Strategy pattern* โดยทั้งสองแพทเทิร์นมีโครงสร้างคลาสเหมือนกันทุกประการ (Context ถือ Interface และ Delegate พฤติกรรมให้ Concrete Implementations)
* **กลยุทธ์การเขียน (Coding Strategy)**: 
  - แทนที่คำสั่งเดียวกัน เช่น `cancel()` จะต้องเขียน `switch-case` ซ้ำซ้อน
  - เราเปลี่ยนเป็น **Dynamic Behavioral Execution Strategy** โดยให้แต่ละสถานะกำหนดกลยุทธ์การตอบสนองเฉพาะของตัวเอง:
    - ใน `PendingOrderState`: กลยุทธ์คือ **Cancel & Restore Stock**
    - ใน `PaidOrderState`: กลยุทธ์คือ **Cancel, Refund & Restore Stock**
    - ใน `ShippingOrderState`: กลยุทธ์คือ **Defensive Anti-Fraud Rejection** (โยน Exception ปฏิเสธทันที)
    - ใน `CompletedOrderState`: กลยุทธ์คือ **Terminal Rejection** (ห้ามแก้สถานะซ้ำ)

#### 2. Greedy Stock-Maximization Selection Strategy (กลยุทธ์การจับคู่ไอดีเกมอัตโนมัติ)
* **ตำแหน่งโค้ด**: [`TradeMatchingServiceImpl.java:97-115`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/service/TradeMatchingServiceImpl.java#L97-L115)
* **กลยุทธ์การเขียน**:
  ```java
  CardInventory bestInventory = inventories.stream()
          .filter(inv -> inv.getGameAccount() != null)
          .filter(inv -> inv.getGameAccount().getTradeStatus() == AccountTradeStatus.READY)
          .filter(inv -> targetCondition == null || inv.getCondition() == targetCondition)
          .filter(inv -> effectiveStock >= item.getQuantity())
          .max(Comparator.comparing(inv -> effectiveStock))
          .orElseThrow(() -> new InsufficientStockException(...));
  ```
* **เหตุผลของกลยุทธ์**: ใช้แนวคิดแบบ **Greedy Algorithm** คัดเลือกไอดีที่มีสถานะพร้อมเทรด (`READY`), สภาพการ์ดตรงเงื่อนไข (`targetCondition`), และถือสต็อกสูงสุด (`Max Quantity First`) เพื่อรวมศูนย์การตัดสต็อกไว้ที่ไอดีหลัก ช่วยป้องกันปัญหาสต็อกกระจายตัวและลดภาระพนักงานในการสลับเครื่องล็อกอิน

#### 3. Two-Tier Prioritization & Ranking Strategy (กลยุทธ์จัดลำดับสองระดับ)
* **ตำแหน่งโค้ด**: [`TradeMatchingServiceImpl.java:180-210`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/service/TradeMatchingServiceImpl.java#L180-L210)
* **กลยุทธ์การเขียน**: ใช้ Multi-Level Comparator ใน Java Stream:
  - **Tier 1 (Priority Strategy)**: บัญชีสถานะ `READY` ได้คะแนนความสำคัญสูงสุด นำหน้าสถานะ `BUSY` หรือ `COOLDOWN`
  - **Tier 2 (Volume Strategy)**: หากสถานะเท่ากัน ให้จัดเรียงตามจำนวนสต็อกคงเหลือจากมากไปน้อย
  - ส่งผลลัพธ์เป็น DTO สองระดับ: **Best Match Candidate (อันดับ 1)** และ **Alternative Candidates (ไอดีสำรอง)** เปิดทางให้เป็นระบบ **Human-in-the-loop** ที่พนักงานคลิกสลับไอดีเองได้ในกรณีฉุกเฉิน

#### 4. Fail-Fast & Defensive Programming Strategy (กลยุทธ์ปฏิเสธทันทีเมื่อผิดกฎ)
* **ตำแหน่งโค้ด**: [`OrderState.java:6-40`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/state/OrderState.java#L6-L40) และ [`ShippingOrderState.java:29-34`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/state/ShippingOrderState.java#L29-L34)
* **กลยุทธ์การเขียน**:
  - ใช้ Java 8 Default Methods โยน `InvalidOrderStateException` เป็นค่าเริ่มต้นทุก Action
  - คลาสลูกเขียน Override เฉพาะ Action ที่อนุญาต ทำให้ Action ใดที่ไม่อนุญาตจะถูก Fail-Fast โยนข้อผิดพลาดทันทีโดยไม่ต้องเขียน if-else ดัก
  - ในสถานะ `SHIPPING` มี Anti-Fraud Guard ปฏิเสธคำขอยกเลิกทันทีเพื่อป้องกัน Free-Card Loss

#### 5. Double-Layer Guard Strategy (กลยุทธ์ป้องกันแบบเจาะลึกสองชั้น / Defense-in-Depth)
* **ตำแหน่งโค้ด**: [`OrderServiceImpl.java:299-307`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/order/service/OrderServiceImpl.java#L299-L307) และ [`ShippingOrderState.java:18-24`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/state/ShippingOrderState.java#L18-L24)
* **กลยุทธ์การเขียน**:
  - ชั้นที่ 1 (Service Layer): ก่อนจะเรียก Auto-Sync ปิดออเดอร์ ตรวจสอบ `allItemsCompleted` ผ่าน Stream `allMatch(...)`
  - ชั้นที่ 2 (State Machine Invariant): แม้จะมีใครพยายามเรียกผ่าน API `PATCH /orders/{id}/status?action=complete` โดยตรง ตัวคลาส `ShippingOrderState.complete()` จะตรวจ `allMatch(...)` ซ้ำอีกครั้ง และโยน `TradeStateConflictException` (HTTP 409 Conflict) ทันทีหากเทรดไม่ครบ รับประกันความปลอดภัย 100%

#### 6. Decoupled Standalone Testing Strategy (กลยุทธ์การทดสอบแยกส่วนความเร็วสูง)
* **ตำแหน่งโค้ด**: [`src/test/java/com/pokevault/modules/trade/`](file:///e:/Coding/Pok-Vault_Commerce/src/test/java/com/pokevault/modules/trade/)
* **กลยุทธ์การเขียน**: ใช้ Pure Java ร่วมกับ Mockito `@Mock` / `@InjectMocks` ในฝั่ง Service/State และใช้ `MockMvcBuilders.standaloneSetup(controller)` ผูก `GlobalExceptionHandler` โดยตรง ไม่ใช้ `@SpringBootTest` ที่ต้องโหลด Spring Context ทั้งหมด ทำให้รันครบ **94 Test Cases ได้ในเวลาเพียง ~2.5 วินาที**

---

### 5.2 การวิเคราะห์ SOLID Principles ทั้ง 5 ข้อ (S, O, L, I, D) ในโค้ดของแทนคุณอย่างละเอียด

ตารางและรายละเอียดเชิงลึกของการประยุกต์ใช้ **SOLID Principles** ในส่วนงานของแทนคุณ ซึ่งอ้างอิงตรงกับเอกสารทางการของโครงการใน [`doc/solid-analysis.md`](file:///e:/Coding/Pok-Vault_Commerce/doc/solid-analysis.md):

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                            การวิเคราะห์ SOLID Principles ในโค้ดของสมาชิกคนที่ 4 (แทนคุณ พันธ์นิกุล)              │
├─────────────────────┬────────────────────────────────────────────┬───────────────────────────────────────────────┤
│ หลักการ (Principle) │ ตำแหน่งไฟล์และบรรทัดในโปรเจกต์             │ การประยุกต์ใช้จริงและพฤติกรรมในโค้ด (Behavior)│
├─────────────────────┼────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ S — Single          │ • GlobalExceptionHandler.java:24-118       │ • แยกหน้าที่ชัดเจน ไม่ปะปนกัน:                │
│   Responsibility    │ • TradeMatchingServiceImpl.java:28-242     │   - GlobalExceptionHandler: ดักจับ Exception  │
│   Principle (SRP)   │ • OrderContext.java:7-109                  │     และจัดรูปแบบ JSON ErrorResponse เท่านั้น   │
│                     │ • Concrete States (Pending, Shipping ฯลฯ)  │   - TradeMatchingServiceImpl: ดูแลอัลกอริทึม  │
│                     │                                            │     จับคู่ไอดีเทรดและการโอนย้ายสต็อกเท่านั้น │
│                     │                                            │   - OrderContext: จัดการ Context & State Sync │
│                     │                                            │   - แต่ละ Concrete State: ดูแลเฉพาะกฎสถานะนั้น │
├─────────────────────┼────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ O — Open/Closed     │ • OrderState.java:6-40                     │ • เปิดรับการขยาย แต่ปิดการแก้ไข:              │
│   Principle (OCP)   │ • Concrete Order States                    │   - หากต้องการเพิ่มสถานะใหม่ (เช่น REFUNDED   │
│                     │ • GlobalExceptionHandler.java:24-118       │     หรือ DISPUTED) สามารถสร้างคลาสใหม่ที่     │
│                     │                                            │     implement OrderState ได้ทันที โดยไม่ต้องแก้│
│                     │                                            │     switch-case ใน Service/Controller เดิม    │
│                     │                                            │   - GlobalExceptionHandler เพิ่ม @ExceptionHandler│
│                     │                                            │     ชนิดใหม่ได้โดยไม่ต้องแก้ Controller       │
├─────────────────────┼────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ L — Liskov          │ • OrderState.java:6-40                     │ • คลาสลูกทดแทนคลาสแม่/Interface ได้สมบูรณ์:   │
│   Substitution      │ • PendingOrderState.java:5-24              │   - ทุก Concrete State (Pending, Paid,        │
│   Principle (LSP)   │ • PaidOrderState.java:5-24                 │     Shipping, Completed, Cancelled) สามารถ    │
│   *(หัวใจของ State)*│ • ShippingOrderState.java:9-40             │     ถูกนำไปแทนที่ในตัวแปร OrderState ของ      │
│                     │ • CompletedOrderState.java:5-23            │     OrderContext ได้อย่างเสมอภาค 100%         │
│                     │ • CancelledOrderState.java:16-57           │   - ทุกคลาสปฏิบัติตามสัญญา ไม่โยน Exception มั่ว│
│                     │ • OrderContext.java:7-109                  │     และรักษา Invariant ของระบบไว้อย่างถูกต้อง │
├─────────────────────┼────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ I — Interface       │ • TradeMatchingService.java:7-23           │ • ไม่สร้าง Fat Interface ที่มีเมธอดบวมเกินไป: │
│   Segregation       │ • OrderState.java:6-40                     │   - OrderState ประกาศเฉพาะ 4 Lifecycle Actions│
│   Principle (ISP)   │                                            │     และ getStatus() ที่จำเป็นต่อการเปลี่ยนสถานะ│
│                     │                                            │   - TradeMatchingService ประกาศเฉพาะ 4 เมธอด │
│                     │                                            │     ที่เกี่ยวกับการจับคู่ไอดีเกม Client ไม่ถูก│
│                     │                                            │     บังคับให้พึ่งพาเมธอดจัดการแคตตาล็อก/ส่วนลด│
├─────────────────────┼────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ D — Dependency      │ • TradeMatchingApiController.java:21       │ • พึ่งพา Abstraction และใช้ Constructor Injection:│
│   Inversion         │ • TradeMatchingServiceImpl.java:34-37      │   - Controller พึ่งพา TradeMatchingService    │
│   Principle (DIP)   │ • OrderContext.java:15-18                  │     (Interface) ไม่พึ่งพา Implementation      │
│                     │                                            │   - Service พึ่งพา Repository Interfaces      │
│                     │                                            │   - ไร้ Field Injection (@Autowired) 100%    │
│                     │                                            │   - ใช้ Lombok @RequiredArgsConstructor เท่านั้น│
└─────────────────────┴────────────────────────────────────────────┴───────────────────────────────────────────────┘
```

#### เจาะลึกโค้ดจริงแต่ละข้อของ SOLID ที่แทนคุณเขียน:

##### 1. Single Responsibility Principle (SRP)
* ใน [`GlobalExceptionHandler.java:24-118`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/advice/GlobalExceptionHandler.java#L24-L118):
  ```java
  @Slf4j
  @RestControllerAdvice
  public class GlobalExceptionHandler {
      @ExceptionHandler({TradeStateConflictException.class, InvalidOrderStateException.class})
      public ResponseEntity<ErrorResponse> handleStateConflict(RuntimeException ex, HttpServletRequest request) {
          return buildResponse(HttpStatus.CONFLICT, "STATE_CONFLICT", ex.getMessage(), request.getRequestURI(), null);
      }
      ...
  }
  ```
  * **คำอธิบาย**: คลาสนี้รับผิดชอบเฉพาะการแปลง Exception เป็น JSON `ErrorResponse` ระดับระบบเท่านั้น ปลดภาระของ Controller ให้ไม่ต้องเขียน try-catch ซ้ำซ้อน

##### 2. Open/Closed Principle (OCP)
* ใน [`OrderState.java:6-40`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/state/OrderState.java#L6-L40):
  * หากวันข้างหน้ามีสถานะใหม่ เช่น `RefundedOrderState` เราเพียงเขียน:
    ```java
    public class RefundedOrderState implements OrderState {
        @Override
        public OrderStatus getStatus() { return OrderStatus.REFUNDED; }
    }
    ```
  * โดยไม่ต้องแก้ไขโค้ดเดิมใน `OrderState`, `PendingOrderState`, หรือแก้ `if-else` ใน `OrderServiceImpl` แม้แต่บรรทัดเดียว!

##### 3. Liskov Substitution Principle (LSP)
* ใน [`OrderContext.java:15-60`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/state/OrderContext.java#L15-L60):
  ```java
  public class OrderContext {
      private Order order;
      private OrderState currentState;

      public void pay() { currentState.pay(this); }
      public void ship() { currentState.ship(this); }
      public void complete() { currentState.complete(this); }
      public void cancel() { currentState.cancel(this); }
  }
  ```
  * **คำอธิบาย**: ไม่ว่าตัวแปร `currentState` จะถูกแทนที่ด้วย `PendingOrderState`, `PaidOrderState`, หรือ `ShippingOrderState` ตัว `OrderContext` สามารถเรียกใช้งานผ่านอินเทอร์เฟซ `OrderState` ได้อย่างถูกต้องตามสัญญา 100% โดยไม่มีการพังทลายของระบบ

##### 4. Interface Segregation Principle (ISP)
* ใน [`TradeMatchingService.java:7-23`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/service/TradeMatchingService.java#L7-L23):
  ```java
  public interface TradeMatchingService {
      TradeRecommendationResponse getRecommendations(Long orderId);
      TradeRecommendationResponse getRecommendationForItem(Long orderItemId);
      TradeRecommendationResponse autoMatchOrderItem(Long orderItemId);
      TradeRecommendationResponse assignAccountToOrderItem(Long orderItemId, Long accountId);
  }
  ```
  * **คำอธิบาย**: อินเทอร์เฟซมีขนาดกะทัดรัด แยกหน้าที่เฉพาะการจับคู่ไอดีเกม ไม่รวมเมธอดสร้างออเดอร์ หรือคำนวณส่วนลด ทำให้ Controller ที่เรียกใช้ไม่ต้องแบกรับเมธอดที่ไม่จำเป็น

##### 5. Dependency Inversion Principle (DIP)
* ใน [`TradeMatchingApiController.java:21`](file:///e:/Coding/Pok-Vault_Commerce/src/main/java/com/pokevault/modules/trade/controller/TradeMatchingApiController.java#L21):
  ```java
  @RestController
  @RequestMapping("/api/v1/trades")
  @RequiredArgsConstructor // Constructor Injection 100%
  public class TradeMatchingApiController {
      private final TradeMatchingService tradeMatchingService; // พึ่งพา Interface เท่านั้น
      ...
  }
  ```
  * **คำอธิบาย**: ไม่มี `@Autowired` บน private field แต่ใช้ Constructor Injection ผ่าน `@RequiredArgsConstructor` ทำให้เราสามารถ Inject Mockito Mock Service ใน Unit Test ได้อย่างง่ายดาย

---

### 5.3 กฎความปลอดภัยทางธุรกิจและ Invariants (Anti-Fraud, Auto-Restore, Completion Guard)

ระบบของแทนคุณมีกลไกป้องกันความผิดพลาดและทุจริต 3 ชั้น:
1. **Anti-Fraud Protection Guard (ใน `ShippingOrderState.cancel`)**:
   - บริบทเกม Pokémon Pocket: เมื่อร้านยื่นส่งการ์ดในเกมแล้ว จะไม่สามารถดึงการ์ดกลับได้ทันที
   - หากยอมให้ลูกค้ายกเลิกคำสั่งซื้อผ่าน API ในสถานะนี้ ลูกค้าจะได้ทั้งเงินคืนและยังกดยอมรับการ์ดในเกมฟรี (Free-Card Loss)
   - ระบบจึง Fail-Fast ปฏิเสธคำขอยกเลิกและโยน `InvalidOrderStateException` ทันที
2. **Automatic Stock Restoration (ใน `CancelledOrderState`)**:
   - เมื่อออเดอร์ถูกยกเลิกในสถานะที่อนุญาต (`PENDING` หรือ `PAID`) คอนสตรัคเตอร์จะคืนการ์ดกลับเข้า `CardInventory` อัตโนมัติ เพื่อไม่ให้เกิดปัญหาสต็อกจม (Phantom Stock Allocation)
3. **Double Completion Guard (ใน `OrderServiceImpl` และ `ShippingOrderState.complete`)**:
   - การปิดคำสั่งซื้อจะเกิดขึ้นได้ก็ต่อเมื่อสินค้าทุกรายการในออเดอร์มีสถานะการเทรดเป็น `COMPLETED` แล้วเท่านั้น
   - หากยังส่งไม่ครบแล้วพยายามสั่งปิด จะโยน `TradeStateConflictException` ตอบกลับเป็น HTTP 409 Conflict

---

### 5.4 อัลกอริทึม In-Game Trade Matching (Greedy Stock-Maximization & Condition Matching)

* **ปัญหาหน้างาน**: ร้านค้ามีไอดีเกมบอทจำนวนมากเปิดซองสะสมการ์ด การ์ดจึงกระจายตัวอยู่ในหลายไอดี และเกมจำกัดโควต้าการเทรดต่อวัน
* **ขั้นตอนวิธี Greedy Auto-Match**:
  1. กรองเฉพาะไอดีที่มีสถานะเป็น `AccountTradeStatus.READY` (ไม่ติด Cooldown และไม่ติดสถานะ BUSY)
  2. ตรวจสอบสภาพการ์ดตรงตามที่ลูกค้าสั่งอย่างเคร่งครัด (`inv.getCondition() == targetCondition` เช่น MINT กับ PLAYED แยกคลังกัน)
  3. คำนวณยอดสต็อกที่จองไว้เดิมกลับมารวมด้วยหากเป็นคลังเดิม (`effectiveStock`)
  4. คัดเลือกไอดีที่มีสต็อกคงเหลือมากที่สุด (`max(Comparator.comparing(CardInventory::getQuantity))`)
  5. หากสลับได้คลังใหม่ จะทำการคืนสต็อกคลังเดิมและตัดสต็อกคลังใหม่อย่างปลอดภัย (Atomic Reservation Transfer)
* **Two-Tier Recommendation Model**:
  - **Tier 1 (Best Match)**: ไอดีที่ดีที่สุดตามการคำนวณของอัลกอริทึม
  - **Tier 2 (Alternative Candidates)**: รายชื่อไอดีสำรอง ส่งกลับไปให้หน้าเว็บแสดงเป็น Dropdown เผื่อกรณีไอดีหลักเกิดเหตุฉุกเฉิน (เช่น แบตเตอรี่หมด หรือเน็ตหลุด)

---

### 5.5 วงจรสถานะการเทรด (Trade Status Lifecycle) และระบบ Auto-Sync

* **ลำดับสถานะรายไอเทม**: `UNASSIGNED` ➔ `FRIEND_PENDING` ➔ `TRADE_SENT` ➔ `COMPLETED`
* **ความเข้มงวดของระบบ**:
  - ปฏิเสธสถานะเป้าหมายที่เป็น `UNASSIGNED` หรือ `FRIEND_PENDING` ผ่าน API ปรับสถานะ (400 Bad Request)
  - ห้ามข้ามขั้น เช่น จาก `UNASSIGNED` ข้ามไป `TRADE_SENT` ไม่ได้ (ต้องผ่านการจับคู่เป็น `FRIEND_PENDING` ก่อน)
  - มี **Idempotency Check**: หากกดสถานะเดิมซ้ำ ระบบจะคืนค่าสถานะปัจจุบันโดยไม่ประมวลผลซ้ำ
* **กลไก Auto-Sync**:
  - เมื่อพนักงานส่งการ์ดในเกมครบและปรับไอเทมสุดท้ายเป็น `COMPLETED`
  - โค้ดจะตรวจสอบ `allItemsCompleted` หากครบทุกชิ้น จะสั่ง `OrderContext.fromOrder(order).complete()` ปรับ Order หลักเป็น `COMPLETED` อัตโนมัติทันที

---

### 5.6 มาตรฐาน REST API และการจัดการข้อยกเว้นด้วย AOP (ทำไมต้อง HTTP 409 Conflict)

* **การใช้ Spring AOP `@RestControllerAdvice`**:
  - รวมศูนย์การจัดการ Exception ไว้ที่ `GlobalExceptionHandler`
  - คอนโทรลเลอร์ไม่ต้องเขียนบล็อก `try-catch` ซ้ำซ้อน (สอดคล้องกับหลัก SRP และ Separation of Concerns)
  - ส่งกลับ JSON `ErrorResponse` โครงสร้างเดียวกันทั้งระบบ (`timestamp`, `status`, `error`, `message`, `path`)
* **ทำไมต้องใช้ HTTP 409 Conflict แทนที่จะเป็น 400 Bad Request?**:
  - อ้างอิงตามมาตรฐาน **RFC 7231 (HTTP/1.1 Specification)**:
    - **400 Bad Request**: ใช้สำหรับ Syntax Error เช่น ส่ง JSON ไม่ครบ, พารามิเตอร์ผิดประเภท (Type Mismatch)
    - **409 Conflict**: ใช้สำหรับกรณีที่คำขอของ Client มี Syntax ถูกต้องสมบูรณ์ แต่ **"ขัดแย้งกับสถานะปัจจุบันของทรัพยากรบนเซิร์ฟเวอร์"** (State Conflict)
    - เช่น พยายามกดยกเลิกขณะออเดอร์กำลังส่ง (`SHIPPING`), พยายามปิดออเดอร์ขณะที่ไอเทมยังเทรดไม่เสร็จ, หรือพยายามส่งการ์ดซ้ำในออเดอร์ที่ปิดไปแล้ว
    - การตอบกลับด้วย 409 Conflict จึงถูกต้องและสะท้อนความหมายทางวิศวกรรมซอฟต์แวร์ระดับสากลอย่างแท้จริง

---

### 5.7 ข้อเท็จจริงที่ต้องตอบให้ตรง (ห้ามผิดเด็ดขาด)

1. **ยอดการทดสอบ**:
   - **ยอดทดสอบรวมทั้งโปรเจกต์ 373 Tests**: เป็นรอบที่บันทึก ณ วันที่ 9 ตุลาคม 2026 ของ commit `82c449b` บนฐานข้อมูลทดสอบ H2 In-Memory (ไม่ใช่การยืนยันว่ารัน commit ใหม่บนเวที และไม่ได้ต่อกับ Neon จริงในรอบเทสนั้น)
   - **ยอดทดสอบเฉพาะตัวของแทนคุณ**: มี **7 Test Classes / 94 Test Cases** ผ่าน 100% (0 Failures, 0 Errors) รันเสร็จในเวลาเพียง ~2.5 วินาที
2. **การทำธุรกรรมจริง**:
   - การโอนเงินและการส่งการ์ดจริงกระทำ **"ภายนอกเว็บ"** (ในแอปธนาคารและในเกม Pokémon TCG Pocket)
   - เว็บทำหน้าที่เก็บการจอง คำนวณราคา จัดสรรคลังไอดี และบันทึกผลตามที่พนักงานยืนยัน
3. **Database Schema**:
   - ความสัมพันธ์ `users` กับ `user_profiles` เป็นแบบ **1 ต่อ 0..1** (ไม่จำเป็นต้องมีโปรไฟล์ทุกคน)
   - ในระบบนี้ **ไม่มีความสัมพันธ์ Many-to-Many (N:M)** ดังนั้นห้ามพูดอ้างคะแนนพิเศษในข้อนี้
4. **SQL vs JPA**:
   - SQL `ON DELETE CASCADE` กับ JPA `CascadeType.ALL` เป็นกลไกคนละระดับกัน อย่าอธิบายว่าเป็นตัวเดียวกัน
5. **CI/CD Pipeline**:
   - GitHub Actions ทำหน้าที่ Test, Build และ Push Docker Image ส่วนการ Deploy บน Render เป็นอีกขั้นตอนหนึ่ง ไม่ใช่อัตโนมัติแบบ Full CD

---

## 6. แนวทางการตอบคำถามช่วง Q&A (3 นาทีหลัง 9:00) และการเปิดหลักฐาน (หน้า 23–49)

---

### 6.1 กฎเหล็กการตอบคำถาม 20–30 วินาที
1. **เจ้าของโมดูลตอบก่อน**: หากอาจารย์ถามเรื่อง State Pattern, Strategy, SOLID, การเทรด, หรือ Exception Handling ให้แทนคุณเป็นผู้ตอบทันที อย่าให้เพื่อนคนอื่นตอบแทน
2. **โครงสร้างการตอบ 3 จังหวะ**:
   - **จังหวะที่ 1 (1 ประโยค)**: ตอบเหตุผลทางวิศวกรรมหรือปัญหาทางธุรกิจสั้นๆ
   - **จังหวะที่ 2 (ชี้ Diagram)**: บอกสรวิชญ์ให้เปิดหน้า Diagram ที่เกี่ยวข้อง (เช่น หน้า 13, 17, 30, 34) แล้วชี้จุดเชื่อมโยง
   - **จังหวะที่ 3 (ชี้โค้ด)**: ชี้ชื่อไฟล์ บรรทัด และ Annotation ที่เกี่ยวข้องในโค้ดจริง
3. **ไม่ท่องนิยามทฤษฎีลอยๆ**: ให้เชื่อมโยงทฤษฎีเข้ากับบริบทของระบบ PokéVault เสมอ

---

### 6.2 เก็ง 12 คำถามเชือดของอาจารย์พร้อมแนวทางตอบแบบคะแนนเต็ม (เน้น Strategy & SOLID)

#### ❓ คำถามที่ 1: "ในส่วนของคุณ มีการใช้ Strategy ในการเขียนแบบไหนบ้าง?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"มี 2 มิติหลักครับอาจารย์:  
  > 1. **GoF State Pattern as a Dynamic Behavioral Strategy**: ในตำรา GoF ระบุว่า State คือคู่แฝดของ Strategy โดยในโค้ดของผม แต่ละ Concrete State ทำหน้าที่เป็น Behavioral Strategy ที่เปลี่ยนขั้นตอนวิธีตามสถานะของ Order เช่น ใน Shipping มีกลยุทธ์ปฏิเสธการยกเลิก ในขณะที่ Pending มีกลยุทธ์ยกเลิกพร้อมคืนสต็อก  
  > 2. **Algorithmic Selection Strategies ใน Trade Matching**: ใน `TradeMatchingServiceImpl` ผมเขียนกลยุทธ์การคัดเลือกบัญชีแบบ **Greedy Stock-Maximization Strategy** (เลือกไอดี READY ที่สภาพตรงและสต็อกสูงสุด) และ **Two-Tier Prioritization Strategy** เพื่อจัดลำดับ Best Match และตัวเลือกสำรองครับ"*

#### ❓ คำถามที่ 2: "อธิบายการปฏิบัติตาม SOLID Principles ทั้ง 5 ข้อในโค้ดที่คุณเขียน พร้อมชี้ไฟล์และบรรทัด?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"ปฏิบัติตามครบทั้ง 5 ข้ออย่างเคร่งครัดครับอาจารย์:  
  > • **S (SRP)**: `GlobalExceptionHandler:24-118` ทำหน้าที่แปลง JSON Error เท่านั้น ไม่ปนกับ Business Logic และ `TradeMatchingServiceImpl` ดูแลเฉพาะการจับคู่เทรด  
  > • **O (OCP)**: `OrderState:6-40` เพิ่มสถานะใหม่ได้โดยสร้างคลาสใหม่ ไม่ต้องแก้ switch-case ใน Service เดิม  
  > • **L (LSP)**: Concrete States ทุกตัว (`Pending`, `Shipping` ฯลฯ) ทดแทนกันใน `OrderContext` ได้สมบูรณ์ตามสัญญาของ Interface  
  > • **I (ISP)**: `TradeMatchingService:7-23` แยกเป็น Interface เฉพาะทาง ไม่สร้าง Fat Interface  
  > • **D (DIP)**: ทุกคลาสใช้ **Constructor Injection ผ่าน `@RequiredArgsConstructor` 100%** ไร้ Field Injection ทำให้ส่ง Mock ใน Unit Test ได้ทันทีครับ"*

#### ❓ คำถามที่ 3: "ทำไมถึงเลือกใช้ GoF State Pattern แทนการเขียน if-else หรือ switch-case ใน Service?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"หากใช้ `switch-case` ใน Service จะเกิดปัญหาใหญ่ 3 ข้อครับอาจารย์:  
  > 1) เกิด Condition Duplication คือทุกเมธอดต้องเขียนเช็กสถานะซ้ำไปมา  
  > 2) ขัดต่อหลัก Open/Closed Principle หากมีสถานะใหม่เข้ามา เช่น REFUNDED เราต้องไล่แก้ switch-case ในทุกไฟล์  
  > 3) ขาดการรวมศูนย์พฤติกรรม (Lack of Encapsulation)  
  > การใช้ State Pattern ช่วยแยกพฤติกรรมและ Invariants ของแต่ละสถานะออกเป็นคลาสอิสระ มี Default Methods ดักจับการกระทำผิดกฎ และ `OrderContext` จะคุยผ่านอินเทอร์เฟซ ทำให้โค้ดไม่มี if-else ซ้ำซ้อน และเขียน Unit Test แยกสถานะได้ 100% ครับ"*

#### ❓ คำถามที่ 4: "ทำไมใน `ShippingOrderState` ถึงต้องห้ามกดยกเลิก (`cancel()`) เด็ดขาด? มีความเสี่ยงอะไร?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"นี่คือกฎความปลอดภัยทางธุรกิจหรือ **Anti-Fraud Guard** ครับอาจารย์ ในเกม Pokémon Pocket เมื่อออเดอร์เข้าสู่สถานะ `SHIPPING` หมายถึงร้านค้าได้ส่งข้อเสนอเทรดการ์ดเข้าไปในระบบเกมจริงแล้ว ซึ่งการ์ดจะถูกล็อคไว้ หากระบบอนุญาตให้ลูกค้ายกเลิกผ่านหน้าเว็บในจังหวะนี้ ลูกค้าจะได้ทั้งเงินคืนและยังกดยอมรับการ์ดฟรีในเกมได้ ทำให้ร้านสูญเสียสินทรัพย์ (Free-Card Loss) ระบบจึงต้องบล็อกด้วยการโยน `InvalidOrderStateException` ทันทีครับ"*

#### ❓ คำถามที่ 5: "ระบบคืนสต็อกการ์ดเข้าคลังอย่างไรเมื่อคำสั่งซื้อถูกยกเลิก (Stock Restoration)?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"ทำงานอัตโนมัติในคอนสตรัคเตอร์ของ `CancelledOrderState` ครับ เมื่อคำสั่งซื้อถูกยกเลิกในสถานะ `PENDING` หรือ `PAID` ตัว State Machine จะส่ง Context เข้ามา และเรียกเมธอด `restoreStock()` วนลูปคืนจำนวนการ์ดกลับเข้า `CardInventory` ทันที  
  > และการทำงานนี้เกิดขึ้นภายใต้ `@Transactional` ของ `OrderServiceImpl` ทำให้การปรับสถานะเป็นยกเลิกและการคืนสต็อกเป็น Atomic Transaction เดียวกัน หากล้มเหลวจะ Rollback ทันที ป้องกันปัญหาสต็อกหายครับ"*

#### ❓ คำถามที่ 6: "อธิบายอัลกอริทึม Auto-Match ทำไมต้องเลือกไอดีที่มีสต็อกสูงสุด (Greedy Stock-Maximization)?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"อัลกอริทึมใน `TradeMatchingServiceImpl` กรองคลังการ์ด 2 เงื่อนไขครับ: 1) บัญชีเกมต้องมีสถานะ `READY` คือไม่ติด Cooldown รายวัน 2) สภาพการ์ดต้องตรงกับที่สั่ง เช่น MINT หรือ PLAYED  
  > จากนั้นเราใช้ Stream คัดเลือกไอดีที่มีสต็อกการ์ดใบนั้นสูงสุดก่อนครับ **เหตุผลทางธุรกิจ** คือทางร้านต้องการตัดการ์ดออกจากไอดีที่มีการ์ดหนาแน่น เพื่อลดการกระจายตัวของคลัง และลดภาระของพนักงานในการสลับบัญชีล็อกอินในแต่ละวันครับ"*

#### ❓ คำถามที่ 7: "ทำไมต้องส่ง Two-Tier Recommendation (Best Match + Alternative Candidates) กลับไปที่หน้าเว็บ?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"เพราะเป็นการออกแบบระบบแบบ **Human-in-the-loop** ครับ ในการทำงานจริงเราไม่ควรพึ่งพา Automation 100% โดยไม่ให้มนุษย์ตัดสินใจ ระบบจึงส่ง Best Match เป็นค่าตั้งต้นเพื่อความรวดเร็ว แต่ส่ง Alternative Candidates ไปด้วย เพื่อให้หน้าเว็บแสดงเป็น Dropdown ให้พนักงานเลือกสลับไอดีได้เอง เผื่อกรณีที่อุปกรณ์ที่ถือไอดีหลักแบตเตอรี่หมดหรือมีปัญหาเฉพาะหน้าครับ"*

#### ❓ คำถามที่ 8: "ขั้นตอนการส่งมอบการ์ด (Trade Fulfillment) มีลำดับอย่างไร และป้องกันความผิดพลาดอย่างไร?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"มี 4 ลำดับอย่างเข้มงวดครับ: `UNASSIGNED` ➔ `FRIEND_PENDING` ➔ `TRADE_SENT` ➔ `COMPLETED`  
  > ใน `OrderServiceImpl` เราตรวจสอบ Invariant เคร่งครัด เช่น จะเป็น `TRADE_SENT` ได้ ออเดอร์ต้องเป็น `SHIPPING` และต้องมีไอดีเกมผูกอยู่แล้วเท่านั้น และไม่อนุญาตให้ข้ามขั้น เพื่อป้องกัน Human Error ที่พนักงานอาจเผลอกดส่งมอบทั้งที่ยังไม่ได้เพิ่มเพื่อนครับ"*

#### ❓ คำถามที่ 9: "ระบบรู้ได้อย่างไรว่าต้องปิดคำสั่งซื้อหลักเป็น COMPLETED โดยที่พนักงานไม่ต้องกดซ้ำ?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"เราใช้กลไก **Auto-Sync** ครับ เมื่อพนักงานปรับสถานะ Item เป็น `COMPLETED` โค้ดจะรัน `order.getItems().stream().allMatch(...)` ตรวจสอบว่าทุกไอเทมในออเดอร์นั้นเป็น `COMPLETED` ครบหรือยัง หากครบแล้ว ระบบจะเรียก `OrderContext.complete()` เปลี่ยนสถานะออเดอร์หลักเป็น `COMPLETED` ผ่าน State Pattern ทันที ช่วยขจัดข้อผิดพลาดที่พนักงานส่งการ์ดครบแต่ลืมมากดปิดออเดอร์หลักครับ"*

#### ❓ คำถามที่ 10: "ทำไมถึงเลือกใช้ HTTP Status 409 Conflict แทนที่จะใช้ 400 Bad Request?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"เพราะปฏิบัติตามมาตรฐาน **RFC 7231 (HTTP/1.1 Specification)** ครับ 400 Bad Request มีไว้สำหรับไวยากรณ์ผิดพลาด เช่น JSON ผิดรูปแบบ แต่ 409 Conflict ออกแบบมาสำหรับกรณีที่ไวยากรณ์ถูกต้อง แต่ **'ขัดแย้งกับสถานะปัจจุบันของทรัพยากรบนเซิร์ฟเวอร์'** เช่น การพยายามยกเลิกคำสั่งซื้อขณะส่งมอบ หรือการสั่งปิดออเดอร์ขณะที่ไอเทมยังเทรดไม่ครบ ซึ่งเป็น Business State Conflict การใช้ 409 จึงถูกต้องตรงตามมาตรฐานสากลที่สุดครับ"*

#### ❓ คำถามที่ 11: "ในโค้ดของคุณ มีการประยุกต์ใช้หลักการ Liskov Substitution (LSP) และ Dependency Inversion (DIP) อย่างไร?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"ชี้ได้ชัดเจนครับ:  
  > 1. **LSP ใน State Pattern**: คลาสสถานะทุกตัว (`PendingOrderState`, `ShippingOrderState` ฯลฯ) สามารถถูกนำไปแทนที่ในตัวแปร `OrderState` ของ `OrderContext` ได้อย่างสมบูรณ์ตามสัญญาของ Interface โดยไม่ทำลาย Invariant ของระบบ  
  > 2. **DIP ใน Controller และ Service**: ทุกคลาสสื่อสารกันผ่าน Interface และใช้ **Constructor Injection ผ่าน Lombok `@RequiredArgsConstructor` 100%** ไม่มีการใช้ Field Injection ทำให้เราสามารถส่ง Mockito Mock Object ใน Unit Test ได้ทันทีครับ"*

#### ❓ คำถามที่ 12: "ทำไมชุดทดสอบ 94 เคสของคุณถึงรันได้เร็วมากในเวลาเพียง 2.5 วินาที?"
* **แนวทางตอบ (20 คะแนนเต็ม)**:
  > *"เพราะเราออกแบบด้วยสถาปัตยกรรม **Decoupled Standalone Unit Testing** ครับ  
  > แทนที่เราจะใช้ `@SpringBootTest` หรือ `@WebMvcTest` ซึ่งต้องรอ Spring Boot สแกน Component ทั้งหมดและสร้าง DB จำลอง ซึ่งกินเวลา 5–10 วินาที  
  > ชุดทดสอบของกระผมเขียนเป็น Pure Java ร่วมกับ Mockito `@Mock` ในฝั่ง Service/State และใช้ `MockMvcBuilders.standaloneSetup()` ร่วมกับ `GlobalExceptionHandler` ในฝั่ง Controller ทำให้ทดสอบ Business Logic, Validation และ HTTP Mapping ได้ครบ 100% บน JVM Memory ตรงๆ จึงรัน 94 เคสผ่านได้ในเวลาเพียง 2.5 วินาทีครับ"*

---

### 6.3 คำสั่งรันเทสสด 94 เคสแสดงหน้าห้อง (Live Verification)

หากในวันนำเสนอ อาจารย์ขอดูผลการรันเทสเฉพาะส่วนของสมาชิกคนที่ 4 ให้เปิด PowerShell ในโปรเจกต์แล้วรันคำสั่งนี้:

```powershell
.\mvnw.cmd test "-Dtest=OrderStateTest,TradeMatchingServiceTest,TradeMatchingApiControllerTest,OrderApiControllerTest,GlobalExceptionHandlerTest,TradeRecommendationResponseTest,CustomExceptionTest"
```

* **ผลลัพธ์ที่จะปรากฏบนหน้าจอ**:
  ```text
  [INFO] Results:
  [INFO] 
  [INFO] Tests run: 94, Failures: 0, Errors: 0, Skipped: 0
  [INFO] 
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time:  ~2.5 s - 4.5 s
  ```
