# A1a card images

Source set: [Mythical Island (A1a), Limitless TCG Pocket](https://pocket.limitlesstcg.com/cards/A1a).
English WebP images are stored in `img/web/cards/A1a_NNN_EN.webp`, converted from the original PNGs at quality 90 without resizing.
Original PNGs are retained locally in `img/originals/A1a`, ignored by Git and Docker, and excluded from Maven runtime resources.
The source page, URL, file size and SHA-256 of every image are in [source manifest](../img/A1a-image-sources.json).
Maven packages these files into `static/images/cards`, served at `/images/cards/...`; the app does not request Limitless at runtime.

## Existing catalog mapping

| Card | Previous number | Correct number | Local file |
|---|---|---|---|
| Mew ex | 026/086 | 032/068 | A1a_032_EN.webp |
| Celebi ex | 003/086 | 003/068 | A1a_003_EN.webp |
| Gyarados ex | 015/086 | 018/068 | A1a_018_EN.webp |
| Aerodactyl ex | 045/086 | 046/068 | A1a_046_EN.webp |

The base set has 68 numbered cards; the full set listing includes 86 prints with alternate/secret cards.
Mew ex #32 has four-diamond rarity (`DOUBLE_RARE`); the old `IMMERSIVE_RARE` metadata was incorrect.
The 86 images are available as assets; this change wires the four existing catalog records and does not invent stock for all remaining cards.

Fresh databases load corrected rows from `data.sql`. Existing PostgreSQL databases need [manual update](../code/src/main/resources/db/manual/a1a-local-images.sql).
The script preserves card IDs and inventory/order links, is not auto-executed, and has not been run against the user's database.

Image source attribution: Limitless TCG Pocket. Card artwork/text belongs to The Pokémon Company, DeNA Co., Ltd., and/or Creatures, Inc., as stated by the source site.

## Recreate WebP assets

Requires Python and Pillow with WebP support. From repository root:

```powershell
python code/tools/convert_a1a_webp.py
```

The converter reads original PNGs from the local archive (or freshly downloaded PNGs in img/web/cards), validates output format/dimensions, and records source and output checksums in the manifest.
