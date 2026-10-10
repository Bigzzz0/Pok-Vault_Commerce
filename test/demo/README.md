# Demo สองมุม: ลูกค้าและคนขาย

ใช้ Robot Framework + Browser Library กด UI จริงใน Chromium สองหน้าต่างแบบเต็มจอ แยก cookies/session กัน ใช้คำสั่งซื้อเดียวตั้งแต่จองจนเสร็จ ไม่แก้โค้ดแอป และไม่ส่ง POST/PATCH ลัดผ่าน API กด F11 เพื่อออกจากเต็มจอได้

## เปิดใช้งาน (PowerShell จาก root repository)

```powershell
# ครั้งแรก: เตรียม Python environment, Node dependencies และ Chromium
powershell -ExecutionPolicy Bypass -File test/demo/run-demo.ps1 -Setup

# วันซ้อม / นำเสนอ: กดปุ่มเล็กในเบราว์เซอร์เมื่อพูดจบแต่ละจุด
powershell -ExecutionPolicy Bypass -File test/demo/run-demo.ps1

# ซ้อมแบบเดินตามเวลา ความเร็ว 1.5 เท่า ไม่อัดวิดีโอ
powershell -ExecutionPolicy Bypass -File test/demo/run-demo.ps1 -Mode timed -Speed 1.5

# ปรับความเร็วกลับปกติ หรือกำหนดความเร็วเอง (ค่าเริ่มต้น 1.5 เท่า)
powershell -ExecutionPolicy Bypass -File test/demo/run-demo.ps1 -Mode timed -Speed 1

# เลือกการ์ด หรือใช้เซิร์ฟเวอร์ local
powershell -ExecutionPolicy Bypass -File test/demo/run-demo.ps1 -CardName 'Charizard' -BaseUrl 'http://localhost:8081'

# ตรวจ syntax/keywords เท่านั้น ไม่เปิดเว็บ ไม่สร้างออเดอร์
powershell -ExecutionPolicy Bypass -File test/demo/run-demo.ps1 -DryRun
```

เครื่องนอก Codex ต้องมี Python 3.12+ และ Node.js รุ่นที่ Browser Library รองรับใน PATH ใช้ dependencies ที่ pin ไว้ใน requirements.txt ดู [Browser installation](https://robotframework-browser.org/docs/start/installation/)

## บัญชี

ค่าเริ่มต้น `customer_red` กับ `staff_ash` รหัสผ่านบัญชีสาธิต `password123` เปลี่ยน username ด้วย `-Customer` / `-Seller` เปลี่ยนรหัสผ่านด้วย environment variables ก่อนรัน:

```powershell
$env:POKEV_DEMO_CUSTOMER_PASSWORD = 'your-demo-password'
$env:POKEV_DEMO_SELLER_PASSWORD = 'your-demo-password'
```

รหัสผ่านกรอกด้วย Fill Secret ไม่บันทึกค่าในขั้นตอน keyword ไม่เก็บ session/cookies ลงไฟล์ ภาพและวิดีโอมีข้อมูลของบัญชีสาธิตที่ใช้อยู่

## เตรียมก่อนเริ่มจับเวลา

1. เปิด Deploy URL ให้เซิร์ฟเวอร์ตื่น ตรวจว่าเข้าระบบสองบัญชีได้
2. มีบัญชีเกม `READY` ที่มีการ์ดและราคาขาย และ offer ที่แกลเลอรีเลือกต้องมาจากบัญชี READY นั้นด้วย ระบบเลือก offer ที่ผ่านเงื่อนไขให้อัตโนมัติ ไม่เปลี่ยน availability หรือเพิ่มสต็อกเอง
3. ตั้ง membership ให้ลูกค้าตามที่ต้องการก่อนรัน ถ้าจะพูด VIP ให้ตั้ง VIP จริง Robot อ่าน tier ปัจจุบันและตรวจส่วนลดจากผล API หลังจอง
4. ให้ Robot login และเตรียมสอง session เสร็จก่อนสลับจากสไลด์ เริ่มจับเวลาที่ cue 01 ไม่รวมการ login/install/warm-up
5. ใช้ชื่อในเกม `Demo-<epoch>` เพื่อแยกออเดอร์ของรอบนี้ Friend ID เป็นตัวอย่าง 16 หลัก ไม่กดส่งข้อความ Messenger และไม่ทำธุรกรรมในเกม

## ลำดับและจังหวะ

- ลูกค้า: ดูแกลเลอรี → ค้นหา → ดูรายละเอียด → กรอก Friend ID → ยืนยันหนึ่งใบ → เห็นยอดสุทธิ → ประวัติ PENDING
- คนขาย: เห็นออเดอร์เดิม → บันทึกรับเงิน → auto-match READY → เริ่มเทรด → TRADE_SENT → item COMPLETED → ตรวจ order COMPLETED อัตโนมัติ
- ลูกค้า: refresh ประวัติ → เห็นออเดอร์เดิม COMPLETED

บทพูดอยู่ `doc/slide/demo-two-perspectives-script.md` โหมด manual มีปุ่ม “คลิกเพื่อไปต่อ” ชั่วคราวเฉพาะ browser ที่ Robot เปิด ไม่บันทึกเป็นส่วนหนึ่งของแอป ปุ่มหายเมื่อกด ใช้ผู้ควบคุม Demo คนเดียวกดตามผู้พูด

โหมด timed ความเร็วปกติมีเวลาหยุดรวม 156 วินาที ค่าเริ่มต้น Speed=1.5 ลดช่วงหยุดเป็น 104 วินาที บวกเวลาโหลดหน้า/กดจริง โดยไม่เร่งหรือข้ามการตรวจผลเครือข่าย โหมด manual ยังคงรอคนกด เป้าหมายไม่เกิน 210 วินาที แต่ต้องซ้อมจับเวลาบน Deploy จริงก่อน ไม่มีการรับประกันเวลาเมื่อเครือข่ายช้า โหมด fast ไม่มีจังหวะหยุด ใช้เมื่อซ้อมตรวจ flow โดยต้องรับรู้ว่ารันสร้างออเดอร์จริงเช่นกัน

## ผลและกรณีหยุดกลางทาง

ผลรอบอยู่ `test/demo/output/<timestamp>/` มี Robot log/report และ screenshots หากใช้ `-RecordVideo` มีวิดีโอแยก customer/seller ซึ่งเริ่มตั้งแต่เตรียม session ต้องตัดช่วงเตรียมออกก่อนใช้เป็นวิดีโอสำรอง สองไฟล์ยังไม่ได้รวมเป็นวิดีโอเดียว

ตรวจสิทธิ์ผู้ใช้จาก session, userId เจ้าของออเดอร์, inventory/จำนวนหนึ่งใบ, สต็อกลดหนึ่ง, ส่วนลดตาม tier, ยอดสุทธิ, PAID/SHIPPING, บัญชีถูกจับคู่, TRADE_SENT และ COMPLETED ทั้งสองมุม

ทุกการกดเปลี่ยนข้อมูลทำครั้งเดียว การ retry ใช้เฉพาะ GET ตรวจผล ถ้าจอง timeout ให้หยุดและดู `/my-orders` โดยหา `Demo-<epoch>` ก่อนเริ่มใหม่ เพราะอาจบันทึกสำเร็จแล้ว ห้ามกดยืนยันจองซ้ำโดยไม่ตรวจ เมื่อ item สุดท้ายครบจะไม่กดปุ่ม complete ของ Order ซ้ำ

การรันจริงใช้สต็อกหนึ่งใบ และทิ้งออเดอร์สาธิตเสร็จสิ้นไว้ ไม่ reset/ลบออเดอร์หรือคืนสต็อกอัตโนมัติ เตรียมสต็อกเพิ่มผ่าน UI ตามจำนวนรอบซ้อม หากล้มก่อนเทรด ให้คนขายตรวจสถานะแล้วตัดสินใจว่าจะทำต่อหรือยกเลิกตามกฎของแอป

การโอนเงินจริงและส่งการ์ดจริงเกิดภายนอกระบบ Demo นี้บันทึกผลจำลองผ่าน UI ต้องกล่าวให้ชัดในบทพูด

## ขอบเขตหลักฐาน

Robot Demo เป็นหลักฐาน flow UI เพิ่มเติม ไม่แทน JUnit 5, Mockito และ Spring Boot Test ที่อาจารย์กำหนด ผล dry-run ยืนยันได้เฉพาะรูปแบบ/การเรียก keyword ไม่ยืนยันว่า Deploy ผ่าน flow จริง
