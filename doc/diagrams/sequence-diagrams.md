# Sequence Diagrams (4 Scenarios หลักแบบละเอียด)
## โครงการ: Pokémon TCG Pocket Vault & Chat Commerce Trade Platform
**หลักสูตร**: CP353002 Principles of Software Design and Development (Spring Boot)  
**รูปแบบแผนภาพ**: UML 2.5 Sequence Diagram พร้อม Activation Lifelines, Alt/Opt Guards, และ Data Payloads

---

## Scenario 1: การสั่งจองการ์ดบนเว็บและส่งต่อเข้า Chat Commerce
### (Customer Card Booking & Chat Commerce Handshake Workflow)

แสดงการทำงานตั้งแต่ลูกค้าเลือกการ์ด, กรอกรหัสเพื่อนในเกม, ระบบคำนวณส่วนลดตาม **Strategy Pattern**, หักสำรองสต็อกการ์ด, ส่งสัญญาณแจ้งเตือนตาม **Observer Pattern**, และแสดงปุ่มเด้งเข้าสู่ **Facebook Messenger** พร้อมข้อความสรุปออเดอร์อัตโนมัติ

```mermaid
sequenceDiagram
    autonumber
    actor Customer as ลูกค้า (Customer)
    participant UI as เว็บเบราว์เซอร์ (app.js / HTML)
    participant Ctrl as OrderApiController
    participant OSvc as OrderServiceImpl
    participant DSvc as DiscountService
    participant Strat as VipDiscountStrategy
    participant InvRepo as CardInventoryRepository
    participant ORepo as OrderRepository
    participant Bus as ApplicationEventPublisher
    participant Obs as LowStockObserver
    participant FB as Facebook Messenger App

    Customer->>UI: 1. เลือกการ์ด + ระบุ Friend ID และระดับสมาชิก (VIP)
    Customer->>UI: 2. คลิกปุ่ม "Confirm Booking & Proceed to Chat"
    activate UI
    UI->>Ctrl: 3. POST /api/v1/orders (PlaceOrderRequest JSON)
    activate Ctrl
    Ctrl->>OSvc: 4. createOrder(PlaceOrderRequest)
    activate OSvc

    OSvc->>InvRepo: 5. findById(inventoryId)
    activate InvRepo
    InvRepo-->>OSvc: 6. CardInventory Entity (quantity: 5, price: 990.00)
    deactivate InvRepo

    alt สต็อกไม่เพียงพอ (quantity < requestedQty)
        OSvc-->>Ctrl: throw InsufficientStockException("Out of stock")
        Ctrl-->>UI: HTTP 400 Bad Request
        UI-->>Customer: แจ้งเตือน: "ขออภัย การ์ดใบนี้ถูกจองหมดแล้ว"
    else สต็อกพร้อมจำหน่าย (quantity >= requestedQty)
        Note over OSvc,Strat: เรียกใช้ Strategy Pattern เพื่อคำนวณส่วนลดตาม Tier
        OSvc->>DSvc: 7. calculateDiscount(MembershipTier.VIP, 990.00)
        activate DSvc
        DSvc->>Strat: 8. calculate(990.00)
        activate Strat
        Strat-->>DSvc: 9. discountAmount = 99.00 (10%)
        deactivate Strat
        DSvc-->>OSvc: 10. return 99.00 (Final = 891.00)
        deactivate DSvc

        Note over OSvc,InvRepo: หักสำรองสต็อกในคลังทันที
        OSvc->>InvRepo: 11. inventory.deductStock(1)
        InvRepo-->>OSvc: 12. อัปเดตคงเหลือ = 4 ใบ

        Note over OSvc,ORepo: บันทึกคำสั่งซื้อสถานะ PENDING
        OSvc->>ORepo: 13. save(Order [Status=PENDING, finalAmount=891.00])
        activate ORepo
        ORepo-->>OSvc: 14. Saved Order Entity (#ORD-2026-001)
        deactivate ORepo

        Note over OSvc,Obs: ส่งสัญญาณตาม Observer Pattern
        OSvc->>Bus: 15. publishEvent(new OrderPlacedEvent(...))
        activate Bus
        Bus->>Obs: 16. @EventListener onOrderPlaced(event)
        activate Obs
        Obs->>Obs: 17. ตรวจสอบระดับสต็อกคงเหลือ (4 > 2: สต็อกปกติ)
        deactivate Obs
        deactivate Bus

        OSvc-->>Ctrl: 18. return OrderResponse DTO
        deactivate OSvc
        Ctrl-->>UI: 19. HTTP 201 Created (JSON Response)
        deactivate Ctrl

        UI->>UI: 20. เปิด Chat Commerce Modal (#ORD-2026-001)
        UI->>UI: 21. คัดลอกข้อความสรุปออเดอร์ลง Clipboard
        Customer->>UI: 22. คลิกปุ่ม "💬 ทักแชท Facebook เพื่อแจ้งโอน & นัดเทรด"
        UI->>FB: 23. Deep Link ไปยัง https://m.me/poketcgpocketstore
        activate FB
        FB-->>Customer: 24. หน้าต่างแชทเปิดขึ้น ลูกค้าวางข้อความและส่งสลิปโอนเงิน
        deactivate FB
    end
    deactivate UI
```

---

## Scenario 2: การเปิดซองได้การ์ดและบันทึกเข้าไอดีเกม
### (Booster Pack Pull & Game Account Sourcing Workflow)

แสดงขั้นตอนที่แอดมินร้านเปิดซองในเกมมือถือ Pokémon TCG Pocket แล้วนำการ์ดหายากที่เปิดได้มาบันทึกผูกความเป็นเจ้าของไว้กับไอดีเกมในระบบ เพื่อเป็นคลังสต็อกรอส่งเทรดให้ลูกค้า

```mermaid
sequenceDiagram
    autonumber
    actor Admin as แอดมินร้าน (Admin)
    participant UI as หน้าเว็บหลังบ้าน (/accounts)
    participant Ctrl as GameAccountApiController
    participant GASvc as GameAccountServiceImpl
    participant GARepo as GameAccountRepository
    participant CardRepo as CardRepository
    participant InvRepo as CardInventoryRepository

    Admin->>UI: 1. เข้าสู่หน้า Game Accounts Vault (/accounts)
    Admin->>UI: 2. คลิกปุ่ม "+ Add Pull" บนแถวไอดี ApexMaster_01
    activate UI
    UI-->>Admin: 3. แสดงหน้าต่างแบบฟอร์มบันทึกการเปิดซอง
    Admin->>UI: 4. เลือกการ์ด "Mewtwo ex", สภาพ MINT, จำนวน 1, ทุน 50.00, ราคา 750.00
    Admin->>UI: 5. คลิกปุ่ม "Record Pull & Add to Vault"

    UI->>Ctrl: 6. POST /api/v1/accounts/{id}/pulls (AddPulledCardRequest JSON)
    activate Ctrl
    Ctrl->>GASvc: 7. addPulledCard(accountId, request)
    activate GASvc

    GASvc->>GARepo: 8. findById(accountId)
    activate GARepo
    GARepo-->>GASvc: 9. GameAccount Entity (ApexMaster_01)
    deactivate GARepo

    GASvc->>CardRepo: 10. findById(cardId)
    activate CardRepo
    CardRepo-->>GASvc: 11. Card Entity (Mewtwo ex - Immersive Rare)
    deactivate CardRepo

    Note over GASvc,InvRepo: สร้าง CardInventory และผูก Foreign Key เข้ากับ GameAccount
    GASvc->>InvRepo: 12. save(CardInventory.builder().card(card).gameAccount(account).build())
    activate InvRepo
    InvRepo-->>GASvc: 13. CardInventory Saved (ID: 105)
    deactivate InvRepo

    GASvc-->>Ctrl: 14. return AccountCardResponse DTO
    deactivate GASvc
    Ctrl-->>UI: 15. HTTP 201 Created (JSON Response)
    deactivate Ctrl

    UI->>UI: 16. อัปเดตตัวเลข Stock Cards ของไอดีนั้นเพิ่มขึ้นทันที
    UI-->>Admin: 17. แสดง Alert แจ้งเตือน: "บันทึกการ์ดเข้าไอดีสำเร็จ พร้อมสำหรับการจับคู่เทรด"
    deactivate UI
```

---

## Scenario 3: การจับคู่ไอดีเกมอัตโนมัติและดำเนินวงจรการเทรดในเกม
### (In-Game Trade Auto-Matching & Multi-Account Assignment Workflow)

แสดงขั้นตอนที่แอดมินเปิดระบบหลังบ้านขึ้นมาดูออเดอร์ของลูกค้าที่ทักแชท Facebook มา แล้วใช้ระบบ **⚡ Auto-Match Best Account** เพื่อค้นหาไอดีเกมที่ถือการ์ดและมีความพร้อม ก่อนดำเนินขั้นตอนส่งคำขอเป็นเพื่อนและส่งการ์ดเทรดในเกม

```mermaid
sequenceDiagram
    autonumber
    actor Admin as แอดมินร้าน (Admin)
    participant UI as หน้าเว็บจัดการออเดอร์ (/orders)
    participant Ctrl as OrderApiController
    participant TMSvc as TradeMatchingServiceImpl
    participant GARepo as GameAccountRepository
    participant ORepo as OrderRepository
    participant State as OrderState (State Pattern)
    participant App as Pokémon TCG Pocket Client
    actor Customer as ลูกค้า (Customer)

    Admin->>UI: 1. เปิดหน้ารายการออเดอร์ (/orders) และดูรหัส #ORD-2026-001
    Admin->>UI: 2. คลิกปุ่ม "Trade Manager"
    activate UI
    UI->>Ctrl: 3. GET /api/v1/orders/{id}/trade-recommendations
    activate Ctrl
    Ctrl->>TMSvc: 4. getRecommendations(orderId)
    activate TMSvc

    TMSvc->>GARepo: 5. ค้นหา GameAccount ที่ถือการ์ดใบนี้ และสถานะ READY
    activate GARepo
    GARepo-->>TMSvc: 6. คืนรายชื่อ Candidate Accounts (ApexMaster_01: สต็อก 2 ใบ, พร้อมเทรด)
    deactivate GARepo

    TMSvc-->>Ctrl: 7. TradeRecommendationResponse DTO
    deactivate TMSvc
    Ctrl-->>UI: 8. HTTP 200 OK (แสดงรายชื่อไอดีที่ถือการ์ด)
    deactivate Ctrl

    Admin->>UI: 9. คลิกปุ่ม "⚡ Auto-Match Best Account"
    UI->>Ctrl: 10. POST /api/v1/orders/{id}/auto-match
    activate Ctrl
    Ctrl->>TMSvc: 11. autoMatchBestAccount(orderId)
    activate TMSvc
    TMSvc->>ORepo: 12. อัปเดต OrderItem (assigned_account_id = 1, trade_status = FRIEND_PENDING)
    ORepo-->>TMSvc: 13. บันทึกสำเร็จ
    TMSvc-->>Ctrl: 14. return Updated OrderResponse
    deactivate TMSvc
    Ctrl-->>UI: 15. HTTP 200 OK (แสดงข้อมูลไอดีผู้ส่งเทรด)
    deactivate Ctrl

    Note over Admin,State: ดำเนินการชำระเงินตาม State Pattern
    Admin->>UI: 16. แอดมินตรวจสลิปในแชทแล้วคลิกปุ่ม "Pay"
    UI->>Ctrl: 17. PATCH /api/v1/orders/{id}/status?action=pay
    activate Ctrl
    Ctrl->>State: 18. orderState.pay(context)
    State-->>Ctrl: 19. สถานะเปลี่ยนเป็น PAID
    Ctrl-->>UI: 20. HTTP 200 OK (สถานะ: PAID)
    deactivate Ctrl

    Note over Admin,Customer: กระบวนการส่งมอบในเกม Pokémon Pocket
    Admin->>App: 21. ล็อกอินเข้าเกมด้วยไอดี ApexMaster_01
    Admin->>App: 22. ส่งคำขอเพื่อนไปยัง Friend ID ของลูกค้า
    Customer->>App: 23. ลูกค้ากดยอมรับคำขอเป็นเพื่อนในเกม
    Admin->>UI: 24. แอดมินคลิกปุ่ม "Start Trade" (action=ship)
    UI->>Ctrl: 25. PATCH /api/v1/orders/{id}/status?action=ship
    Ctrl->>State: 26. orderState.ship(context) -> สถานะเปลี่ยนเป็น SHIPPING

    Admin->>App: 27. แอดมินส่งการ์ดเทรดในเกมให้ลูกค้า
    Customer->>App: 28. ลูกค้ารับการ์ดในเกมสำเร็จสมบูรณ์
    Admin->>UI: 29. แอดมินคลิกปุ่ม "Complete Trade" (action=complete)
    UI->>Ctrl: 30. PATCH /api/v1/orders/{id}/status?action=complete
    Ctrl->>State: 31. orderState.complete(context) -> สถานะเปลี่ยนเป็น COMPLETED
    UI-->>Admin: 32. ปิดคำสั่งซื้อสมบูรณ์ 100%
    deactivate UI
```

---

## Scenario 4: การยกเลิกคำสั่งซื้อและการคืนสต็อกการ์ดอัตโนมัติ
### (Order Cancellation & Inventory Stock Rollback via State Pattern)

แสดงขั้นตอนเมื่อลูกค้าเปลี่ยนใจขอยกเลิกออเดอร์ในแชท หรือไม่โอนเงินตามกำหนด ระบบจะสั่งคืนสต็อกการ์ดเข้าคลังอัตโนมัติผ่าน State Pattern และปฏิเสธการยกเลิกหากออเดอร์เข้าสู่ขั้นตอนส่งการ์ดในเกมแล้ว

```mermaid
sequenceDiagram
    autonumber
    actor Admin as แอดมิน / ลูกค้า (User)
    participant UI as หน้าเว็บจัดการออเดอร์ (/orders)
    participant Ctrl as OrderApiController
    participant OSvc as OrderServiceImpl
    participant State as OrderState (PendingOrderState / PaidOrderState)
    participant Inv as CardInventory Entity
    participant InvRepo as CardInventoryRepository
    participant ORepo as OrderRepository

    Admin->>UI: 1. คลิกปุ่ม "Cancel Order" ที่ออเดอร์ #ORD-2026-001
    activate UI
    UI->>Ctrl: 2. PATCH /api/v1/orders/{id}/status?action=cancel
    activate Ctrl
    Ctrl->>OSvc: 3. transitionOrderStatus(orderId, "cancel")
    activate OSvc

    OSvc->>ORepo: 4. findById(orderId)
    ORepo-->>OSvc: 5. Order Entity (สถานะปัจจุบัน: PENDING)

    alt สถานะปัจจุบันคือ PENDING หรือ PAID (ยกเลิกได้)
        OSvc->>State: 6. currentState.cancel(context)
        activate State
        
        Note over State,InvRepo: ทำการคืนสต็อกการ์ดเข้าคลังอัตโนมัติ (Rollback Stock)
        loop วนลูปตามรายการ OrderItem ในออเดอร์
            State->>Inv: 7. inventory.restoreStock(item.getQuantity())
            State->>InvRepo: 8. save(inventory)
            InvRepo-->>State: 9. บันทึกสต็อกการ์ดคืนเรียบร้อย (+1 ใบ)
        end

        State->>State: 10. order.setOrderStatus(OrderStatus.CANCELLED)
        State-->>OSvc: 11. ดำเนินการสำเร็จ
        deactivate State

        OSvc->>ORepo: 12. save(order)
        ORepo-->>OSvc: 13. บันทึกสถานะ CANCELLED
        OSvc-->>Ctrl: 14. return OrderResponse DTO
        Ctrl-->>UI: 15. HTTP 200 OK
        UI-->>Admin: 16. แสดงข้อความ: "ยกเลิกออเดอร์สำเร็จ และคืนสต็อกเข้าคลังเรียบร้อยแล้ว"

    else สถานะปัจจุบันคือ SHIPPING (ส่งการ์ดในเกมแล้ว - ยกเลิกไม่ได้!)
        OSvc->>State: 17. shippingState.cancel(context)
        activate State
        State-->>OSvc: 18. throw InvalidOrderStateException("Cannot cancel an order currently in-game trading")
        deactivate State
        OSvc-->>Ctrl: 19. ส่งต่อ Exception
        Ctrl-->>UI: 20. HTTP 400 Bad Request
        UI-->>Admin: 21. แจ้งเตือนสีแดง: "ไม่สามารถยกเลิกได้ เนื่องจากส่งการ์ดเข้าสู่ระบบเทรดในเกมแล้ว"
    end
    deactivate OSvc
    deactivate Ctrl
    deactivate UI
```

---

## Scenario 5: การเปลี่ยนไอดีส่งมอบพร้อมรักษาสภาพการ์ดและการถ่ายโอนสต็อก
### (Order Item Account Reassignment & Card Condition Guard Workflow)

แสดงขั้นตอนเมื่อเจ้าหน้าที่เปลี่ยนไอดีเกมผู้ส่งมอบสำหรับรายการสั่งซื้อ โดยระบบจะบังคับตรวจสอบสภาพการ์ด (เช่น MINT ต้องคงเป็น MINT), โอนย้ายการจองสต็อกระหว่างคลังเดิมและคลังใหม่อย่างแม่นยำ, คงราคาซื้อขายเดิม และ Rollback ทันทีหากล้มเหลว

```mermaid
sequenceDiagram
    autonumber
    actor Staff as เจ้าหน้าที่ / แอดมิน (Staff/Admin)
    participant UI as หน้าเว็บจัดการออเดอร์ (/orders)
    participant Ctrl as OrderApiController
    participant OSvc as OrderServiceImpl
    participant GARepo as GameAccountRepository
    participant InvRepo as CardInventoryRepository
    participant ORepo as OrderRepository

    Staff->>UI: 1. เลือกเปลี่ยนบัญชีส่งการ์ดเป็น VAULT-ACC-B สำหรับ OrderItem #501 (สภาพ MINT)
    activate UI
    UI->>Ctrl: 2. PATCH /api/v1/orders/{orderId}/items/{itemId}/assign?accountId=2
    activate Ctrl
    Ctrl->>OSvc: 3. reassignOrderItemAccount(orderId, itemId, accountId)
    activate OSvc

    OSvc->>GARepo: 4. findById(targetAccountId)
    GARepo-->>OSvc: 5. Target GameAccount (สถานะ READY)

    OSvc->>InvRepo: 6. findByCardId(cardId)
    InvRepo-->>OSvc: 7. รายการ Inventories ในระบบ

    Note over OSvc: กรอง Inventory ในไอดีใหม่ที่มี สภาพตรงกัน (MINT == MINT)
    alt ไอดีเป้าหมายไม่มีการ์ดสภาพเดียวกัน หรือสต็อกไม่พอ
        OSvc-->>Ctrl: throw InsufficientStockException("Target account does not hold matching condition")
        Ctrl-->>UI: HTTP 400 Bad Request
        UI-->>Staff: แจ้งเตือน: "ไอดีนี้ไม่มีการ์ดสภาพ MINT หรือสต็อกไม่พอ"
    else ไอดีเป้าหมายมีการ์ดสภาพตรงกันและสต็อกพร้อม
        Note over OSvc,InvRepo: สลับการจองสต็อกข้ามคลัง (Atomic Transfer)
        OSvc->>InvRepo: 8. oldInventory.restoreStock(quantity)
        OSvc->>InvRepo: 9. targetInventory.deductStock(quantity)
        OSvc->>InvRepo: 10. save(oldInventory) & save(targetInventory)

        Note over OSvc: ผูก targetInventory และ targetAccount เข้ากับ OrderItem
        Note over OSvc: รักษาราคาเดิม (unitPrice & subtotal ไม่เปลี่ยนแปลง)
        OSvc->>ORepo: 11. orderItem.setInventory(targetInventory) & setAssignedAccount(targetAccount)
        OSvc->>ORepo: 12. save(order)

        OSvc-->>Ctrl: 13. return OrderItemResponse DTO
        deactivate OSvc
        Ctrl-->>UI: 14. HTTP 200 OK
        deactivate Ctrl
        UI-->>Staff: 15. อัปเดตไอดีผู้ส่งมอบและคลังใหม่สำเร็จ
    end
    deactivate UI
```

