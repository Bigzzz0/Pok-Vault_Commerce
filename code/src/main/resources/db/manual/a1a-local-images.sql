-- Existing PostgreSQL databases only. Not auto-executed by Spring Boot.
-- Back up first, then: psql -v ON_ERROR_STOP=1 -f a1a-local-images.sql
-- Keeps card IDs and inventory/order references. Unique conflicts abort/rollback.
BEGIN;

UPDATE cards c
SET card_number = m.new_number,
    image_url = m.image_url,
    rarity = 'DOUBLE_RARE',
    updated_at = CURRENT_TIMESTAMP
FROM card_expansions e,
    (VALUES
        ('Mew ex', '026/086', '032/068', '/images/cards/A1a_032_EN.webp'),
        ('Celebi ex', '003/086', '003/068', '/images/cards/A1a_003_EN.webp'),
        ('Gyarados ex', '015/086', '018/068', '/images/cards/A1a_018_EN.webp'),
        ('Aerodactyl ex', '045/086', '046/068', '/images/cards/A1a_046_EN.webp')
    ) AS m(card_name, old_number, new_number, image_url)
WHERE c.expansion_id = e.id
    AND e.code = 'A1a'
    AND c.name = m.card_name
    AND c.card_number IN (m.old_number, m.new_number);

COMMIT;
