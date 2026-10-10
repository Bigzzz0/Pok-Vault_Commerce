"""Join the successful customer/seller recording, removing setup and backend checks.

Usage: python render-demo-video.py output/20261010-184438 --speed 1.5
Requires ffmpeg and ffprobe on PATH. Does not open the app or modify orders.
"""
from pathlib import Path
from datetime import datetime, timedelta
import argparse
import json
import shutil
import math
import struct
import subprocess
import xml.etree.ElementTree as ET

p = argparse.ArgumentParser()
p.add_argument('run', type=Path)
p.add_argument('--speed', type=float, default=1.5)
args = p.parse_args()
if args.speed <= 0:
    p.error('--speed must be greater than zero')
ffmpeg = shutil.which('ffmpeg')
ffprobe = shutil.which('ffprobe')
if not ffmpeg or not ffprobe:
    p.error('Install ffmpeg/ffprobe or add them to PATH first')
root = ET.parse(args.run/'output.xml').getroot()
test = root.find('suite/test')
if test.find('status').get('status') != 'PASS':
    p.error('Only a successful demo run can be exported as a complete video')

def start(kw):
    return datetime.fromisoformat(kw.find('status').get('start'))

pages = [kw for kw in root.findall('.//kw') if kw.get('name') == 'New Page']
customer_origin, seller_origin = [start(kw) for kw in pages[:2]]
cues = [kw for kw in test.findall('kw') if kw.get('name') == 'Cue']
switches = [kw for kw in test.findall('kw') if kw.get('name') == 'Switch Page']
# Keep the brief backend verification in the customer view instead of flashing the seller browser.
seller_switch = [kw for kw in switches if kw.find('arg').text == '${SELLER_PAGE}'][-1]
customer_switch = [kw for kw in switches if kw.find('arg').text == '${CUSTOMER_PAGE}'][-1]
status = test.find('status')
end = datetime.fromisoformat(status.get('start')) + timedelta(seconds=float(status.get('elapsed')))
customer_video = next((args.run/'videos/customer').glob('*.webm'))
seller_video = next((args.run/'videos/seller').glob('*.webm'))
segments = [(customer_video, customer_origin, start(cues[0]), start(seller_switch)),
            (seller_video, seller_origin, start(seller_switch), start(customer_switch)),
            (customer_video, customer_origin, start(customer_switch), end)]
command = [ffmpeg, '-hide_banner', '-loglevel', 'error', '-y']
for video, *_ in segments:
    command += ['-i', str(video)]
filters = []
# Playwright pads recordings when a fullscreen monitor has a different aspect ratio.
# Recover the viewport from its PNG screenshot and crop only that padding.
shots = list((args.run/'browser/screenshot').glob('customer-completed.png'))
crop = None
target = (1920, 1080)
if shots:
    with shots[0].open('rb') as png:
        header = png.read(24)
    sw, sh = struct.unpack('>II', header[16:24])
    meta = json.loads(subprocess.check_output([ffprobe, '-v', 'error', '-show_entries',
                     'stream=width,height', '-of', 'json', str(customer_video)], text=True))
    vw, vh = meta['streams'][0]['width'], meta['streams'][0]['height']
    ratio = min(vw/sw, vh/sh, 1)
    target = (min(vw, math.ceil(sw*ratio/2)*2), min(vh, math.ceil(sh*ratio/2)*2))
    crop = f'crop={target[0]}:{target[1]}:0:0,'
for i, (_, origin, begin, finish) in enumerate(segments):
    a, b = (begin-origin).total_seconds(), (finish-origin).total_seconds()
    filters.append(f'[{i}:v]trim=start={a:.3f}:end={b:.3f},setpts=(PTS-STARTPTS)/{args.speed},{crop or ""}scale={target[0]}:{target[1]},setsar=1,fps=30[v{i}]')
filters.append('[v0][v1][v2]concat=n=3:v=1:a=0[out]')
output = args.run/f'demo-two-perspectives-{args.speed:g}x.mp4'
command += ['-filter_complex', ';'.join(filters), '-map', '[out]', '-an', '-c:v', 'libx264',
            '-preset', 'fast', '-crf', '22', '-pix_fmt', 'yuv420p', '-movflags', '+faststart', str(output)]
subprocess.run(command, check=True)
result = json.loads(subprocess.check_output([ffprobe, '-v', 'error', '-show_entries',
                    'format=duration:stream=width,height', '-of', 'json', str(output)], text=True))
print(json.dumps({'file':str(output.resolve()),'metadata':result}, ensure_ascii=False))
