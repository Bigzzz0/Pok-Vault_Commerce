# Report 05: Vault DTOs & Validation

**ผู้รับผิดชอบ:** สมาชิกคนที่ 2 — Game Account Vault & Inventory Manager  
**Roadmap item:** `feat: create DTOs for GameAccount and AddPulledCard requests`  
**สถานะ:** Implemented

## สิ่งที่ทำ

- พัฒนา Data Transfer Objects (DTO) ครบทั้ง 4 คลาสในแพ็กเกจ `com.pokevault.modules.vault.dto`:
  1. `GameAccountRequest`: DTO รับข้อมูลลงทะเบียนไอดีเกม พร้อม Bean Validation (`@NotBlank`, `@Pattern` รหัสเพื่อน 16 หลัก, `@DecimalMin`)
  2. `GameAccountResponse`: DTO ส่งข้อมูลไอดีเกมกลับไปยัง Client พร้อม Static Factory Method `fromEntity`
  3. `AddPulledCardRequest`: DTO รับข้อมูลการเปิดซองการ์ด (+ Add Pull) บันทึกเข้าไอดีเกม พร้อม Validation ตรวจสอบสภาพการ์ดและราคา
  4. `AccountCardResponse`: DTO ส่งข้อมูลการ์ดที่เก็บอยู่ในไอดีเกมกลับไปยังหน้า UI พร้อม Null Safety Mapper
- วางรากฐานเพื่อเตรียมนำไปใช้ใน `GameAccountService` และ `GameAccountApiController` ในสเต็ปถัดไป

## ไฟล์ที่เกี่ยวข้อง

- `src/main/java/com/pokevault/modules/vault/dto/GameAccountRequest.java`
- `src/main/java/com/pokevault/modules/vault/dto/GameAccountResponse.java`
- `src/main/java/com/pokevault/modules/vault/dto/AddPulledCardRequest.java`
- `src/main/java/com/pokevault/modules/vault/dto/AccountCardResponse.java`
- `knowledge/knowledge05.md`

## ผลตรวจสอบ

- รัน `./mvnw test-compile` สำเร็จ: **BUILD SUCCESS**
- รัน `./mvnw test` สำเร็จ: **18 tests ผ่าน 100%, 0 failures, 0 errors**

## งานถัดไป

ทำข้อ 6: `feat: implement GameAccountService interface` ตาม roadmap
