# Requirements Checklist — CP353002

ตรวจเทียบโค้ด commit `ea2dcd7` วันที่ 9 ตุลาคม 2026; รอบนี้แก้เอกสาร ไม่ได้รันทดสอบใหม่

รายการนี้เทียบข้อกำหนดที่ผู้ใช้ให้มา โดยแยกหลักฐานที่มีจากงานส่งมอบที่ยังขาด ไม่ใช้คำว่าเสร็จ 100% จากผล unit tests เพียงอย่างเดียว

| ข้อกำหนด | หลักฐาน / สถานะ |
|---|---|
| Spring Boot 3+, Java 17+, Maven | [pom.xml](../code/pom.xml), Maven Wrapper |
| SQL / JPA | [schema.sql](../code/src/main/resources/schema.sql), [data.sql](../code/src/main/resources/data.sql); local H2 / prod PostgreSQL |
| Layered / MVC / Repository / Service / DTO / DI | [Class](diagrams/class-diagram.md), [Component](diagrams/component-deployment-diagram.md), [Patterns](design-patterns.md) |
| SOLID พร้อมไฟล์และบรรทัด | [SOLID Analysis](solid-analysis.md); ตัวอย่างและข้อจำกัดตาม implementation |
| GoF 3 แบบในกลุ่ม Behavioral | Strategy, State, Observer ใน [Patterns](design-patterns.md) พร้อมปัญหาและ class diagrams |
| ≥6 ตาราง, 1:1, 1:N, FK/index/Cascade/Fetch | 8 ตารางใน [ER / Data Dictionary](diagrams/er-diagram.md) |
| CRUD ≥2 resources | CardApiController และ GameAccountApiController: POST/GET/PUT/DELETE; ดู [README](../README.md#rest-api) |
| Validation / status / error handler / pagination | DTO @Valid, GlobalExceptionHandler, Security ErrorResponse, GET cards/paged; ดู README |
| Swagger | /swagger-ui.html และ /v3/api-docs ตั้งค่าไว้; ต้องตรวจ URL deployment จริงก่อนส่ง |
| Use Case + descriptions | [Use Case](diagrams/use-case-diagram.md) |
| Domain / Class | [Domain](diagrams/domain-model.md), [Class](diagrams/class-diagram.md) |
| Sequence ≥3 / Activity | [Sequence 5 scenarios](diagrams/sequence-diagrams.md), [Activity](diagrams/activity-diagram.md) |
| Component / Deployment / State | [Component & Deployment](diagrams/component-deployment-diagram.md), [State](diagrams/state-diagram.md) |
| Tests / Test Report | clean verify หลังย้ายโครงสร้างบน source 82c449b ผ่าน 373 เคส, 0 failures/errors/skips; [รายงานรวม](../test/reports/project/TEST-REPORT.md) และรายสมาชิกสร้างจากการรันเดียวกัน ไม่ครอบคลุม UI/PostgreSQL จริง/Cloud |
| README สมาชิกพร้อม Section/Branch | เพิ่มครบแล้วตาม branch สมาชิกที่มี; แต่ละคนต้องยืนยัน section ของตนตรงทะเบียน |
| root code/, test/, doc/, img/ | ครบแล้ว: source/config ใน code/, tests/report ใน test/, เอกสารใน doc/, รูปใน img/; ดู README |
| Dockerfile / Compose / CI | มีไฟล์ครบ; build/push image ไม่ใช่หลักฐาน public deployment |
| Public Deployment URL | **ยังไม่มีหลักฐาน** ต้อง deploy และใส่ URL ที่เข้าได้จริงใน README |
| Slides ใน doc/slide | **เว้นการแก้ตามคำขอผู้ใช้**; ไม่รับรองความถูกต้อง/ความพร้อมของ slides ในงานนี้ |
| Branch/commit ≥15/บัญชีตนเอง/PR reviewer ≥1 | ต้องตรวจประวัติ GitHub, Contributors และ approvals ของแต่ละคน; เอกสารไม่สามารถทดแทนหลักฐานประวัติได้ |
| Final main ผ่าน PR | ต้องรวม version ส่งมอบเข้า main ผ่าน PR พร้อม reviewer หลัง code/tests/deploy พร้อม |

## ขั้นตอนก่อนส่ง

1. รวมการแก้โค้ดและให้ reviewer ในทีมตรวจ PR
2. รัน `code/mvnw.cmd -f code/pom.xml clean verify` บน commit ส่งมอบ แล้วสร้างรายงานใหม่จาก Surefire XML; ระบุ commit/date/environment
3. root folders จัดแล้ว; ตรวจ build/CI และลิงก์สไลด์ในงานรอบถัดไป
4. ตรวจสไลด์ในงานแยกจากรอบนี้
5. Deploy ด้วย PostgreSQL และตรวจเว็บ/Swagger/health ผ่าน URL สาธารณะ
6. ตรวจหลักฐานส่วนบุคคลและส่ง final main ผ่าน PR
