-- Manual PostgreSQL migration for existing databases; NOT auto-executed by Spring.
-- Back up the database first. Run with psql -v ON_ERROR_STOP=1 -f <this-file>.
-- Invalid legacy values abort the transaction instead of being truncated or deleted.
BEGIN;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM cards WHERE length(card_number) > 20) THEN
        RAISE EXCEPTION 'Existing card_number exceeds 20 characters; correct those rows before migrating';
    END IF;
    IF EXISTS (SELECT 1 FROM user_profiles WHERE length(shipping_address) > 500 OR length(membership_tier) > 30) THEN
        RAISE EXCEPTION 'Existing profile address/tier exceeds the core schema limits; correct those rows first';
    END IF;
    IF EXISTS (SELECT 1 FROM card_expansions WHERE series IS NULL OR length(series) > 50 OR total_cards IS NULL) THEN
        RAISE EXCEPTION 'Existing expansion series/total_cards violates the core schema contract; correct those rows first';
    END IF;
END $$;

ALTER TABLE user_profiles ALTER COLUMN full_name DROP NOT NULL;
ALTER TABLE user_profiles ALTER COLUMN shipping_address TYPE VARCHAR(500);
ALTER TABLE user_profiles ALTER COLUMN membership_tier TYPE VARCHAR(30);
ALTER TABLE card_expansions ALTER COLUMN series TYPE VARCHAR(50);
ALTER TABLE card_expansions ALTER COLUMN series SET NOT NULL;
ALTER TABLE card_expansions ALTER COLUMN total_cards SET NOT NULL;
ALTER TABLE cards ALTER COLUMN card_number TYPE VARCHAR(20);

COMMIT;
