"""Save one clean-verify run, then refresh member snapshots from the same XML.

This reads evidence; it never executes Maven or changes application code.
Usage: python test/generate_project_report.py --build-log PATH --exit-code 0
"""
import argparse
from datetime import datetime, timedelta, timezone
import hashlib
import json
import os
from pathlib import Path
import platform
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'test/reports/project'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--build-log', type=Path, required=True)
parser.add_argument('--exit-code', type=int, required=True)
args = parser.parse_args()
raw = args.build_log.read_bytes()
log = raw.decode('utf-16' if raw.startswith((b'\xff\xfe', b'\xfe\xff')) else 'utf-8', errors='replace')
matches = re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)(?:\r?\n|$)', log)
paths = sorted((ROOT / 'code/target/surefire-reports').glob('TEST-*.xml'))
if not matches or not paths:
    raise SystemExit('Missing Maven final totals or Surefire XML; run clean verify first.')
keys = ('tests', 'failures', 'errors', 'skipped')
suites = []
trees = []
for path in paths:
    tree = ET.parse(path)
    suite = tree.getroot()
    row = {'suite': suite.get('name'), **{k: int(suite.get(k, '0')) for k in keys}}
    row['seconds'] = float(suite.get('time', '0'))
    suites.append(row)
    for child in list(suite):
        if child.tag in ('properties', 'system-out', 'system-err'):
            suite.remove(child)
    trees.append((path.name, tree))
totals = {k: sum(s[k] for s in suites) for k in keys}
if tuple(totals.values()) != tuple(map(int, matches[-1])):
    raise SystemExit('Maven totals differ from XML; refuse to combine stale/mismatched results.')
if args.exit_code != 0 or '[INFO] BUILD SUCCESS' not in log or any(totals[k] for k in keys[1:]):
    raise SystemExit('This passing-run generator requires successful build, zero failures/errors/skips.')

def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT).decode('utf-8').strip()

java_home = os.environ.get('JAVA_HOME')
java = str(Path(java_home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java')) if java_home else 'java'
version = subprocess.run([java, '-version'], capture_output=True, text=True, check=True)
java_version = (version.stderr or version.stdout).strip()
# Hash current sources/config, including files moved but not yet committed.
source_paths = sorted(str(p.relative_to(ROOT)).replace('\\', '/') for folder in ('code/src', 'code/.mvn', 'test/java', 'test/resources', 'img/web') for p in (ROOT / folder).rglob('*') if p.is_file())
source_paths += ['code/pom.xml', 'code/mvnw', 'code/mvnw.cmd']
digest = hashlib.sha256()
for path in source_paths:
    digest.update(path.encode('utf-8') + b'\0' + (ROOT / path).read_bytes() + b'\0')
metadata = {
    'generated_at': datetime.now(timezone(timedelta(hours=7))).isoformat(),
    'timezone': 'Asia/Bangkok',
    'commit': git('rev-parse', 'HEAD'), 'branch': git('branch', '--show-current'),
    'command': 'code/mvnw.cmd -f code/pom.xml clean verify', 'exit_code': args.exit_code,
    'build': 'BUILD SUCCESS', 'platform': platform.platform(), 'java': java_version,
    'database': 'Local H2; includes H2 PostgreSQL mode tests, not a live PostgreSQL server',
    'tracked_source_sha256': digest.hexdigest(),
    'source_diff_from_commit': git('status', '--porcelain', '--', 'src', 'code', 'test/java', 'test/resources', 'img/web'),
    'maven_total_time': re.search(r'Total time:\s+([^\r\n]+)', log).group(1),
    'whole_suite': totals,
}
OUT.mkdir(parents=True, exist_ok=True)
for name, tree in trees:
    tree.write(OUT / name, encoding='utf-8', xml_declaration=True)
(OUT / 'summary.json').write_text(json.dumps({**metadata, 'suites': suites}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
(OUT / 'BUILD-SUMMARY.txt').write_text(
    'Command: code/mvnw.cmd -f code/pom.xml clean verify\nCommit: ' + metadata['commit'] + '\n'
    + 'Tests run: ' + str(totals['tests']) + ', Failures: 0, Errors: 0, Skipped: 0\n'
    + 'BUILD SUCCESS\nExit code: 0\nTotal time: ' + metadata['maven_total_time'] + '\n', encoding='utf-8')

evidence = f"""## หลักฐานรอบทดสอบล่าสุด

- Commit: `{metadata['commit']}`
- Branch: `{metadata['branch']}`
- วันที่บันทึก: {metadata['generated_at']} (Asia/Bangkok)
- คำสั่งจริง: `code/mvnw.cmd -f code/pom.xml clean verify` — exit code 0, **BUILD SUCCESS**
- ทั้งระบบ: **{totals['tests']} tests, 0 failures, 0 errors, 0 skipped**
- เวลารวม Maven: {metadata['maven_total_time']}
- ระบบ: {metadata['platform']}
- Java: `{java_version.splitlines()[0]}`
- Database: H2 local รวม H2 PostgreSQL mode; ไม่ได้เชื่อม PostgreSQL จริง
- Source/config ตรง commit: {'ใช่ (มีการแก้เอกสารและรายงานที่ยังไม่ commit)' if not metadata['source_diff_from_commit'] else 'มีความต่าง ดู summary.json'}
- SHA-256 ของ source/config ใน working tree: `{metadata['tracked_source_sha256']}`

"""
lines = ['# Project Test Report', '', evidence, '## ผลราย test suite', '',
         '| Suite | Tests | Failures | Errors | Skipped |', '|---|---:|---:|---:|---:|']
lines += ['| ' + row['suite'] + ' | ' + ' | '.join(str(row[k]) for k in keys) + ' |' for row in suites]
lines += ['', '## ขอบเขตและข้อจำกัด', '',
          '- Unit tests (Mockito), controller/security tests (MockMvc), JPA/H2 persistence และ schema tests ตาม suites ข้างต้น',
          '- ผลนี้เป็นการรันหนึ่งครั้งบน source ที่ระบุ ไม่ใช่ coverage 100% หรือการรับรองทุกสถานการณ์',
          '- ไม่ครอบคลุม browser UI, PostgreSQL จริง, manual migration และ public cloud deployment',
          '- รายงานสมาชิกมี scope ทับซ้อนกัน ห้ามบวกยอดรายสมาชิกเป็นยอดทั้งระบบ',
          '- XML ในโฟลเดอร์นี้ตัด machine properties และ application logs ออก; raw อยู่ code/target/surefire-reports',
          '- [สรุป JSON](summary.json), [Build Summary](BUILD-SUMMARY.txt)', '',
          '## รายงานรายสมาชิก', '',
          '- [ศิฆรินทร์](../sikarin/TEST-REPORT.md)', '- [สัพพัญญู](../sapphanyu/TEST-REPORT.md)',
          '- [ธนภูมิ](../tanapoom/TEST-REPORT.md)', '- [แทนคุณ](../tankun/TEST-REPORT.md)']
(OUT / 'TEST-REPORT.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')

for script, member in [('test/generate_sikarin_report.py', 'sikarin'),
                       ('test/generate_sapphanyu_report.py', 'sapphanyu'),
                       ('test/generate_tanapoom_report.py', 'tanapoom'),
                       ('test/generate_tankun_report.py', 'tankun')]:
    env = {**os.environ, 'PYTHONIOENCODING': 'utf-8'}
    subprocess.run([sys.executable, str(ROOT / script)], cwd=ROOT, env=env, check=True, capture_output=True)
    folder = OUT.parent / member
    summary = json.loads((folder / 'summary.json').read_text(encoding='utf-8-sig'))
    summary.update({k: v for k, v in metadata.items() if k != 'whole_suite'})
    (folder / 'summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    report = (folder / 'TEST-REPORT.md').read_text(encoding='utf-8-sig')
    title, rest = report.split('\n', 1)
    (folder / 'TEST-REPORT.md').write_text(title + '\n\n' + evidence + rest, encoding='utf-8')
print(json.dumps({'whole_suite': totals, 'commit': metadata['commit'], 'report': str(OUT / 'TEST-REPORT.md')}, ensure_ascii=False))
