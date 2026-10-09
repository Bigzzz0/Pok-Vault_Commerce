# Tests

JUnit 5 / Mockito / Spring Boot Test source files are in `src/test/java` (Maven convention).

Run all tests from the project root:

```powershell
.\mvnw.cmd clean test
python test/generate_sikarin_report.py
```

Use JDK 17 or later. If Java is not configured, set `JAVA_HOME` to your JDK folder first.

Sikarin's scope is `com.pokevault.modules.catalog` and `com.pokevault.common.security`. See [Test Report](reports/sikarin/TEST-REPORT.md) for results and limitations. The generator reads existing Surefire results; it does not execute tests itself.

Maven's raw output is under `target/surefire-reports`. The saved catalog/security JUnit XML under `test/reports/sikarin` retains test results while omitting machine properties and verbose logs.
