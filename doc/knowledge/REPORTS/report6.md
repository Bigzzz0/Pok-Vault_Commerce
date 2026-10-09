# Report 06: GameAccountService Interface

**ผู้รับผิดชอบ:** สมาชิกคนที่ 2 — Game Account Vault & Inventory Manager  
**Roadmap item:** `feat: implement GameAccountService interface`  
**สถานะ:** Implemented

## สิ่งที่ทำ

- กำหนด Service Interface `GameAccountService` สำหรับเป็นข้อตกลงในการให้บริการทางธุรกิจของโมดูล Vault ครอบคลุม:
  1. การสร้างและดึงข้อมูลไอดีเกม (`createAccount`, `getAllAccounts`, `getAccountById`)
  2. การบันทึกการเปิดซองการ์ด (+ Add Pull) บันทึกสต็อกเข้าไอดีเกม (`addPulledCard`)
  3. การดึงรายการการ์ดในไอดีเกม (`getAccountCards`)
  4. การอัปเดตสถานะความพร้อมในการเทรดของไอดี (`updateTradeStatus`)
  5. การประเมินมูลค่ารวมของคลังการ์ด (`calculateTotalVaultCostValue`, `calculateTotalVaultSellingValue`)
- สร้างโครงร่างเมธอดตั้งต้นใน `GameAccountServiceImpl` เพื่อให้ระบบคอมไพล์ผ่านและพร้อมรองรับการเขียน Business Logic เต็มรูปแบบใน Step 7

## ไฟล์ที่เกี่ยวข้อง

- `src/main/java/com/pokevault/modules/vault/service/GameAccountService.java`
- `src/main/java/com/pokevault/modules/vault/service/GameAccountServiceImpl.java`
- `knowledge/knowledge06.md`

## ผลตรวจสอบ

- รัน `./mvnw test-compile` สำเร็จ: **BUILD SUCCESS**
- รัน `./mvnw test` สำเร็จ: **31 tests ผ่าน 100%, 0 failures, 0 errors**

## งานถัดไป

ทำข้อ 7: `feat: implement GameAccountServiceImpl with pack pull logging` ตาม roadmap
