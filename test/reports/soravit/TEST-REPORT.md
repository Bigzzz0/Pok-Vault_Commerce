# Test Report — นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1)

**Role**: Member 5 — Frontend, Chat Commerce Handshake, Swagger, Docker, CI/CD, Deploy  
**Branch**: `soravit_6733802941_02`

## หลักฐานรอบทดสอบล่าสุด

- Commit: `0fb103b391d298087c9c9712d1e984a605bd5282`
- Branch ที่รัน: `soravit_6733802941_02`
- วันที่บันทึก: 2026-10-09T22:14:06.649775+07:00 (Asia/Bangkok)
- คำสั่งจริง: `code/mvnw.cmd -f code/pom.xml clean verify`
- ทั้งระบบ: **373 tests, 0 failures, 0 errors, 0 skipped**
- ระบบ: Windows-11-10.0.26200-SP0
- Java: `java version "17.0.12" 2024-07-16 LTS`
- Database: H2 in-memory ในเครื่อง; ไม่ได้เชื่อม PostgreSQL จริง (CI รันชุดเดียวกันบน PostgreSQL 16)
- Source ตรง commit: ไม่ทั้งหมด มีไฟล์ที่ยังไม่ commit: `code/src/main/resources/static/js/app.js`, `code/src/main/resources/static/js/ux-improvements.js`, `code/src/main/resources/templates/my-orders.html`

## 1. ผลการรันภาพรวม

| ขอบเขต | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| ทั้งโปรเจกต์ | 373 | 0 | 0 | 0 |
| หน้าเว็บ / สมัครสมาชิก / งานหลังบ้าน / Swagger และ Health (`modules.web`) | 49 | 0 | 0 | 0 |

## 2. ชุดทดสอบของสรวิชญ์

| Test class | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| PlatformEndpointsTest | 4 | 0 | 0 | 0 |
| RegistrationServiceTest | 3 | 0 | 0 | 0 |
| StoreAdminServiceTest | 5 | 0 | 0 | 0 |
| WebPageServiceTest | 8 | 0 | 0 | 0 |
| WebPageServiceTest.CardGallery | 5 | 0 | 0 | 0 |
| WebViewControllerTest.AccessAndNavbar | 11 | 0 | 0 | 0 |
| WebViewControllerTest.LoginPage | 5 | 0 | 0 | 0 |
| WebViewControllerTest.PageModels | 8 | 0 | 0 | 0 |

## 3. สิ่งที่ครอบคลุมในการทดสอบ

- **WebViewControllerTest** (`@WebMvcTest` + `SecurityConfig` จริง, mock `WebPageService`): ชั้น presentation ของหน้าเว็บ Thymeleaf
  - `AccessAndNavbar`: guest เปิด `/` และ `/cards` ได้, หน้าที่ต้องล็อกอินส่งไป `/login`, CUSTOMER เข้าหน้าพนักงานได้ 403, เมนู navbar ตามบทบาท (STAFF / ADMIN เห็น Swagger / CUSTOMER เห็นคำสั่งซื้อของฉัน), ฟอร์มออกจากระบบมี CSRF token
  - `LoginPage`: ฟอร์ม `POST /login`, กล่องแจ้ง `?error` และ `?logout`, ล็อกอินถูกไป `/`, รหัสผิดไป `/login?error=true`, ออกจากระบบไป `/login?logout=true`
  - `PageModels`: ทุกหน้าใส่ข้อมูลจาก service ลง Model ครบ, `/cards` ส่งตัวกรองให้ service ตามที่รับมา, `/my-orders` ขอเฉพาะออเดอร์ของผู้ใช้ที่ล็อกอิน และ render template จริงด้วย View DTO
- **WebPageServiceTest** (Mockito, repository แบบ mock): กฎของร้านที่ใช้ประกอบข้อมูลหน้าเว็บ ได้แก่ แสดงเฉพาะการ์ดที่มีสต็อก, ตัวกรองธาตุ / ความหายาก / ประเภท / คำค้นใช้ร่วมกัน, Trainer นับเป็น COLORLESS, ลิงก์ pill คงตัวกรองอื่น, ราคามาจากล็อตที่ถูกที่สุดที่ยังมีของ, รูปการ์ด fallback เป็นหลังการ์ด, Spotlight ไม่เกิน 6 ใบ, ฟอร์มจองแสดงเฉพาะลูกค้า (Guest ก่อน), ป้ายสต็อกใกล้หมด, สรุปสถานะบัญชีเกม และออเดอร์ของผู้ใช้เรียงใหม่สุดก่อน
- **PlatformEndpointsTest** (`@SpringBootTest` บูตแอปเต็มตัว): `/v3/api-docs` ใช้ข้อมูลจาก `OpenApiConfig`, `/swagger-ui.html` เปิดได้โดยไม่ล็อกอิน, `/actuator/health` ตอบ UP (ใช้กับ Docker healthcheck) และไฟล์ static โหลดได้
- **RegistrationServiceTest** (Mockito): สมัครสมาชิกลูกค้า เข้ารหัสรหัสผ่าน และปฏิเสธชื่อผู้ใช้หรืออีเมลซ้ำ
- **StoreAdminServiceTest** (Mockito): ปรับราคาขายการ์ด และกำหนดระดับสมาชิกของลูกค้า รวมกรณีข้อมูลไม่ถูกต้องและหาไม่พบ

## 4. วิธีรันซ้ำ

```powershell
.\code\mvnw.cmd -f code/pom.xml clean verify
python test/generate_soravit_report.py
```

รันเฉพาะส่วนของคนที่ 5:

```powershell
.\code\mvnw.cmd -f code/pom.xml '-Dtest=WebViewControllerTest,WebPageServiceTest,PlatformEndpointsTest,RegistrationServiceTest,StoreAdminServiceTest' test
```

## 5. ขอบเขตและข้อจำกัด

- ครอบคลุมโค้ด Java ของ `modules.web` (controller, service, mapper, View DTO) และการ render template ฝั่งเซิร์ฟเวอร์
- ไม่ครอบคลุม JavaScript ในเบราว์เซอร์ (`app.js`, `ux-improvements.js`) เช่น Chat Commerce Handshake, เอฟเฟกต์การ์ด, การอัปเดตหน้า `/my-orders` อัตโนมัติ และข้อความแจ้ง 409 ส่วนนี้ทดสอบด้วยมือ
- ไม่ครอบคลุม Docker build, GitHub Actions และระบบที่ deploy บน Cloud ผลของส่วนนั้นดูจาก CI และหัวข้อ Deployment ใน README
- `PlatformEndpointsTest` รันบน H2 ในเครื่อง และบน PostgreSQL 16 ใน CI
- รายงานนี้เป็น snapshot ของการรันหนึ่งครั้งบน source ที่ระบุด้านบน ไม่ใช่การรับรองทุกสถานการณ์
- XML ในโฟลเดอร์นี้ตัด machine properties และ application logs ออก; raw อยู่ที่ `code/target/surefire-reports`

## 6. ผลการทดสอบรายกรณี

### PlatformEndpointsTest

- `actuatorHealth_IsPublicAndUp` — **passed** (0.041s)
- `openApiDocs_DescribeAllModules` — **passed** (0.761s)
- `swaggerUi_IsPublic` — **passed** (0.027s)
- `staticAssets_ArePublic` — **passed** (0.119s)

### RegistrationServiceTest

- `registerCustomer_WhenUsernameTaken_ShouldThrow` — **passed** (0.004s)
- `registerCustomer_WhenValid_ShouldSaveCustomerWithHashedPassword` — **passed** (0.143s)
- `registerCustomer_WhenEmailRegistered_ShouldThrow` — **passed** (0.002s)

### StoreAdminServiceTest

- `updateMembershipTier_WhenCustomer_ShouldChangeTier` — **passed** (0.001s)
- `updateSellingPrice_WhenNegative_ShouldReject` — **passed** (0.003s)
- `updateSellingPrice_WhenValid_ShouldSaveNewPrice` — **passed** (0.001s)
- `updateSellingPrice_WhenMissing_ShouldThrowNotFound` — **passed** (0.001s)
- `updateMembershipTier_WhenNotCustomer_ShouldReject` — **passed** (0.002s)

### WebPageServiceTest

- `inventory_FlagsLowAndOutOfStock` — **passed** (0.007s)
- `dashboard_FeaturedCards_InStockOnly_MaxSix` — **passed** (0.012s)
- `price_ComesFromCheapestInventoryWithStock` — **passed** (0.008s)
- `ordersOfUser_UnknownUser_ReturnsEmpty` — **passed** (0.003s)
- `ordersOfUser_OnlyThatUsersOrders_NewestFirst` — **passed** (0.004s)
- `imageUrl_UsesBundledImageOrCardBack` — **passed** (0.003s)
- `accounts_CountsStatusesAndCardsPerAccount` — **passed** (0.008s)
- `inventory_BookingCustomers_OnlyCustomers_GuestFirst` — **passed** (0.002s)

### WebPageServiceTest.CardGallery

- `filterByElement_TreatsTrainerAsColorless` — **passed** (0.004s)
- `filterPillUrls_KeepOtherFiltersAndSearch` — **passed** (0.004s)
- `filtersCombine` — **passed** (0.007s)
- `showsOnlyCardsInStock` — **passed** (0.002s)
- `unknownFilterValue_IsIgnored` — **passed** (0.003s)

### WebViewControllerTest.AccessAndNavbar

- `customer_SeesMyOrdersAndSignOut_ButNotStaffMenu` — **passed** (0.067s)
- `guest_CanBrowsePublicPages_AndSeesSignInOnly` — **passed** (0.099s)
- `staff_SeesStaffMenu_AndCsrfProtectedSignOut` — **passed** (0.06s)
- `guest_IsRedirectedToLogin_FromProtectedPages(String)[1]` — **passed** (0.006s)
- `guest_IsRedirectedToLogin_FromProtectedPages(String)[2]` — **passed** (0.004s)
- `guest_IsRedirectedToLogin_FromProtectedPages(String)[3]` — **passed** (0.004s)
- `guest_IsRedirectedToLogin_FromProtectedPages(String)[4]` — **passed** (0.004s)
- `customer_IsForbidden_FromStaffPages(String)[1]` — **passed** (0.005s)
- `customer_IsForbidden_FromStaffPages(String)[2]` — **passed** (0.004s)
- `customer_IsForbidden_FromStaffPages(String)[3]` — **passed** (0.005s)
- `admin_SeesSwaggerLink` — **passed** (0.072s)

### WebViewControllerTest.LoginPage

- `loginForm_PostsUsernamePasswordWithCsrf` — **passed** (0.053s)
- `formLogin_RedirectsOnSuccessAndFailure` — **passed** (0.21s)
- `loginPage_WithErrorParam_ShowsErrorBox` — **passed** (0.014s)
- `loginPage_WithLogoutParam_ShowsLogoutBox` — **passed** (0.013s)
- `signOut_RedirectsToLoginWithLogoutMessage` — **passed** (0.021s)

### WebViewControllerTest.PageModels

- `cards_ForwardsFiltersAndPutsGalleryInModel` — **passed** (0.693s)
- `dashboard_PutsServiceDataInModel` — **passed** (0.089s)
- `myOrders_AsksOnlyForSignedInUsersOrders` — **passed** (0.091s)
- `currentUserId_ForGuest_IsNull` — **passed** (0.075s)
- `orders_PutsAllOrdersInModel` — **passed** (0.126s)
- `accounts_PutsServiceDataInModel` — **passed** (0.128s)
- `inventory_PutsServiceDataInModel` — **passed** (0.125s)
- `currentUserId_ForSignedInUser` — **passed** (0.097s)
