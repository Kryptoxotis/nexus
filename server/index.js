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
  const cipher = crypto.createCipher('aes-256-cbc', GIFT_CARD_KEY);
  let encrypted = cipher.update(JSON.stringify(data), 'utf8', 'hex');
  encrypted += cipher.final('hex');
  return iv.toString('hex') + ':' + encrypted;
}

// Helper function to decrypt gift card data
function decryptGiftCard(encryptedData) {
  try {
    const parts = encryptedData.split(':');
    const iv = Buffer.from(parts[0], 'hex');
    const decipher = crypto.createDecipher('aes-256-cbc', GIFT_CARD_KEY);
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

  db.run(`CREATE TABLE IF NOT EXISTS members (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    phone TEXT,
    membership_type TEXT DEFAULT 'basic',
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
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS gift_cards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    card_number TEXT UNIQUE NOT NULL,
    encrypted_data TEXT NOT NULL,
    balance REAL NOT NULL,
    original_amount REAL NOT NULL,
    status TEXT DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    used_at DATETIME,
    recipient_name TEXT,
    notes TEXT
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
  const { name, price, category, stock } = req.body;
  db.run('INSERT INTO products (name, price, category, stock) VALUES (?, ?, ?, ?)',
    [name, price, category, stock],
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
  db.run('INSERT INTO members (name, email, phone, membership_type) VALUES (?, ?, ?, ?)',
    [name, email, phone, membership_type],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ id: this.lastID, success: true });
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
  const { items, total, payment_method, gift_card_used } = req.body;
  db.run('INSERT INTO transactions (items, total, payment_method, gift_card_used) VALUES (?, ?, ?, ?)',
    [JSON.stringify(items), total, payment_method, gift_card_used],
    function(err) {
      if (err) {
        res.status(500).json({ error: err.message });
        return;
      }
      res.json({ id: this.lastID, success: true });
    }
  );
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

app.listen(PORT, () => {
  console.log(`Nexus POS Server running on port ${PORT}`);
});