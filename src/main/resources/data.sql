-- ====================================================================
-- PokéVault Commerce - Initial Seed Data (DML)
-- ====================================================================

-- 1. Seed Expansions
INSERT INTO card_expansions (id, code, name, series, release_date, total_cards, created_at, updated_at)
VALUES 
(1, 'A1', 'Genetic Apex', 'Genetic Apex', '2024-10-30', 226, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'A1a', 'Mythical Island', 'Mythical Island', '2024-12-17', 86, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Seed Pokémon Cards (22 Cards: Genetic Apex & Mythical Island)
INSERT INTO cards (id, expansion_id, card_number, name, card_type, rarity, element_type, hp, retreat_cost, image_url, created_at, updated_at)
VALUES
-- Charizard ex (Immersive)
(1, 1, '036/226', 'Charizard ex', 'POKEMON', 'IMMERSIVE_RARE', 'FIRE', 180, 2, 'https://assets.pokemon-zone.com/cards/a1/036.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Mewtwo ex (Immersive)
(2, 1, '129/226', 'Mewtwo ex', 'POKEMON', 'IMMERSIVE_RARE', 'PSYCHIC', 150, 2, 'https://assets.pokemon-zone.com/cards/a1/129.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Pikachu ex (Immersive)
(3, 1, '095/226', 'Pikachu ex', 'POKEMON', 'IMMERSIVE_RARE', 'LIGHTNING', 120, 1, 'https://assets.pokemon-zone.com/cards/a1/095.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Articuno ex
(4, 1, '084/226', 'Articuno ex', 'POKEMON', 'DOUBLE_RARE', 'WATER', 140, 2, 'https://assets.pokemon-zone.com/cards/a1/084.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Zapdos ex
(5, 1, '104/226', 'Zapdos ex', 'POKEMON', 'DOUBLE_RARE', 'LIGHTNING', 130, 1, 'https://assets.pokemon-zone.com/cards/a1/104.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Moltres ex
(6, 1, '047/226', 'Moltres ex', 'POKEMON', 'DOUBLE_RARE', 'FIRE', 140, 2, 'https://assets.pokemon-zone.com/cards/a1/047.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Venusaur ex
(7, 1, '004/226', 'Venusaur ex', 'POKEMON', 'DOUBLE_RARE', 'GRASS', 190, 3, 'https://assets.pokemon-zone.com/cards/a1/004.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Blastoise ex
(8, 1, '056/226', 'Blastoise ex', 'POKEMON', 'DOUBLE_RARE', 'WATER', 180, 3, 'https://assets.pokemon-zone.com/cards/a1/056.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Gengar ex
(9, 1, '143/226', 'Gengar ex', 'POKEMON', 'DOUBLE_RARE', 'PSYCHIC', 170, 2, 'https://assets.pokemon-zone.com/cards/a1/143.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Machamp ex
(10, 1, '154/226', 'Machamp ex', 'POKEMON', 'DOUBLE_RARE', 'FIGHTING', 180, 3, 'https://assets.pokemon-zone.com/cards/a1/154.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Dragonite
(11, 1, '184/226', 'Dragonite', 'POKEMON', 'SUPER_RARE', 'COLORLESS', 160, 3, 'https://assets.pokemon-zone.com/cards/a1/184.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Alakazam
(12, 1, '124/226', 'Alakazam', 'POKEMON', 'RARE', 'PSYCHIC', 130, 1, 'https://assets.pokemon-zone.com/cards/a1/124.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Gyarados
(13, 1, '062/226', 'Gyarados', 'POKEMON', 'RARE', 'WATER', 150, 3, 'https://assets.pokemon-zone.com/cards/a1/062.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Arcanine ex
(14, 1, '040/226', 'Arcanine ex', 'POKEMON', 'DOUBLE_RARE', 'FIRE', 150, 2, 'https://assets.pokemon-zone.com/cards/a1/040.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Starmie ex
(15, 1, '069/226', 'Starmie ex', 'POKEMON', 'DOUBLE_RARE', 'WATER', 130, 0, 'https://assets.pokemon-zone.com/cards/a1/069.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Marowak ex
(16, 1, '160/226', 'Marowak ex', 'POKEMON', 'DOUBLE_RARE', 'FIGHTING', 140, 1, 'https://assets.pokemon-zone.com/cards/a1/160.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Wigglytuff ex
(17, 1, '198/226', 'Wigglytuff ex', 'POKEMON', 'DOUBLE_RARE', 'COLORLESS', 140, 2, 'https://assets.pokemon-zone.com/cards/a1/198.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Sabrina Supporter
(18, 1, '225/226', 'Sabrina', 'TRAINER_SUPPORTER', 'SUPER_RARE', NULL, NULL, NULL, 'https://assets.pokemon-zone.com/cards/a1/225.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Mew ex (Mythical Island)
(19, 2, '026/086', 'Mew ex', 'POKEMON', 'IMMERSIVE_RARE', 'PSYCHIC', 130, 1, 'https://assets.pokemon-zone.com/cards/a1a/026.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Celebi ex (Mythical Island)
(20, 2, '003/086', 'Celebi ex', 'POKEMON', 'DOUBLE_RARE', 'GRASS', 130, 1, 'https://assets.pokemon-zone.com/cards/a1a/003.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Gyarados ex (Mythical Island)
(21, 2, '015/086', 'Gyarados ex', 'POKEMON', 'DOUBLE_RARE', 'WATER', 180, 3, 'https://assets.pokemon-zone.com/cards/a1a/015.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Aerodactyl ex (Mythical Island)
(22, 2, '045/086', 'Aerodactyl ex', 'POKEMON', 'DOUBLE_RARE', 'FIGHTING', 140, 1, 'https://assets.pokemon-zone.com/cards/a1a/045.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Seed Users
INSERT INTO users (id, username, email, password, role, created_at, updated_at)
VALUES
(1, 'admin', 'admin@pokevault.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKI2S', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'staff_ash', 'ash@pokevault.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKI2S', 'STAFF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'customer_red', 'red@kanto.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKI2S', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. Seed User Profiles
INSERT INTO user_profiles (id, user_id, full_name, phone_number, shipping_address, membership_tier, reward_points, created_at, updated_at)
VALUES
(1, 1, 'PokéVault Admin', '081-111-2222', 'Pallet Town HQ 101', 'WHOLESALE', 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'Ash Ketchum', '082-333-4444', 'Pallet Town Vault 02', 'REGULAR', 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 'Red Champion', '089-999-8888', 'Mt. Silver Cabin', 'VIP', 250, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
