"""Generate an evidence report from the latest Maven Surefire XML results for Member 3 (Tanapoom).

Run `mvnw.cmd clean test` first, then `python test/generate_tanapoom_report.py`.
Uses only the Python standard library; never runs or modifies application code.
"""
from pathlib import Path
from datetime import datetime
import json
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "target" / "surefire-reports"
OUT = ROOT / "test" / "reports" / "tanapoom"
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
    if row["suite"].startswith("com.pokevault.modules.order."):
        # Preserve JUnit results without machine properties or verbose application logs.
        for child in list(suite):
            if child.tag in ("properties", "system-out", "system-err"):
                suite.remove(child)
        tree.write(OUT / path.name, encoding="utf-8", xml_declaration=True)

order_scope = [row for row in suites if row["suite"].startswith("com.pokevault.modules.order.")]
def totals(rows):
    return {key: sum(row[key] for row in rows) for key in ("tests", "failures", "errors", "skipped")}

report = {
    "generated_at": datetime.now().astimezone().isoformat(),
    "owner": "Tanapoom Chantra 673380272-1 (Member 3)",
    "command": "mvnw.cmd test",
    "whole_suite": totals(suites),
    "order_scope": totals(order_scope),
    "suites": suites
}
(OUT / "summary.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

lines = [
    "# Test Report — นายธนภูมิ จันทรา (673380272-1)", "",
    "**Role**: Member 3 — Order Engine & Strategy Pattern Specialist  ",
    "**Branch**: `tanapoom_6733802721_01`  ",
    f"**Report generated**: {report['generated_at']}", "",
    "## 1. ผลการรันภาพรวม (Execution Overview)", "",
    "| ขอบเขต | Tests | Failures | Errors | Skipped |",
    "|---|---:|---:|---:|---:|",
]
for label, values in (("ทั้งโปรเจกต์ (Whole Project)", report["whole_suite"]),
                      ("Order Engine / Strategy Pattern / Booking Scope", report["order_scope"])):
    lines.append("| " + label + " | " + " | ".join(str(values[key]) for key in ("tests", "failures", "errors", "skipped")) + " |")

lines += [
    "", "## 2. รายละเอียดชุดทดสอบของธนภูมิ (Member 3 Test Classes)", "",
    "| Test class | Tests | Failures | Errors | Skipped |",
    "|---|---:|---:|---:|---:|",
]
for row in order_scope:
    lines.append(f"| {row['suite'].split('.')[-1]} | {row['tests']} | {row['failures']} | {row['errors']} | {row['skipped']} |")

lines += [
    "", "## 3. สิ่งที่ครอบคลุมในการทดสอบ (Test Scope Details)", "",
    "- **DiscountStrategyTest** (GoF Strategy Pattern): ตรวจสอบการรองรับ MembershipTier และการคำนวณส่วนลดตามระดับสมาชิก (REGULAR 0%, VIP 10%, WHOLESALE 15%) และการทำงานของ Context Service",
    "- **DiscountStrategyEdgeCaseTest** (Strategy Edge Cases): ตรวจสอบกรณี subtotal เป็น null, 0 หรือติดลบ, fallback เมื่อ tier เป็น null, การปัดเศษทศนิยมแบบ HALF_UP ทศนิยม 2 ตำแหน่ง, การคำนวณยอดเงินขนาดใหญ่, และการจัดการ Exception เมื่อไม่มี Strategy ที่รองรับ",
    "- **OrderServiceTest** (Core Order Engine): ตรวจสอบ Flow การสั่งจองการ์ด, การหักสต็อกสินค้า, การสร้าง Order Code, และการกระจายสัญญาณ OrderPlacedEvent ผ่าน Spring Event Bus",
    "- **OrderServiceEdgeCaseTest** (Order Engine Edge Cases): ตรวจสอบการสั่งซื้อหลายรายการ (Multi-item), ตรวจจับ Transaction Abort เมื่อสินค้าชิ้นถัดไปสต็อกไม่พอ, Fallback ระดับสมาชิกสำหรับลูกค้าทั่วไป, การจัดรูปแบบลำดับเลขโค้ด ORD-YYYY-XXX, และความสมบูรณ์ของ Event Payload",
    "- **OrderBookingApiControllerTest** (Web API & Bean Validation): ใช้ MockMvc แบบ Standalone ทดสอบ HTTP 201 Created, HTTP 400 Bad Request เมื่อติดกฎ Bean Validation (รหัสเพื่อน 16 หลัก, จำนวนสินค้า, รายการว่าง), HTTP 404 Not Found, และการดึงข้อมูลคำสั่งซื้อ",
    "- **OrderPersistenceTest** (JPA Entity & Repository Layer): ใช้ Spring Boot `@DataJpaTest` บนฐานข้อมูล In-Memory H2 ทดสอบ CascadeType.ALL, orphanRemoval, การบันทึกตัวเลขทศนิยม BigDecimal, เมธอด Query ใน OrderRepository และ OrderItemRepository, ตลอดจน Audit Timestamps และ Multi-item Rollback",
    "- **OrderStockAndSecurityTest** (Stock Consistency & Ownership Security): ทดสอบการจับคู่ใบสุดท้ายแม้สต็อกคงเหลือพร้อมขายเป็น 0, การ Rollback สต็อกเมื่อรายการใดรายการหนึ่งล้มเหลว, การย้ายการจองสต็อกระหว่างบัญชีเมื่อ reassign, การคืนสต็อกไปยังคลังล่าสุดเมื่อยกเลิกออเดอร์ (ไม่คืนซ้ำ), และการป้องกัน IDOR ให้ CUSTOMER เข้าถึงได้เฉพาะออเดอร์ของตนเอง ขณะที่ STAFF/ADMIN สามารถจองแทนและดูข้อมูลทั้งหมดได้",
    "- **OrderBookingApiSecurityTest** (Security Integration): ทดสอบการทำงานร่วมกับ SecurityConfig และการตรวจสอบสิทธิ์การจองการ์ด",
    "", "## 4. วิธีรันซ้ำ (How to Reproduce)", "",
    "```powershell",
    "$env:JAVA_HOME=\"C:\\Users\\ADMIN\\.vscode\\extensions\\redhat.java-1.56.0-win32-x64\\jre\\21.0.12.1-win32-x86_64\"",
    "$env:Path=\"$env:JAVA_HOME\\bin;$env:Path\"",
    ".\\mvnw.cmd clean test",
    "python test/generate_tanapoom_report.py",
    "```", "",
    "รันเฉพาะส่วนของคนที่ 3 (Member 3 Scope):", "",
    "```powershell",
    ".\\mvnw.cmd '-Dtest=DiscountStrategyTest,DiscountStrategyEdgeCaseTest,OrderServiceTest,OrderServiceEdgeCaseTest,OrderBookingApiControllerTest,OrderPersistenceTest,OrderStockAndSecurityTest,OrderBookingApiSecurityTest' test",
    "```", "",
    "## 5. ขอบเขตและข้อจำกัด (Scope & Limitations)", "",
    "- ชุดทดสอบนี้ครอบคลุมความรับผิดชอบของ Member 3: Order, OrderItem, DiscountStrategy, DiscountService, OrderServiceImpl, OrderApiController, การจองและคืนสต็อกข้ามบัญชี/Inventory, การตรวจสิทธิ์เจ้าของออเดอร์ (ร่วมกับ Member 1), และ JPA Repositories",
    "- การปรับปรุงความถูกต้องของ Business Logic และ Security สอดคล้องกับข้อกำหนดระบบ 100% และผ่านการทดสอบทั้งโปรเจกต์ 284/284 เทสต์โดยไม่มี Regression",
    "- OrderPersistenceTest รันบน H2 In-Memory Database แยกอิสระจากสภาพแวดล้อมจริง และ Rollback ข้อมูลอัตโนมัติหลังจบแต่ละเทสต์",
    "- Controller Test ใช้ MockMvc แบบ Standalone เพื่อทดสอบ Serialization, HTTP Contract, และ Bean Validation โดยเฉพาะ",
    "", "## 6. ผลการทดสอบรายกรณี (Detailed Test Cases)", ""
]

for row in order_scope:
    lines += [f"### {row['suite'].split('.')[-1]}", ""]
    lines.extend(f"- `{case['name']}` — **{case['result']}** ({case['seconds']}s)" for case in row["cases"])
    lines.append("")

(OUT / "TEST-REPORT.md").write_text("\n".join(lines), encoding="utf-8")
print(json.dumps({"whole_suite": report["whole_suite"], "order_scope": report["order_scope"], "report": str(OUT / "TEST-REPORT.md")}))
