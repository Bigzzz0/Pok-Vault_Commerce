# Test Report — ศิฆรินทร์ อุปจันทร์

Branch: `sikarin_6733802925_01`

Report generated: 2026-10-09T16:26:23.930678+07:00

## ผลการรัน

| ขอบเขต | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| ทั้งโปรเจกต์ | 122 | 0 | 0 | 0 |
| Card Catalog / User / Persistence | 60 | 0 | 0 | 0 |

## รายละเอียดส่วนของศิฆรินทร์

| Test class | Tests | Failures | Errors |
|---|---:|---:|---:|
| CardApiControllerTest | 19 | 0 | 0 |
| CardServiceEdgeCaseTest | 13 | 0 | 0 |
| CardServiceTest | 13 | 0 | 0 |
| CatalogPersistenceTest | 7 | 0 | 0 |
| UserServiceTest | 8 | 0 | 0 |

## สิ่งที่ทดสอบ

- CardServiceTest (เดิม): อ่าน/ค้นหา/กรองการ์ด, expansion, pagination และ CRUD
- CardServiceEdgeCaseTest (ใหม่): ค้นหาค่าว่าง/เว้นวรรค, กรองหลายเงื่อนไข, หน้าว่าง, ตรวจค่าที่ส่งบันทึก และกรณี update ไม่พบข้อมูล
- UserServiceTest (ใหม่): อ่านโปรไฟล์, สร้าง/อัปเดตที่อยู่, รักษาระดับสมาชิกและแต้ม และกรณีไม่พบข้อมูล
- CardApiControllerTest (ใหม่): MockMvc ทดสอบ HTTP 200/201/204/400/404, binding, pagination/sort และ Bean Validation
- CatalogPersistenceTest (ใหม่): Spring Boot @DataJpaTest + H2, query จริง, Specification, pagination/sort, unique constraint, cascade/orphan removal และ audit timestamps

## วิธีรันซ้ำ

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\mvnw.cmd clean test
python test/generate_sikarin_report.py
```

รันเฉพาะส่วนของศิฆรินทร์:

```powershell
.\mvnw.cmd '-Dtest=CardServiceTest,CardServiceEdgeCaseTest,UserServiceTest,CardApiControllerTest,CatalogPersistenceTest' test
```

หากรันเฉพาะส่วน อย่านับผลเก่าที่ค้างใน target/surefire-reports เป็นผลการรันใหม่ ให้ใช้ clean ก่อนเมื่อสร้างรายงาน

## ขอบเขตและข้อจำกัด

- แก้เฉพาะไฟล์ทดสอบและรายงาน ไม่เปลี่ยน production Java, UI หรือ pom.xml
- H2 ใช้ฐานข้อมูลใหม่แยกจากแอป, ปิด SQL seed และ rollback หลังแต่ละเทสต์
- MVC ใช้ standalone MockMvc และ mock service; ไม่ทดสอบ security filters หรือเว็บผ่าน browser
- ยังไม่ได้ทดสอบ PostgreSQL/Cloud, seed data.sql, ทุก constraint หรือทุกเส้นทางของระบบ
- ผล unit/integration tests นี้ไม่ยืนยันว่า API เทรดรายสินค้าที่เคยพบ HTTP 500 ถูกแก้แล้ว
- WARN/ERROR ของ unique constraint ในเทสต์ duplicate เป็นข้อผิดพลาดที่คาดไว้และ assert แล้ว
- ผลรายกรณีดู summary.json และ JUnit XML ของโมดูล catalog ในโฟลเดอร์เดียวกัน

## ผลรายกรณี

### CardApiControllerTest

- `searchUsesLiteralRouteRatherThanIdRoute` — passed
- `listBindsFiltersAndReturnsData` — passed
- `paginationBindsPageSizeAndDescendingSort` — passed
- `invalidCreateReturns400WithoutCallingService(String)[1]` — passed
- `invalidCreateReturns400WithoutCallingService(String)[2]` — passed
- `invalidCreateReturns400WithoutCallingService(String)[3]` — passed
- `invalidCreateReturns400WithoutCallingService(String)[4]` — passed
- `invalidCreateReturns400WithoutCallingService(String)[5]` — passed
- `invalidCreateReturns400WithoutCallingService(String)[6]` — passed
- `invalidCreateReturns400WithoutCallingService(String)[7]` — passed
- `invalidCreateReturns400WithoutCallingService(String)[8]` — passed
- `invalidCreateReturns400WithoutCallingService(String)[9]` — passed
- `deleteReturns204WithEmptyBody` — passed
- `createReturns201AndBindsRequest` — passed
- `updateReturns200` — passed
- `getCardReturns200` — passed
- `expansionRoutesDelegateToService` — passed
- `missingCardReturns404` — passed
- `defaultPaginationUsesCardNumberAscending` — passed

### CardServiceEdgeCaseTest

- `createPersistsActualRequestFields` — passed
- `blankSearchReturnsAllCards(String)[1]` — passed
- `blankSearchReturnsAllCards(String)[2]` — passed
- `blankSearchReturnsAllCards(String)[3]` — passed
- `blankSearchReturnsAllCards(String)[4]` — passed
- `searchTrimsQueryBeforeRepositoryCall` — passed
- `allExpansionsMapToResponses` — passed
- `updateMissingCardDoesNotWrite` — passed
- `updateMissingExpansionDoesNotMutateExistingCard` — passed
- `expansionCardsMapToResponse` — passed
- `emptyPagePreservesRequestedPageAndSort` — passed
- `combinedFiltersAreCaseInsensitiveAndTrimmed` — passed
- `filtersWithNoMatchReturnEmptyList` — passed

### CardServiceTest

- `deleteCard_CardExists_DeletesSuccessfully` — passed
- `getExpansionByCode_ValidCode_ReturnsExpansion` — passed
- `createCard_ValidRequest_ReturnsCardResponse` — passed
- `getCardsPaged_WithPagination_ReturnsPageResponse` — passed
- `getCardById_CardNotFound_ThrowsException` — passed
- `getAllCards_WithFilter_ReturnsFilteredCards` — passed
- `getCardById_CardExists_ReturnsCard` — passed
- `getExpansionByCode_NotFound_ThrowsException` — passed
- `createCard_ExpansionNotFound_ThrowsException` — passed
- `getAllCards_NoFilter_ReturnsAllCards` — passed
- `searchCards_ValidQuery_ReturnsMatchingCards` — passed
- `deleteCard_CardNotFound_ThrowsException` — passed
- `updateCard_CardExists_ReturnsUpdatedCardResponse` — passed

### CatalogPersistenceTest

- `auditCreationTimestampIsRetainedOnUpdate` — passed
- `duplicateCardNumberWithinSameExpansionIsRejected` — passed
- `sameCardNumberInDifferentExpansionsIsAllowed` — passed
- `realSearchAndExpansionQueriesFindMatchingCards` — passed
- `userProfileCascadesAndCanBeQueriedAfterReload` — passed
- `removingUserProfileDeletesOrphan` — passed
- `realSpecificationCombinesAllFiltersAndSortsBeforePaging` — passed

### UserServiceTest

- `missingRewardProfileThrowsNotFound` — passed
- `profileMapsMembershipAndContactFields` — passed
- `absentProfileReturnsZeroPointsAndNullAddress` — passed
- `missingUserCannotBeRead` — passed
- `missingUserCannotUpdateAddress` — passed
- `addressUpdatePreservesExistingMembershipAndPoints` — passed
- `rewardPointsComeFromProfileRepository` — passed
- `addressUpdateCreatesAndLinksMissingProfile` — passed
