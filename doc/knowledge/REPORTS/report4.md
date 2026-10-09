# Report 04: CardInventoryRepository

**ผู้รับผิดชอบ:** สมาชิกคนที่ 2 — Game Account Vault & Inventory Manager  
**Roadmap item:** `feat: create CardInventoryRepository with card and account lookups`  
**สถานะ:** Implemented

## สิ่งที่ทำ

- สร้าง Spring Data JPA Repository interface `CardInventoryRepository` สำหรับจัดการเอนทิตี `CardInventory`
- เพิ่ม Derived Query Methods สำหรับค้นหาสต็อกการ์ดตาม `cardId` และตาม `gameAccountId`
- เพิ่ม Query ค้นหาสต็อกแบบเฉพาะเจาะจงด้วย `cardId` + `gameAccountId` + `condition` เพื่อรองรับฟังก์ชันบันทึกการเปิดซองการ์ด (`+ Add Pull`)
- เพิ่ม Custom Aggregate JPQL Query `sumQuantityByCardId` สำหรับคำนวณผลรวมสต็อกการ์ดแต่ละใบจากทุกไอดีเกมของร้าน เพื่อรองรับระบบแจ้งเตือนสต็อกต่ำ (`LowStockObserver`)
- เพิ่ม Custom Valuation JPQL Queries: `calculateTotalVaultCostValue` และ `calculateTotalVaultSellingValue` สำหรับคำนวณมูลค่าราคาทุนและราคาขายรวมของคลังการ์ดทั้งร้าน

## ไฟล์ที่เกี่ยวข้อง

- `src/main/java/com/pokevault/repository/CardInventoryRepository.java`
- `knowledge/knowledge04.md`

## ผลตรวจสอบ

- รัน `./mvnw test-compile` สำเร็จ: **BUILD SUCCESS**
- รัน `./mvnw test` สำเร็จ: **18 tests ผ่าน 100%, 0 failures, 0 errors**

## งานถัดไป

ทำข้อ 5: `feat: create DTOs for GameAccount and AddPulledCard requests` ตาม roadmap
