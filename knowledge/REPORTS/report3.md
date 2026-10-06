# Report 03: GameAccountRepository

**ผู้รับผิดชอบ:** สมาชิกคนที่ 2 — Game Account Vault & Inventory Manager  
**Roadmap item:** `feat: create GameAccountRepository with custom query methods`  
**สถานะ:** Implemented

## สิ่งที่ทำ

- สร้าง Spring Data JPA Repository interface `GameAccountRepository` สำหรับจัดการเอนทิตี `GameAccount`
- เพิ่ม Derived Query Methods สำหรับค้นหาและตรวจสอบความซ้ำซ้อนด้วย `accountCode` และ `friendId`
- เพิ่ม Query ค้นหาไอดีเกมตามสถานะการเทรด `tradeStatus` พร้อมเรียงลำดับจากเก่าไปใหม่ (`findByTradeStatusOrderByIdAsc`) เพื่อรองรับการหมุนเวียนคิวแบบ Round-robin
- เพิ่ม Query ค้นหาตาม Trainer In-Game Name แบบไม่สนใจตัวพิมพ์เล็กใหญ่ (`findByInGameNameContainingIgnoreCase`)
- เพิ่มฟังก์ชันนับจำนวนไอดีตามสถานะ (`countByTradeStatus`) สำหรับรายงานแดชบอร์ด
- เพิ่ม Custom JPQL Query `findAvailableAccountsByCardId` ทำการ Join ข้ามไปยัง `CardInventory` เพื่อค้นหาไอดีเกมที่ถือการ์ดใบที่ต้องการ มีสต็อกคงเหลือ และมีสถานะพร้อมเทรด (`READY`) เพื่อรองรับระบบ Trade Matching ของสมาชิกคนที่ 4

## ไฟล์ที่เกี่ยวข้อง

- `src/main/java/com/pokevault/repository/GameAccountRepository.java`
- `knowledge/knowledge03.md`

## ผลตรวจสอบ

- รัน `./mvnw test-compile` สำเร็จ: **BUILD SUCCESS**
- รัน `./mvnw test` สำเร็จ: **18 tests ผ่าน 100%, 0 failures, 0 errors**

## งานถัดไป

ทำข้อ 4: `feat: create CardInventoryRepository with card and account lookups` ตาม roadmap
