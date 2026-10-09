"""Convert downloaded A1a images to WebP without resizing (requires Pillow).

Run from repository root: python code/tools/convert_a1a_webp.py
Original PNGs are retained locally in img/originals/A1a (ignored by Git/Docker).
"""
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timedelta, timezone
import hashlib
import json
from pathlib import Path

from PIL import Image, features

ROOT = Path(__file__).resolve().parents[2]
WEB = ROOT / 'img/web/cards'
ORIGINALS = ROOT / 'img/originals/A1a'
MANIFEST = ROOT / 'img/A1a-image-sources.json'
QUALITY = 90

if not features.check('webp'):
    raise SystemExit('Pillow needs WebP support.')
manifest = json.loads(MANIFEST.read_text(encoding='utf-8'))
if manifest['count'] != 86 or len(manifest['images']) != 86:
    raise SystemExit('Expected 86 A1a source records.')
ORIGINALS.mkdir(parents=True, exist_ok=True)

def convert(row):
    filename = f"A1a_{row['number']:03d}_EN.png"
    source = ORIGINALS / filename if (ORIGINALS / filename).exists() else WEB / filename
    original = ORIGINALS / filename
    target = WEB / Path(filename).with_suffix('.webp')
    for path in (source, original, target):
        if not path.resolve().is_relative_to(ROOT.resolve()):
            raise ValueError('Asset path outside repository.')
    raw = source.read_bytes()
    with Image.open(source) as image:
        image.load()
        dimensions = image.size
        has_alpha = 'A' in image.getbands() or 'transparency' in image.info
        encoded = image.convert('RGBA' if has_alpha else 'RGB')
        encoded.save(target, format='WEBP', quality=QUALITY, method=6)
    with Image.open(target) as check:
        check.load()
        if check.format != 'WEBP' or check.size != dimensions:
            raise ValueError('Unexpected output: ' + str(target))
    # Preserve original after successful encoding; never overwrite another original.
    if source != original:
        if original.exists():
            raise ValueError('Original archive destination already exists.')
        source.replace(original)
    data = target.read_bytes()
    return {**row, 'file': target.relative_to(ROOT).as_posix(),
            'bytes': len(data), 'sha256': hashlib.sha256(data).hexdigest(),
            'original_file': original.relative_to(ROOT).as_posix(),
            'original_bytes': len(raw), 'original_sha256': hashlib.sha256(raw).hexdigest(),
            'width': dimensions[0], 'height': dimensions[1]}

with ThreadPoolExecutor(max_workers=4) as pool:
    rows = list(pool.map(convert, manifest['images']))
manifest.update({'images': rows, 'format': 'WebP', 'conversion': {
    'converted_at': datetime.now(timezone(timedelta(hours=7))).isoformat(),
    'quality': QUALITY, 'method': 6, 'resized': False,
    'original_total_bytes': sum(row['original_bytes'] for row in rows),
    'webp_total_bytes': sum(row['bytes'] for row in rows)}})
MANIFEST.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
original_size = manifest['conversion']['original_total_bytes']
webp_size = manifest['conversion']['webp_total_bytes']
print(json.dumps({'converted': len(rows), 'original_mb': round(original_size / 1048576, 2),
                  'webp_mb': round(webp_size / 1048576, 2),
                  'reduction_percent': round((1 - webp_size / original_size) * 100, 2),
                  'resized': False}, ensure_ascii=False))
