# Project Test Report

## หลักฐานรอบทดสอบล่าสุด

- Commit: `82c449b857d32dc7ad231e97a57ba3ebd976c761`
- Branch: `sikarin_6733802925_01`
- วันที่บันทึก: 2026-10-09T20:32:29.821969+07:00 (Asia/Bangkok)
- คำสั่งจริง: `code/mvnw.cmd -f code/pom.xml clean verify` — exit code 0, **BUILD SUCCESS**
- ทั้งระบบ: **373 tests, 0 failures, 0 errors, 0 skipped**
- เวลารวม Maven: 46.613 s
- ระบบ: Windows-11-10.0.26200-SP0
- Java: `openjdk version "21.0.10" 2026-01-20`
- Database: H2 local รวม H2 PostgreSQL mode; ไม่ได้เชื่อม PostgreSQL จริง
- Source/config ตรง commit: ใช่ (มีการแก้เอกสารและรายงานที่ยังไม่ commit)
- SHA-256 ของ source/config ใน working tree: `98ab0cc2bddbbb1825d0e0e615f0b1fe8ee069d061c9ddc990a4fb03acfca502`


## ผลราย test suite

| Suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| com.pokevault.common.exception.CustomExceptionTest | 2 | 0 | 0 | 0 |
| com.pokevault.common.security.ApiAuthorizationTest | 36 | 0 | 0 | 0 |
| com.pokevault.common.security.CustomUserDetailsServiceTest | 3 | 0 | 0 | 0 |
| com.pokevault.common.security.OrderAccessPolicyTest | 3 | 0 | 0 | 0 |
| com.pokevault.domain.entity.CardInventoryTest | 5 | 0 | 0 | 0 |
| com.pokevault.modules.catalog.CardApiControllerTest | 19 | 0 | 0 | 0 |
| com.pokevault.modules.catalog.CardServiceEdgeCaseTest | 13 | 0 | 0 | 0 |
| com.pokevault.modules.catalog.CardServiceTest | 13 | 0 | 0 | 0 |
| com.pokevault.modules.catalog.CatalogPersistenceTest | 7 | 0 | 0 | 0 |
| com.pokevault.modules.catalog.CatalogSchemaSqlTest | 6 | 0 | 0 | 0 |
| com.pokevault.modules.catalog.UserServiceTest | 8 | 0 | 0 | 0 |
| com.pokevault.modules.order.DiscountStrategyEdgeCaseTest | 11 | 0 | 0 | 0 |
| com.pokevault.modules.order.DiscountStrategyTest | 6 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderApiControllerTest | 15 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderBookingApiControllerTest | 24 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderBookingApiSecurityTest | 2 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderPersistenceTest | 7 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderServiceEdgeCaseTest | 8 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderServiceTest | 23 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderStockAndSecurityTest$CancelOrderRestorationTests | 1 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderStockAndSecurityTest$LastCardMatchingTests | 1 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderStockAndSecurityTest$MultiItemRollbackTests | 1 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderStockAndSecurityTest$OwnershipSecurityTests | 5 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderStockAndSecurityTest$ReassignAccountTests | 5 | 0 | 0 | 0 |
| com.pokevault.modules.order.OrderStockAndSecurityTest | 0 | 0 | 0 | 0 |
| com.pokevault.modules.trade.advice.GlobalExceptionHandlerTest | 10 | 0 | 0 | 0 |
| com.pokevault.modules.trade.controller.TradeMatchingApiControllerTest | 13 | 0 | 0 | 0 |
| com.pokevault.modules.trade.dto.TradeRecommendationResponseTest | 3 | 0 | 0 | 0 |
| com.pokevault.modules.trade.service.TradeMatchingServiceTest$AutoMatchAlgorithmTests | 6 | 0 | 0 | 0 |
| com.pokevault.modules.trade.service.TradeMatchingServiceTest$BatchAutoMatchTests | 2 | 0 | 0 | 0 |
| com.pokevault.modules.trade.service.TradeMatchingServiceTest$CardConditionMatchingTests | 7 | 0 | 0 | 0 |
| com.pokevault.modules.trade.service.TradeMatchingServiceTest$ManualAssignmentTests | 9 | 0 | 0 | 0 |
| com.pokevault.modules.trade.service.TradeMatchingServiceTest$RecommendationQueryTests | 4 | 0 | 0 | 0 |
| com.pokevault.modules.trade.service.TradeMatchingServiceTest | 0 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$CancellationTests | 4 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$CancelledOrderStateEdgeCaseTests | 3 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$ExecuteActionStringTests | 2 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$FactoryMethodTests | 2 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$HappyPathTests | 1 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$IllegalTransitionTests | 3 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$OrderContextMutatorTests | 3 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$ShippingGuardTests | 3 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest$TerminalStateTests | 2 | 0 | 0 | 0 |
| com.pokevault.modules.trade.state.OrderStateTest | 0 | 0 | 0 | 0 |
| com.pokevault.modules.vault.observer.LowStockObserverTest | 7 | 0 | 0 | 0 |
| com.pokevault.modules.vault.service.GameAccountServiceTest$AccountManagementTests | 12 | 0 | 0 | 0 |
| com.pokevault.modules.vault.service.GameAccountServiceTest$AddPulledCardTests | 4 | 0 | 0 | 0 |
| com.pokevault.modules.vault.service.GameAccountServiceTest | 0 | 0 | 0 | 0 |
| com.pokevault.modules.web.PlatformEndpointsTest | 4 | 0 | 0 | 0 |
| com.pokevault.modules.web.RegistrationServiceTest | 3 | 0 | 0 | 0 |
| com.pokevault.modules.web.StoreAdminServiceTest | 5 | 0 | 0 | 0 |
| com.pokevault.modules.web.WebPageServiceTest$CardGallery | 13 | 0 | 0 | 0 |
| com.pokevault.modules.web.WebPageServiceTest | 0 | 0 | 0 | 0 |
| com.pokevault.modules.web.WebViewControllerTest$AccessAndNavbar | 11 | 0 | 0 | 0 |
| com.pokevault.modules.web.WebViewControllerTest$LoginPage | 5 | 0 | 0 | 0 |
| com.pokevault.modules.web.WebViewControllerTest$PageModels | 8 | 0 | 0 | 0 |
| com.pokevault.modules.web.WebViewControllerTest | 0 | 0 | 0 | 0 |

## ขอบเขตและข้อจำกัด

- Unit tests (Mockito), controller/security tests (MockMvc), JPA/H2 persistence และ schema tests ตาม suites ข้างต้น
- ผลนี้เป็นการรันหนึ่งครั้งบน source ที่ระบุ ไม่ใช่ coverage 100% หรือการรับรองทุกสถานการณ์
- ไม่ครอบคลุม browser UI, PostgreSQL จริง, manual migration และ public cloud deployment
- รายงานสมาชิกมี scope ทับซ้อนกัน ห้ามบวกยอดรายสมาชิกเป็นยอดทั้งระบบ
- XML ในโฟลเดอร์นี้ตัด machine properties และ application logs ออก; raw อยู่ code/target/surefire-reports
- [สรุป JSON](summary.json), [Build Summary](BUILD-SUMMARY.txt)

## รายงานรายสมาชิก

- [ศิฆรินทร์](../sikarin/TEST-REPORT.md)
- [สัพพัญญู](../sapphanyu/TEST-REPORT.md)
- [ธนภูมิ](../tanapoom/TEST-REPORT.md)
- [แทนคุณ](../tankun/TEST-REPORT.md)
