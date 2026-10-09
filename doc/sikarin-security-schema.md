# งานแก้ Security และ Core Schema — ศิฆรินทร์

## สิทธิ์ API

| API | Anonymous | CUSTOMER | STAFF / ADMIN |
|---|---|---|---|
| GET Card Catalog / Expansions | อนุญาต | อนุญาต | อนุญาต |
| POST สมัครสมาชิก | อนุญาต | อนุญาต | อนุญาต |
| POST / PUT / DELETE Card Catalog | 401 | 403 | อนุญาต |
| Game Accounts / Trade Matching / Store Admin | 401 | 403 | อนุญาต |
| GET ออเดอร์ทั้งหมด / PATCH สถานะ | 401 | 403 | อนุญาต |
| POST สร้างออเดอร์ | 401 | เฉพาะ userId ของตน | จองแทนลูกค้าได้ |
| GET ออเดอร์รายตัว | 401 | เฉพาะออเดอร์ของตน | ดูได้ทุกออเดอร์ |

- `SecurityConfig` ตรวจบทบาทที่ filter chain; API ตอบ JSON 401/403 ส่วนหน้าเว็บยัง redirect ไป login
- `OrderAccessPolicy` เป็น Service ตรวจ username ของผู้ที่ล็อกอินกับ UserRepository ไม่เชื่อ userId ใน payload โดยลำพัง
- `OrderApiController` ใช้ `@PreAuthorize` ก่อนสร้างและ `@PostAuthorize` ก่อนส่ง response ออเดอร์กลับ
- ไม่มีการอ่าน Repository โดยตรงใน OrderApiController สำหรับการตรวจสิทธิ์
- Swagger/OpenAPI, health check, static assets และ form login ยังใช้ได้
- ใช้ session login เดิม; REST API ยังยกเว้น CSRF ตาม configuration เดิม ส่วน form login/logout ใช้ CSRF

## Core Schema ที่ปรับให้ตรงกัน

| คอลัมน์ | ข้อกำหนด |
|---|---|
| user_profiles.full_name | VARCHAR(100), nullable |
| user_profiles.phone_number | VARCHAR(20), nullable |
| user_profiles.shipping_address | VARCHAR(500), nullable; validation ไม่เกิน 500 |
| user_profiles.membership_tier | VARCHAR(30), NOT NULL, default REGULAR |
| user_profiles.reward_points | INTEGER, NOT NULL, default 0, ไม่ติดลบ |
| card_expansions.series | VARCHAR(50), NOT NULL |
| card_expansions.total_cards | INTEGER, NOT NULL; SQL CHECK มากกว่า 0 |
| cards.card_number | VARCHAR(20), NOT NULL; CardRequest จำกัด 20 |
| cards.element_type | VARCHAR(30), nullable สำหรับ Trainer/Item |

โปรไฟล์ที่ UserService สร้างให้เมื่อเพิ่มที่อยู่จึงไม่จำเป็นต้องมี full_name และไม่ขัดกับ SQL

## ฐานข้อมูลเดิม

`schema.sql` ใช้ CREATE TABLE IF NOT EXISTS จึงไม่แก้คอลัมน์ในฐานข้อมูลที่มีอยู่แล้ว

สำหรับ PostgreSQL เดิมมี script manual ที่ `code/src/main/resources/db/manual/core-schema-alignment.sql`:

```bash
psql -v ON_ERROR_STOP=1 -d YOUR_DATABASE -f code/src/main/resources/db/manual/core-schema-alignment.sql
```

ให้สำรองข้อมูลและตรวจ script ก่อนรัน โดย script ใช้ transaction และปฏิเสธข้อมูลเดิมที่ไม่ตรงข้อกำหนด ไม่ตัด/ลบข้อมูลให้เอง ไม่ได้รันกับฐานข้อมูลผู้ใช้ในงานรอบนี้

## การทดสอบ

- `ApiAuthorizationTest`: Spring Boot + MockMvc ใช้ security filters และ method authorization จริง, mock business services, รวม form login ผ่าน BCrypt
- `OrderBookingApiSecurityTest` ของคนที่ 3: ปรับหลังรวม develop ให้ Guest ได้ 401 และ STAFF จองแทนได้ตามสิทธิ์ใหม่
- `OrderAccessPolicyTest`: identity และ anonymous/unknown role guards ด้วย Mockito
- `CatalogSchemaSqlTest`: โหลด schema.sql/data.sql จริงบน H2 PostgreSQL mode แล้วให้ Hibernate validate; ตรวจ metadata, seed, null profile name, address boundaries และ card number boundaries
- `CatalogPersistenceTest`: mapping และ repository บน schema ที่ Hibernate สร้าง
- H2 PostgreSQL mode ไม่ใช่ PostgreSQL จริง; Docker engine ในเครื่องไม่พร้อม จึงยังไม่ได้ยืนยัน migration/DDL บน PostgreSQL จริง ต้องตรวจต่อใน CI/PostgreSQL

ผลล่าสุดและคำสั่งรันอยู่ใน [Test Report](../test/reports/sikarin/TEST-REPORT.md)

## ขอบเขตบันทึก

เอกสารนี้บันทึกงาน Security/Core schema ของสมาชิกคนที่ 1 ไม่ใช่รายการงานค้างของทีมล่าสุด ดู [Requirements Checklist](requirements-checklist.md) สำหรับสถานะส่งมอบ
