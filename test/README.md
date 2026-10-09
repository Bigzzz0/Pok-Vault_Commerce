# Tests

JUnit 5 / Mockito / Spring Boot Test source files are in `src/test/java` (Maven convention).

Run all tests from the project root:

```powershell
.\mvnw.cmd clean test
python test/generate_sikarin_report.py
python test/generate_sapphanyu_report.py
```

Use JDK 17 or later. If Java is not configured, set `JAVA_HOME` to your JDK folder first.

### สมาชิกคนที่ 1: ศิฆรินทร์ อุปจันทร์ (673380292-5)
- **ขอบเขต:** `com.pokevault.modules.catalog` (Card Catalog, User Profile, Catalog Persistence)
- **รายงานผล:** [Sikarin Test Report](reports/sikarin/TEST-REPORT.md)
- **สคริปต์รายงาน:** `python test/generate_sikarin_report.py`

### สมาชิกคนที่ 2: สัพพัญญู คำตุ้ม (673380066-4)
- **ขอบเขต:** Game Account Vault, `CardInventory`, Low Stock Observer Pattern, และ Order Item Trade Status API/Lifecycle
- **คำสั่งรันเฉพาะขอบเขต:**
  ```powershell
  .\mvnw.cmd test '-Dtest=GameAccountServiceTest,LowStockObserverTest,CardInventoryTest,OrderServiceTest,OrderApiControllerTest'
  python test/generate_sapphanyu_report.py
  ```
- **รายงานผล:** [Sapphanyu Test Report](reports/sapphanyu/TEST-REPORT.md)
- **สคริปต์รายงาน:** `python test/generate_sapphanyu_report.py`

Maven's raw output is under `target/surefire-reports`. The saved JUnit XML files under `test/reports/sikarin` and `test/reports/sapphanyu` retain test results while omitting machine properties and verbose logs.

Sikarin's scope is `com.pokevault.modules.catalog` and `com.pokevault.common.security`. See [Test Report](reports/sikarin/TEST-REPORT.md) for results and limitations. The generator reads existing Surefire results; it does not execute tests itself.

Maven's raw output is under `target/surefire-reports`. The saved catalog/security JUnit XML under `test/reports/sikarin` retains test results while omitting machine properties and verbose logs.
