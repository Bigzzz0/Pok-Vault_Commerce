# Tests and Reports

JUnit 5 / Mockito / Spring Boot Test sources อยู่ใน `test/java` โดยกำหนด testSourceDirectory ใน code/pom.xml
รายงานที่ commit ใน `test/reports/` เป็น snapshot ไม่ใช่ผลทดสอบที่รันใหม่ทุกครั้งที่อ่านเอกสาร

## ผลล่าสุด — 9 ตุลาคม 2026

`code/mvnw.cmd -f code/pom.xml clean verify` หลังย้ายโครงสร้าง บน source commit `82c449b`: **373 tests, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS**
ดู [รายงานรวมพร้อม commit/environment](reports/project/TEST-REPORT.md) และรายงานสมาชิกที่สร้างจาก XML ของการรันเดียวกัน
ส่วน Catalog / User / Security ของศิฆรินทร์ผ่าน **108 เคส**

## รันทั้งหมดและสร้างรายงาน

ใช้ JDK 17 ขึ้นไป จาก root ของโปรเจกต์:

```powershell
.\code\mvnw.cmd -f code/pom.xml clean verify
python test/generate_sikarin_report.py
python test/generate_sapphanyu_report.py
python test/generate_tanapoom_report.py
python test/generate_tankun_report.py
python test/generate_soravit_report.py
```

หากต้องการรายงานรวมและ metadata รอบเดียวกัน ให้เก็บ build log และใช้ generator รวมหลัง Maven จบ:

```powershell
.\code\mvnw.cmd -f code/pom.xml clean verify *> "$env:TEMP\pokevault-verify.log"
$taskVerifyExit = $LASTEXITCODE
python test/generate_project_report.py --build-log "$env:TEMP\pokevault-verify.log" --exit-code $taskVerifyExit
```

Generator รวมตรวจยอดใน log เทียบ XML และยอมรับเฉพาะ build สำเร็จที่ไม่มี failure/error/skipped; หาก build ไม่ผ่านให้อ่าน raw log ก่อน

ตรวจ build สำเร็จก่อนสร้างรายงาน; generator อ่าน Surefire XML ไม่ได้รันทดสอบเอง ควรใช้ clean เพื่อไม่รวม XML เก่าจากคลาสที่ถูกย้าย/ลบ
เพิ่ม commit SHA และสภาพแวดล้อมของรอบทดสอบก่อนส่ง ไม่ใช้ยอด tests ในรายงานเก่าเป็นการรับรองโค้ดใหม่

| สมาชิก | ขอบเขตหลัก | รายงาน |
|---|---|---|
| ศิฆรินทร์ | Catalog, User/Profile, persistence/schema, Security/ownership | [Sikarin](reports/sikarin/TEST-REPORT.md) |
| สัพพัญญู | Account/Inventory, Observer, ขอบเขต Order ที่รายงานระบุ | [Sapphanyu](reports/sapphanyu/TEST-REPORT.md) |
| ธนภูมิ | Order, Strategy, stock/ownership และ reassign | [Tanapoom](reports/tanapoom/TEST-REPORT.md) |
| แทนคุณ | State, Matching, controller/exception mapping | [Tankun](reports/tankun/TEST-REPORT.md) |
| สรวิชญ์ | หน้าเว็บ (controller/service/View DTO), สิทธิ์ตามบทบาทบนหน้าเว็บ, สมัครสมาชิก, งานหลังบ้าน, Swagger/Health | [Soravit](reports/soravit/TEST-REPORT.md) |

Scope บางส่วนทับซ้อนกัน จึงห้ามบวกยอดรายสมาชิกเป็นยอดทั้งระบบ ให้ใช้ยอด Surefire ของการรันทั้งหมดหนึ่งรอบ
รายงานของสรวิชญ์สร้างแยกด้วย `generate_soravit_report.py` และบันทึก commit/สภาพแวดล้อมของรอบที่รันไว้ในรายงานเอง จึงอาจเป็นคนละรอบกับรายงานรวม
Raw results อยู่ `code/target/surefire-reports`; XML ที่บันทึกในรายงานบาง scope ตัด machine properties/logs ออก
H2 PostgreSQL mode ไม่ใช่ PostgreSQL จริง และ unit tests ไม่ยืนยัน cloud deployment หรือ manual migration

รอบล่าสุดเป็นผลหลังย้ายบน `82c449b`; raw results อยู่ `code/target/surefire-reports` และรายงานรวม/รายสมาชิกสร้างจาก XML ของรอบเดียวกัน
