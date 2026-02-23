import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  CssBaseline,
  ThemeProvider,
  Switch,
  FormControlLabel,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  IconButton,
  Badge,
  Chip,
  Slide,
  Fade,
  Zoom,
  useTheme,
  alpha,
} from '@mui/material';
import theme from './theme';
import {
  PointOfSale,
  Inventory,
  People,
  Event,
  Receipt,
  CardGiftcard,
  Assessment,
  Security,
  SecurityUpdateGood,
  Star,
  Menu as MenuIcon,
  Close,
  Notifications,
  Settings,
  TrendingUp,
  ShoppingCart,
  History,
} from '@mui/icons-material';
import PosInterface from './components/PosInterface';
import InventoryManager from './components/InventoryManager';
import MembershipManager from './components/MembershipManager';
import EventsManager from './components/EventsManager';
import GiftCardManager from './components/GiftCardManager';
import ReportsManager from './components/ReportsManager';
import LoyaltyManager from './components/LoyaltyManager';
import TransactionHistory from './components/TransactionHistory';

type View = 'pos' | 'inventory' | 'members' | 'events' | 'gift-cards' | 'reports' | 'loyalty' | 'transactions';

const menuItems = [
  { key: 'pos', label: 'Point of Sale', icon: PointOfSale, color: 'primary', badge: null },
  { key: 'inventory', label: 'Inventory', icon: Inventory, color: 'info', badge: '12 Low Stock' },
  { key: 'members', label: 'Members', icon: People, color: 'secondary', badge: '5 New' },
  { key: 'events', label: 'Events', icon: Event, color: 'warning', badge: '3 Today' },
  { key: 'gift-cards', label: 'Gift Cards', icon: CardGiftcard, color: 'secondary', badge: null },
  { key: 'reports', label: 'Reports', icon: Assessment, color: 'success', badge: null },
  { key: 'transactions', label: 'Transaction History', icon: History, color: 'info', badge: null },
  { key: 'loyalty', label: 'Loyalty', icon: Star, color: 'warning', badge: '2 Expiring' },
] as const;

const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<View>('pos'); // Start with POS as default
  const [pinRequired, setPinRequired] = useState(true);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const muiTheme = useTheme();

  const currentMenuItem = menuItems.find(item => item.key === currentView);

  const handleViewChange = (view: View) => {
    setCurrentView(view);
    setSidebarOpen(false);
  };

  const renderContent = () => {
    const props = { pinRequired };
    switch (currentView) {
      case 'pos':
        return <PosInterface {...props} />;
      case 'inventory':
        return <InventoryManager {...props} />;
      case 'members':
        return <MembershipManager {...props} />;
      case 'events':
        return <EventsManager {...props} />;
      case 'gift-cards':
        return <GiftCardManager {...props} />;
      case 'reports':
        return <ReportsManager />;
      case 'transactions':
        return <TransactionHistory />;
      case 'loyalty':
        return <LoyaltyManager {...props} />;
    }
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      
      {/* Enhanced App Bar */}
      <AppBar 
        position="static" 
        elevation={0}
        sx={{
          background: 'linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 50%, #2a2a2a 100%)',
          backdropFilter: 'blur(10px)',
          borderBottom: '1px solid',
          borderBottomImage: 'linear-gradient(90deg, transparent, #00d4ff, #00ff88, transparent)',
        }}
      >
        <Toolbar sx={{ minHeight: '70px !important' }}>
          <IconButton
            color="inherit"
            edge="start"
            onClick={() => setSidebarOpen(true)}
            sx={{ 
              mr: 2,
              background: 'linear-gradient(135deg, #00ff88, #1dd1a1)',
              color: '#000',
              '&:hover': {
                background: 'linear-gradient(135deg, #66ffaa, #55efc4)',
                transform: 'scale(1.05)',
              },
              transition: 'all 0.3s ease',
            }}
          >
            <MenuIcon />
          </IconButton>
          
          <Box 
            component="img" 
            src="/nexus-logo.png" 
            alt="Nexus Logo" 
            sx={{ 
              height: 32, 
              width: 'auto', 
              mr: 2,
              filter: 'drop-shadow(0 0 8px rgba(0, 212, 255, 0.4)) drop-shadow(0 0 4px rgba(0, 255, 136, 0.3))'
            }} 
          />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 600 }}>
            Nexus
            {currentMenuItem && (
              <Chip
                icon={<currentMenuItem.icon />}
                label={currentMenuItem.label}
                size="small"
                sx={{ 
                  ml: 2,
                  background: `linear-gradient(45deg, ${muiTheme.palette[currentMenuItem.color].main}, ${muiTheme.palette[currentMenuItem.color].light})`,
                  color: currentMenuItem.color === 'primary' ? '#000' : '#fff',
                }}
              />
            )}
          </Typography>

          {/* Quick Stats */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mr: 3 }}>
            <Chip
              icon={<TrendingUp />}
              label="$1,247 Today"
              size="small"
              color="success"
            />
            <Chip
              icon={<ShoppingCart />}
              label="23 Orders"
              size="small"
              color="info"
            />
          </Box>

          <FormControlLabel
            control={
              <Switch
                checked={!pinRequired}
                onChange={(e) => setPinRequired(!e.target.checked)}
                color="primary"
              />
            }
            label={
              <Box display="flex" alignItems="center" gap={1}>
                {pinRequired ? <Security fontSize="small" /> : <SecurityUpdateGood fontSize="small" />}
                <Typography variant="body2">
                  {pinRequired ? 'PIN Required' : 'PIN Disabled'}
                </Typography>
              </Box>
            }
            sx={{ mr: 2 }}
          />
          
          <IconButton color="inherit" sx={{ mr: 1 }}>
            <Badge badgeContent={3} color="error">
              <Notifications />
            </Badge>
          </IconButton>
          
          <IconButton color="inherit">
            <Settings />
          </IconButton>
        </Toolbar>
      </AppBar>

      {/* Enhanced Sidebar Navigation */}
      <Drawer
        anchor="left"
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        PaperProps={{
          sx: {
            width: 300,
            background: 'linear-gradient(180deg, #0a0a0a 0%, #1a1a1a 50%, #2a2a2a 100%)',
            border: 'none',
            borderRight: '1px solid #333',
          }
        }}
      >
        <Box sx={{ p: 2, borderBottom: '1px solid #333' }}>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Typography variant="h6" sx={{ color: '#00ff88', fontWeight: 600 }}>
              Navigation
            </Typography>
            <IconButton onClick={() => setSidebarOpen(false)} sx={{ color: '#fff' }}>
              <Close />
            </IconButton>
          </Box>
        </Box>
        
        <List sx={{ pt: 2 }}>
          {menuItems.map((item, index) => (
            <Slide key={item.key} direction="right" in={sidebarOpen} timeout={300 + index * 100}>
              <ListItem
                onClick={() => handleViewChange(item.key as View)}
                sx={{
                  cursor: 'pointer',
                  mx: 1,
                  mb: 1,
                  borderRadius: '12px',
                  transition: 'all 0.3s ease',
                  ...(currentView === item.key && {
                    background: `linear-gradient(135deg, ${alpha(muiTheme.palette[item.color].main, 0.2)}, ${alpha(muiTheme.palette[item.color].light, 0.1)})`,
                    border: `1px solid ${muiTheme.palette[item.color].main}`,
                    '&:hover': {
                      background: `linear-gradient(135deg, ${alpha(muiTheme.palette[item.color].main, 0.3)}, ${alpha(muiTheme.palette[item.color].light, 0.2)})`,
                    }
                  }),
                  '&:hover': {
                    background: alpha(muiTheme.palette[item.color].main, 0.1),
                    transform: 'translateX(8px)',
                  }
                }}
              >
                <ListItemIcon>
                  <item.icon sx={{ color: muiTheme.palette[item.color].main }} />
                </ListItemIcon>
                <ListItemText 
                  primary={item.label}
                  primaryTypographyProps={{
                    fontWeight: currentView === item.key ? 600 : 400,
                    color: currentView === item.key ? muiTheme.palette[item.color].main : '#fff',
                  }}
                />
                {item.badge && (
                  <Chip
                    label={item.badge}
                    size="small"
                    color={item.color}
                    sx={{ fontSize: '0.7rem' }}
                  />
                )}
              </ListItem>
            </Slide>
          ))}
        </List>

        {/* Quick Actions at Bottom */}
        <Box sx={{ p: 2, mt: 'auto', borderTop: '1px solid #333' }}>
          <Typography variant="caption" sx={{ color: '#888', mb: 1, display: 'block' }}>
            Quick Actions
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <Button
              size="small"
              startIcon={<Receipt />}
              variant="outlined"
              sx={{ fontSize: '0.7rem' }}
              onClick={() => handleViewChange('pos')}
            >
              New Sale
            </Button>
            <Button
              size="small"
              startIcon={<Inventory />}
              variant="outlined"
              sx={{ fontSize: '0.7rem' }}
              onClick={() => handleViewChange('inventory')}
            >
              Add Product
            </Button>
          </Box>
        </Box>
      </Drawer>

      {/* Main Content with Smooth Transitions */}
      <Box 
        sx={{ 
          minHeight: 'calc(100vh - 70px)', 
          bgcolor: 'background.default',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <Fade in={true} timeout={600}>
          <Box sx={{ height: '100%' }}>
            {renderContent()}
          </Box>
        </Fade>
        
        {/* Floating Action Button for Quick POS Access */}
        {currentView !== 'pos' && (
          <Zoom in={true} timeout={400} style={{ transitionDelay: '400ms' }}>
            <IconButton
              onClick={() => handleViewChange('pos')}
              sx={{
                position: 'fixed',
                bottom: 24,
                right: 24,
                background: 'linear-gradient(135deg, #00ff88, #1dd1a1)',
                color: '#000',
                width: 64,
                height: 64,
                boxShadow: '0 8px 16px rgba(0, 255, 136, 0.4)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #66ffaa, #55efc4)',
                  transform: 'scale(1.1)',
                  boxShadow: '0 12px 24px rgba(0, 255, 136, 0.6)',
                },
                transition: 'all 0.3s ease',
              }}
            >
              <PointOfSale sx={{ fontSize: 28 }} />
            </IconButton>
          </Zoom>
        )}
      </Box>
    </ThemeProvider>
  );
};

export default App;
