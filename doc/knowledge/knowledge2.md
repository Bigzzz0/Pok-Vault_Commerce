# Knowledge 02: CardInventory Entity และการควบคุมสต็อก

> **Roadmap item:** `feat: create CardInventory entity with price and stock checks`  
> **ผู้รับผิดชอบ:** นายสัพพัญญู คำตุ้ม (673380066-4) — สมาชิกคนที่ 2  
> **สถานะ:** Implemented

## 1. วัตถุประสงค์

`CardInventory` แทนจำนวนการ์ดแต่ละชนิดที่เก็บอยู่ในคลังของร้าน โดยบันทึกการ์ดที่อ้างอิง, ไอดีเกมที่ถือครอง, สภาพ, จำนวน, ราคาทุน, ราคาขาย และตำแหน่งจัดเก็บ ข้อมูลนี้เป็นฐานสำหรับขั้นถัดไป ได้แก่บันทึกการเปิดซองและให้ Order Engine หัก/คืนสต็อก

## 2. JPA Mapping

ไฟล์ `src/main/java/com/pokevault/domain/entity/CardInventory.java` แมปกับตาราง `card_inventories` และมีฟิลด์หลักดังนี้:

| ฟิลด์ | Mapping / ความหมาย |
| --- | --- |
| `id` | Primary key แบบ `IDENTITY` |
| `card` | `ManyToOne` ไปยัง `Card`; `card_id` ต้องมีค่า |
| `gameAccount` | `ManyToOne` ไปยัง `GameAccount`; คอลัมน์ `game_account_id` nullable ให้สอดคล้องกับ schema ปัจจุบันที่กำหนด `ON DELETE SET NULL` |
| `condition` | Enum เก็บเป็น String; ค่าเริ่มต้น `MINT` |
| `quantity` | จำนวนการ์ด ค่าเริ่มต้น 0 |
| `buyInPrice` | ราคาทุน `DECIMAL(10,2)` ค่าเริ่มต้น 0.00 |
| `sellingPrice` | ราคาขาย `DECIMAL(10,2)` ค่าเริ่มต้น 0.00 |
| `storageSlot` | ตำแหน่งจัดเก็บ ไม่บังคับ |

Entity สืบทอดจาก `BaseEntity` เพื่อใช้ audit timestamps ที่มีอยู่ในฐานโค้ด

## 3. CardCondition

ไฟล์ `src/main/java/com/pokevault/domain/enums/CardCondition.java` กำหนดค่าสภาพการ์ด:

- `MINT`
- `NEAR_MINT`
- `EXCELLENT`
- `PLAYED`

JPA จัดเก็บ enum ด้วยชื่อค่า (`EnumType.STRING`) จึงไม่ผูกข้อมูลในฐานข้อมูลกับลำดับ ordinal

## 4. Business Invariants

- `hasSufficientStock(requestedQuantity)` รับจำนวนที่มากกว่า 0 แล้วตรวจว่าจำนวนคงเหลือเพียงพอหรือไม่
- `deductStock(count)` ปฏิเสธจำนวน 0/ค่าติดลบ และโยน `InsufficientStockException` หากขอสต็อกเกินที่มี โดยไม่เปลี่ยนจำนวนเดิม
- `restoreStock(count)` รับเฉพาะจำนวนที่มากกว่า 0
- setter ของจำนวนและราคาปฏิเสธค่าติดลบ
- `@PrePersist` และ `@PreUpdate` ตรวจว่าการ์ดและ condition มีค่า รวมทั้งตรวจจำนวนและราคาอีกครั้งก่อน persist
- ราคาทุนและราคาขายใช้ `BigDecimal` เพื่อหลีกเลี่ยงความคลาดเคลื่อนของ floating point

`InsufficientStockException` ใน `src/main/java/com/pokevault/common/exception/InsufficientStockException.java` รองรับ constructor ที่รับข้อความ เพื่อส่งรายละเอียดจำนวนที่ขอและจำนวนที่เหลือ

## 5. Tests และการตรวจสอบ

เพิ่ม `src/test/java/com/pokevault/domain/entity/CardInventoryTest.java` เพื่อทดสอบการหักสต็อกสำเร็จ, สต็อกไม่พอ, จำนวนไม่ถูกต้อง, การคืนสต็อก และการปฏิเสธราคาติดลบ

ตรวจด้วย `./mvnw clean test`:

```text
CardInventoryTest: 5 tests, 0 failures, 0 errors
CardServiceTest:   13 tests, 0 failures, 0 errors
Total:             18 tests, 0 failures, 0 errors
BUILD SUCCESS
```

ตรวจ whitespace ด้วย `git diff --check` แล้วผ่าน

## 6. ขอบเขตที่ยังไม่ได้ทำ

งานนี้ยังไม่ได้สร้าง repositories, service, controller หรือ observer และยังไม่ได้เพิ่ม bidirectional collection ฝั่ง `GameAccount`; เป็นงานตาม roadmap ถัดไป ไม่ได้รวมอยู่ใน commit นี้
