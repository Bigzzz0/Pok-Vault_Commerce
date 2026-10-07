# สไลด์นำเสนอโครงงาน (Presentation Slide Deck Outline)
## โครงการ: Pokémon TCG Pocket Vault & Chat Commerce Trade Platform
**หลักสูตร**: CP353002 Principles of Software Design and Development (Spring Boot)  
**กลุ่ม**: PokéVault Commerce  
**ที่เก็บเอกสารนำเสนอ**: `doc/slide/presentation-outline.md`

---

## 👥 ตารางสมาชิกและบทบาทการนำเสนอ (Team Presenters)

| สไลด์ที่ | หัวข้อการนำเสนอ | ผู้รับผิดชอบนำเสนอ (Presenter) | เนื้อหาหลัก |
| :---: | :--- | :--- | :--- |
| **1–3** | บทนำ, ปัญหาทางธุรกิจ และ Tech Stack | นายศิฆรินทร์ อุปจันทร์ (คนที่ 1) | ที่มาของโปรเจกต์ Pokémon TCG Pocket, ข้อจำกัดการเทรดในเกม, สถาปัตยกรรม 4-Tier Layered Architecture |
| **4–6** | ฐานข้อมูล ER Diagram & ความสัมพันธ์ | นายศิฆรินทร์ อุปจันทร์ (คนที่ 1) | 8 ตาราง, One-to-One (`users` $\leftrightarrow$ `user_profiles`), One-to-Many (`cards`, `orders`), Normalization BCNF |
| **7–9** | Game Account Vault & GoF Observer Pattern | นายสัพพัญญู คำตุ้ม (คนที่ 2) | การจัดการไอดีบอทเปิดซอง, สต็อกการ์ด, และ GoF Observer Pattern (`OrderPlacedEvent` $\rightarrow$ `LowStockObserver` สต็อก $\le 2$) |
| **10–12** | Order Engine & GoF Strategy Pattern | นายธนภูมิ จันทรา (คนที่ 3) | ตรรกะการสั่งซื้อ Chat Commerce, การหักสต็อก, และ GoF Strategy Pattern (`DiscountStrategy`: Regular 0%, VIP 10%, Wholesale 15%) |
| **13–15** | In-Game Trade Matching & GoF State Pattern | นายแทนคุณ พันธ์นิกุล (คนที่ 4) | อัลกอริทึม Auto-Match ไอดีเทรด, GoF State Pattern (Pending $\rightarrow$ Paid $\rightarrow$ Shipping $\rightarrow$ Completed), Global Exception Handler |
| **16–18** | Frontend Web UI, 3D Holo Cards & DevOps | นายสรวิชญ์ ศาสนสุพินธุ์ (คนที่ 5) | Thymeleaf Web Views, 3D CSS Holographic Foil Shader, Chat Commerce Facebook Messenger Handshake, Docker CI/CD, Cloud URL |
| **19–20** | สรุปผลการทดสอบ & ถาม-ตอบ (Q&A) | ทุกคนในกลุ่ม | Unit Testing 47+ ข้อ (JUnit 5 + Mockito BUILD SUCCESS 100%), สรุปการประยุกต์ใช้ SOLID Principles |

---

## 📑 รายละเอียดโครงร่างสไลด์ทีละหน้า (Slide-by-Slide Content)

### Slide 1: หน้าปก (Title Slide)
* **หัวข้อ**: PokéVault Commerce
* **คำโปรย**: Pokémon TCG Pocket Vault & Chat Commerce Trade Platform
* **วิชา**: CP353002 Principles of Software Design and Development
* **สมาชิก**: นายศิฆรินทร์, นายสัพพัญญู, นายธนภูมิ, นายแทนคุณ, นายสรวิชญ์

### Slide 2: ที่มาและความสำคัญ (Problem Statement & Business Value)
* **ปัญหา**: เกม Pokémon TCG Pocket มีระบบสุ่มซองการ์ดและมีข้อจำกัดด้าน Stamina/โควต้าเทรดรายวัน ทำให้ผู้เล่นหาการ์ดที่ต้องการได้ยาก
* **ทางออก**: ร้านค้าจัดตั้ง "ไอดีเกมกระจายคลัง" (Game Accounts Vault) เพื่อเปิดซองสะสมการ์ดหายาก แล้วนำมาจำหน่ายผ่านระบบ Chat Commerce พร้อมบริการจับคู่ไอดีเทรดในเกมอัตโนมัติ

### Slide 3: สถาปัตยกรรมระบบ (Layered Architecture & Tech Stack)
* **Backend**: Spring Boot 3.4.x (Java 17), Spring Data JPA, Spring Security 6
* **Database**: PostgreSQL (Production) / H2 (Development & Testing)
* **Frontend**: Thymeleaf, Vanilla HTML5/CSS3/JavaScript, 3D CSS Holographic Foil Engine
* **CI/CD & DevOps**: GitHub Actions, Docker, Docker Compose, Cloud Platform (Render/Railway)

### Slide 4: การออกแบบฐานข้อมูล (Database Schema & Normalization)
* 8 ตารางหลัก: `users`, `user_profiles`, `card_expansions`, `cards`, `game_accounts`, `card_inventories`, `orders`, `order_items`
* ความสัมพันธ์บังคับ:
  * **One-to-One**: `users` $\leftrightarrow$ `user_profiles`
  * **One-to-Many**: `users` $\rightarrow$ `orders`, `orders` $\rightarrow$ `order_items`, `cards` $\rightarrow$ `card_inventories`
* ผ่านเกณฑ์ BCNF ไร้ Redundancy

### Slide 5: การประยุกต์ใช้หลักการ SOLID ครบ 5 ข้อ (SOLID Highlights)
* **S (SRP)**: แยก Service แต่ละโมดูล ไม่ปะปน Data Access หรือ Security
* **O (OCP)**: เพิ่มกลยุทธ์ส่วนลดใหม่ได้โดยไม่ต้องแก้ `DiscountService`
* **L (LSP)**: ทุกสถานะ `OrderState` ทดแทนกันได้โดยไม่ผิด Invariant
* **I (ISP)**: ไม่มี Fat Interface แยก `CardService`, `UserService`, `TradeMatchingService`
* **D (DIP)**: ใช้ Constructor Injection ผ่าน Lombok `@RequiredArgsConstructor` เท่านั้น

### Slide 6: GoF Behavioral Pattern 1: Strategy Pattern (ส่วนลดตามระดับสมาชิก)
* **Interface**: `DiscountStrategy`
* **Implementations**:
  * `RegularDiscountStrategy` (0%)
  * `VipDiscountStrategy` (10%)
  * `WholesaleDiscountStrategy` (15%)
* **Context**: `DiscountService` ทำการรวบรวมผ่าน Spring DI `List<DiscountStrategy>`

### Slide 7: GoF Behavioral Pattern 2: State Pattern (วงจรชีวิตคำสั่งซื้อ)
* **วงจรสถานะ**: `PENDING` $\rightarrow$ `PAID` $\rightarrow$ `SHIPPING` $\rightarrow$ `COMPLETED` / `CANCELLED`
* **จุดเด่นความปลอดภัย**: ห้ามกดยกเลิกในสถานะ `SHIPPING` เด็ดขาดเพื่อป้องกันการสูญเสียการ์ดฟรีในเกม
* **Side-Effect อัตโนมัติ**: การยกเลิกออเดอร์ในสถานะที่อนุญาต จะคืนสต็อกการ์ดเข้าคลังอัตโนมัติ

### Slide 8: GoF Behavioral Pattern 3: Observer Pattern (แจ้งเตือนสต็อกต่ำ)
* **Event**: `OrderPlacedEvent`
* **Publisher**: `ApplicationEventPublisher` (Spring Event Bus)
* **Listener**: `LowStockObserver` (@EventListener)
* ตรวจสอบว่าหากสต็อกการ์ดลดลงเหลือ $\le 2$ ใบ จะส่งสัญญาณแจ้งเตือนระดับ Warning ทันที

### Slide 9: In-Game Trade Matching Engine (อัลกอริทึมจับคู่ไอดีเทรด)
* ค้นหาไอดีเกมของร้านที่ถือการ์ดใบที่ลูกค้าสั่งซื้อ
* คัดกรองเฉพาะไอดีที่มีสถานะ `READY` และมีสต็อกการ์ดเหลือสูงสุด
* มอบหมาย `assigned_account_id` ให้กับ `OrderItem` พร้อมสถานะ `FRIEND_PENDING` $\rightarrow$ `TRADE_SENT` $\rightarrow$ `COMPLETED`

### Slide 10: Chat Commerce Handshake & 3D Exhibition
* เลือกระดับความหายากและธาตุในแกลเลอรีการ์ด 3D พร้อมเอฟเฟกต์โฮโลแกรม
* กรอก Friend ID 16 หลัก และ Trainer IGN
* กดสร้างออเดอร์แล้วระบบคัดลอกข้อความสรุปยอดเงินและรหัสออเดอร์ลง Clipboard อัตโนมัติ พร้อมส่งต่อไปยัง Facebook Messenger ทันที

### Slide 11: การทดสอบและความปลอดภัย (Testing & Security)
* สถิติชุดทดสอบ: Unit Tests ครอบคลุม **34/34 Tests (และ 47 Tests เมื่อรวมครบทุกกิ่ง)** รันผ่าน 100% BUILD SUCCESS
* ระบบยืนยันตัวตน: Spring Security 6 ฟอร์มล็อกอินแยกสิทธิ์ Customer และ Manager
* API Documentation: Swagger UI เข้าถึงได้จริงผ่าน `/swagger-ui.html`

### Slide 12: การส่งมอบและ Deploy (Production Cloud URL & GitHub)
* GitHub Public Repository: ประวัติ Commit สม่ำเสมอ ทุกคนมี $\ge 15$ commits
* Docker Multi-stage build + GitHub Actions Automated CI/CD
* Cloud Deployment URL ใช้งานได้จริง ณ วันนำเสนอ
