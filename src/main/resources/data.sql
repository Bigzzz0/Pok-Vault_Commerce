-- ====================================================================
-- PokéVault Commerce - Initial Seed Data (DML)
-- Note: Primary Key IDs are omitted so the database sequence generator
-- automatically advances (prevents duplicate key conflicts in PostgreSQL)
-- ====================================================================

-- 1. Seed Expansions
INSERT INTO card_expansions (code, name, series, release_date, total_cards, created_at, updated_at)
VALUES 
('A1', 'Genetic Apex', 'Genetic Apex', '2024-10-30', 226, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('A1a', 'Mythical Island', 'Mythical Island', '2024-12-17', 86, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Seed Pokémon Cards (28 Cards: Genetic Apex & Mythical Island)
INSERT INTO cards (expansion_id, card_number, name, card_type, rarity, element_type, hp, retreat_cost, image_url, created_at, updated_at)
VALUES
-- Cards with a bundled image in static/images/cards (file name: {expansion}_{number}_EN.png);
-- number, rarity, HP and retreat cost follow what is printed on the card image
-- Charizard ex (Immersive, 3-star)
((SELECT id FROM card_expansions WHERE code = 'A1'), '280/226', 'Charizard ex', 'POKEMON', 'IMMERSIVE_RARE', 'FIRE', 180, 2, '/images/cards/A1_280_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Mewtwo ex (Crown)
((SELECT id FROM card_expansions WHERE code = 'A1'), '286/226', 'Mewtwo ex', 'POKEMON', 'CROWN_RARE', 'PSYCHIC', 150, 2, '/images/cards/A1_286_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Mewtwo ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '129/226', 'Mewtwo ex', 'POKEMON', 'DOUBLE_RARE', 'PSYCHIC', 150, 2, '/images/cards/A1_129_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Pikachu ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '096/226', 'Pikachu ex', 'POKEMON', 'DOUBLE_RARE', 'LIGHTNING', 120, 1, '/images/cards/A1_096_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Zapdos ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '104/226', 'Zapdos ex', 'POKEMON', 'DOUBLE_RARE', 'LIGHTNING', 130, 1, '/images/cards/A1_104_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Venusaur ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '004/226', 'Venusaur ex', 'POKEMON', 'DOUBLE_RARE', 'GRASS', 190, 3, '/images/cards/A1_004_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Charizard
((SELECT id FROM card_expansions WHERE code = 'A1'), '035/226', 'Charizard', 'POKEMON', 'RARE', 'FIRE', 150, 2, '/images/cards/A1_035_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Moltres
((SELECT id FROM card_expansions WHERE code = 'A1'), '046/226', 'Moltres', 'POKEMON', 'RARE', 'FIRE', 100, 1, '/images/cards/A1_046_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Beedrill
((SELECT id FROM card_expansions WHERE code = 'A1'), '010/226', 'Beedrill', 'POKEMON', 'RARE', 'GRASS', 120, 1, '/images/cards/A1_010_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Mr. Mime
((SELECT id FROM card_expansions WHERE code = 'A1'), '126/226', 'Mr. Mime', 'POKEMON', 'UNCOMMON', 'PSYCHIC', 80, 1, '/images/cards/A1_126_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Sabrina Supporter
((SELECT id FROM card_expansions WHERE code = 'A1'), '225/226', 'Sabrina', 'TRAINER_SUPPORTER', 'UNCOMMON', NULL, NULL, NULL, '/images/cards/A1_225_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Brock Supporter
((SELECT id FROM card_expansions WHERE code = 'A1'), '224/226', 'Brock', 'TRAINER_SUPPORTER', 'UNCOMMON', NULL, NULL, NULL, '/images/cards/A1_224_EN.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Cards without a bundled image (the web UI falls back to the card back)
-- Articuno ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '084/226', 'Articuno ex', 'POKEMON', 'DOUBLE_RARE', 'WATER', 140, 2, 'https://assets.pokemon-zone.com/cards/a1/084.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Moltres ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '047/226', 'Moltres ex', 'POKEMON', 'DOUBLE_RARE', 'FIRE', 140, 2, 'https://assets.pokemon-zone.com/cards/a1/047.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Blastoise ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '056/226', 'Blastoise ex', 'POKEMON', 'DOUBLE_RARE', 'WATER', 180, 3, 'https://assets.pokemon-zone.com/cards/a1/056.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Gengar ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '143/226', 'Gengar ex', 'POKEMON', 'DOUBLE_RARE', 'PSYCHIC', 170, 2, 'https://assets.pokemon-zone.com/cards/a1/143.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Machamp ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '154/226', 'Machamp ex', 'POKEMON', 'DOUBLE_RARE', 'FIGHTING', 180, 3, 'https://assets.pokemon-zone.com/cards/a1/154.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Dragonite
((SELECT id FROM card_expansions WHERE code = 'A1'), '184/226', 'Dragonite', 'POKEMON', 'SUPER_RARE', 'COLORLESS', 160, 3, 'https://assets.pokemon-zone.com/cards/a1/184.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Alakazam
((SELECT id FROM card_expansions WHERE code = 'A1'), '124/226', 'Alakazam', 'POKEMON', 'RARE', 'PSYCHIC', 130, 1, 'https://assets.pokemon-zone.com/cards/a1/124.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Gyarados
((SELECT id FROM card_expansions WHERE code = 'A1'), '062/226', 'Gyarados', 'POKEMON', 'RARE', 'WATER', 150, 3, 'https://assets.pokemon-zone.com/cards/a1/062.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Arcanine ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '040/226', 'Arcanine ex', 'POKEMON', 'DOUBLE_RARE', 'FIRE', 150, 2, 'https://assets.pokemon-zone.com/cards/a1/040.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Starmie ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '069/226', 'Starmie ex', 'POKEMON', 'DOUBLE_RARE', 'WATER', 130, 0, 'https://assets.pokemon-zone.com/cards/a1/069.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Marowak ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '160/226', 'Marowak ex', 'POKEMON', 'DOUBLE_RARE', 'FIGHTING', 140, 1, 'https://assets.pokemon-zone.com/cards/a1/160.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Wigglytuff ex
((SELECT id FROM card_expansions WHERE code = 'A1'), '198/226', 'Wigglytuff ex', 'POKEMON', 'DOUBLE_RARE', 'COLORLESS', 140, 2, 'https://assets.pokemon-zone.com/cards/a1/198.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Mew ex (Mythical Island)
((SELECT id FROM card_expansions WHERE code = 'A1a'), '026/086', 'Mew ex', 'POKEMON', 'IMMERSIVE_RARE', 'PSYCHIC', 130, 1, 'https://assets.pokemon-zone.com/cards/a1a/026.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Celebi ex (Mythical Island)
((SELECT id FROM card_expansions WHERE code = 'A1a'), '003/086', 'Celebi ex', 'POKEMON', 'DOUBLE_RARE', 'GRASS', 130, 1, 'https://assets.pokemon-zone.com/cards/a1a/003.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Gyarados ex (Mythical Island)
((SELECT id FROM card_expansions WHERE code = 'A1a'), '015/086', 'Gyarados ex', 'POKEMON', 'DOUBLE_RARE', 'WATER', 180, 3, 'https://assets.pokemon-zone.com/cards/a1a/015.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Aerodactyl ex (Mythical Island)
((SELECT id FROM card_expansions WHERE code = 'A1a'), '045/086', 'Aerodactyl ex', 'POKEMON', 'DOUBLE_RARE', 'FIGHTING', 140, 1, 'https://assets.pokemon-zone.com/cards/a1a/045.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Seed Users
INSERT INTO users (username, email, password, role, created_at, updated_at)
VALUES
('admin', 'admin@pokevault.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKI2S', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('staff_ash', 'ash@pokevault.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKI2S', 'STAFF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('customer_red', 'red@kanto.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKI2S', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. Seed User Profiles
INSERT INTO user_profiles (user_id, full_name, phone_number, shipping_address, membership_tier, reward_points, created_at, updated_at)
VALUES
((SELECT id FROM users WHERE username = 'admin'), 'PokéVault Admin', '081-111-2222', 'Pallet Town HQ 101', 'WHOLESALE', 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
((SELECT id FROM users WHERE username = 'staff_ash'), 'Ash Ketchum', '082-333-4444', 'Pallet Town Vault 02', 'REGULAR', 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
((SELECT id FROM users WHERE username = 'customer_red'), 'Red Champion', '089-999-8888', 'Mt. Silver Cabin', 'VIP', 250, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
