"""Generate an evidence report from the latest Maven Surefire XML results for Member 5 (Soravit).

Run `code/mvnw.cmd -f code/pom.xml clean verify` first, then `python test/generate_soravit_report.py`.
Uses only the Python standard library; never runs or modifies application code.
"""
from pathlib import Path
from datetime import datetime, timedelta, timezone
import json
import os
import platform
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "code" / "target" / "surefire-reports"
OUT = ROOT / "test" / "reports" / "soravit"
SCOPE = "com.pokevault.modules.web."
KEYS = ("tests", "failures", "errors", "skipped")

paths = sorted(SOURCE.glob("TEST-*.xml"))
if not paths:
    raise SystemExit("No Surefire XML found. Run Maven tests first.")
OUT.mkdir(parents=True, exist_ok=True)
suites = []
for path in paths:
    tree = ET.parse(path)
    suite = tree.getroot()
    cases = []
    for case in suite.findall("testcase"):
        result = next((tag for tag in ("failure", "error", "skipped") if case.find(tag) is not None), "passed")
        cases.append({"name": case.get("name"), "class": case.get("classname"), "result": result,
                      "seconds": float(case.get("time", 0))})
    row = {"suite": suite.get("name"), **{key: int(suite.get(key, 0)) for key in KEYS},
           "seconds": float(suite.get("time", 0)), "cases": cases}
    suites.append(row)
    if row["suite"].startswith(SCOPE):
        # Preserve JUnit results without machine properties or verbose application logs.
        for child in list(suite):
            if child.tag in ("properties", "system-out", "system-err"):
                suite.remove(child)
        tree.write(OUT / path.name, encoding="utf-8", xml_declaration=True)

web_scope = [row for row in suites if row["suite"].startswith(SCOPE)]
# Surefire files nested-class results under whichever suite ran them, so group by each case's own class.
by_class = {}
for row in web_scope:
    for case in row["cases"]:
        by_class.setdefault(case["class"][len(SCOPE):].replace("$", "."), []).append(case)


def totals(rows):
    return {key: sum(row[key] for row in rows) for key in KEYS}


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT).decode("utf-8").strip()


java_home = os.environ.get("JAVA_HOME")
java = str(Path(java_home) / "bin" / ("java.exe" if os.name == "nt" else "java")) if java_home else "java"
java_run = subprocess.run([java, "-version"], capture_output=True, text=True, check=True)
java_version = (java_run.stderr or java_run.stdout).strip().splitlines()[0]
# Source files that differ from the recorded commit, so the report never claims a cleaner state than it ran on.
uncommitted = [line.split(maxsplit=1)[1] for line in
               git("status", "--porcelain", "--", "code", "test/java", "test/resources", "img/web").splitlines()]

report = {
    "generated_at": datetime.now(timezone(timedelta(hours=7))).isoformat(),
    "timezone": "Asia/Bangkok",
    "owner": "Soravit Sassanasupin 673380294-1 (Member 5)",
    "commit": git("rev-parse", "HEAD"),
    "branch": git("branch", "--show-current"),
    "uncommitted_source_files": uncommitted,
    "command": "code/mvnw.cmd -f code/pom.xml clean verify",
    "platform": platform.platform(),
    "java": java_version,
    "database": "Local H2 in-memory; not a live PostgreSQL server",
    "whole_suite": totals(suites),
    "web_scope": totals(web_scope),
    "suites": suites,
}
(OUT / "summary.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

whole, web = report["whole_suite"], report["web_scope"]
lines = [
    "# Test Report — นายสรวิชญ์ ศาสนสุพินธุ์ (673380294-1)", "",
    "**Role**: Member 5 — Frontend, Chat Commerce Handshake, Swagger, Docker, CI/CD, Deploy  ",
    "**Branch**: `soravit_6733802941_02`", "",
    "## หลักฐานรอบทดสอบล่าสุด", "",
    f"- Commit: `{report['commit']}`",
    f"- Branch ที่รัน: `{report['branch']}`",
    f"- วันที่บันทึก: {report['generated_at']} (Asia/Bangkok)",
    "- คำสั่งจริง: `code/mvnw.cmd -f code/pom.xml clean verify`",
    f"- ทั้งระบบ: **{whole['tests']} tests, {whole['failures']} failures, {whole['errors']} errors, {whole['skipped']} skipped**",
    f"- ระบบ: {report['platform']}",
    f"- Java: `{java_version}`",
    "- Database: H2 in-memory ในเครื่อง; ไม่ได้เชื่อม PostgreSQL จริง (CI รันชุดเดียวกันบน PostgreSQL 16)",
    "- Source ตรง commit: " + ("ใช่" if not uncommitted else "ไม่ทั้งหมด มีไฟล์ที่ยังไม่ commit: " + ", ".join(f"`{name}`" for name in uncommitted)),
    "",
    "## 1. ผลการรันภาพรวม", "",
    "| ขอบเขต | Tests | Failures | Errors | Skipped |",
    "|---|---:|---:|---:|---:|",
    "| ทั้งโปรเจกต์ | " + " | ".join(str(whole[key]) for key in KEYS) + " |",
    "| หน้าเว็บ / สมัครสมาชิก / งานหลังบ้าน / Swagger และ Health (`modules.web`) | " + " | ".join(str(web[key]) for key in KEYS) + " |",
    "",
    "## 2. ชุดทดสอบของสรวิชญ์", "",
    "| Test class | Tests | Failures | Errors | Skipped |",
    "|---|---:|---:|---:|---:|",
]
for name, cases in sorted(by_class.items()):
    counts = [len(cases)] + [sum(case["result"] == tag for case in cases) for tag in ("failure", "error", "skipped")]
    lines.append(f"| {name} | " + " | ".join(map(str, counts)) + " |")

lines += [
    "", "## 3. สิ่งที่ครอบคลุมในการทดสอบ", "",
    "- **WebViewControllerTest** (`@WebMvcTest` + `SecurityConfig` จริง, mock `WebPageService`): ชั้น presentation ของหน้าเว็บ Thymeleaf",
    "  - `AccessAndNavbar`: guest เปิด `/` และ `/cards` ได้, หน้าที่ต้องล็อกอินส่งไป `/login`, CUSTOMER เข้าหน้าพนักงานได้ 403, เมนู navbar ตามบทบาท (STAFF / ADMIN เห็น Swagger / CUSTOMER เห็นคำสั่งซื้อของฉัน), ฟอร์มออกจากระบบมี CSRF token",
    "  - `LoginPage`: ฟอร์ม `POST /login`, กล่องแจ้ง `?error` และ `?logout`, ล็อกอินถูกไป `/`, รหัสผิดไป `/login?error=true`, ออกจากระบบไป `/login?logout=true`",
    "  - `PageModels`: ทุกหน้าใส่ข้อมูลจาก service ลง Model ครบ, `/cards` ส่งตัวกรองให้ service ตามที่รับมา, `/my-orders` ขอเฉพาะออเดอร์ของผู้ใช้ที่ล็อกอิน และ render template จริงด้วย View DTO",
    "- **WebPageServiceTest** (Mockito, repository แบบ mock): กฎของร้านที่ใช้ประกอบข้อมูลหน้าเว็บ ได้แก่ แสดงเฉพาะการ์ดที่มีสต็อก, ตัวกรองธาตุ / ความหายาก / ประเภท / คำค้นใช้ร่วมกัน, Trainer นับเป็น COLORLESS, ลิงก์ pill คงตัวกรองอื่น, ราคามาจากล็อตที่ถูกที่สุดที่ยังมีของ, รูปการ์ด fallback เป็นหลังการ์ด, Spotlight ไม่เกิน 6 ใบ, ฟอร์มจองแสดงเฉพาะลูกค้า (Guest ก่อน), ป้ายสต็อกใกล้หมด, สรุปสถานะบัญชีเกม และออเดอร์ของผู้ใช้เรียงใหม่สุดก่อน",
    "- **PlatformEndpointsTest** (`@SpringBootTest` บูตแอปเต็มตัว): `/v3/api-docs` ใช้ข้อมูลจาก `OpenApiConfig`, `/swagger-ui.html` เปิดได้โดยไม่ล็อกอิน, `/actuator/health` ตอบ UP (ใช้กับ Docker healthcheck) และไฟล์ static โหลดได้",
    "- **RegistrationServiceTest** (Mockito): สมัครสมาชิกลูกค้า เข้ารหัสรหัสผ่าน และปฏิเสธชื่อผู้ใช้หรืออีเมลซ้ำ",
    "- **StoreAdminServiceTest** (Mockito): ปรับราคาขายการ์ด และกำหนดระดับสมาชิกของลูกค้า รวมกรณีข้อมูลไม่ถูกต้องและหาไม่พบ",
    "", "## 4. วิธีรันซ้ำ", "",
    "```powershell",
    ".\\code\\mvnw.cmd -f code/pom.xml clean verify",
    "python test/generate_soravit_report.py",
    "```", "",
    "รันเฉพาะส่วนของคนที่ 5:", "",
    "```powershell",
    ".\\code\\mvnw.cmd -f code/pom.xml '-Dtest=WebViewControllerTest,WebPageServiceTest,PlatformEndpointsTest,RegistrationServiceTest,StoreAdminServiceTest' test",
    "```", "",
    "## 5. ขอบเขตและข้อจำกัด", "",
    "- ครอบคลุมโค้ด Java ของ `modules.web` (controller, service, mapper, View DTO) และการ render template ฝั่งเซิร์ฟเวอร์",
    "- ไม่ครอบคลุม JavaScript ในเบราว์เซอร์ (`app.js`, `ux-improvements.js`) เช่น Chat Commerce Handshake, เอฟเฟกต์การ์ด, การอัปเดตหน้า `/my-orders` อัตโนมัติ และข้อความแจ้ง 409 ส่วนนี้ทดสอบด้วยมือ",
    "- ไม่ครอบคลุม Docker build, GitHub Actions และระบบที่ deploy บน Cloud ผลของส่วนนั้นดูจาก CI และหัวข้อ Deployment ใน README",
    "- `PlatformEndpointsTest` รันบน H2 ในเครื่อง และบน PostgreSQL 16 ใน CI",
    "- รายงานนี้เป็น snapshot ของการรันหนึ่งครั้งบน source ที่ระบุด้านบน ไม่ใช่การรับรองทุกสถานการณ์",
    "- XML ในโฟลเดอร์นี้ตัด machine properties และ application logs ออก; raw อยู่ที่ `code/target/surefire-reports`",
    "", "## 6. ผลการทดสอบรายกรณี", "",
]
for name, cases in sorted(by_class.items()):
    lines += [f"### {name}", ""]
    lines.extend(f"- `{case['name']}` — **{case['result']}** ({case['seconds']}s)" for case in cases)
    lines.append("")

(OUT / "TEST-REPORT.md").write_text("\n".join(lines), encoding="utf-8")
print(json.dumps({"whole_suite": whole, "web_scope": web, "report": str(OUT / "TEST-REPORT.md")}))
