require('dotenv').config();
const express = require('express');
const app = express();

// Load port and database connection credentials from process.env
const PORT = process.env.PORT || 3000;

const parseDatabaseUrl = (url) => {
    if (!url) return null;
    try {
        // Simple regex or URL parser to extract info from standard postgres://user:pass@host:port/db URL
        const parsed = new URL(url);
        return {
            host: parsed.hostname,
            port: parsed.port || '5432',
            user: parsed.username,
            password: parsed.password,
            database: parsed.pathname.substring(1) // remove leading /
        };
    } catch (e) {
        return null;
    }
};

const databaseUrlConfig = parseDatabaseUrl(process.env.DATABASE_URL);

const dbConfig = {
    host: databaseUrlConfig?.host || process.env.DB_HOST || 'localhost',
    port: databaseUrlConfig?.port || process.env.DB_PORT || 5432,
    user: databaseUrlConfig?.user || process.env.DB_USER || 'postgres',
    password: databaseUrlConfig?.password || process.env.DB_PASSWORD || 'password',
    database: databaseUrlConfig?.database || process.env.DB_NAME || 'yanga_market_db'
};

const { Pool } = require('pg');

// Initialize Connection Pool for PostgreSQL/Supabase
let pool = null;
try {
    if (process.env.DATABASE_URL && process.env.DATABASE_URL !== 'placeholder_string') {
        pool = new Pool({
            connectionString: process.env.DATABASE_URL,
            ssl: { rejectUnauthorized: false } // Crucial for Supabase and other managed cloud DBs
        });
        console.log("PostgreSQL connection pool initialized with DATABASE_URL");
    } else if (process.env.DB_HOST && process.env.DB_HOST !== 'localhost') {
        pool = new Pool({
            host: dbConfig.host,
            port: dbConfig.port,
            user: dbConfig.user,
            password: dbConfig.password,
            database: dbConfig.database,
            ssl: { rejectUnauthorized: false }
        });
        console.log(`PostgreSQL connection pool initialized with Host: ${dbConfig.host}`);
    } else {
        // Fallback or local dev without SSL rejection bypass
        pool = new Pool({
            host: dbConfig.host,
            port: dbConfig.port,
            user: dbConfig.user,
            password: dbConfig.password,
            database: dbConfig.database
        });
        console.log("PostgreSQL pool initialized locally");
    }
} catch (err) {
    console.error("Failed to initialize database connection pool:", err.message);
}

// Auto-initialize Quotes table & seeds
async function initDatabase() {
    try {
        if (!pool) return;
        
        // Create table quotes if it doesn't exist
        await executeQuery(`
            CREATE TABLE IF NOT EXISTS quotes (
                id VARCHAR(100) PRIMARY KEY,
                author VARCHAR(255) NOT NULL,
                text TEXT NOT NULL,
                category VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);
        console.log("Quotes table verified/created in database");

        // Seed if table is empty
        const countRes = await executeQuery("SELECT COUNT(*)::int as count FROM quotes");
        if (countRes.rows[0].count === 0) {
            const initialQuotes = [
                { id: "Q-01", author: "Balogun Merchant", text: "Style is the signature of your soul. Make it bold, make it loud.", category: "Fashion" },
                { id: "Q-02", author: "Alaba Tech Guru", text: "A powerbank in hand is worth two in the shop. Stay fully charged always.", category: "Electronics" },
                { id: "Q-03", author: "Mega Rite Grocer", text: "Quality is not an act, it is a habit of parboiled premium choice.", category: "Groceries" },
                { id: "Q-04", author: "Yanga CEO", text: "No matter how fast a cheetah runs, it cannot catch a business built on trust.", category: "Business" },
                { id: "Q-05", author: "Lagos Vibe Master", text: "Work hard, but make sure you have enough Jollof for the road.", category: "Motivation" }
            ];
            
            for (const q of initialQuotes) {
                await executeQuery(
                    "INSERT INTO quotes(id, author, text, category) VALUES($1, $2, $3, $4)",
                    [q.id, q.author, q.text, q.category]
                );
            }
            console.log("Seeded database with custom Yanga starter quotes");
        }

        // Create table product_categories if it doesn't exist
        await executeQuery(`
            CREATE TABLE IF NOT EXISTS product_categories (
                id VARCHAR(100) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);
        console.log("Product categories table verified/created in database");

        // Seed product_categories if empty
        const catCountRes = await executeQuery("SELECT COUNT(*)::int as count FROM product_categories");
        if (catCountRes.rows[0].count === 0) {
            const categoriesToSeed = [
                { id: "CAT-FOOD", name: "Food" },
                { id: "CAT-FRUITS", name: "Fruits" },
                { id: "CAT-SHOP", name: "Shop" }
            ];
            for (const cat of categoriesToSeed) {
                await executeQuery(
                    "INSERT INTO product_categories (id, name) VALUES ($1, $2)",
                    [cat.id, cat.name]
                );
            }
            console.log("Seeded database with core Yanga product categories ('Food', 'Fruits', 'Shop')");
        }

        // Create table product if it doesn't exist
        await executeQuery(`
            CREATE TABLE IF NOT EXISTS product (
                product_id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
                price DECIMAL(9, 2) NOT NULL CHECK (price >= 0.0),
                category_id VARCHAR(100) REFERENCES product_categories(id) ON UPDATE CASCADE ON DELETE SET NULL
            )
        `);
        console.log("Product inventory table (singular) verified/created in database");

        // Dynamically alter existing product table to ensure category_id is available if table was already created
        try {
            await executeQuery(`
                ALTER TABLE product ADD COLUMN IF NOT EXISTS category_id VARCHAR(100) REFERENCES product_categories(id) ON UPDATE CASCADE ON DELETE SET NULL;
            `);
            console.log("Product table structure verified (category_id column checked/added)");
        } catch (alterErr) {
            console.log("Information: product table extension column already verified or modified.");
        }

        // Create table locales if it doesn't exist
        await executeQuery(`
            CREATE TABLE IF NOT EXISTS locales (
                zip VARCHAR(20) PRIMARY KEY,
                city VARCHAR(100) NOT NULL,
                state VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);

        // Create table customers if it doesn't exist
        await executeQuery(`
            CREATE TABLE IF NOT EXISTS customers (
                id VARCHAR(100) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                phone VARCHAR(30) NOT NULL,
                address VARCHAR(255) NOT NULL,
                zip VARCHAR(20),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_customer_locale FOREIGN KEY (zip) REFERENCES locales(zip) ON UPDATE CASCADE ON DELETE SET NULL
            )
        `);

        // Create table wallet_transactions if it doesn't exist
        await executeQuery(`
            CREATE TABLE IF NOT EXISTS wallet_transactions (
                id VARCHAR(100) PRIMARY KEY,
                customer_id VARCHAR(100),
                amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0.0),
                type VARCHAR(50) NOT NULL CHECK (type IN ('Credit', 'Debit', 'FUND', 'PAYMENT')),
                description VARCHAR(255) NOT NULL,
                timestamp BIGINT NOT NULL,
                security_hash VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_transaction_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON UPDATE CASCADE ON DELETE SET NULL
            )
        `);

        // Create table wallets if it doesn't exist to store atomic balances with CHECK constraint
        await executeQuery(`
            CREATE TABLE IF NOT EXISTS wallets (
                customer_id VARCHAR(100) PRIMARY KEY REFERENCES customers(id) ON UPDATE CASCADE ON DELETE CASCADE,
                balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0.00),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);

        // Create database trigger function to update wallet balance on every transaction insertion
        await executeQuery(`
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

                -- Ensure wallets record exists
                INSERT INTO wallets (customer_id, balance)
                VALUES (NEW.customer_id, 0.00)
                ON CONFLICT (customer_id) DO NOTHING;

                -- Update wallets balance, which will enforce the CHECK(balance >= 0.00) constraint database-side
                UPDATE wallets
                SET balance = balance + delta
                WHERE customer_id = NEW.customer_id;

                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
        `);

        // Recreate the database trigger cleanly
        await executeQuery(`
            DROP TRIGGER IF EXISTS trg_update_wallet_balance ON wallet_transactions;
        `);
        await executeQuery(`
            CREATE TRIGGER trg_update_wallet_balance
            AFTER INSERT OR UPDATE ON wallet_transactions
            FOR EACH ROW
            EXECUTE FUNCTION update_wallet_balance();
        `);

        console.log("Wallet ledger database schema verified/created in database");

        // Seed default customer if empty
        const customerCountRes = await executeQuery("SELECT COUNT(*)::int as count FROM customers");
        if (customerCountRes.rows[0].count === 0) {
            await executeQuery("INSERT INTO locales (zip, city, state) VALUES ($1, $2, $3) ON CONFLICT (zip) DO NOTHING", ["100001", "Gbagada", "Lagos State"]);
            await executeQuery(`
                INSERT INTO customers (id, name, email, phone, address, zip)
                VALUES ($1, $2, $3, $4, $5, $6)
            `, ["CUST-01", "Eniola Agbeyindo", "eniolaagbeyindo@gmail.com", "+2348012345678", "12 Yanga Street, Gbagada, Lagos", "100001"]);
            console.log("Seeded basic customer account CUST-01 inside PostgreSQL");
            
            // Give ₦150,000 baseline starting balance so checkout does not fail if they play immediately
            const fundTxId = `TXN-FUND-SEEDED`;
            await executeQuery(`
                INSERT INTO wallet_transactions (id, customer_id, amount, type, description, timestamp, security_hash)
                VALUES ($1, $2, $3, $4, $5, $6, $7)
            `, [fundTxId, "CUST-01", 150000.0, "FUND", "Starting Promotional Wallet Gift", Date.now(), "SHA-SEED-PROMO"]);
            console.log("Provisioned ₦150,000 welcome credit for transaction checks");
        }
    } catch (err) {
        console.error("Failed to initialize system tables/seeds:", err.message);
    }
}

// Call initialization
setTimeout(initDatabase, 1500);

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ==========================================================
// RUNTIME MEMORY REPOSITORIES (Active Local Fallback Seeds)
// ==========================================================
const mockFoods = [
    { name: "Jollof Rice & Spicy Chicken", price: 2200.0, category: "Meals", description: "Classic smoky Nigerian Jollof rice served with grilled peppered chicken and plantains.", isFruit: false },
    { name: "Yam Fries & Pepper Sauce", price: 1500.0, category: "Snacks", description: "Vibrant crispy golden yam fries served with traditional hot chili pepper sauce.", isFruit: false },
    { name: "Pounded Yam with Egusi Soup", price: 2800.0, category: "Traditional", description: "Classic heavy pounded yam paste served with rich, delicious Egusi (melon seed) soup with assorted meat.", isFruit: false },
    { name: "Suya Spiced Beef Burger", price: 1900.0, category: "Burgers", description: "Juicy flame-grilled double-patty beef burger rubbed with authentic Hausa suya spice blend.", isFruit: false },
    { name: "Exotic Fruit Bowl Platter", price: 1200.0, category: "Fruits", description: "Vibrant mixture of fresh Pawpaw, Mango, Pineapple, Watermelon, and Mint.", isFruit: true },
    { name: "Sweet Pawpaw & Mango Slices", price: 950.0, category: "Fruits", description: "Succulent ripe organic pawpaw paired with premium sweet mango chunks.", isFruit: true },
    { name: "Avocado & Banana Smoothie Pack", price: 1350.0, category: "Fruits", description: "Ready-to-blend mix of fresh green butter avocados and sweet local yellow bananas.", isFruit: true }
];

const mockShops = [
    {
        name: "Balogun Fashion World",
        specialty: "Fashion & Attire",
        distanceKm: 1.2,
        items: [
            { name: "Ankara Playful Summer Dress", price: 4500.0, category: "Clothing" },
            { name: "Custom Velvet Agbada Set", price: 18500.0, category: "Luxury" },
            { name: "Yanga Custom Purple Socks", price: 800.0, category: "Accessories" }
        ]
    },
    {
        name: "Alaba Tech Gadgets Hub",
        specialty: "Electronics",
        distanceKm: 3.6,
        items: [
            { name: "Playful LED RGB Headphones", price: 12000.0, category: "Audio" },
            { name: "10000mAh Compact Powerbank", price: 6500.0, category: "Power" },
            { name: "USB-C Braided Fast Charger", price: 2500.0, category: "Cables" }
        ]
    },
    {
        name: "Mega Rite Supermarket",
        specialty: "Groceries & Kitchen",
        distanceKm: 0.5,
        items: [
            { name: "Yanga Premium Chocolate Bark", price: 2200.0, category: "Snack" },
            { name: "1kg Long Grain Parboiled Rice", price: 3400.0, category: "Pantry" },
            { name: "Fresh Organic Coconut Oil", price: 1800.0, category: "Kitchen" }
        ]
    }
];

const mockEvents = [
    { title: "Yanga Vibes Festival & Concert", host: "Yanga Entertainment", date: "June 25, 2026", time: "18:00", venue: "Eko Atlantic, VI", price: 5000.0, rsvpCount: 284 },
    { title: "Tech & Suya Networking Night", host: "Lagos Techies Cohort", date: "June 29, 2026", time: "17:30", venue: "The Zone Tech Hub, Gbagada", price: 0.0, rsvpCount: 145 },
    { title: "Culinary African Food Expo 2026", host: "Chow Chefs Initiative", date: "July 03, 2026", time: "11:00", venue: "Landmark Centre, Victoria Island", price: 2500.0, rsvpCount: 92 }
];

const mockHospitals = [
    { name: "St. Nicholas Premium Hospital", location: "Campus Square, Lagos Island", distanceKm: 1.1, specialties: ["General Wellness", "Pediatrics", "Post-natal Diagnostics", "Cardiology"] },
    { name: "Reddington Multi-Specialist Clinic", location: "Adetokunbo Ademola St, VI", distanceKm: 2.4, specialties: ["Emergency General Medicine", "Dental Surgery", "Advanced Ultrasound Lab"] },
    { name: "Evercare Hospital Lekki", location: "Lekki Phase 1, Lagos", distanceKm: 4.8, specialties: ["MRI & Lab Radiography", "Immunization", "Ophthalmology"] }
];

// Helper wrapper to run postgres queries with try-catch fallback
async function executeQuery(queryText, params = []) {
    if (!pool) throw new Error("Database pool is offline");
    return await pool.query(queryText, params);
}

// ==========================================================
// 1. BASELINE STATUS ROUTE
// ==========================================================
app.get('/', (req, res) => {
    res.json({
        message: "Welcome to Yanga Market Backend API!",
        status: "Running",
        environment: process.env.NODE_ENV || 'development',
        database: {
            host: dbConfig.host,
            user: dbConfig.user,
            port: dbConfig.port,
            database: dbConfig.database,
            password_configured: !!process.env.DB_PASSWORD,
            pool_connected: !!pool
        }
    });
});

// ==========================================================
// 2. FOOD MODULE ROUTES (/food)
// ==========================================================
app.get('/food', async (req, res) => {
    try {
        let items = [];
        try {
            // Relational fetch querying products categorized alongside supplier context
            const result = await executeQuery(`
                SELECT p.id, p.name, p.price::float as price, p.quantity, 
                       c.name as category, m.name as manufacturer 
                FROM products p
                JOIN product_categories c ON p.category_id = c.id
                JOIN manufacturers m ON p.manufacturer_id = m.id
                WHERE LOWER(c.name) LIKE '%food%' OR LOWER(c.name) LIKE '%meals%' OR LOWER(c.name) LIKE '%fruits%'
            `);
            if (result.rows.length > 0) {
                items = result.rows.map(row => ({
                    name: row.name,
                    price: row.price,
                    category: row.category,
                    description: `Supplied by ${row.manufacturer}. Stock level: ${row.quantity}`,
                    isFruit: row.category.toLowerCase().includes('fruit')
                }));
            }
        } catch (dbErr) {
            console.log("Postgres /food query failed, serving loaded run-time memory catalog instead:", dbErr.message);
        }

        // Fallback to seeded items if db returns nothing or throws
        if (items.length === 0) {
            items = [...mockFoods];
        }

        // Apply filters locally (if any)
        if (req.query.isFruit !== undefined) {
            const isFruitQuery = req.query.isFruit === 'true';
            items = items.filter(f => f.isFruit === isFruitQuery);
        }
        if (req.query.category) {
            items = items.filter(f => f.category.toLowerCase() === req.query.category.toLowerCase());
        }

        res.json({ success: true, count: items.length, data: items, origin: items === mockFoods ? "memory_fallback" : "database_pool" });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

app.post('/food', async (req, res) => {
    const { name, price, category, description, isFruit } = req.body;
    if (!name || !price || !category) {
        return res.status(400).json({ success: false, error: "Missing required fields: name, price, or category." });
    }

    try {
        const newItem = {
            name,
            price: parseFloat(price),
            category,
            description: description || "Tasty traditional delicacy made fresh for you.",
            isFruit: !!isFruit
        };

        // Try writing to database if configured
        let dbSaved = false;
        try {
            // First, find or seed a category and manufacturer representing this food
            const catResult = await executeQuery("SELECT id FROM product_categories WHERE LOWER(name) = $1 LIMIT 1", [category.toLowerCase()]);
            const catId = catResult.rows[0]?.id || "CAT-FOOD";
            
            const manResult = await executeQuery("SELECT id FROM manufacturers LIMIT 1");
            const manId = manResult.rows[0]?.id || "MANF-LOCAL";

            const newId = `PROD-${Date.now().toString().slice(-6)}`;
            await executeQuery(`
                INSERT INTO products (id, name, price, quantity, manufacturer_id, category_id)
                VALUES ($1, $2, $3, $4, $5, $6)
            `, [newId, name, parseFloat(price), 100, manId, catId]);
            dbSaved = true;
        } catch (dbErr) {
            console.log("Could not save food item to postgres database, persisting to memory:", dbErr.message);
        }

        // Always push to in-memory array so fallback is immediately updated too
        mockFoods.push(newItem);

        res.status(201).json({
            success: true,
            message: "Food item successfully created!",
            data: newItem,
            persisted: dbSaved ? "postgres_database" : "memory_only"
        });
    } catch (err) {
        res.status(400).json({ success: false, error: err.message });
    }
});

// ==========================================================
// 2B. MENU CHECKER SEARCH API
// ==========================================================
app.get('/food/check-menu', async (req, res) => {
    const rawQuery = (req.query.q || '').trim();
    if (!rawQuery) {
        return res.status(400).json({ success: false, error: "Please enter an item name to search on our menu." });
    }

    let menuItems = [];
    try {
        if (pool) {
            // Retrieve dynamic foods/fruits/snacks from database
            const result = await executeQuery(`
                SELECT p.name, p.price::float as price, COALESCE(c.name, 'General') as category, 'Live Fresh Supplier' as description
                FROM products p
                LEFT JOIN product_categories c ON p.category_id = c.id
                WHERE LOWER(c.name) LIKE '%food%' 
                   OR LOWER(c.name) LIKE '%meals%' 
                   OR LOWER(c.name) LIKE '%fruits%'
            `);
            if (result.rows.length > 0) {
                menuItems = result.rows.map(row => ({
                    name: row.name,
                    price: row.price,
                    category: row.category,
                    description: row.description
                }));
            }
        }
    } catch (e) {
        console.log("Postgres read during check-menu fell back to standard catalog:", e.message);
    }

    // Combine database list with static starter list for maximum resilience
    const combinedMenus = [...menuItems];
    
    // De-duplicate mockFoods by name
    mockFoods.forEach(mockItem => {
        if (!combinedMenus.some(item => item.name.toLowerCase() === mockItem.name.toLowerCase())) {
            combinedMenus.push({
                name: mockItem.name,
                price: mockItem.price,
                category: mockItem.category || "General",
                description: mockItem.description || "Tasty traditional delicacy made fresh for you.",
                isFruit: mockItem.isFruit
            });
        }
    });

    const query = rawQuery.toLowerCase();
    
    // Matching logic comparing user requests against the day's available menu items
    const matchedItem = combinedMenus.find(item => 
        item.name.toLowerCase().includes(query) || 
        query.includes(item.name.toLowerCase())
    );

    if (matchedItem) {
        return res.json({
            success: true,
            status: "found",
            message: "Excellent choice!",
            suggestion: rawQuery,
            data: matchedItem
        });
    } else {
        return res.status(404).json({
            success: false,
            status: "not_found",
            message: `The item "${rawQuery}" is not on today's menu. Try searching for Jollof, Yam Fries, Egusi or Slices!`,
            suggestion: rawQuery
        });
    }
});

// ==========================================================
// 3. EVENTS MODULE ROUTES (/events)
// ==========================================================
app.get('/events', async (req, res) => {
    try {
        let events = [];
        try {
            // Check if standard table exists and fetch
            const result = await executeQuery("SELECT id, title, host, event_date as date, event_time as time, venue, price::float as price, rsvp_count as rsvpCount FROM events_catalog");
            if (result.rows.length > 0) {
                events = result.rows;
            }
        } catch (dbErr) {
            console.log("Postgres /events query failed, serving loaded run-time memory catalog instead:", dbErr.message);
        }

        if (events.length === 0) {
            events = [...mockEvents];
        }

        // Apply filters
        if (req.query.free === 'true') {
            events = events.filter(e => e.price === 0.0);
        }
        if (req.query.host) {
            events = events.filter(e => e.host.toLowerCase().includes(req.query.host.toLowerCase()));
        }

        res.json({ success: true, count: events.length, data: events, origin: events === mockEvents ? "memory_fallback" : "database_pool" });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

app.post('/events', async (req, res) => {
    const { title, host, date, time, venue, price, rsvpCount } = req.body;
    if (!title || !host || !venue) {
        return res.status(400).json({ success: false, error: "Missing required fields: title, host, and venue are mandatory." });
    }

    try {
        const newEvent = {
            title,
            host,
            date: date || "June 30, 2026",
            time: time || "12:00",
            venue,
            price: price ? parseFloat(price) : 0.0,
            rsvpCount: rsvpCount ? parseInt(rsvpCount) : 0
        };

        // Try to insert in postgreSQL events catalog table
        let dbSaved = false;
        try {
            await executeQuery(`
                INSERT INTO events_catalog (id, title, host, event_date, event_time, venue, price, rsvp_count)
                VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
            `, [`EVT-${Date.now().toString().slice(-6)}`, title, host, newEvent.date, newEvent.time, venue, newEvent.price, newEvent.rsvpCount]);
            dbSaved = true;
        } catch (dbErr) {
            console.log("Failed to insert event into database, persisting to local runtime storage:", dbErr.message);
        }

        mockEvents.push(newEvent);

        res.status(201).json({
            success: true,
            message: "Event registered successfully!",
            data: newEvent,
            persisted: dbSaved ? "postgres_database" : "memory_only"
        });
    } catch (err) {
        res.status(400).json({ success: false, error: err.message });
    }
});

// ==========================================================
// 4. SHOPS MODULE ROUTES (/shops)
// ==========================================================
app.get('/shops', async (req, res) => {
    try {
        let shops = [];
        try {
            const result = await executeQuery(`
                SELECT s.id, s.name, s.specialty, s.distance_km::float as distance_km, s.zip,
                       l.city, l.state
                FROM shops s
                JOIN locales l ON s.zip = l.zip
            `);
            if (result.rows.length > 0) {
                // Populate shops with their custom sub-items or simple metadata
                shops = result.rows.map(row => ({
                    name: row.name,
                    specialty: row.specialty,
                    distanceKm: row.distance_km,
                    zip: row.zip,
                    location: `${row.city}, ${row.state}`,
                    items: [
                        { name: "Superapp Premium Promo Voucher", price: 500.0, category: "Deals" }
                    ]
                }));
            }
        } catch (dbErr) {
            console.log("Postgres /shops query failed, serving loaded run-time memory catalog instead:", dbErr.message);
        }

        if (shops.length === 0) {
            shops = [...mockShops];
        }

        if (req.query.specialty) {
            shops = shops.filter(s => s.specialty.toLowerCase().includes(req.query.specialty.toLowerCase()));
        }

        res.json({ success: true, count: shops.length, data: shops, origin: shops === mockShops ? "memory_fallback" : "database_pool" });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

app.post('/shops', async (req, res) => {
    const { name, specialty, distanceKm, zip, items } = req.body;
    if (!name || !specialty) {
        return res.status(400).json({ success: false, error: "Missing required fields: name and specialty are required." });
    }

    try {
        const newShop = {
            name,
            specialty,
            distanceKm: distanceKm ? parseFloat(distanceKm) : 1.0,
            items: items || []
        };

        let dbSaved = false;
        try {
            const targetZip = zip || "100001";
            // Ensure locale exists
            await executeQuery("INSERT INTO locales (zip, city, state) VALUES ($1, $2, $3) ON CONFLICT (zip) DO NOTHING", [targetZip, "Gbagada", "Lagos State"]);
            
            const shopId = `SHP-${Date.now().toString().slice(-6)}`;
            await executeQuery(`
                INSERT INTO shops (id, name, specialty, distance_km, zip)
                VALUES ($1, $2, $3, $4, $5)
            `, [shopId, name, specialty, newShop.distanceKm, targetZip]);
            
            dbSaved = true;
        } catch (dbErr) {
            console.log("Failed to auto-insert shop connection into postgres relational tables:", dbErr.message);
        }

        mockShops.push(newShop);

        res.status(201).json({
            success: true,
            message: "New retail store registered successfully!",
            data: newShop,
            persisted: dbSaved ? "postgres_database" : "memory_only"
        });
    } catch (err) {
        res.status(400).json({ success: false, error: err.message });
    }
});

// ==========================================================
// 5. HOSPITALS MODULE ROUTES (/hospitals)
// ==========================================================
app.get('/hospitals', async (req, res) => {
    try {
        let hospitals = [];
        try {
            const result = await executeQuery("SELECT name, location, distance_km::float as distanceKm, specialties::text as specialties_raw FROM healthcare_catalog");
            if (result.rows.length > 0) {
                hospitals = result.rows.map(row => ({
                    name: row.name,
                    location: row.location,
                    distanceKm: row.distancekm,
                    specialties: row.specialties_raw ? JSON.parse(row.specialties_raw) : ["General Medicine"]
                }));
            }
        } catch (dbErr) {
            console.log("Postgres /hospitals query failed, serving loaded run-time memory catalog instead:", dbErr.message);
        }

        if (hospitals.length === 0) {
            hospitals = [...mockHospitals];
        }

        if (req.query.specialty) {
            const filterSpec = req.query.specialty.toLowerCase();
            hospitals = hospitals.filter(h => h.specialties.some(s => s.toLowerCase().includes(filterSpec)));
        }

        res.json({ success: true, count: hospitals.length, data: hospitals, origin: hospitals === mockHospitals ? "memory_fallback" : "database_pool" });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

app.post('/hospitals', async (req, res) => {
    const { name, location, distanceKm, specialties } = req.body;
    if (!name || !location) {
        return res.status(400).json({ success: false, error: "Missing required fields: hospital name and location address are mandatory." });
    }

    try {
        const parsedSpecialties = Array.isArray(specialties) ? specialties : ["General Wellness"];
        const newHospital = {
            name,
            location,
            distanceKm: distanceKm ? parseFloat(distanceKm) : 1.5,
            specialties: parsedSpecialties
        };

        let dbSaved = false;
        try {
            await executeQuery(`
                INSERT INTO healthcare_catalog (id, name, location, distance_km, specialties)
                VALUES ($1, $2, $3, $4, $5)
            `, [`HSP-${Date.now().toString().slice(-6)}`, name, location, newHospital.distanceKm, JSON.stringify(parsedSpecialties)]);
            dbSaved = true;
        } catch (dbErr) {
            console.log("Could not register healthcare provider to postgresql tabular repository:", dbErr.message);
        }

        mockHospitals.push(newHospital);

        res.status(201).json({
            success: true,
            message: "Clinical Healthcare provider enrolled successfully!",
            data: newHospital,
            persisted: dbSaved ? "postgres_database" : "memory_only"
        });
    } catch (err) {
        res.status(400).json({ success: false, error: err.message });
    }
});

// ==========================================================
// 6. QUOTES MODULE ROUTES (/quotes & /graphql)
// ==========================================================

const mockQuotes = [
    { id: "Q-01", author: "Balogun Merchant", text: "Style is the signature of your soul. Make it bold, make it loud.", category: "Fashion" },
    { id: "Q-02", author: "Alaba Tech Guru", text: "A powerbank in hand is worth two in the shop. Stay fully charged always.", category: "Electronics" },
    { id: "Q-03", author: "Mega Rite Grocer", text: "Quality is not an act, it is a habit of parboiled premium choice.", category: "Groceries" },
    { id: "Q-04", author: "Yanga CEO", text: "No matter how fast a cheetah runs, it cannot catch a business built on trust.", category: "Business" },
    { id: "Q-05", author: "Lagos Vibe Master", text: "Work hard, but make sure you have enough Jollof for the road.", category: "Motivation" }
];

app.get('/quotes', async (req, res) => {
    try {
        let quotes = [];
        try {
            const result = await executeQuery("SELECT id, author, text, category, created_at FROM quotes");
            if (result.rows.length > 0) {
                quotes = result.rows;
            }
        } catch (dbErr) {
            console.log("Postgres /quotes query failed, serving loaded run-time memory catalog instead:", dbErr.message);
        }

        if (quotes.length === 0) {
            quotes = [...mockQuotes];
        }

        const category = req.query.category || "All";
        let filteredQuotes = quotes;
        if (category !== "All") {
            filteredQuotes = quotes.filter(q => q.category.toLowerCase() === category.toLowerCase());
        }

        // Return connection container type: list + count + category
        res.json({
            success: true,
            quotes: filteredQuotes,
            totalCount: filteredQuotes.length,
            category: category,
            origin: quotes === mockQuotes ? "memory_fallback" : "database_pool"
        });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

app.post('/quotes', async (req, res) => {
    const { author, text, category } = req.body;
    if (!author || !text || !category) {
        return res.status(400).json({ success: false, error: "Missing required fields: author, text, or category." });
    }

    try {
        const newQuote = {
            id: `Q-${Date.now().toString().slice(-6)}`,
            author,
            text,
            category,
            created_at: new Date()
        };

        let dbSaved = false;
        try {
            await executeQuery(
                "INSERT INTO quotes (id, author, text, category) VALUES ($1, $2, $3, $4)",
                [newQuote.id, author, text, category]
            );
            dbSaved = true;
        } catch (dbErr) {
            console.log("Could not save quote to postgres database, persisting to memory:", dbErr.message);
        }

        mockQuotes.push(newQuote);

        res.status(201).json({
            success: true,
            message: "Quote created successfully!",
            data: newQuote,
            persisted: dbSaved ? "postgres_database" : "memory_only"
        });
    } catch (err) {
        res.status(400).json({ success: false, error: err.message });
    }
});

// ==========================================================
// 7. INVENTORY CONTROL MODULE (REST API & Web HTML GUI)
// ==========================================================

const mockProductInventory = [
    { product_id: 1, name: "Vibrant Premium Jollof Sauce Mix", quantity: 120, price: 1850.00, category_id: "CAT-FOOD", category_name: "Food" },
    { product_id: 2, name: "Yanga Branded Insulated Food Bag", quantity: 35, price: 8500.00, category_id: "CAT-SHOP", category_name: "Shop" },
    { product_id: 3, name: "Organic Gbagada Soursop Puree", quantity: 70, price: 4200.00, category_id: "CAT-FRUITS", category_name: "Fruits" },
    { product_id: 4, name: "Fun Speckled Suya Savor Rub", quantity: 240, price: 950.00, category_id: "CAT-FOOD", category_name: "Food" }
];

// Helper validation escape inside template literal string
function escapeHtml(text) {
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Serving the beautiful Interactive HTML View
app.get('/inventory', async (req, res) => {
    let listHtml = '';
    let dbStatus = "In-Memory Sandbox fallback (Standalone Mode)";
    let isDbLive = false;
    let items = [];
    const searchQ = (req.query.q || '').trim();

    try {
        if (pool) {
            if (searchQ) {
                // Perform SQL search with % wildcard and LIKE operator matching name or category name
                const wildcardQuery = `%${searchQ}%`;
                const result = await executeQuery(`
                    SELECT p.product_id, p.name, p.quantity, p.price::float as price, p.category_id, c.name as category_name 
                    FROM product p 
                    LEFT JOIN product_categories c ON p.category_id = c.id
                    WHERE LOWER(p.name) LIKE LOWER($1) OR LOWER(c.name) LIKE LOWER($1)
                    ORDER BY p.product_id DESC
                `, [wildcardQuery]);
                items = result.rows;
                dbStatus = `Connected to Postgres - Filtered by "${searchQ}"`;
            } else {
                const result = await executeQuery(`
                    SELECT p.product_id, p.name, p.quantity, p.price::float as price, p.category_id, c.name as category_name
                    FROM product p
                    LEFT JOIN product_categories c ON p.category_id = c.id
                    ORDER BY p.product_id DESC
                `);
                items = result.rows;
                dbStatus = "Connected to Live PostgreSQL Database Engine";
            }
            isDbLive = true;
        }
    } catch (err) {
        console.log("Failed to query product table from postgres, leveraging memory repository:", err.message);
    }

    if (items.length === 0) {
        // Local in-memory filtering fallback using same standard logic
        const allItems = [...mockProductInventory];
        if (searchQ) {
            const lowerQ = searchQ.toLowerCase();
            items = allItems.filter(p => 
                p.name.toLowerCase().includes(lowerQ) || 
                (p.category_name && p.category_name.toLowerCase().includes(lowerQ))
            );
        } else {
            items = allItems;
        }
    }

    if (items.length === 0) {
        listHtml = `
            <tr id="noItemsPlaceholder">
                <td colspan="5" class="px-6 py-12 text-center text-gray-400 font-medium whitespace-nowrap">
                    <div class="flex flex-col items-center justify-center space-y-2">
                        <span class="text-4xl">🔍</span>
                        <p class="text-base text-gray-500 font-bold">No results match your search: "${escapeHtml(searchQ)}"</p>
                    </div>
                </td>
            </tr>
        `;
    } else {
        items.forEach(item => {
            const qtyClass = item.quantity === 0 ? "bg-red-100 text-red-800" : "bg-green-100 text-green-800";
            const catName = item.category_name || "General";
            listHtml += `
                <tr class="hover:bg-purple-50/40 border-b border-gray-100 transition-colors">
                    <td class="px-6 py-4 font-mono text-sm text-purple-900 font-bold">#${item.product_id}</td>
                    <td class="px-6 py-4 font-semibold text-gray-800">${escapeHtml(item.name)}</td>
                    <td class="px-6 py-4">
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-100">
                            ${escapeHtml(catName)}
                        </span>
                    </td>
                    <td class="px-6 py-4">
                        <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold ${qtyClass}">
                            ${item.quantity} units
                        </span>
                    </td>
                    <td class="px-6 py-4 font-bold text-indigo-600">₦${parseFloat(item.price).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                </tr>
            `;
        });
    }

    let categories = [];
    try {
        if (pool) {
            const catRes = await executeQuery("SELECT id, name FROM product_categories ORDER BY name ASC");
            categories = catRes.rows;
        }
    } catch (err) {
        console.log("Failed to query product_categories:", err.message);
    }
    if (categories.length === 0) {
        categories = [
            { id: "CAT-FOOD", name: "Food" },
            { id: "CAT-FRUITS", name: "Fruits" },
            { id: "CAT-SHOP", name: "Shop" }
        ];
    }

    const htmlContent = `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yanga Market - Fun Superapp Inventory</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;700&display=swap" rel="stylesheet">
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        yangaPurple: '#7C3AED',
                        yangaDarkPurple: '#4C1D95',
                        yangaYellow: '#FCD34D',
                        yangaPaleYellow: '#FEF9C3',
                        yangaGreen: '#10B981',
                        yangaOrange: '#F97316'
                    },
                    fontFamily: {
                        sans: ['Plus Jakarta Sans', 'sans-serif'],
                        mono: ['JetBrains Mono', 'monospace']
                    }
                }
            }
        }
    </script>
    <style>
        body {
            background-color: #FAF8F2;
        }
    </style>
</head>
<body class="font-sans text-gray-800 min-h-screen">
    
    <!-- HEADER BAR -->
    <header class="bg-yangaPurple text-white py-6 px-4 shadow-lg sticky top-0 z-50">
        <div class="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
            <div class="flex items-center space-x-3">
                <span class="text-3xl">🛍️</span>
                <div>
                    <h1 class="text-2xl font-extrabold tracking-tight">YANGA MARKET</h1>
                    <p class="text-xs text-yangaPaleYellow/95 font-bold uppercase tracking-wider">Superapp Supply & Logistics Engine</p>
                </div>
            </div>
            
            <div class="flex items-center space-x-3 bg-white/10 px-4 py-2 rounded-full border border-white/20 text-xs font-bold select-none">
                <span class="h-2.5 w-2.5 rounded-full ${isDbLive ? 'bg-green-400' : 'bg-orange-400'} animate-pulse"></span>
                <span>${dbStatus}</span>
            </div>
        </div>
    </header>

    <main class="max-w-6xl mx-auto px-4 py-10">
        
        <!-- HERO CONTAINER -->
        <div class="bg-gradient-to-br from-yangaPurple via-yangaDarkPurple to-indigo-900 rounded-3xl p-8 md:p-10 text-white mb-10 shadow-xl overflow-hidden relative">
            <!-- Orange and pale yellow accent circles -->
            <div class="absolute -right-10 -bottom-10 w-44 h-44 rounded-full bg-yangaOrange/30 blur-xl"></div>
            <div class="absolute -left-10 -top-10 w-40 h-40 rounded-full bg-yangaPaleYellow/15 blur-lg"></div>
            
            <div class="relative z-10 max-w-2xl">
                <span class="bg-yangaOrange font-black text-xs uppercase px-3 py-1.5 rounded-full text-white tracking-widest inline-block mb-4 shadow">INVENTORY HUB</span>
                <h2 class="text-3xl md:text-4xl font-extrabold mb-3 leading-tight">Add Your Goods & Fruits Stocks seamlessly</h2>
                <p class="text-sm md:text-base text-gray-200 font-medium leading-relaxed mb-4">
                     Register raw foods, freshly harvested fruits, and retail goods directly inside our superapp registry. Data flows atomically into the database with built-in non-negative audits.
                </p>
                <div class="flex flex-wrap gap-3">
                    <span class="bg-white/10 backdrop-blur-md px-3.5 py-1.5 rounded-full text-xs font-bold border border-white/20 text-yangaYellow">⚡ Automatic Math Validation</span>
                    <span class="bg-white/10 backdrop-blur-md px-3.5 py-1.5 rounded-full text-xs font-bold border border-white/20 text-green-300">✓ Non-Negative Stock Audit</span>
                </div>
            </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-10">
            
            <!-- INPUT FORM COLUMN (5 cols) -->
            <div class="lg:col-span-5">
                <div class="bg-white rounded-3xl shadow-xl p-8 border border-gray-100 relative overflow-hidden">
                    <div class="absolute top-0 left-0 w-full h-2 bg-yangaPurple"></div>
                    
                    <h3 class="text-xl font-extrabold text-yangaDarkPurple mb-6 flex items-center gap-2">
                        <span>📝</span> Register New Stock Item
                    </h3>
                    
                    <div id="alert" class="p-4 rounded-2xl mb-6 text-sm font-semibold transition-all border hidden" style="display: none;"></div>

                    <form id="inventoryForm" class="space-y-5" novalidate>
                        <!-- Name Field -->
                        <div>
                            <label for="name" class="block text-xs font-bold uppercase text-gray-500 tracking-wider mb-2">Item Name <span class="text-red-500">*</span></label>
                            <input 
                                type="text" 
                                id="name" 
                                name="name" 
                                required
                                placeholder="e.g. Traditional Spicy Egusi Pack"
                                class="w-full px-4 py-3.5 bg-gray-50 border border-gray-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-yangaPurple transition-all font-semibold placeholder:text-gray-400 text-sm"
                            >
                        </div>

                        <!-- Category Select Dropdown (links to product_categories table) -->
                        <div>
                            <label for="category_id" class="block text-xs font-bold uppercase text-gray-500 tracking-wider mb-2">Category <span class="text-red-500">*</span></label>
                            <select 
                                id="category_id" 
                                name="category_id" 
                                required
                                class="w-full px-4 py-3.5 bg-gray-50 border border-gray-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-yangaPurple transition-all font-semibold text-sm text-gray-700"
                            >
                                <option value="" disabled selected>-- Select Product Category --</option>
                                ${categories.map(c => `<option value="${escapeHtml(c.id)}">${escapeHtml(c.name)}</option>`).join('')}
                            </select>
                        </div>

                        <div class="grid grid-cols-2 gap-4">
                            <!-- Price Field -->
                            <div>
                                <label for="price" class="block text-xs font-bold uppercase text-gray-500 tracking-wider mb-2">Price (₦) <span class="text-red-500">*</span></label>
                                <div class="relative">
                                    <span class="absolute left-4 top-3.5 text-gray-400 font-bold text-sm">₦</span>
                                    <input 
                                        type="number" 
                                        id="price" 
                                        name="price" 
                                        required 
                                        min="0.01" 
                                        step="0.01"
                                        placeholder="1200.00"
                                        class="w-full pl-8 pr-4 py-3.5 bg-gray-55/60 border border-gray-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-yangaPurple transition-all font-semibold text-sm"
                                    >
                                </div>
                            </div>

                            <!-- Quantity Field -->
                            <div>
                                <label for="quantity" class="block text-xs font-bold uppercase text-gray-500 tracking-wider mb-2">Quantity <span class="text-red-500">*</span></label>
                                <input 
                                    type="number" 
                                    id="quantity" 
                                    name="quantity" 
                                    required 
                                    min="0" 
                                    step="1"
                                    placeholder="50"
                                    class="w-full px-4 py-3.5 bg-gray-55/60 border border-gray-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-yangaPurple transition-all font-semibold text-sm"
                                >
                            </div>
                        </div>

                        <button 
                            type="submit"
                            class="w-full py-4 px-6 bg-yangaPurple hover:bg-yangaDarkPurple text-white font-extrabold uppercase tracking-wider rounded-2xl transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg flex items-center justify-center space-x-2 text-sm mt-8 active:scale-95"
                        >
                            <span>🚀</span>
                            <span>ADD TO INVENTORY</span>
                        </button>
                    </form>

                    <!-- MENU SEARCH CONTROLLER (Check Today's Menu vibes - comparing user requests against available array) -->
                    <div class="mt-8 bg-gradient-to-br from-indigo-50/50 via-purple-50/45 to-white/95 rounded-3xl p-6 border-2 border-purple-100 flex flex-col relative overflow-hidden shadow-md">
                        <div class="absolute -right-6 -bottom-6 w-16 h-16 rounded-full bg-yangaYellow/20 blur-md"></div>
                        <h4 class="text-base font-extrabold text-yangaDarkPurple mb-1 flex items-center gap-2">
                            <span>🍽️</span> Yanga Menu Search Engine
                        </h4>
                        <p class="text-[11px] text-gray-500 font-semibold leading-relaxed mb-4">
                            Type a dish or ingredient name (like "Jollof", "Yam Fries", "Egusi", "Burger") to verify if it is on today's catalog!
                        </p>
                        
                        <div class="text-xs font-bold text-gray-500 mb-2 uppercase tracking-wide">Enter customer request:</div>
                        <div class="flex gap-2">
                            <input 
                                type="text" 
                                id="menuSearchInput" 
                                placeholder="e.g. Avocado, Jollof Rice, Suya..."
                                class="flex-grow px-4 py-3 bg-white border-2 border-purple-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-yangaPurple font-bold text-xs placeholder:text-gray-400 transition-all shadow-sm"
                            >
                            <button 
                                id="checkMenuBtn"
                                class="px-5 py-3 bg-yangaPurple hover:bg-yangaDarkPurple text-white font-extrabold text-xs uppercase tracking-wider rounded-xl transition-all shadow-md active:scale-95"
                            >
                                Search
                            </button>
                        </div>
                        
                        <div id="menuResult" class="mt-4 p-4 rounded-2xl text-xs font-semibold hidden transition-all border animate-fade-in"></div>
                    </div>
                </div>
            </div>

            <!-- INVENTORY LIST COLUMN (7 cols) -->
            <div class="lg:col-span-7">
                <div class="bg-white rounded-3xl shadow-xl overflow-hidden border border-gray-100 flex flex-col h-full col-span-7">
                    <div class="p-6 md:p-8 bg-gradient-to-r from-gray-50 to-white border-b border-gray-100 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
                        <div>
                            <h3 class="text-xl font-extrabold text-yangaDarkPurple flex items-center gap-2">
                                <span>📦</span> Current Market Stock List
                            </h3>
                            <p class="text-xs text-gray-500 mt-1 font-medium">Real-time status of items in Yanga superapp database</p>
                        </div>

                        <!-- LIVE SEARCH CONTROLLER (Matches Name / Category) -->
                        <div class="relative min-w-[200px] md:max-w-xs flex-grow">
                            <span class="absolute left-3.5 top-3 text-gray-400 text-sm">🔍</span>
                            <input 
                                type="text" 
                                id="searchInput" 
                                placeholder="Search by name or category..." 
                                value="${escapeHtml(searchQ)}"
                                class="w-full pl-9 pr-4 py-2.5 bg-white border border-gray-300 rounded-2xl focus:outline-none focus:ring-2 focus:ring-yangaPurple font-semibold text-sm placeholder:text-gray-400 transition-all shadow-sm"
                            >
                        </div>
                    </div>

                    <div class="overflow-x-auto flex-grow max-h-[500px]">
                        <table class="w-full text-left border-collapse">
                            <thead>
                                <tr class="bg-gray-50 text-gray-400 font-bold text-xs uppercase tracking-wider border-b border-gray-200">
                                    <th class="px-6 py-4 font-bold">UID</th>
                                    <th class="px-6 py-4 font-bold">Item Name</th>
                                    <th class="px-6 py-4 font-bold">Category</th>
                                    <th class="px-6 py-4 font-bold">Status</th>
                                    <th class="px-6 py-4 font-bold">Price</th>
                                </tr>
                            </thead>
                            <tbody id="inventoryList" class="divide-y divide-gray-100">
                                ${listHtml}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <footer class="mt-20 border-t border-gray-200 py-8 bg-gray-50 text-center text-xs text-gray-400 font-semibold tracking-wider uppercase">
        <p>&copy; 2026 Yanga Market superapp ecosystem limit. All rights reserved.</p>
    </footer>

    <script>
        // Inline helper to escape HTML tags to mitigate cross-site scripting
        function escapeHtml(text) {
            return String(text)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }

        // Handle the submission securely inside browser using fetch API with full double-checks
        document.getElementById('inventoryForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const nameField = document.getElementById('name');
            const categoryField = document.getElementById('category_id');
            const priceField = document.getElementById('price');
            const quantityField = document.getElementById('quantity');
            
            const name = nameField.value.trim();
            const category_id = categoryField.value;
            const priceVal = priceField.value;
            const qtyVal = quantityField.value;
            
            const alertDiv = document.getElementById('alert');
            alertDiv.style.display = 'none';
            alertDiv.className = 'p-4 rounded-2xl mb-6 text-sm font-semibold transition-all border ';
            
            // Client-side quick double-check validations
            if (!name) {
                alertDiv.textContent = '❌ Invalid Input: Please specify a valid item name.';
                alertDiv.classList.add('bg-red-100', 'text-red-800', 'border-red-300');
                alertDiv.style.display = 'block';
                nameField.focus();
                return;
            }

            if (!category_id) {
                alertDiv.textContent = '❌ Invalid Input: Please select a valid product category.';
                alertDiv.classList.add('bg-red-100', 'text-red-800', 'border-red-300');
                alertDiv.style.display = 'block';
                categoryField.focus();
                return;
            }
            
            const price = parseFloat(priceVal);
            if (isNaN(price) || price <= 0) {
                alertDiv.textContent = '❌ Invalid Input: Price must be a positive number greater than ₦0.00.';
                alertDiv.classList.add('bg-red-100', 'text-red-800', 'border-red-300');
                alertDiv.style.display = 'block';
                priceField.focus();
                return;
            }
            
            const quantity = parseInt(qtyVal, 10);
            if (isNaN(quantity) || quantity < 0) {
                alertDiv.textContent = '❌ Invalid Input: Quantity cannot be a negative value.';
                alertDiv.classList.add('bg-red-100', 'text-red-800', 'border-red-300');
                alertDiv.style.display = 'block';
                quantityField.focus();
                return;
            }

            try {
                const response = await fetch('/inventory', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({ name, category_id, price, quantity })
                });

                const result = await response.json();
                
                if (response.ok && result.success) {
                    // Success callback
                    alertDiv.textContent = '🎉 ' + result.message + ' (Synced to ' + result.persisted + ')';
                    alertDiv.classList.add('bg-green-100', 'text-green-800', 'border-green-300');
                    alertDiv.style.display = 'block';

                    // Clear inputs
                    nameField.value = '';
                    categoryField.selectedIndex = 0;
                    priceField.value = '';
                    quantityField.value = '';

                    // Prepend new row elegantly to stock table
                    const listContainer = document.getElementById('inventoryList');
                    const placeholder = document.getElementById('noItemsPlaceholder');
                    if (placeholder) {
                        placeholder.remove();
                    }

                    const qtyClass = result.data.quantity === 0 ? "bg-red-100 text-red-100" : "bg-green-100 text-green-800";
                    const formattedPrice = parseFloat(result.data.price).toLocaleString('en-US', { 
                        minimumFractionDigits: 2, 
                        maximumFractionDigits: 2 
                    });
                    const catName = result.data.category_name || "General";

                    const newRowHtml = \`
                        <tr class="hover:bg-purple-50/40 border-b border-gray-100 transition-colors bg-green-50/20">
                            <td class="px-6 py-4 font-mono text-sm text-purple-900 font-bold">#\${result.data.product_id}</td>
                            <td class="px-6 py-4 font-semibold text-gray-800">\${escapeHtml(result.data.name)}</td>
                            <td class="px-6 py-4">
                                <span class="inline-flex items-center px-2.5 py-0.5 rounded text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-100">
                                    \${escapeHtml(catName)}
                                </span>
                            </td>
                            <td class="px-6 py-4">
                                <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold \${qtyClass}">
                                    \${result.data.quantity} units
                                </span>
                            </td>
                            <td class="px-6 py-4 font-bold text-indigo-600">₦\${formattedPrice}</td>
                        </tr>
                    \`;

                    listContainer.insertAdjacentHTML('afterbegin', newRowHtml);
                } else {
                    // Fail response from server validation
                    alertDiv.textContent = '❌ ' + (result.error || 'Registry rejected. Please check inputs.');
                    alertDiv.classList.add('bg-red-100', 'text-red-800', 'border-red-300');
                    alertDiv.style.display = 'block';
                }
            } catch (err) {
                alertDiv.textContent = '❌ Server offline or network connection issue occurred.';
                alertDiv.classList.add('bg-red-100', 'text-red-800', 'border-red-300');
                alertDiv.style.display = 'block';
            }
        });

        // Dynamic Live DB Wildcard Search
        let debounceTimer;
        const searchInput = document.getElementById('searchInput');
        const listContainer = document.getElementById('inventoryList');

        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            const query = e.target.value.trim();

            debounceTimer = setTimeout(async () => {
                try {
                    // Fetch from wild-card search endpoint
                    const response = await fetch('/products/search?q=' + encodeURIComponent(query));
                    const result = await response.json();

                    if (response.ok && result.success) {
                        listContainer.innerHTML = '';
                        
                        if (result.data.length === 0) {
                            listContainer.innerHTML = `
                                <tr>
                                    <td colspan="5" class="px-6 py-12 text-center text-gray-400 font-medium whitespace-nowrap">
                                        <div class="flex flex-col items-center justify-center space-y-2">
                                            <span class="text-4xl font-normal">🔍</span>
                                            <p class="text-base text-gray-500 font-bold">No results found for "${escapeHtml(query)}"</p>
                                        </div>
                                    </td>
                                </tr>
                            `;
                        } else {
                            result.data.forEach(item => {
                                const qtyClass = item.quantity === 0 ? "bg-red-100 text-red-800" : "bg-green-100 text-green-800";
                                const formattedPrice = parseFloat(item.price).toLocaleString('en-US', { 
                                    minimumFractionDigits: 2, 
                                    maximumFractionDigits: 2 
                                });
                                const displayCategory = item.category || "General";

                                listContainer.innerHTML += `
                                    <tr class="hover:bg-purple-50/40 border-b border-gray-100 transition-colors animate-fade-in">
                                        <td class="px-6 py-4 font-mono text-sm text-purple-900 font-bold">#${item.id || item.product_id || '-'}</td>
                                        <td class="px-6 py-4 font-semibold text-gray-800">${escapeHtml(item.name)}</td>
                                        <td class="px-6 py-4">
                                            <span class="inline-flex items-center px-2.5 py-0.5 rounded text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-100">
                                                ${escapeHtml(displayCategory)}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4">
                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold ${qtyClass}">
                                                ${item.quantity} units
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 font-bold text-indigo-600">₦${formattedPrice}</td>
                                    </tr>
                                `;
                            });
                        }
                    }
                } catch (err) {
                    console.error("Live search failed:", err);
                }
            }, 300); // 300ms sweet-spot debounce window
        });

        // Menu Checker Custom Search Handler
        const menuSearchInput = document.getElementById('menuSearchInput');
        const checkMenuBtn = document.getElementById('checkMenuBtn');
        const menuResult = document.getElementById('menuResult');

        const performMenuCheck = async () => {
            const query = menuSearchInput.value.trim();
            if (!query) {
                menuResult.style.display = 'block';
                menuResult.className = "mt-4 p-4 rounded-2xl text-xs font-bold bg-orange-100 text-orange-900 border border-orange-200 animate-fade-in";
                menuResult.innerHTML = "⚠️ Please enter a dish or fruit item to look up!";
                return;
            }

            try {
                menuResult.style.display = 'block';
                menuResult.className = "mt-4 p-4 rounded-2xl text-xs font-bold bg-purple-50 text-purple-700 border border-purple-200 animate-pulse";
                menuResult.innerHTML = "🌀 Searching today's fresh menu...";

                const res = await fetch('/food/check-menu?q=' + encodeURIComponent(query));
                const json = await res.json();

                if (res.ok && json.success) {
                    menuResult.className = "mt-4 p-4 rounded-2xl text-xs font-bold bg-green-100 text-green-800 border-2 border-green-300 animate-fade-in";
                    const priceFormatted = parseFloat(json.data.price).toLocaleString('en-US', { minimumFractionDigits: 2 });
                    menuResult.innerHTML = `
                        <div class="flex flex-col gap-1.5">
                            <div class="flex items-center gap-1.5 text-sm">
                                <span>🎉</span>
                                <span>Excellent choice!</span>
                            </div>
                            <div class="text-xs text-green-900 mt-1">
                                <span class="underline font-black">"${escapeHtml(json.data.name)}"</span> is available on today's menu under <strong class="bg-green-200/50 px-1.5 py-0.5 rounded text-[10px] text-green-800 uppercase font-black">${escapeHtml(json.data.category)}</strong>.
                            </div>
                            <div class="text-xs text-green-950 font-black mt-1">
                                Price: ₦${priceFormatted}
                            </div>
                            <div class="text-[10px] text-green-700 italic mt-0.5">
                                ${escapeHtml(json.data.description || 'Prepared fresh on-demand by our culinary masters.')}
                            </div>
                        </div>
                    `;
                } else {
                    throw new Error(json.message || "Item is not on the day's menu.");
                }
            } catch (err) {
                menuResult.className = "mt-4 p-4 rounded-2xl text-xs font-bold bg-red-100 text-red-800 border-2 border-red-300 animate-fade-in";
                menuResult.innerHTML = `
                    <div class="flex flex-col gap-1">
                        <div class="flex items-center gap-1 text-sm font-black">
                            <span>❌ Not Available Today</span>
                        </div>
                        <p class="text-xs mt-1 text-red-900">${escapeHtml(err.message)}</p>
                    </div>
                `;
            }
        };

        checkMenuBtn.addEventListener('click', performMenuCheck);
        menuSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                performMenuCheck();
            }
        });
    </script>
</body>
</html>
`;
    res.send(htmlContent);
});

// ==========================================================
// 8. PRODUCT SEARCH API (SQL LIKE % WILD-CARD PATTERN MATCHING)
// ==========================================================
app.get('/products/search', async (req, res) => {
    const searchQ = (req.query.q || '').trim();
    const wildcard = `%${searchQ}%`;
    let results = [];
    let source = "Memory fallback";

    try {
        if (pool) {
            if (searchQ) {
                // Distinctly fetch matching items from the custom core table "product" joined with "product_categories" via LIKE matching
                const productRes = await executeQuery(`
                    SELECT p.product_id as id, p.name, p.price::float as price, p.quantity, COALESCE(c.name, 'General') as category
                    FROM product p
                    LEFT JOIN product_categories c ON p.category_id = c.id
                    WHERE LOWER(p.name) LIKE LOWER($1) OR LOWER(c.name) LIKE LOWER($1)
                    ORDER BY p.product_id DESC
                `, [wildcard]);

                // Distinctly fetch matching items from the normalized relational table "products" and relational "product_categories" via LIKE matching
                const productsRes = await executeQuery(`
                    SELECT p.id, p.name, p.price::float as price, p.quantity, COALESCE(c.name, 'General') as category
                    FROM products p
                    LEFT JOIN product_categories c ON p.category_id = c.id
                    WHERE LOWER(p.name) LIKE LOWER($1) OR LOWER(c.name) LIKE LOWER($1)
                    ORDER BY p.id DESC
                `, [wildcard]);

                results = [
                    ...productRes.rows.map(r => ({ ...r, origin: "product" })),
                    ...productsRes.rows.map(r => ({ ...r, origin: "products" }))
                ];
                source = "PostgreSQL Database Pool (LIKE wildcard matched)";
            } else {
                // Return all items if no query is specified
                const productRes = await executeQuery(`
                    SELECT p.product_id as id, p.name, p.price::float as price, p.quantity, COALESCE(c.name, 'General') as category
                    FROM product p
                    LEFT JOIN product_categories c ON p.category_id = c.id
                    ORDER BY p.product_id DESC
                `);
                const productsRes = await executeQuery(`
                    SELECT p.id, p.name, p.price::float as price, p.quantity, COALESCE(c.name, 'General') as category
                    FROM products p
                    LEFT JOIN product_categories c ON p.category_id = c.id
                    ORDER BY p.id DESC
                `);
                results = [
                    ...productRes.rows.map(r => ({ ...r, origin: "product" })),
                    ...productsRes.rows.map(r => ({ ...r, origin: "products" }))
                ];
                source = "PostgreSQL Database Pool (Full Scan)";
            }
        }
    } catch (err) {
        console.log("PostgreSQL query search execution crashed. Reverting to sandbox store fallback:", err.message);
    }

    if (results.length === 0) {
        // Run sandbox matching using javascript equivalents for testing/fallback integrity
        const lowerQ = searchQ.toLowerCase();
        
        // Load fallback stores with their correct categories
        const fallbackList = [
            ...mockProductInventory.map(p => ({ id: p.product_id, name: p.name, price: p.price, quantity: p.quantity, category: p.category_name || "General" })),
            ...mockFoods.map((f, i) => ({ id: `FOOD-${i}`, name: f.name, price: f.price, quantity: 100, category: f.category }))
        ];

        if (searchQ) {
            results = fallbackList.filter(p => 
                p.name.toLowerCase().includes(lowerQ) || 
                (p.category && p.category.toLowerCase().includes(lowerQ))
            );
        } else {
            results = fallbackList;
        }
    }

    return res.json({
        success: true,
        count: results.length,
        query: searchQ,
        wildcardUsed: searchQ ? wildcard : null,
        data: results,
        backendEngine: source
    });
});

// Handling form and API JSON submissions atomically
app.post('/inventory', async (req, res) => {
    const { name, price, quantity, category_id } = req.body;

    // Strict Server-Side Validation: Name is required and non-empty
    if (!name || typeof name !== 'string' || name.trim() === '') {
        return res.status(400).json({ 
            success: false, 
            error: "Validation Failure: Item name serves as a required non-empty string identifier." 
        });
    }

    // Strict Server-Side Validation: Category ID is required and non-empty
    if (!category_id || typeof category_id !== 'string' || category_id.trim() === '') {
        return res.status(400).json({ 
            success: false, 
            error: "Validation Failure: Product category selection is required." 
        });
    }

    // Strict Server-Side Validation: Price is required and positive
    const parsedPrice = parseFloat(price);
    if (isNaN(parsedPrice) || parsedPrice <= 0) {
        return res.status(400).json({ 
            success: false, 
            error: "Validation Failure: Pricing must represent a strict positive number (₦ > 0)." 
        });
    }

    // Strict Server-Side Validation: Quantity is required and non-negative
    const parsedQty = parseInt(quantity, 10);
    if (isNaN(parsedQty) || parsedQty < 0) {
        return res.status(400).json({ 
            success: false, 
            error: "Validation Failure: Stock quantity values cannot represent negative ranges (>= 0)." 
        });
    }

    try {
        let dbSaved = false;
        let newId = null;
        let categoryName = "General";

        try {
            // Write to Postgres product table with category_id relation
            const result = await executeQuery(`
                INSERT INTO product (name, price, quantity, category_id)
                VALUES ($1, $2, $3, $4)
                RETURNING product_id
            `, [name.trim(), parsedPrice, parsedQty, category_id.trim()]);

            const insertedId = result.rows[0].product_id;
            
            // Relational join to retrieve category information freshly for client feedback
            const itemRes = await executeQuery(`
                SELECT p.product_id, p.name, p.price::float as price, p.quantity, p.category_id, c.name as category_name
                FROM product p
                LEFT JOIN product_categories c ON p.category_id = c.id
                WHERE p.product_id = $1
            `, [insertedId]);

            if (itemRes.rows.length > 0) {
                newId = itemRes.rows[0].product_id;
                categoryName = itemRes.rows[0].category_name || "General";
            } else {
                newId = insertedId;
            }
            dbSaved = true;
        } catch (dbErr) {
            console.log("Postgres singular product registry write failed, utilizing fallback store:", dbErr.message);
        }

        if (!dbSaved) {
            newId = mockProductInventory.length > 0 
                ? Math.max(...mockProductInventory.map(p => p.product_id)) + 1 
                : 1;

            // Map standard category name from static map
            const staticCats = {
                "CAT-FOOD": "Food",
                "CAT-FRUITS": "Fruits",
                "CAT-SHOP": "Shop"
            };
            categoryName = staticCats[category_id] || "General";
        }

        const newItem = {
            product_id: newId,
            name: name.trim(),
            price: parsedPrice,
            quantity: parsedQty,
            category_id: category_id,
            category_name: categoryName
        };

        // Prepend to fallback store
        mockProductInventory.unshift(newItem);

        return res.status(201).json({
            success: true,
            message: "New stock uploaded into marketplace successfully!",
            data: newItem,
            persisted: dbSaved ? "postgres_database" : "memory_only"
        });
    } catch (err) {
        return res.status(500).json({ success: false, error: err.message });
    }
});

app.post('/graphql', async (req, res) => {
    const { query, variables } = req.body;
    if (!query) {
        return res.status(400).json({ error: "No GraphQL query provided." });
    }

    const norm = query.replace(/\s+/g, ' ');
    try {
        // Handle GraphQL Introspection queries (__schema)
        if (norm.includes('__schema') || norm.includes('__type')) {
            return res.json({
                data: {
                    __schema: {
                        queryType: { name: "Query" },
                        mutationType: { name: "Mutation" },
                        subscriptionType: null,
                        types: [
                            {
                                kind: "OBJECT",
                                name: "Query",
                                description: "The superapp core GraphQL queries query type definitions.",
                                fields: [
                                    { name: "foods", type: { kind: "NON_NULL", ofType: { kind: "LIST", ofType: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "FoodItem" } } } } },
                                    { name: "fruits", type: { kind: "NON_NULL", ofType: { kind: "LIST", ofType: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "FoodItem" } } } } },
                                    { name: "shops", type: { kind: "NON_NULL", ofType: { kind: "LIST", ofType: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "RetailShop" } } } } },
                                    { name: "events", type: { kind: "NON_NULL", ofType: { kind: "LIST", ofType: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "Event" } } } } },
                                    { name: "hospitals", type: { kind: "NON_NULL", ofType: { kind: "LIST", ofType: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "Hospital" } } } } },
                                    { name: "restaurants", type: { kind: "NON_NULL", ofType: { kind: "LIST", ofType: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "Restaurant" } } } } },
                                    { 
                                        name: "restaurant", 
                                        args: [{ name: "id", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "ID" } } }],
                                        type: { kind: "OBJECT", name: "Restaurant" } 
                                    },
                                    { name: "quotes", type: { kind: "NON_NULL", ofType: { kind: "LIST", ofType: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "BusinessQuote" } } } } },
                                    { 
                                        name: "quotesLibrary", 
                                        args: [{ name: "category", type: { kind: "SCALAR", name: "String" } }],
                                        type: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "QuotesLibraryType" } } 
                                    }
                                ]
                            },
                            {
                                kind: "OBJECT",
                                name: "Mutation",
                                description: "The superapp transactional mutations.",
                                fields: [
                                    {
                                        name: "fundWallet",
                                        args: [{ name: "amount", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "Float" } } }],
                                        type: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "WalletResponse" } }
                                    },
                                    {
                                        name: "payWithWallet",
                                        args: [
                                            { name: "amount", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "Float" } } },
                                            { name: "note", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } }
                                        ],
                                        type: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "WalletResponse" } }
                                    },
                                    {
                                        name: "postVibe",
                                        args: [
                                            { name: "author", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                            { name: "content", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } }
                                        ],
                                        type: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "VibePost" } }
                                    },
                                    {
                                        name: "reactionVibe",
                                        args: [{ name: "id", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "ID" } } }],
                                        type: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "VibePost" } }
                                    },
                                    {
                                        name: "submitOrder",
                                        args: [
                                            { name: "amount", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "Float" } } },
                                            { name: "itemsSummary", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } }
                                        ],
                                        type: { kind: "OBJECT", name: "OrderResponse" }
                                    }
                                ]
                            },
                            {
                                kind: "OBJECT",
                                name: "FoodItem",
                                fields: [
                                    { name: "id", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "ID" } } },
                                    { name: "name", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "price", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "Float" } } },
                                    { name: "category", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "description", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "isFruit", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "Boolean" } } }
                                ]
                            },
                            {
                                kind: "OBJECT",
                                name: "BusinessQuote",
                                fields: [
                                    { name: "id", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "ID" } } },
                                    { name: "author", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "text", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "category", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "createdAt", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } }
                                ]
                            },
                            {
                                kind: "OBJECT",
                                name: "QuotesLibraryType",
                                fields: [
                                    { name: "quotes", type: { kind: "NON_NULL", ofType: { kind: "LIST", ofType: { kind: "NON_NULL", ofType: { kind: "OBJECT", name: "BusinessQuote" } } } } },
                                    { name: "totalCount", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "Int" } } },
                                    { name: "category", type: { kind: "SCALAR", name: "String" } }
                                ]
                            },
                            {
                                kind: "OBJECT",
                                name: "Restaurant",
                                fields: [
                                    { name: "id", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "ID" } } },
                                    { name: "name", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "cuisine", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "rating", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "Float" } } },
                                    { name: "address", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } }
                                ]
                            },
                            {
                                kind: "OBJECT",
                                name: "OrderResponse",
                                fields: [
                                    { name: "id", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "ID" } } },
                                    { name: "status", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } },
                                    { name: "estimatedDeliveryMinutes", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "Int" } } },
                                    { name: "message", type: { kind: "NON_NULL", ofType: { kind: "SCALAR", name: "String" } } }
                                ]
                            }
                        ]
                    }
                }
            });
        }

        if (norm.includes('query GetQuotesLibrary') || norm.includes('quotesLibrary')) {
            const category = variables?.category || "All";
            let quotes = [];
            try {
                const result = await executeQuery("SELECT id, author, text, category, created_at FROM quotes");
                if (result.rows.length > 0) {
                    quotes = result.rows;
                }
            } catch (dbErr) {
                quotes = [...mockQuotes];
            }
            if (quotes.length === 0) quotes = [...mockQuotes];

            let filtered = quotes;
            if (category !== "All") {
                filtered = quotes.filter(q => q.category.toLowerCase() === category.toLowerCase());
            }

            return res.json({
                data: {
                    quotesLibrary: {
                        quotes: filtered.map(q => ({
                            id: q.id,
                            author: q.author,
                            text: q.text,
                            category: q.category,
                            createdAt: q.created_at || q.createdAt || "2026-06-16T12:00:00Z"
                        })),
                        totalCount: filtered.length,
                        category: category
                    }
                }
            });
        }

        if (norm.includes('mutation FundWallet') || norm.includes('fundWallet')) {
            const amount = parseFloat(variables?.amount || 0);
            if (amount <= 0) {
                return res.json({ errors: [{ message: "Deposit amount must be positive." }] });
            }

            const txId = `TXN-FUND-${Math.floor(100000 + Math.random() * 900000)}`;
            const timestamp = Date.now();
            const securityHash = `SHA-${Math.floor(10000000 + Math.random() * 90000000)}`;
            const customerId = 'CUST-01';

            let updatedBalance = 0.00;

            if (pool) {
                const client = await pool.connect();
                try {
                    await client.query('BEGIN');
                    
                    // Insert funding transaction
                    await client.query(
                        "INSERT INTO wallet_transactions (id, customer_id, amount, type, description, timestamp, security_hash) VALUES ($1, $2, $3, $4, $5, $6, $7)",
                        [txId, customerId, amount, 'FUND', 'Funded wallet via Secure Card Payment on Server', timestamp, securityHash]
                    );

                    // Compute current balance atomically under transaction lock
                    const balRes = await client.query(
                        "SELECT COALESCE(SUM(CASE WHEN type IN ('Credit', 'FUND') THEN amount ELSE -amount END), 0.00) AS balance FROM wallet_transactions WHERE customer_id = $1",
                        [customerId]
                    );
                    updatedBalance = parseFloat(balRes.rows[0].balance);

                    await client.query('COMMIT');
                } catch (txErr) {
                    await client.query('ROLLBACK');
                    console.error("Fund wallet transaction rolled back due to error:", txErr.message);
                    return res.json({ errors: [{ message: `Transaction failed: ${txErr.message}` }] });
                } finally {
                    client.release();
                }
            } else {
                updatedBalance = amount;
            }

            return res.json({
                data: {
                    fundWallet: {
                        success: true,
                        balance: updatedBalance,
                        message: `Successfully funded wallet with amount ₦${amount}`
                    }
                }
            });
        }

        if (norm.includes('mutation PayWithWallet') || norm.includes('pay')) {
            const amount = parseFloat(variables?.amount || 0);
            const note = variables?.note || "Yanga Superapp Purchase";
            const txId = `TXN-PAY-${Math.floor(100000 + Math.random() * 900000)}`;
            const timestamp = Date.now();
            const securityHash = `SHA-${Math.floor(10000000 + Math.random() * 90000000)}`;
            const customerId = 'CUST-01';

            let currentBalance = 0.00;
            let transactionSucceeded = false;

            if (pool) {
                const client = await pool.connect();
                try {
                    await client.query('BEGIN');

                    // 1. ATOMIC BALANCE CHECK: Select and sum within transaction locks
                    const balRes = await client.query(
                        "SELECT COALESCE(SUM(CASE WHEN type IN ('Credit', 'FUND') THEN amount ELSE -amount END), 0.00) AS balance FROM wallet_transactions WHERE customer_id = $1 FOR UPDATE",
                        [customerId]
                    );
                    currentBalance = parseFloat(balRes.rows[0].balance);

                    if (currentBalance < amount) {
                        throw new Error(`Insufficient Wallet Balance! Current: ₦${currentBalance}. Charge attempt: ₦${amount}. Please fund your wallet.`);
                    }

                    // 2. ATOMIC DEDUCTION: Insert payment transaction
                    await client.query(
                        "INSERT INTO wallet_transactions (id, customer_id, amount, type, description, timestamp, security_hash) VALUES ($1, $2, $3, $4, $5, $6, $7)",
                        [txId, customerId, amount, 'PAYMENT', note, timestamp, securityHash]
                    );

                    await client.query('COMMIT');
                    transactionSucceeded = true;
                    currentBalance -= amount;
                } catch (txErr) {
                    await client.query('ROLLBACK');
                    console.error("Pay transaction rolled back due to error:", txErr.message);
                    return res.json({ errors: [{ message: txErr.message }] });
                } finally {
                    client.release();
                }
            } else {
                transactionSucceeded = true;
                currentBalance = 150000.00 - amount;
            }

            return res.json({
                data: {
                    pay: {
                        success: transactionSucceeded,
                        balance: currentBalance,
                        message: `Successfully paid ₦${amount}`
                    },
                    payWithWallet: {
                        success: transactionSucceeded,
                        balance: currentBalance,
                        message: `Successfully paid ₦${amount}`
                    }
                }
            });
        }

        if (norm.includes('mutation SubmitOrder') || norm.includes('submitOrder')) {
            const amount = parseFloat(variables?.amount || 0);
            const itemsSummary = variables?.itemsSummary || "Yanga Superapp Order";
            const orderId = `YNG-ORD-${Math.floor(100000 + Math.random() * 900000)}`;

            try {
                await executeQuery(
                    "INSERT INTO saved_bookings (id, booking_type, title, subtitle, date_or_time, price, extra_details) VALUES ($1, $2, $3, $4, $5, $6, $7)",
                    [orderId, "ORDER", "Food & Fruits Superapp Order", itemsSummary, "Today", amount, '{"status":"CONFIRMED"}']
                ).catch(e => console.log("Ignore pg order insert error:", e.message));
            } catch (err) {
                console.log("Not in PG mode:", err.message);
            }

            return res.json({
                data: {
                    submitOrder: {
                        id: orderId,
                        status: "CONFIRMED",
                        estimatedDeliveryMinutes: 30,
                        message: `Congratulations! Your order with Yanga Market is placed under ID: ${orderId}. Fast-delivery courier is matching! 🛵⚡`
                    }
                }
            });
        }

        // Default or unparsed query
        return res.json({
            errors: [{ message: "GraphQL Mock Router: Query not fully supported on backend Router, use REST endpoints for advanced operations." }]
        });
    } catch (err) {
        res.status(500).json({ errors: [{ message: err.message }] });
    }
});

app.listen(PORT, () => {
    console.log(`Server is running in ${process.env.NODE_ENV || 'development'} mode on port ${PORT}`);
    console.log(`Database target configured at: ${dbConfig.host}:${dbConfig.port}/${dbConfig.database}`);
});

