# Domain Model / Conceptual Class Diagram
## โครงการ: Pokémon TCG Pocket Vault & Chat Commerce Trade Platform
**หลักสูตร**: CP353002 Principles of Software Design and Development (Spring Boot)  
**ระเบียบวิธี**: Domain-Driven Design (DDD) Tactical Modeling & Ubiquitous Language

---

## 1. Domain Model Overview (ภาพรวมแบบจำลองโดเมน)

Domain Model แสดงแนวคิดเชิงธุรกิจ (Business Concepts), เอนทิตีหลัก (Entities), อ็อบเจกต์ค่า (Value Objects), ความสัมพันธ์และจำนวนนับ (Multiplicities) รวมถึงกฎความคงสภาพทางธุรกิจ (Domain Invariants) ของแพลตฟอร์มบริหารคลังการ์ดและการซื้อขายผ่าน Chat Commerce

```mermaid
classDiagram
    direction TB

    %% ==========================================
    %% DOMAIN ENTITIES
    %% ==========================================
    class User {
        +Long id
        +String username
        +String email
        +String passwordHash
        +UserRole role
        +LocalDateTime createdAt
    }

    class UserProfile {
        +Long id
        +String fullName
        +String phoneNumber
        +String shippingAddress
        +MembershipTier membershipTier
        +Integer rewardPoints
    }

    class CardExpansion {
        +Long id
        +String code
        +String name
        +String series
        +LocalDate releaseDate
        +Integer totalCards
    }

    class Card {
        +Long id
        +String cardNumber
        +String name
        +CardType cardType
        +Rarity rarity
        +ElementType elementType
        +Integer hp
        +Integer retreatCost
        +String imageUrl
    }

    class GameAccount {
        +Long id
        +String accountCode
        +String inGameName
        +String friendId
        +AccountTradeStatus tradeStatus
        +BigDecimal buyInCost
        +String notes
        +getTotalCardsCount() int
    }

    class CardInventory {
        +Long id
        +CardCondition condition
        +Integer quantity
        +BigDecimal buyInPrice
        +BigDecimal sellingPrice
        +String storageSlot
        +hasSufficientStock(int) boolean
        +deductStock(int) void
        +restoreStock(int) void
    }

    class Order {
        +Long id
        +String orderCode
        +OrderStatus orderStatus
        +String customerFriendId
        +String customerInGameName
        +BigDecimal totalAmount
        +BigDecimal discountAmount
        +BigDecimal finalAmount
        +String notes
        +LocalDateTime createdAt
        +addItem(OrderItem) void
    }

    class OrderItem {
        +Long id
        +TradeFulfillmentStatus tradeStatus
        +Integer quantity
        +BigDecimal unitPrice
        +BigDecimal subtotal
    }

    class Deck {
        +Long id
        +String name
        +String description
        +getTotalCards() int
    }

    class DeckCard {
        +Long id
        +Integer quantity
    }

    %% ==========================================
    %% DOMAIN ENUMERATIONS (VALUE TYPES)
    %% ==========================================
    class MembershipTier {
        <<enumeration>>
        REGULAR
        VIP
        WHOLESALE
    }

    class OrderStatus {
        <<enumeration>>
        PENDING
        PAID
        SHIPPING
        COMPLETED
        CANCELLED
    }

    class AccountTradeStatus {
        <<enumeration>>
        READY
        COOLDOWN
        BUSY_TRADING
        SUSPENDED
    }

    class TradeFulfillmentStatus {
        <<enumeration>>
        UNASSIGNED
        FRIEND_PENDING
        TRADE_SENT
        COMPLETED
    }

    class CardCondition {
        <<enumeration>>
        MINT
        NEAR_MINT
        PLAYED
    }

    class Rarity {
        <<enumeration>>
        DIAMOND_1
        DIAMOND_2
        DIAMOND_3
        DIAMOND_4
        STAR_1
        STAR_2
        STAR_3
        CROWN_RARE
    }

    class ElementType {
        <<enumeration>>
        GRASS
        FIRE
        WATER
        LIGHTNING
        PSYCHIC
        FIGHTING
        DARKNESS
        METAL
        DRAGON
        COLORLESS
    }

    class UserRole {
        <<enumeration>>
        ADMIN
        STAFF
        CUSTOMER
    }

    %% ==========================================
    %% RELATIONSHIPS & MULTIPLICITIES
    %% ==========================================
    User "1" *-- "1" UserProfile : has profile
    User "1" o-- "0..*" Order : places
    User "1" o-- "0..*" Deck : creates

    CardExpansion "1" o-- "1..*" Card : publishes
    Card "1" <-- "0..*" CardInventory : defines template
    Card "1" <-- "0..*" DeckCard : references

    Deck "1" *-- "1..*" DeckCard : contains
    
    GameAccount "1" o-- "0..*" CardInventory : holds in vault
    GameAccount "1" <-- "0..*" OrderItem : assigned trader
    
    Order "1" *-- "1..*" OrderItem : consists of
    CardInventory "1" <-- "0..*" OrderItem : reserves stock
```

---

## 2. กฎความคงสภาพของแบบจำลองโดเมน (Domain Invariants & Business Rules)

ในสถาปัตยกรรมการออกแบบเชิงวัตถุ กฎธุรกิจ (Business Invariants) จะต้องได้รับการตรวจสอบและควบคุมอย่างเข้มงวดภายในโดเมนโมเดล:

### 2.1 โมเดลบัญชีเกมคลังการ์ด (Game Account Vault & Card Inventory)
* **Invariant 1 (Account-Inventory Association)**: การ์ดแต่ละใบในสต็อกที่เปิดได้จากซอง (`CardInventory`) จะต้องถูกผูกเข้ากับ `GameAccount` เสมอ เพื่อให้ระบบทราบอย่างชัดเจนว่าการ์ดใบนี้อยู่ในกระเป๋าของไอดีใดในเกม
* **Invariant 2 (Non-negative Stock Quantity)**: จำนวนการ์ดในสต็อกจะต้องเป็นศูนย์หรือมากกว่าเสมอ (`quantity >= 0`) หากมีการสั่งซื้อและตัดสต็อก ระบบจะป้องกันไม่ให้สต็อกติดลบ (`deductStock() throws InsufficientStockException`)
* **Invariant 3 (Cost & Price Integrity)**: ราคาซื้อเข้า (`buyInPrice`) และราคาขายหน้าร้าน (`sellingPrice`) ต้องไม่ติดลบ เพื่อให้รายงานกำไรขั้นต้นมีความถูกต้อง

### 2.2 โมเดลคำสั่งซื้อ Chat Commerce (Order & Chat Commerce Handshake)
* **Invariant 4 (Frozen Historical Price)**: เมื่อลูกค้าทำการจองการ์ด ราคาต่อหน่วย (`unitPrice`) ในแถวรายการ `OrderItem` จะต้องถูกบันทึกคัดลอกมาจากราคาขายในสต็อก ณ วินาทีนั้น และจะไม่มีการเปลี่ยนแปลงตามราคาคลังในอนาคต
* **Invariant 5 (Discount Strategy Invariant)**: ส่วนลดของคำสั่งซื้อจะต้องคำนวณผ่าน **Strategy Pattern** โดยพิจารณาจาก `MembershipTier` ของลูกค้า:
  * `REGULAR`: ส่วนลด 0%
  * `VIP`: ส่วนลด 10%
  * `WHOLESALE`: ส่วนลด 15%
* **Invariant 6 (Order Balance Calculation)**: ยอดเงินสุทธิจะต้องมีค่าตรงตามสมการเสมอ:
  $$\text{finalAmount} = \text{totalAmount} - \text{discountAmount}$$
* **Invariant 7 (In-Game Trade Identifiers)**: เมื่อสร้างออเดอร์ ลูกค้าต้องระบุ `customerFriendId` (รหัสเพื่อน 16 หลัก) เพื่อใช้เป็นพิกัดสำหรับให้ไอดีร้านส่งคำขอเป็นเพื่อนในเกม

### 2.3 โมเดลการจับคู่เทรดในเกม (In-Game Trade Matching)
* **Invariant 8 (Single Active Trader Assignment)**: แต่ละรายการการ์ด `OrderItem` จะต้องถูกมอบหมายให้กับ `GameAccount` เพียงไอดีเดียวเท่านั้นที่เป็นเจ้าของสต็อกการ์ดใบนั้น
* **Invariant 9 (Account Trade Readiness)**: ไอดีเกมที่จะได้รับคัดเลือกให้เป็นผู้ส่งการ์ดเทรด ต้องมีสถานะเป็น `READY` เท่านั้น (ไม่ติด `COOLDOWN`, ไม่ติด `BUSY_TRADING`, และไม่ติด `SUSPENDED`)

### 2.4 โมเดลการจัดเด็ค (Deck & DeckCard Constraints)
* **Invariant 10 (Card Copy Limit)**: ตามกฎสากลของเกม Pokémon TCG Pocket เด็คหนึ่งจะใส่การ์ดที่มีชื่อและเลขชุดเดียวกันได้ไม่เกิน **2 ใบ** (`quantity BETWEEN 1 AND 2`)

---

## 3. พจนานุกรมภาษาเฉพาะโดเมน (Ubiquitous Language Dictionary)

เพื่อให้การสื่อสารระหว่างทีมพัฒนา ซอฟต์แวร์ และผู้ใช้งานเป็นไปในทิศทางเดียวกัน จึงกำหนดศัพท์เฉพาะทางโดเมนไว้ดังนี้:

| คำศัพท์โดเมน (Term) | บริบททางธุรกิจ (Business Context) | ความหมายในระบบซอฟต์แวร์ (Software Meaning) |
| :--- | :--- | :--- |
| **Booster Pack Pull** | การเปิดซองการ์ดในเกมแล้วได้การ์ดหายาก | การสร้างเรคคอร์ด `CardInventory` ผูกเข้ากับ `GameAccount` ผ่านคำสั่ง `+ Add Pull` |
| **Game Account Vault** | ไอดีเกมของร้านที่ใช้เก็บสะสมการ์ด | เอนทิตี `GameAccount` ที่มีรหัสเพื่อนในเกม (Friend ID) และสถานะความพร้อมในการเทรด |
| **Chat Commerce Flow** | การกดจองบนเว็บแล้วเด้งไปคุยในแชท | โฟลว์การทำงานที่เว็บสร้าง Order Code จริง และเปิดปุ่ม Deep Link ส่งต่อไปยัง Facebook Messenger พร้อมข้อความสรุปออเดอร์ |
| **In-Game Trade Matching**| การจับคู่ไอดีเกมที่ถือการ์ดให้ออเดอร์ | กระบวนการใน `TradeMatchingService` ที่จับคู่ `assigned_account_id` ให้กับ `OrderItem` |
| **Trade Quota & Cooldown**| ข้อจำกัดการเทรดในเกมต่อวัน | การติดตามสถานะ `trade_status` ของบัญชีเกม เพื่อป้องกันการส่งคำขอเพื่อนหรือเทรดเกินขีดจำกัด |
| **Frozen Price** | การคงราคาขาย ณ วันที่สั่งซื้อ | การบันทึก `unit_price` ใน `order_items` ให้เป็นอิสระจากราคาใน `card_inventories` |
| **Holographic Shader** | เอฟเฟกต์โฮโลแกรม 3 มิติสะท้อนแสง | ระบบ CSS3 3D Transform & Shader Math บนหน้า Frontend ที่คำนวณการเอียงตามตำแหน่งเมาส์ |
