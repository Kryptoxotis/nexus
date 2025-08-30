const express = require('express');
const cors = require('cors');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const crypto = require('crypto');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(express.json());

const db = new sqlite3.Database(path.join(__dirname, 'nexus.db'));

// Encryption key for gift cards (in production, use environment variables)
const GIFT_CARD_KEY = crypto.randomBytes(32);

// Helper function to encrypt gift card data
function encryptGiftCard(data) {
  const iv = crypto.randomBytes(16);
  const cipher = crypto.createCipheriv('aes-256-cbc', crypto.scryptSync(GIFT_CARD_KEY, 'salt', 32), iv);
  let encrypted = cipher.update(JSON.stringify(data), 'utf8', 'hex');
  encrypted += cipher.final('hex');
  return iv.toString('hex') + ':' + encrypted;
}

// Helper function to decrypt gift card data
function decryptGiftCard(encryptedData) {
  try {
    const parts = encryptedData.split(':');
    const iv = Buffer.from(parts[0], 'hex');
    const decipher = crypto.createDecipheriv('aes-256-cbc', crypto.scryptSync(GIFT_CARD_KEY, 'salt', 32), iv);
    let decrypted = decipher.update(parts[1], 'hex', 'utf8');
    decrypted += decipher.final('utf8');
    return JSON.parse(decrypted);
  } catch (err) {
    return null;
  }
}

db.serialize(() => {
  db.run(`CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    price REAL NOT NULL,
    category TEXT,
    stock INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);
  
  // Add sample gift card products
  db.run(`INSERT OR IGNORE INTO products (id, name, price, category, stock) VALUES 
    (9998, '$25 Gift Card', 25.00, 'Gift Card', 999),
    (9999, '$50 Gift Card', 50.00, 'Gift Card', 999)`);
    
  // Add sample promotion
  db.run(`INSERT OR IGNORE INTO promotions (id, name, description, discount_type, discount_value, min_purchase, start_date, end_date, is_active) VALUES 
    (1, '10% Off $50+', 'Get 10% off your purchase of $50 or more', 'percentage', 10, 50, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1)`);

  // Add sample membership tiers
  db.run(`INSERT OR IGNORE INTO membership_tiers (id, name, price, description, benefits, duration_months) VALUES 
    (1, 'Basic', 0, 'Free basic membership', 'Access to basic facilities', 12),
    (2, 'Premium', 99.99, 'Premium membership with added benefits', 'Priority booking, 10% discounts', 12),
    (3, 'Gold', 199.99, 'Gold membership with exclusive perks', 'Free guest passes, 15% discounts, priority support', 12),
    (4, 'Platinum', 299.99, 'Ultimate membership experience', 'Unlimited guest passes, 20% discounts, VIP access, dedicated support', 12)`);

  // Add sample categories
  db.run(`INSERT OR IGNORE INTO categories (id, name, description) VALUES 
    (1, 'Memberships', 'Membership passes and subscriptions'),
    (2, 'Gift Shop', 'Retail merchandise and souvenirs'),
    (3, 'Passes', 'Day passes and event tickets'),
    (4, 'Snacks', 'Snacks and beverages'),
    (5, 'Food', 'Full meals and dining options')`);

  // Add membership products linked to membership tiers
  db.run(`INSERT OR IGNORE INTO products (id, name, price, category, category_id, stock) VALUES 
    (9001, 'Basic Membership', 0, 'Memberships', 1, 9999),
    (9002, 'Premium Membership', 99.99, 'Memberships', 1, 9999),
    (9003, 'Gold Membership', 199.99, 'Memberships', 1, 9999),
    (9004, 'Platinum Membership', 299.99, 'Memberships', 1, 9999)`);

  // Update existing products with categories
  db.run(`UPDATE products SET category_id = 2 WHERE category LIKE '%gift%' OR category LIKE '%merchandise%'`);
  db.run(`UPDATE products SET category_id = 4 WHERE category LIKE '%snack%' OR category LIKE '%drink%' OR category LIKE '%beverage%'`);
  db.run(`UPDATE products SET category_id = 5 WHERE category LIKE '%food%' OR category LIKE '%meal%'`);
  db.run(`UPDATE products SET category_id = 3 WHERE category LIKE '%pass%' OR category LIKE '%ticket%'`);

  // Generate unique IDs for existing members
  db.all(`SELECT id FROM members WHERE unique_id IS NULL`, (err, rows) => {
    if (!err && rows) {
      rows.forEach(row => {
        const uniqueId = 'MEM' + Date.now() + Math.random().toString(36).substr(2, 6).toUpperCase();
        db.run(`UPDATE members SET unique_id = ? WHERE id = ?`, [uniqueId, row.id]);
      });
    }
  });

  db.run(`CREATE TABLE IF NOT EXISTS members (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    unique_id TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    phone TEXT,
    membership_type TEXT DEFAULT 'basic',
    loyalty_points INTEGER DEFAULT 0,
    total_spent REAL DEFAULT 0,
    joined_date DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    event_date DATETIME NOT NULL,
    capacity INTEGER DEFAULT 0,
    booked INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    items TEXT NOT NULL,
    total REAL NOT NULL,
    payment_method TEXT DEFAULT 'cash',
    gift_card_used TEXT,
    member_id INTEGER,
    points_earned INTEGER DEFAULT 0,
    discount_applied REAL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members (id)
  )`);

  // Add new tax columns if they don't exist
  db.run(`ALTER TABLE transactions ADD COLUMN subtotal REAL DEFAULT 0`, (err) => {
    if (err && !err.message.includes('duplicate column name')) {
      console.error('Error adding subtotal column:', err.message);
    }
  });

  db.run(`ALTER TABLE transactions ADD COLUMN tax_amount REAL DEFAULT 0`, (err) => {
    if (err && !err.message.includes('duplicate column name')) {
      console.error('Error adding tax_amount column:', err.message);
    }
  });

  // Add unique_id column to members if it doesn't exist
  db.run(`ALTER TABLE members ADD COLUMN unique_id TEXT UNIQUE`, (err) => {
    if (err && !err.message.includes('duplicate column name')) {
      console.error('Error adding unique_id column:', err.message);
    }
  });

  // Create membership tiers table
  db.run(`CREATE TABLE IF NOT EXISTS membership_tiers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    price REAL NOT NULL,
    description TEXT,
    benefits TEXT,
    duration_months INTEGER DEFAULT 12,
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  // Create product categories table
  db.run(`CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    description TEXT,
    parent_id INTEGER,
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories (id)
  )`);

  // Add category_id to products
  db.run(`ALTER TABLE products ADD COLUMN category_id INTEGER REFERENCES categories(id)`, (err) => {
    if (err && !err.message.includes('duplicate column name')) {
      console.error('Error adding category_id column:', err.message);
    }
  });

  db.run(`CREATE TABLE IF NOT EXISTS gift_cards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    card_number TEXT UNIQUE NOT NULL,
    encrypted_data TEXT NOT NULL,
    balance REAL NOT NULL,
    original_amount REAL NOT NULL,
    status TEXT DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    used_at DATETIME,
    recipient_name TEXT,
    notes TEXT,
    transaction_id INTEGER,
    FOREIGN KEY (transaction_id) REFERENCES transactions (id)
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS promotions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    discount_type TEXT DEFAULT 'percentage',
    discount_value REAL NOT NULL,
    min_purchase REAL DEFAULT 0,
    max_discount REAL DEFAULT 0,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT 1,
    usage_limit INTEGER DEFAULT 0,
    times_used INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);
});

app.post('/api/verify-pin', (req, res) => {
  const { pin } = req.body;
  if (pin === '12345') {
    res.json({ success: true });
  } else {
    res.status(401).json({ success: false, message: 'Invalid PIN' });
  }
});

app.get('/api/products', (req, res) => {
  db.all('SELECT * FROM products ORDER BY created_at DESC', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json(rows);
  });
});

app.post('/api/products', (req, res) => {
  const { name, price, category, category_id, stock } = req.body;
  db.run('INSERT INTO products (name, price, category, category_id, stock) VALUES (?, ?, ?, ?, ?)',
    [name, price, category, category_id, stock],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ id: this.lastID, success: true });
    }
  );
});

app.delete('/api/products/:id', (req, res) => {
  db.run('DELETE FROM products WHERE id = ?', req.params.id, function(err) {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json({ success: true });
  });
});

app.get('/api/members', (req, res) => {
  db.all('SELECT * FROM members ORDER BY joined_date DESC', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json(rows);
  });
});

app.post('/api/members', (req, res) => {
  const { name, email, phone, membership_type } = req.body;
  
  // Generate unique member ID
  const uniqueId = 'MEM' + Date.now() + Math.random().toString(36).substr(2, 6).toUpperCase();
  
  db.run('INSERT INTO members (unique_id, name, email, phone, membership_type) VALUES (?, ?, ?, ?, ?)',
    [uniqueId, name, email, phone, membership_type],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ id: this.lastID, unique_id: uniqueId, success: true });
    }
  );
});

app.put('/api/members/:id', (req, res) => {
  const { name, email, phone, membership_type } = req.body;
  
  db.run('UPDATE members SET name = ?, email = ?, phone = ?, membership_type = ? WHERE id = ?',
    [name, email, phone, membership_type, req.params.id],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ success: true, changes: this.changes });
    }
  );
});

app.delete('/api/members/:id', (req, res) => {
  db.run('DELETE FROM members WHERE id = ?', req.params.id, function(err) {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json({ success: true });
  });
});

app.get('/api/events', (req, res) => {
  db.all('SELECT * FROM events ORDER BY event_date ASC', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json(rows);
  });
});

app.post('/api/events', (req, res) => {
  const { title, description, event_date, capacity } = req.body;
  db.run('INSERT INTO events (title, description, event_date, capacity) VALUES (?, ?, ?, ?)',
    [title, description, event_date, capacity],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ id: this.lastID, success: true });
    }
  );
});

app.delete('/api/events/:id', (req, res) => {
  db.run('DELETE FROM events WHERE id = ?', req.params.id, function(err) {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json({ success: true });
  });
});

app.post('/api/transactions', (req, res) => {
  const { items, subtotal, tax_amount, total, payment_method, gift_card_used, member_id, promotion_id } = req.body;
  
  let finalTotal = total;
  let pointsEarned = Math.floor(total); // 1 point per dollar
  let discountApplied = 0;
  
  // Apply promotion if provided
  if (promotion_id) {
    db.get('SELECT * FROM promotions WHERE id = ? AND is_active = 1 AND start_date <= datetime("now") AND end_date >= datetime("now")', 
      [promotion_id], (err, promotion) => {
        if (!err && promotion && total >= promotion.min_purchase) {
          if (promotion.discount_type === 'percentage') {
            discountApplied = Math.min((total * promotion.discount_value / 100), promotion.max_discount || total);
          } else {
            discountApplied = Math.min(promotion.discount_value, total);
          }
          finalTotal = Math.max(0, total - discountApplied);
        }
        
        completeTransaction();
      });
  } else {
    completeTransaction();
  }
  
  function completeTransaction() {
    // Create gift card if it's in the cart
    let giftCardPromise = Promise.resolve();
    const giftCardItems = items.filter(item => item.product.category === 'Gift Card');
    
    if (giftCardItems.length > 0) {
      const giftCardPromises = giftCardItems.map(item => {
        return new Promise((resolve, reject) => {
          for (let i = 0; i < item.quantity; i++) {
            const cardNumber = 'GC' + Date.now() + Math.random().toString(36).substr(2, 6).toUpperCase();
            const cardData = {
              amount: item.product.price,
              created_at: new Date().toISOString(),
              security_code: Math.random().toString(36).substr(2, 8).toUpperCase()
            };
            const encryptedData = encryptGiftCard(cardData);
            
            db.run('INSERT INTO gift_cards (card_number, encrypted_data, balance, original_amount, recipient_name, status) VALUES (?, ?, ?, ?, ?, ?)',
              [cardNumber, encryptedData, item.product.price, item.product.price, 'Customer', 'active'],
              function(err) {
                if (err) reject(err);
                else resolve(cardNumber);
              }
            );
          }
        });
      });
      
      giftCardPromise = Promise.all(giftCardPromises);
    }
    
    giftCardPromise.then(() => {
      db.run('INSERT INTO transactions (items, subtotal, tax_amount, total, payment_method, gift_card_used, member_id, points_earned, discount_applied) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [JSON.stringify(items), subtotal || 0, tax_amount || 0, finalTotal, payment_method, gift_card_used, member_id, pointsEarned, discountApplied],
        function(err) {
          if (err) {
            res.status(500).json({ error: err.message });
            return;
          }
          
          // Update member points if member transaction
          if (member_id) {
            db.run('UPDATE members SET loyalty_points = loyalty_points + ?, total_spent = total_spent + ? WHERE id = ?',
              [pointsEarned, finalTotal, member_id]);
          }
          
          res.json({ 
            id: this.lastID, 
            success: true, 
            points_earned: pointsEarned,
            discount_applied: discountApplied,
            final_total: finalTotal
          });
        }
      );
    }).catch(err => {
      res.status(500).json({ error: 'Gift card creation failed' });
    });
  }
});

// Gift Card API Endpoints
app.get('/api/gift-cards', (req, res) => {
  db.all('SELECT id, card_number, balance, original_amount, status, created_at, recipient_name, notes FROM gift_cards ORDER BY created_at DESC', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json(rows);
  });
});

app.post('/api/gift-cards', (req, res) => {
  const { amount, recipient_name, notes } = req.body;
  
  // Generate unique card number
  const cardNumber = 'GC' + Date.now() + Math.random().toString(36).substr(2, 6).toUpperCase();
  
  // Create encrypted data for security
  const cardData = {
    amount: amount,
    created_at: new Date().toISOString(),
    security_code: Math.random().toString(36).substr(2, 8).toUpperCase()
  };
  const encryptedData = encryptGiftCard(cardData);
  
  db.run('INSERT INTO gift_cards (card_number, encrypted_data, balance, original_amount, recipient_name, notes) VALUES (?, ?, ?, ?, ?, ?)',
    [cardNumber, encryptedData, amount, amount, recipient_name, notes],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ 
        id: this.lastID, 
        success: true, 
        card_number: cardNumber,
        balance: amount
      });
    }
  );
});

app.post('/api/gift-cards/validate', (req, res) => {
  const { card_number } = req.body;
  
  db.get('SELECT * FROM gift_cards WHERE card_number = ? AND status = "active"', [card_number], (err, row) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    
    if (!row) {
      res.status(404).json({ error: 'Gift card not found or inactive' });
      return;
    }
    
    res.json({
      valid: true,
      card_number: row.card_number,
      balance: row.balance,
      original_amount: row.original_amount
    });
  });
});

app.post('/api/gift-cards/redeem', (req, res) => {
  const { card_number, amount } = req.body;
  
  db.get('SELECT * FROM gift_cards WHERE card_number = ? AND status = "active"', [card_number], (err, row) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    
    if (!row) {
      res.status(404).json({ error: 'Gift card not found or inactive' });
      return;
    }
    
    if (row.balance < amount) {
      res.status(400).json({ error: 'Insufficient balance on gift card' });
      return;
    }
    
    const newBalance = row.balance - amount;
    const newStatus = newBalance <= 0 ? 'used' : 'active';
    const usedAt = newBalance <= 0 ? new Date().toISOString() : null;
    
    db.run('UPDATE gift_cards SET balance = ?, status = ?, used_at = ? WHERE card_number = ?',
      [newBalance, newStatus, usedAt, card_number],
      function(err) {
        if (err) {
          res.status(500).json({ error: err.message });
          return;
        }
        
        res.json({
          success: true,
          remaining_balance: newBalance,
          amount_redeemed: amount,
          status: newStatus
        });
      }
    );
  });
});

app.delete('/api/gift-cards/:id', (req, res) => {
  db.run('UPDATE gift_cards SET status = "cancelled" WHERE id = ?', req.params.id, function(err) {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json({ success: true });
  });
});

// Reporting API Endpoints
app.get('/api/reports/sales', (req, res) => {
  const { start_date, end_date } = req.query;
  
  let query = 'SELECT * FROM transactions WHERE 1=1';
  let params = [];
  
  if (start_date) {
    query += ' AND created_at >= ?';
    params.push(start_date);
  }
  
  if (end_date) {
    query += ' AND created_at <= ?';
    params.push(end_date + ' 23:59:59');
  }
  
  query += ' ORDER BY created_at DESC';
  
  db.all(query, params, (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    
    // Calculate summary statistics
    const totalSales = rows.reduce((sum, row) => sum + row.total, 0);
    const totalTransactions = rows.length;
    const avgTransaction = totalTransactions > 0 ? totalSales / totalTransactions : 0;
    
    // Group by payment method
    const paymentMethods = {};
    rows.forEach(row => {
      if (!paymentMethods[row.payment_method]) {
        paymentMethods[row.payment_method] = { count: 0, total: 0 };
      }
      paymentMethods[row.payment_method].count++;
      paymentMethods[row.payment_method].total += row.total;
    });
    
    res.json({
      transactions: rows,
      summary: {
        total_sales: totalSales,
        total_transactions: totalTransactions,
        average_transaction: avgTransaction,
        payment_methods: paymentMethods
      }
    });
  });
});

app.get('/api/reports/inventory', (req, res) => {
  db.all('SELECT * FROM products ORDER BY stock ASC', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    
    const totalProducts = rows.length;
    const totalValue = rows.reduce((sum, row) => sum + (row.price * row.stock), 0);
    const lowStockItems = rows.filter(row => row.stock <= 5);
    const outOfStockItems = rows.filter(row => row.stock <= 0);
    
    // Group by category
    const categories = {};
    rows.forEach(row => {
      if (!categories[row.category]) {
        categories[row.category] = { count: 0, total_stock: 0, total_value: 0 };
      }
      categories[row.category].count++;
      categories[row.category].total_stock += row.stock;
      categories[row.category].total_value += row.price * row.stock;
    });
    
    res.json({
      products: rows,
      summary: {
        total_products: totalProducts,
        total_inventory_value: totalValue,
        low_stock_count: lowStockItems.length,
        out_of_stock_count: outOfStockItems.length,
        categories: categories
      },
      alerts: {
        low_stock_items: lowStockItems,
        out_of_stock_items: outOfStockItems
      }
    });
  });
});

// Promotions API Endpoints
app.get('/api/promotions', (req, res) => {
  db.all('SELECT * FROM promotions ORDER BY created_at DESC', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json(rows);
  });
});

app.get('/api/promotions/active', (req, res) => {
  db.all('SELECT * FROM promotions WHERE is_active = 1 AND start_date <= datetime("now") AND end_date >= datetime("now")', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json(rows);
  });
});

app.post('/api/promotions', (req, res) => {
  const { name, description, discount_type, discount_value, min_purchase, max_discount, start_date, end_date } = req.body;
  
  db.run('INSERT INTO promotions (name, description, discount_type, discount_value, min_purchase, max_discount, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
    [name, description, discount_type, discount_value, min_purchase, max_discount, start_date, end_date],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ id: this.lastID, success: true });
    }
  );
});

// Loyalty API Endpoints
app.get('/api/members/search', (req, res) => {
  const { q } = req.query;
  if (!q) {
    res.json([]);
    return;
  }
  
  db.all('SELECT * FROM members WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? LIMIT 10',
    [`%${q}%`, `%${q}%`, `%${q}%`], (err, rows) => {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json(rows);
    });
});

app.post('/api/members/:id/redeem-points', (req, res) => {
  const { points } = req.body;
  const memberId = req.params.id;
  
  db.get('SELECT loyalty_points FROM members WHERE id = ?', [memberId], (err, member) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    
    if (!member || member.loyalty_points < points) {
      res.status(400).json({ error: 'Insufficient points' });
      return;
    }
    
    db.run('UPDATE members SET loyalty_points = loyalty_points - ? WHERE id = ?',
      [points, memberId], function(err) {
        if (err) {
          res.status(500).json({ error: err.message });
          return;
        }
        res.json({ success: true, points_redeemed: points });
      });
  });
});

// Categories API Endpoints
app.get('/api/categories', (req, res) => {
  db.all('SELECT * FROM categories WHERE is_active = 1 ORDER BY name', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json(rows);
  });
});

app.post('/api/categories', (req, res) => {
  const { name, description, parent_id } = req.body;
  
  db.run('INSERT INTO categories (name, description, parent_id) VALUES (?, ?, ?)',
    [name, description, parent_id || null],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ id: this.lastID, success: true });
    }
  );
});

app.put('/api/categories/:id', (req, res) => {
  const { name, description, parent_id } = req.body;
  
  db.run('UPDATE categories SET name = ?, description = ?, parent_id = ? WHERE id = ?',
    [name, description, parent_id || null, req.params.id],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ success: true, changes: this.changes });
    }
  );
});

app.delete('/api/categories/:id', (req, res) => {
  db.run('UPDATE categories SET is_active = 0 WHERE id = ?', req.params.id, function(err) {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json({ success: true });
  });
});

// Membership Tiers API Endpoints
app.get('/api/membership-tiers', (req, res) => {
  db.all('SELECT * FROM membership_tiers WHERE is_active = 1 ORDER BY price', (err, rows) => {
    if (err) {
      res.status(500).json({ error: err.message });
      return;
    }
    res.json(rows);
  });
});

app.post('/api/membership-tiers', (req, res) => {
  const { name, price, description, benefits, duration_months } = req.body;
  
  db.run('INSERT INTO membership_tiers (name, price, description, benefits, duration_months) VALUES (?, ?, ?, ?, ?)',
    [name, price, description, benefits, duration_months],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ id: this.lastID, success: true });
    }
  );
});

app.put('/api/membership-tiers/:id', (req, res) => {
  const { name, price, description, benefits, duration_months } = req.body;
  
  db.run('UPDATE membership_tiers SET name = ?, price = ?, description = ?, benefits = ?, duration_months = ? WHERE id = ?',
    [name, price, description, benefits, duration_months, req.params.id],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ success: true, changes: this.changes });
    }
  );
});

// Member search endpoint for unique ID, email, or phone
app.get('/api/members/search/:query', (req, res) => {
  const query = req.params.query;
  
  db.all('SELECT * FROM members WHERE unique_id LIKE ? OR email LIKE ? OR phone LIKE ? OR name LIKE ? ORDER BY name',
    [`%${query}%`, `%${query}%`, `%${query}%`, `%${query}%`],
    (err, rows) => {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json(rows);
    }
  );
});

// Products by category endpoint
app.get('/api/products/category/:categoryId', (req, res) => {
  db.all('SELECT p.*, c.name as category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.category_id = ? ORDER BY p.name',
    [req.params.categoryId],
    (err, rows) => {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json(rows);
    }
  );
});

app.listen(PORT, () => {
  console.log(`Nexus POS Server running on port ${PORT}`);
});