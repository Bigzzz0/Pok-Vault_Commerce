# ผลรัน Demo สองมุมบน Deploy

## รอบล่าสุด: เต็มจอ 1.5 เท่า ไม่อัดวิดีโอ

วันที่ 10 ตุลาคม 2026 เริ่มรันเวลา 18:52 (Asia/Bangkok), output `test/demo/output/20261010-185237/`

- **PASS** ครบ customer → seller → customer บน Deploy จริง
- Mode timed, Speed 1.5, fullscreen ทั้งสอง browser, RECORD_VIDEO=False
- Order `ORD-2026-004`, id `4`, Venusaur ex จำนวนหนึ่งใบ
- ราคาก่อนส่วนลด 180 บาท, VIP ลด 18 บาท, สุทธิ 162 บาท
- สต็อกลดจาก 2 เป็น 1 ผ่านการตรวจ
- ตรวจ PAID, SHIPPING, item TRADE_SENT/COMPLETED และ Order COMPLETED อัตโนมัติ
- ลูกค้าโหลด `/my-orders` แล้วพบออเดอร์เดียวกัน COMPLETED
- ไม่มีไฟล์วิดีโอในรอบนี้ เก็บ Robot report และ screenshots สำหรับหลักฐาน

---

## รอบก่อนหน้า: จังหวะปกติ

วันที่ 10 ตุลาคม 2026 เวลา 18:44–18:48 (Asia/Bangkok)

- URL: https://pok-vault-commerce.onrender.com
- ผล: **PASS**, 1 flow ผ่านครบ
- Customer: `customer_red`, Seller: `staff_ash` แยก browser/session เปิดเต็มจอ
- Order: `ORD-2026-003`, id `3`
- ชื่อเกมของรอบ: `Demo-1791632679`
- การ์ด: Venusaur ex, 004/226, MINT, inventory id 1, จำนวนหนึ่งใบ
- สต็อกก่อนจอง 3 หลังจอง 2 ผ่านการตรวจ
- ราคาก่อนส่วนลด 180 บาท, VIP 10% ลด 18 บาท, สุทธิ 162 บาท
- ตรวจ PENDING → PAID → SHIPPING → COMPLETED
- ตรวจ assignedAccountId ไม่ว่าง และ item FRIEND_PENDING → TRADE_SENT → COMPLETED
- Order ปิดอัตโนมัติเมื่อ item ครบ และหน้า `/my-orders` ของลูกค้าแสดง COMPLETED
- ทุกการเปลี่ยนข้อมูลกดผ่าน UI จริง API GET ใช้อ่านเตรียมข้อมูล/ตรวจผล
- การรับเงินและส่งการ์ดในเกมเป็นการจำลองบันทึกผล ไม่ได้โอนเงินจริงหรือส่งการ์ดจริงในเกม

Flow เต็มนับจากเริ่ม test 189.44 วินาที ช่วงแสดง Demo ไม่รวมต้นเตรียมประมาณ 186.3 วินาที อยู่ในช่วง 3.5 นาที การ login/warm-up/ติดตั้งไม่รวมเวลานี้

## ไฟล์หลักฐาน (local, ignored by Git)

รอบสำเร็จ: `test/demo/output/20261010-184438/`

- `output.xml`, `log.html`, `report.html`
- `browser/screenshot/customer-pending.png`
- `browser/screenshot/seller-assigned.png`
- `browser/screenshot/seller-completed.png`
- `browser/screenshot/customer-completed.png`
- `videos/customer/*.webm`, `videos/seller/*.webm`
- `demo-two-perspectives-1.5x.mp4`: รวมลูกค้า → คนขาย → ลูกค้า ตัดการเตรียมออก เร่ง 1.5 เท่า ไม่มีเสียงพากย์

## รอบเตรียมก่อนสำเร็จ

1. 18:40: Render แสดง Service waking up เกิน timeout เดิม 30 วินาที ไม่มี order ถูกสร้าง
2. 18:42: Login ได้ แต่ helper JavaScript คืนค่าว่างจาก comment ก่อน function และพบ END ผิดตำแหน่งใน teardown ไม่มี order ถูกสร้าง
3. แก้ script แล้วรอบ 18:44 ผ่านครบบน Deploy จริง มี screenshot ชื่อ fail-screenshot-1 ระหว่าง polling ตรวจสถานะก่อน server อัปเดต แต่ final flow PASS ไม่ใช่คำสั่งซื้ออีกหนึ่งรายการ

## การเปลี่ยนความเร็วหลังรัน

ผู้ใช้ขอเร็วขึ้น 1.5 เท่าระหว่างจบรอบนี้ ค่าเริ่มต้น `-Speed 1.5` ถูกใช้สำหรับการรันครั้งถัดไปในโหมด timed ช่วงหยุดรวมจะลดจาก 156 เป็น 104 วินาที รอบจริงที่รายงานนี้ยังเป็นจังหวะ 1 เท่า วิดีโอ MP4 ถูกเร่ง 1.5 เท่าแยกต่างหาก ไม่สร้างออเดอร์ซ้ำเพื่อทำวิดีโอ

ผลนี้ยืนยัน flow หนึ่งใบของสองบัญชีในรอบและข้อมูลนี้ ไม่แทน JUnit/Mockito/Spring Boot Test หรือยืนยันทุก edge case
