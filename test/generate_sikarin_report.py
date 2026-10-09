"""Generate an evidence report from the latest Maven Surefire XML results.

Run `mvnw.cmd clean test` first, then `python test/generate_sikarin_report.py`.
Uses only the Python standard library; never runs or modifies application code.
"""
from pathlib import Path
from datetime import datetime
import json
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "target" / "surefire-reports"
OUT = ROOT / "test" / "reports" / "sikarin"
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
        cases.append({"name": case.get("name"), "result": result, "seconds": float(case.get("time", 0))})
    row = {"suite": suite.get("name"), "tests": int(suite.get("tests", 0)),
           "failures": int(suite.get("failures", 0)), "errors": int(suite.get("errors", 0)),
           "skipped": int(suite.get("skipped", 0)), "seconds": float(suite.get("time", 0)), "cases": cases}
    suites.append(row)
    if row["suite"].startswith("com.pokevault.modules.catalog."):
        # Preserve JUnit results without machine properties or verbose application logs.
        for child in list(suite):
            if child.tag in ("properties", "system-out", "system-err"):
                suite.remove(child)
        tree.write(OUT / path.name, encoding="utf-8", xml_declaration=True)

catalog = [row for row in suites if row["suite"].startswith("com.pokevault.modules.catalog.")]
def totals(rows):
    return {key: sum(row[key] for row in rows) for key in ("tests", "failures", "errors", "skipped")}

report = {"generated_at": datetime.now().astimezone().isoformat(), "owner": "Sikarin 673380292-5",
          "command": "mvnw.cmd test", "whole_suite": totals(suites), "catalog_scope": totals(catalog), "suites": suites}
(OUT / "summary.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
lines = ["# Test Report — ศิฆรินทร์ อุปจันทร์", "", "Branch: `sikarin_6733802925_01`", "",
         f"Report generated: {report['generated_at']}", "", "## ผลการรัน", "",
         "| ขอบเขต | Tests | Failures | Errors | Skipped |", "|---|---:|---:|---:|---:|"]
for label, values in (("ทั้งโปรเจกต์", report["whole_suite"]), ("Card Catalog / User / Persistence", report["catalog_scope"])):
    lines.append("| " + label + " | " + " | ".join(str(values[key]) for key in ("tests", "failures", "errors", "skipped")) + " |")
lines += ["", "## รายละเอียดส่วนของศิฆรินทร์", "", "| Test class | Tests | Failures | Errors |", "|---|---:|---:|---:|"]
for row in catalog:
    lines.append(f"| {row['suite'].split('.')[-1]} | {row['tests']} | {row['failures']} | {row['errors']} |")
lines += ["", "## สิ่งที่ทดสอบ", "",
          "- CardServiceTest (เดิม): อ่าน/ค้นหา/กรองการ์ด, expansion, pagination และ CRUD",
          "- CardServiceEdgeCaseTest (ใหม่): ค้นหาค่าว่าง/เว้นวรรค, กรองหลายเงื่อนไข, หน้าว่าง, ตรวจค่าที่ส่งบันทึก และกรณี update ไม่พบข้อมูล",
          "- UserServiceTest (ใหม่): อ่านโปรไฟล์, สร้าง/อัปเดตที่อยู่, รักษาระดับสมาชิกและแต้ม และกรณีไม่พบข้อมูล",
          "- CardApiControllerTest (ใหม่): MockMvc ทดสอบ HTTP 200/201/204/400/404, binding, pagination/sort และ Bean Validation",
          "- CatalogPersistenceTest (ใหม่): Spring Boot @DataJpaTest + H2, query จริง, Specification, pagination/sort, unique constraint, cascade/orphan removal และ audit timestamps",
          "", "## วิธีรันซ้ำ", "", "```powershell", "$env:JAVA_HOME='C:\\Program Files\\Android\\Android Studio\\jbr'",
          ".\\mvnw.cmd clean test", "python test/generate_sikarin_report.py", "```", "",
          "รันเฉพาะส่วนของศิฆรินทร์:", "", "```powershell",
          ".\\mvnw.cmd '-Dtest=CardServiceTest,CardServiceEdgeCaseTest,UserServiceTest,CardApiControllerTest,CatalogPersistenceTest' test", "```", "",
          "หากรันเฉพาะส่วน อย่านับผลเก่าที่ค้างใน target/surefire-reports เป็นผลการรันใหม่ ให้ใช้ clean ก่อนเมื่อสร้างรายงาน", "",
          "## ขอบเขตและข้อจำกัด", "",
          "- แก้เฉพาะไฟล์ทดสอบและรายงาน ไม่เปลี่ยน production Java, UI หรือ pom.xml",
          "- H2 ใช้ฐานข้อมูลใหม่แยกจากแอป, ปิด SQL seed และ rollback หลังแต่ละเทสต์",
          "- MVC ใช้ standalone MockMvc และ mock service; ไม่ทดสอบ security filters หรือเว็บผ่าน browser",
          "- ยังไม่ได้ทดสอบ PostgreSQL/Cloud, seed data.sql, ทุก constraint หรือทุกเส้นทางของระบบ",
          "- ผล unit/integration tests นี้ไม่ยืนยันว่า API เทรดรายสินค้าที่เคยพบ HTTP 500 ถูกแก้แล้ว",
          "- WARN/ERROR ของ unique constraint ในเทสต์ duplicate เป็นข้อผิดพลาดที่คาดไว้และ assert แล้ว",
          "- ผลรายกรณีดู summary.json และ JUnit XML ของโมดูล catalog ในโฟลเดอร์เดียวกัน", "",
          "## ผลรายกรณี", ""]
for row in catalog:
    lines += [f"### {row['suite'].split('.')[-1]}", ""]
    lines.extend(f"- `{case['name']}` — {case['result']}" for case in row["cases"])
    lines.append("")
(OUT / "TEST-REPORT.md").write_text("\n".join(lines), encoding="utf-8")
print(json.dumps({"whole_suite": report["whole_suite"], "catalog_scope": report["catalog_scope"], "report": str(OUT / "TEST-REPORT.md")}))
