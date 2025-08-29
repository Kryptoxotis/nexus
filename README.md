# Nexus POS System

A comprehensive Point of Sale system that handles payments and manages multiple databases including inventory, memberships, events, and bookings. Built for scalability to support multiple locations with up to 6 cashiers per location.

## Features

### Core Functionality
- **Point of Sale**: Clean, intuitive interface for processing transactions
- **Inventory Management**: Add, delete, and track products with stock levels
- **Membership Database**: Customer management with different membership tiers
- **Events & Calendar**: Schedule and manage events with booking capacity
- **Multi-Payment Support**: Cash, card, digital wallet, and gift card options
- **PIN Security**: All database operations protected with PIN authentication (12345)

### 🎁 Gift Card System
- **Secure Generation**: Encrypted gift cards with unique numbers (GC + timestamp + random)
- **PIN-Protected Management**: Create, validate, and cancel gift cards securely
- **Real-time Validation**: Instant balance checking and fraud prevention
- **POS Integration**: Seamless gift card payments with automatic redemption
- **Hassle-free Usage**: Simple card number entry with instant validation

### 📊 Advanced Reporting & Analytics  
- **Sales Reports**: Comprehensive transaction analysis with date filtering
- **Inventory Analytics**: Real-time stock tracking with category breakdown
- **Revenue Insights**: Payment method analysis and trend tracking
- **Alert System**: Low stock and out-of-stock notifications
- **Data Export**: CSV and JSON export for external analysis
- **Visual Dashboard**: Modern charts and statistics overview

### Planned Features
- **Multi-location Support**: Manage multiple business locations
- **Multi-cashier Support**: Up to 6 concurrent cashiers per location  
- **Real-time Synchronization**: Live updates across all cashiers

## Technology Stack

- **Backend**: Node.js + Express + SQLite
- **Frontend**: React + TypeScript + Material-UI
- **Database**: SQLite with separate tables for products, members, events, transactions
- **Authentication**: PIN-based security system
- **UI/UX**: Modern Material-UI with responsive design

## Quick Start

1. **Install dependencies**:
   ```bash
   npm run install-all
   ```

2. **Start the development servers**:
   ```bash
   npm run dev
   ```

3. **Access the application**:
   - Frontend: http://localhost:3001
   - Backend API: http://localhost:5000

## Usage

### PIN Authentication
All database add/delete operations require PIN: **12345**

### Main Modules

1. **Point of Sale**
   - Select products and add to cart
   - Apply gift cards with real-time validation
   - Choose payment method (cash/card/digital/gift card)
   - Process transactions with automatic stock updates

2. **Inventory Management** 
   - Add new products (requires PIN)
   - Delete products (requires PIN)
   - Track stock levels with low-stock warnings
   - Category-based organization

3. **Membership Management**
   - Add customers (requires PIN)
   - Delete members (requires PIN)
   - Manage membership tiers (basic, premium, gold, platinum)
   - Contact information tracking

4. **Events & Calendar**
   - Schedule events (requires PIN)
   - Delete events (requires PIN)
   - Track capacity and bookings
   - Event status monitoring

5. **🎁 Gift Card Management**
   - Create encrypted gift cards (requires PIN)
   - Validate card numbers and balances
   - Cancel/deactivate cards (requires PIN)
   - Track usage and transaction history

6. **📊 Reports & Analytics**
   - Generate sales reports with date filtering
   - View inventory analytics and alerts
   - Export data in CSV/JSON formats
   - Monitor payment method trends

## Database Schema

### Products
- ID, name, price, category, stock, created_at

### Members  
- ID, name, email, phone, membership_type, joined_date

### Events
- ID, title, description, event_date, capacity, booked, created_at

### Transactions
- ID, items, total, payment_method, created_at

## Development Status

✅ **Phase 1**: Single-location POS with database management
✅ **Phase 2**: Gift cards and advanced reporting system  
🔄 **Phase 3**: Multi-location support with 6-cashier capability
🔄 **Phase 4**: Real-time synchronization and scaling

## Contributing

This project is actively being developed. Current focus is on expanding to multi-location support with concurrent cashier functionality.

## License

MIT License