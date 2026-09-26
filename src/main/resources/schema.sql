-- ====================================================================
-- PokéVault Commerce - Database Schema Definition (DDL)
-- Compatible with PostgreSQL and H2 Database
-- ====================================================================

-- 1. Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email)
);

-- 2. Table: user_profiles
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    shipping_address VARCHAR(500),
    membership_tier VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    reward_points INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_profile_user UNIQUE (user_id),
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_profile_reward_points CHECK (reward_points >= 0)
);

-- 3. Table: card_expansions
CREATE TABLE IF NOT EXISTS card_expansions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    series VARCHAR(100),
    release_date DATE,
    total_cards INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_expansion_code UNIQUE (code),
    CONSTRAINT chk_expansion_total CHECK (total_cards > 0)
);

-- 4. Table: cards
CREATE TABLE IF NOT EXISTS cards (
    id BIGSERIAL PRIMARY KEY,
    expansion_id BIGINT NOT NULL,
    card_number VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    card_type VARCHAR(30) NOT NULL,
    rarity VARCHAR(30) NOT NULL,
    element_type VARCHAR(30),
    hp INTEGER,
    retreat_cost INTEGER,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_expansion FOREIGN KEY (expansion_id) REFERENCES card_expansions(id) ON DELETE RESTRICT,
    CONSTRAINT uk_card_expansion_number UNIQUE (expansion_id, card_number),
    CONSTRAINT chk_card_hp CHECK (hp >= 0),
    CONSTRAINT chk_card_retreat CHECK (retreat_cost >= 0)
);

-- 5. Table: game_accounts
CREATE TABLE IF NOT EXISTS game_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL,
    in_game_name VARCHAR(100) NOT NULL,
    friend_id VARCHAR(50) NOT NULL,
    trade_status VARCHAR(30) NOT NULL DEFAULT 'READY',
    buy_in_cost DECIMAL(10,2) DEFAULT 0.00,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_account_code UNIQUE (account_code),
    CONSTRAINT chk_account_buy_in CHECK (buy_in_cost >= 0.00)
);

-- 6. Table: card_inventories
CREATE TABLE IF NOT EXISTS card_inventories (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT NOT NULL,
    game_account_id BIGINT,
    card_condition VARCHAR(30) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    buy_in_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    selling_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    storage_slot VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_game_account FOREIGN KEY (game_account_id) REFERENCES game_accounts(id) ON DELETE SET NULL,
    CONSTRAINT chk_inventory_qty CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_buy_price CHECK (buy_in_price >= 0.00),
    CONSTRAINT chk_inventory_sell_price CHECK (selling_price >= 0.00)
);

-- 7. Table: orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    customer_friend_id VARCHAR(50),
    customer_in_game_name VARCHAR(100),
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_code UNIQUE (order_code),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_total CHECK (total_amount >= 0.00),
    CONSTRAINT chk_order_discount CHECK (discount_amount >= 0.00),
    CONSTRAINT chk_order_final CHECK (final_amount >= 0.00)
);

-- 8. Table: order_items
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    assigned_account_id BIGINT,
    trade_status VARCHAR(30) DEFAULT 'UNASSIGNED',
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_inventory FOREIGN KEY (inventory_id) REFERENCES card_inventories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_item_assigned_account FOREIGN KEY (assigned_account_id) REFERENCES game_accounts(id) ON DELETE SET NULL,
    CONSTRAINT chk_item_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_item_unit_price CHECK (unit_price >= 0.00),
    CONSTRAINT chk_item_subtotal CHECK (subtotal >= 0.00)
);
