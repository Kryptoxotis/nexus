# Nexus POS System

A comprehensive Point of Sale system that handles payments and manages multiple databases including inventory, memberships, events, and bookings. Built for scalability to support multiple locations with up to 6 cashiers per location.

## Features

### Core Functionality
- **Point of Sale**: Clean, intuitive interface for processing transactions
- **Inventory Management**: Add, delete, and track products with stock levels
- **Membership Database**: Customer management with different membership tiers
- **Events & Calendar**: Schedule and manage events with booking capacity
- **Multi-Payment Support**: Cash, card, and digital wallet options
- **PIN Security**: All database operations protected with PIN authentication (12345)

### Planned Features
- **Multi-location Support**: Manage multiple business locations
- **Multi-cashier Support**: Up to 6 concurrent cashiers per location  
- **Gift Card System**: Secure gift card management
- **Reporting System**: Location-based reports and analytics
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
   - Choose payment method (cash/card/digital)
   - Process transactions

2. **Inventory Management** 
   - Add new products (requires PIN)
   - Delete products (requires PIN)
   - Track stock levels with low-stock warnings

3. **Membership Management**
   - Add customers (requires PIN)
   - Delete members (requires PIN)
   - Manage membership tiers (basic, premium, gold, platinum)

4. **Events & Calendar**
   - Schedule events (requires PIN)
   - Delete events (requires PIN)
   - Track capacity and bookings

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
🔄 **Phase 2**: Multi-location support and gift cards
🔄 **Phase 3**: Advanced reporting and analytics
🔄 **Phase 4**: Real-time synchronization and scaling

## Contributing

This project is actively being developed. Current focus is on expanding to multi-location support with concurrent cashier functionality.

## License

MIT License