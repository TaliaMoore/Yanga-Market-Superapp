-- ==========================================
-- Relational Database Schema Design
-- Target Entities: Locales, Customers, Manufacturers, Product Categories, Products, Shops, Wallet Transactions
-- ==========================================

-- 1. LOCALES TABLE
-- Stores City, State, and Zip code to ensure database normalization
CREATE TABLE locales (
    zip VARCHAR(20) PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. MANUFACTURERS TABLE
-- Represents original suppliers & manufacturing entities
CREATE TABLE manufacturers (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. PRODUCT CATEGORIES TABLE
-- Represents categories to differentiate between food, fruits, and shop items
CREATE TABLE product_categories (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. CUSTOMERS TABLE
-- Represents marketplace purchasers and superapp system users, linked to a locale via Zip code
CREATE TABLE customers (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    zip VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Relational foreign key constraint with SET NULL on delete
    CONSTRAINT fk_customer_locale
        FOREIGN KEY (zip)
        REFERENCES locales(zip)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- 5. PRODUCTS TABLE
-- Represents market inventories and digital offerings linked to a manufacturer and a category
CREATE TABLE products (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0.0),
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    manufacturer_id VARCHAR(100) NOT NULL,
    category_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relational foreign key constraint ensuring referential integrity with manufacturers
    CONSTRAINT fk_product_manufacturer
        FOREIGN KEY (manufacturer_id)
        REFERENCES manufacturers(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    -- Relational foreign key constraint ensuring referential integrity with categories
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES product_categories(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- 6. SHOPS TABLE
-- Represents retail shops integrated into the superapp, linked to a locale via Zip code
CREATE TABLE shops (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    distance_km DECIMAL(10, 2) NOT NULL,
    zip VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Relational foreign key constraint ensuring referential integrity
    CONSTRAINT fk_shop_locale
        FOREIGN KEY (zip)
        REFERENCES locales(zip)
        ON UPDATE CASCADE
        ON DELETE CASCADE
    );

-- 7. WALLET TRANSACTIONS TABLE
-- Tracks IDs for the customer, the amount (decimal), and the transaction type (Credit/Debit/FUND/PAYMENT)
CREATE TABLE wallet_transactions (
    id VARCHAR(100) PRIMARY KEY,
    customer_id VARCHAR(100),
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0.0),
    type VARCHAR(50) NOT NULL CHECK (type IN ('Credit', 'Debit', 'FUND', 'PAYMENT')),
    description VARCHAR(255) NOT NULL,
    timestamp BIGINT NOT NULL,
    security_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Relational foreign key constraint linking a transaction to a Customer
    CONSTRAINT fk_transaction_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- 8. WALLETS BALANCE STATE TABLE
-- Automatically managed by table triggers with strict database constraints maintaining balance >= 0.00
CREATE TABLE wallets (
    customer_id VARCHAR(100) PRIMARY KEY,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0.00),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- 9. WALLET AUTOMATIC LEDGER BALANCE TRIGGER
CREATE OR REPLACE FUNCTION update_wallet_balance()
RETURNS TRIGGER AS $$
DECLARE
    delta DECIMAL(15, 2);
BEGIN
    IF NEW.type IN ('Credit', 'FUND') THEN
        delta := NEW.amount;
    ELSE
        delta := -NEW.amount;
    END IF;

    -- Ensure a wallet row exists for the customer
    INSERT INTO wallets (customer_id, balance)
    VALUES (NEW.customer_id, 0.00)
    ON CONFLICT (customer_id) DO NOTHING;

    -- Update balance (which will trigger CHECK (balance >= 0.00))
    UPDATE wallets
    SET balance = balance + delta
    WHERE customer_id = NEW.customer_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_wallet_balance
AFTER INSERT OR UPDATE ON wallet_transactions
FOR EACH ROW
EXECUTE FUNCTION update_wallet_balance();

