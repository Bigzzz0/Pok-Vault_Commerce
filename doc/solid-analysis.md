# SOLID Principles Analysis
## โครงการ: Pokémon TCG Pocket Vault & Chat Commerce Trade Platform
**หลักสูตร**: CP353002 Principles of Software Design and Development (Spring Boot)

---

## ตารางวิเคราะห์การประยุกต์ใช้หลักการ SOLID (SOLID Principles Matrix)

| หลักการ (Principle) | คำอธิบายแนวคิด (Concept & Definition) | ตัวอย่างคลาสในโปรเจกต์ (Applied Classes) | เหตุผลและประโยชน์ที่ได้รับ (Rationale & Benefits) |
| :--- | :--- | :--- | :--- |
| **S - Single Responsibility Principle (SRP)** | แต่ละคลาสควรมีหน้าที่ความรับผิดชอบเพียงเรื่องเดียว และมีเหตุผลในการเปลี่ยนแปลงเพียงเหตุผลเดียว | `CardService`<br>`DiscountService`<br>`TradeMatchingService`<br>`LowStockObserver` | แยกหน้าที่ชัดเจน เช่น `DiscountService` มีหน้าที่คำนวณส่วนลดเท่านั้น ไม่ยุ่งเกี่ยวกับการจัดการสต็อกหรือบันทึกคำสั่งซื้อ |
| **O - Open/Closed Principle (OCP)** | คลาสควรเปิดรับการต่อขยาย (Open for extension) แต่ปิดกั้นการแก้ไขโค้ดเดิม (Closed for modification) | `DiscountStrategy`<br>(`RegularDiscountStrategy`, `VipDiscountStrategy`, `WholesaleDiscountStrategy`) | เมื่อต้องการเพิ่มระดับส่วนลดใหม่ (เช่น FlashSaleDiscountStrategy) สามารถสร้างคลาสใหม่ที่ implement `DiscountStrategy` ได้ทันทีโดยไม่ต้องแก้โค้ดเดิม |
| **L - Liskov Substitution Principle (LSP)** | คลาสลูก (Subclass) หรือคลาสที่ Implement Interface ต้องสามารถใช้แทนคลาสแม่หรือ Interface ได้โดยไม่ทำให้โปรแกรมทำงานผิดพลาด | `OrderState`<br>(`PendingOrderState`, `PaidOrderState`, `ShippingOrderState`, `CompletedOrderState`, `CancelledOrderState`) | คลาส State ทุกตัวสามารถทำงานผ่าน Interface `OrderState` ใน `OrderContext` ได้อย่างสม่ำเสมอตามสัญญาของ State Pattern |
| **I - Interface Segregation Principle (ISP)** | Client ไม่ควรถูกบังคับให้พึ่งพา Interface หรือ Method ที่ตัวเองไม่ได้ใช้งาน ควรรวมกลุ่ม Interface ให้มีความเฉพาะเจาะจง | `CardService`<br>`GameAccountService`<br>`OrderService`<br>`TradeMatchingService` | แยก Interface ย่อยตามบริบทของโมดูล แทนที่จะรวมเป็น Monolithic Service ส่งผลให้ Controller และ Client เรียกใช้เฉพาะเมธอดที่ต้องการจริง |
| **D - Dependency Inversion Principle (DIP)** | โมดูลระดับสูง (High-level modules) ต้องไม่พึ่งพาโมดูลระดับต่ำ (Low-level modules) แต่ทั้งคู่ต้องพึ่งพา Abstraction (Interface) | Controller พึ่งพา Service Interface<br>Service พึ่งพา Repository Interface | ใช้ Spring Dependency Injection (`@Autowired`) ฉีด implementation เข้ามา ทำให้สามารถทำ Unit Test ด้วย Mockito ได้อย่างอิสระ |
