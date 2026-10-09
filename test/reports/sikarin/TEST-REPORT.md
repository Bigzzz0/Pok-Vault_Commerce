# Test Report — ศิฆรินทร์ อุปจันทร์

Branch: `sikarin_6733802925_01`

Report generated: 2026-10-09T17:44:31.865054+07:00

## ผลการรัน

| ขอบเขต | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| ทั้งโปรเจกต์ | 269 | 0 | 0 | 0 |
| Card Catalog / User / Security / Persistence | 108 | 0 | 0 | 0 |

## รายละเอียดส่วนของศิฆรินทร์

| Test class | Tests | Failures | Errors |
|---|---:|---:|---:|
| ApiAuthorizationTest | 36 | 0 | 0 |
| CustomUserDetailsServiceTest | 3 | 0 | 0 |
| OrderAccessPolicyTest | 3 | 0 | 0 |
| CardApiControllerTest | 19 | 0 | 0 |
| CardServiceEdgeCaseTest | 13 | 0 | 0 |
| CardServiceTest | 13 | 0 | 0 |
| CatalogPersistenceTest | 7 | 0 | 0 |
| CatalogSchemaSqlTest | 6 | 0 | 0 |
| UserServiceTest | 8 | 0 | 0 |

## สิ่งที่ทดสอบ

- CardServiceTest (เดิม): อ่าน/ค้นหา/กรองการ์ด, expansion, pagination และ CRUD
- CardServiceEdgeCaseTest (ใหม่): ค้นหาค่าว่าง/เว้นวรรค, กรองหลายเงื่อนไข, หน้าว่าง, ตรวจค่าที่ส่งบันทึก และกรณี update ไม่พบข้อมูล
- UserServiceTest (ใหม่): อ่านโปรไฟล์, สร้าง/อัปเดตที่อยู่, รักษาระดับสมาชิกและแต้ม และกรณีไม่พบข้อมูล
- CardApiControllerTest (ใหม่): MockMvc ทดสอบ HTTP 200/201/204/400/404, binding, pagination/sort และ Bean Validation
- CatalogPersistenceTest (ใหม่): Spring Boot @DataJpaTest + H2, query จริง, Specification, pagination/sort, unique constraint, cascade/orphan removal และ audit timestamps
- CatalogSchemaSqlTest: โหลด schema.sql/data.sql จริงบน H2 PostgreSQL mode, Hibernate validate และตรวจขอบเขตข้อมูล
- ApiAuthorizationTest: security filter chain และ method authorization จริง, 401/403, บทบาท, เจ้าของออเดอร์ และ form login ผ่าน BCrypt
- OrderAccessPolicyTest: ตรวจ anonymous/unknown role, identity และบัญชีที่ไม่อยู่ในฐานข้อมูล
- CustomUserDetailsServiceTest: ชุดทดสอบ authentication เดิม

## วิธีรันซ้ำ

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\mvnw.cmd clean test
python test/generate_sikarin_report.py
```

รันเฉพาะส่วนของศิฆรินทร์:

```powershell
.\mvnw.cmd '-Dtest=CardServiceTest,CardServiceEdgeCaseTest,UserServiceTest,CardApiControllerTest,CatalogPersistenceTest,CatalogSchemaSqlTest,ApiAuthorizationTest,OrderAccessPolicyTest,CustomUserDetailsServiceTest' clean test
```

หากรันเฉพาะส่วน อย่านับผลเก่าที่ค้างใน target/surefire-reports เป็นผลการรันใหม่ ให้ใช้ clean ก่อนเมื่อสร้างรายงาน

## ขอบเขตและข้อจำกัด

- ทดสอบ production SecurityConfig, OrderAccessPolicy, ownership annotations และ core schema ที่แก้ในรอบนี้ด้วย
- H2 ใช้ฐานข้อมูลแยกจากแอป, rollback หลังแต่ละเทสต์ฐานข้อมูล; schema test เปิด SQL seed จริง
- Controller unit tests ใช้ standalone MockMvc; ApiAuthorizationTest ใช้ security filters และ method authorization จริง โดย mock business services
- ยังไม่ได้ทดสอบ PostgreSQL จริง/Cloud หรือ migration manual: Docker engine ในเครื่องไม่พร้อม
- ไม่ได้ทดสอบ browser end-to-end; ผลชุดเทสต์ไม่ยืนยันทุกเส้นทางธุรกิจหรือทุก constraint ของระบบ
- WARN/ERROR ของ unique constraint ในเทสต์ duplicate เป็นข้อผิดพลาดที่คาดไว้และ assert แล้ว
- ผลรายกรณีดู summary.json และ JUnit XML ของ catalog/security ในโฟลเดอร์เดียวกัน

## ผลรายกรณี

### ApiAuthorizationTest

- `customerCanReadOwnOrder` — passed
- `customerCanBookForSelf` — passed
- `customerCannotReadOtherCustomersOrderOrReceiveItsData` — passed
- `staffAndAdminCanBookForCustomersReadOrdersAndManageCards(String)[1]` — passed
- `staffAndAdminCanBookForCustomersReadOrdersAndManageCards(String)[2]` — passed
- `browserStillRedirectsToLoginAndFormLoginUsesBcrypt` — passed
- `customerCannotUseStoreManagementApis(String)[1]` — passed
- `customerCannotUseStoreManagementApis(String)[2]` — passed
- `customerCannotUseStoreManagementApis(String)[3]` — passed
- `customerCannotUseStoreManagementApis(String)[4]` — passed
- `customerCannotUseStoreManagementApis(String)[5]` — passed
- `customerCannotUseStoreManagementApis(String)[6]` — passed
- `customerCannotUseStoreManagementApis(String)[7]` — passed
- `customerCannotUseStoreManagementApis(String)[8]` — passed
- `customerCannotUseStoreManagementApis(String)[9]` — passed
- `customerCannotUseStoreManagementApis(String)[10]` — passed
- `customerCannotUseStoreManagementApis(String)[11]` — passed
- `customerCannotUseStoreManagementApis(String)[12]` — passed
- `customerCannotUseStoreManagementApis(String)[13]` — passed
- `anonymousApiRequestsReceiveJson401(String)[1]` — passed
- `anonymousApiRequestsReceiveJson401(String)[2]` — passed
- `anonymousApiRequestsReceiveJson401(String)[3]` — passed
- `anonymousApiRequestsReceiveJson401(String)[4]` — passed
- `anonymousApiRequestsReceiveJson401(String)[5]` — passed
- `anonymousApiRequestsReceiveJson401(String)[6]` — passed
- `anonymousApiRequestsReceiveJson401(String)[7]` — passed
- `anonymousApiRequestsReceiveJson401(String)[8]` — passed
- `anonymousApiRequestsReceiveJson401(String)[9]` — passed
- `anonymousApiRequestsReceiveJson401(String)[10]` — passed
- `anonymousApiRequestsReceiveJson401(String)[11]` — passed
- `anonymousApiRequestsReceiveJson401(String)[12]` — passed
- `anonymousApiRequestsReceiveJson401(String)[13]` — passed
- `anonymousApiRequestsReceiveJson401(String)[14]` — passed
- `anonymousApiRequestsReceiveJson401(String)[15]` — passed
- `customerCannotSpoofAnotherCustomerId` — passed
- `catalogAndRegistrationRemainPublic` — passed

### CustomUserDetailsServiceTest

- `loadUserByUsername_WhenUserExists_ShouldReturnUserDetails` — passed
- `passwordEncoder_MatchesRawPassword` — passed
- `loadUserByUsername_WhenUserNotFound_ShouldThrowException` — passed

### OrderAccessPolicyTest

- `customerMustMatchDatabaseIdentity` — passed
- `deletedCustomerAccountAndNullIdAreDenied` — passed
- `anonymousAndUnknownRolesAreDeniedWithoutLookup` — passed

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

### CatalogSchemaSqlTest

- `cardNumberAtMaximumLengthAndNullTrainerElementCanBePersisted` — passed
- `overlongAddressIsRejectedBeforeCreatingProfile` — passed
- `addressServiceCanCreateProfileWithoutFullNameAgainstRealSql` — passed
- `schemaLengthsAndNullabilityMatchCoreEntityContract` — passed
- `sqlRejectsCardNumberLongerThanTwentyEvenWithoutRequestValidation` — passed
- `realSqlSeedContainsAtLeastTwentyCardsAndAllExpansionsHaveRequiredFields` — passed

### UserServiceTest

- `missingRewardProfileThrowsNotFound` — passed
- `profileMapsMembershipAndContactFields` — passed
- `absentProfileReturnsZeroPointsAndNullAddress` — passed
- `missingUserCannotBeRead` — passed
- `missingUserCannotUpdateAddress` — passed
- `addressUpdatePreservesExistingMembershipAndPoints` — passed
- `rewardPointsComeFromProfileRepository` — passed
- `addressUpdateCreatesAndLinksMissingProfile` — passed
