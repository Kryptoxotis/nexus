import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Container,
  Grid,
  Card,
  CardContent,
  Button,
  Box,
  CssBaseline,
  ThemeProvider,
  Switch,
  FormControlLabel,
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
} from '@mui/icons-material';
import PosInterface from './components/PosInterface';
import InventoryManager from './components/InventoryManager';
import MembershipManager from './components/MembershipManager';
import EventsManager from './components/EventsManager';
import GiftCardManager from './components/GiftCardManager';
import ReportsManager from './components/ReportsManager';
import LoyaltyManager from './components/LoyaltyManager';

// Theme imported from separate file

type View = 'home' | 'pos' | 'inventory' | 'members' | 'events' | 'gift-cards' | 'reports' | 'loyalty';

const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<View>('home');
  const [pinRequired, setPinRequired] = useState(true);

  const renderContent = () => {
    switch (currentView) {
      case 'pos':
        return <PosInterface pinRequired={pinRequired} />;
      case 'inventory':
        return <InventoryManager pinRequired={pinRequired} />;
      case 'members':
        return <MembershipManager pinRequired={pinRequired} />;
      case 'events':
        return <EventsManager pinRequired={pinRequired} />;
      case 'gift-cards':
        return <GiftCardManager pinRequired={pinRequired} />;
      case 'reports':
        return <ReportsManager />;
      case 'loyalty':
        return <LoyaltyManager pinRequired={pinRequired} />;
      default:
        return (
          <Container maxWidth="lg" sx={{ mt: 4 }}>
            <Typography variant="h4" gutterBottom align="center" color="primary">
              Welcome to Nexus POS
            </Typography>
            <Typography variant="h6" gutterBottom align="center" color="text.secondary" sx={{ mb: 4 }}>
              Your complete business management solution
            </Typography>
            
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 3,
                    },
                  }}
                  onClick={() => setCurrentView('pos')}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <PointOfSale sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
                    <Typography variant="h6" gutterBottom>
                      Point of Sale
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Process transactions and payments
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 3,
                    },
                  }}
                  onClick={() => setCurrentView('inventory')}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <Inventory sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
                    <Typography variant="h6" gutterBottom>
                      Inventory
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Manage products and stock
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 3,
                    },
                  }}
                  onClick={() => setCurrentView('members')}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <People sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
                    <Typography variant="h6" gutterBottom>
                      Members
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Manage customer memberships
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 3,
                    },
                  }}
                  onClick={() => setCurrentView('events')}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <Event sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
                    <Typography variant="h6" gutterBottom>
                      Events
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Manage calendar and bookings
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 3,
                    },
                  }}
                  onClick={() => setCurrentView('gift-cards')}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <CardGiftcard sx={{ fontSize: 48, color: 'secondary.main', mb: 2 }} />
                    <Typography variant="h6" gutterBottom>
                      Gift Cards
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Create and manage gift cards
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 3,
                    },
                  }}
                  onClick={() => setCurrentView('reports')}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <Assessment sx={{ fontSize: 48, color: 'success.main', mb: 2 }} />
                    <Typography variant="h6" gutterBottom>
                      Reports
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      View sales and inventory reports
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 3,
                    },
                  }}
                  onClick={() => setCurrentView('loyalty')}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <Star sx={{ fontSize: 48, color: 'secondary.main', mb: 2 }} />
                    <Typography variant="h6" gutterBottom>
                      Loyalty & Promotions
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Manage rewards and promotional campaigns
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Container>
        );
    }
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AppBar position="static" elevation={2}>
        <Toolbar>
          <Receipt sx={{ mr: 2 }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Nexus POS
          </Typography>
          
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
          
          {currentView !== 'home' && (
            <Button color="inherit" onClick={() => setCurrentView('home')}>
              Home
            </Button>
          )}
        </Toolbar>
      </AppBar>
      
      <Box sx={{ minHeight: 'calc(100vh - 64px)', bgcolor: 'background.default', py: 2 }}>
        {renderContent()}
      </Box>
    </ThemeProvider>
  );
};

export default App;
