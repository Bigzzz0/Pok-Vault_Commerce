# Report 02: CardInventory Entity

**ผู้รับผิดชอบ:** สมาชิกคนที่ 2 — Game Account Vault & Inventory Manager  
**Roadmap item:** `feat: create CardInventory entity with price and stock checks`  
**สถานะ:** Implemented

## สิ่งที่ทำ

- สร้าง JPA entity `CardInventory` สำหรับตาราง `card_inventories` โดยสืบทอดจาก `BaseEntity` พร้อมความสัมพันธ์กับ `Card` และ `GameAccount`
- เพิ่มข้อมูลสภาพการ์ด (`MINT`, `NEAR_MINT`, `EXCELLENT`, `PLAYED`), จำนวนคงเหลือ, ราคาทุน, ราคาขาย และช่องจัดเก็บ
- เพิ่มค่าเริ่มต้นเป็นสภาพ `MINT`, จำนวน 0 ใบ และราคา 0.00
- เพิ่มการตรวจสอบไม่ให้จำนวนหรือราคาติดลบ และไม่ให้ราคาว่างก่อนบันทึก/อัปเดต
- เพิ่ม `hasSufficientStock`, `deductStock` และ `restoreStock`; การหักเกินสต็อกจะโยน `InsufficientStockException`
- เพิ่ม constructor ครบถ้วนให้ `InsufficientStockException` รองรับการทำงานร่วมกับระบบอื่น
- เพิ่ม unit tests ครอบคลุมการหัก/คืนสต็อก จำนวนไม่ถูกต้อง สต็อกไม่พอ และราคาติดลบ

## ไฟล์ที่เกี่ยวข้อง

- `src/main/java/com/pokevault/domain/entity/CardInventory.java`
- `src/main/java/com/pokevault/domain/enums/CardCondition.java`
- `src/main/java/com/pokevault/common/exception/InsufficientStockException.java`
- `src/test/java/com/pokevault/domain/entity/CardInventoryTest.java`

## ผลตรวจสอบ

รัน `./mvnw clean test` สำเร็จ: **18 tests ผ่าน, 0 failures, 0 errors** (CardInventory 5 tests และ CardService 13 tests) และ `git diff --check` ผ่าน

## งานถัดไป

ทำ `GameAccountRepository` และ `CardInventoryRepository` พร้อม query methods ตาม roadmap
