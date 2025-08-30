import React, { useState, useEffect } from 'react';
import {
  Container,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  IconButton,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  InputAdornment,
} from '@mui/material';
import { Add, Delete, Edit, LocalOffer, Star, Percent } from '@mui/icons-material';
import PinDialog from './PinDialog';

interface Promotion {
  id: number;
  name: string;
  description: string;
  discount_type: 'percentage' | 'fixed_amount';
  discount_value: number;
  min_purchase: number;
  start_date: string;
  end_date: string;
  is_active: boolean;
  usage_limit?: number;
  usage_count: number;
  created_at: string;
}

interface LoyaltyRule {
  id: number;
  name: string;
  points_per_dollar: number;
  min_purchase: number;
  bonus_multiplier: number;
  membership_type: string;
  is_active: boolean;
}

interface LoyaltyManagerProps {
  pinRequired?: boolean;
}

const LoyaltyManager: React.FC<LoyaltyManagerProps> = ({ pinRequired = true }) => {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [loyaltyRules, setLoyaltyRules] = useState<LoyaltyRule[]>([]);
  const [showPromotionDialog, setShowPromotionDialog] = useState(false);
  const [showLoyaltyDialog, setShowLoyaltyDialog] = useState(false);
  const [showPinDialog, setShowPinDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{ type: 'add-promotion' | 'add-loyalty' | 'delete-promotion' | 'delete-loyalty'; id?: number }>({ type: 'add-promotion' });
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [newPromotion, setNewPromotion] = useState({
    name: '',
    description: '',
    discount_type: 'percentage',
    discount_value: '',
    min_purchase: '',
    start_date: '',
    end_date: '',
    usage_limit: '',
  });
  const [newLoyaltyRule, setNewLoyaltyRule] = useState({
    name: '',
    points_per_dollar: '',
    min_purchase: '',
    bonus_multiplier: '',
    membership_type: 'basic',
  });

  useEffect(() => {
    fetchPromotions();
    fetchLoyaltyRules();
  }, []);

  const fetchPromotions = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/promotions');
      const data = await response.json();
      setPromotions(data);
    } catch (err) {
      console.error('Failed to fetch promotions:', err);
    }
  };

  const fetchLoyaltyRules = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/loyalty-rules');
      const data = await response.json();
      setLoyaltyRules(data);
    } catch (err) {
      console.error('Failed to fetch loyalty rules:', err);
    }
  };

  const handleAddPromotion = () => {
    setPendingAction({ type: 'add-promotion' });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowPromotionDialog(true);
    }
  };

  const handleAddLoyaltyRule = () => {
    setPendingAction({ type: 'add-loyalty' });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowLoyaltyDialog(true);
    }
  };

  const handleDeletePromotion = (id: number) => {
    setPendingAction({ type: 'delete-promotion', id });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      deletePromotion(id);
    }
  };

  const handleDeleteLoyaltyRule = (id: number) => {
    setPendingAction({ type: 'delete-loyalty', id });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      deleteLoyaltyRule(id);
    }
  };

  const executePendingAction = async () => {
    if (pendingAction.type === 'add-promotion') {
      setShowPromotionDialog(true);
    } else if (pendingAction.type === 'add-loyalty') {
      setShowLoyaltyDialog(true);
    } else if (pendingAction.type === 'delete-promotion' && pendingAction.id) {
      await deletePromotion(pendingAction.id);
    } else if (pendingAction.type === 'delete-loyalty' && pendingAction.id) {
      await deleteLoyaltyRule(pendingAction.id);
    }
  };

  const addPromotion = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/promotions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: newPromotion.name,
          description: newPromotion.description,
          discount_type: newPromotion.discount_type,
          discount_value: parseFloat(newPromotion.discount_value),
          min_purchase: parseFloat(newPromotion.min_purchase) || 0,
          start_date: newPromotion.start_date,
          end_date: newPromotion.end_date,
          usage_limit: newPromotion.usage_limit ? parseInt(newPromotion.usage_limit) : null,
        }),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Promotion added successfully!' });
        setNewPromotion({ name: '', description: '', discount_type: 'percentage', discount_value: '', min_purchase: '', start_date: '', end_date: '', usage_limit: '' });
        setShowPromotionDialog(false);
        fetchPromotions();
      } else {
        setAlert({ type: 'error', message: 'Failed to add promotion' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const addLoyaltyRule = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/loyalty-rules', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: newLoyaltyRule.name,
          points_per_dollar: parseFloat(newLoyaltyRule.points_per_dollar),
          min_purchase: parseFloat(newLoyaltyRule.min_purchase) || 0,
          bonus_multiplier: parseFloat(newLoyaltyRule.bonus_multiplier) || 1,
          membership_type: newLoyaltyRule.membership_type,
        }),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Loyalty rule added successfully!' });
        setNewLoyaltyRule({ name: '', points_per_dollar: '', min_purchase: '', bonus_multiplier: '', membership_type: 'basic' });
        setShowLoyaltyDialog(false);
        fetchLoyaltyRules();
      } else {
        setAlert({ type: 'error', message: 'Failed to add loyalty rule' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const deletePromotion = async (id: number) => {
    try {
      const response = await fetch(`http://localhost:5000/api/promotions/${id}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Promotion deleted successfully!' });
        fetchPromotions();
      } else {
        setAlert({ type: 'error', message: 'Failed to delete promotion' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const deleteLoyaltyRule = async (id: number) => {
    try {
      const response = await fetch(`http://localhost:5000/api/loyalty-rules/${id}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Loyalty rule deleted successfully!' });
        fetchLoyaltyRules();
      } else {
        setAlert({ type: 'error', message: 'Failed to delete loyalty rule' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const getPromotionStatus = (promotion: Promotion) => {
    const now = new Date();
    const startDate = new Date(promotion.start_date);
    const endDate = new Date(promotion.end_date);
    
    if (!promotion.is_active) {
      return { label: 'Inactive', color: 'default' as const };
    } else if (now < startDate) {
      return { label: 'Scheduled', color: 'info' as const };
    } else if (now > endDate) {
      return { label: 'Expired', color: 'error' as const };
    } else if (promotion.usage_limit && promotion.usage_count >= promotion.usage_limit) {
      return { label: 'Limit Reached', color: 'warning' as const };
    } else {
      return { label: 'Active', color: 'success' as const };
    }
  };

  return (
    <Container maxWidth="lg">
      <Box display="flex" alignItems="center" gap={2} mb={3}>
        <Star color="primary" />
        <Typography variant="h5" color="primary">
          Loyalty & Promotions Management
        </Typography>
      </Box>

      {alert && (
        <Alert
          severity={alert.type}
          onClose={() => setAlert(null)}
          sx={{ mb: 2 }}
        >
          {alert.message}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Promotions Section */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Box display="flex" alignItems="center" gap={1}>
                  <Percent color="secondary" />
                  <Typography variant="h6">
                    Promotions ({promotions.length})
                  </Typography>
                </Box>
                <Button
                  variant="contained"
                  startIcon={<Add />}
                  onClick={handleAddPromotion}
                  color="secondary"
                >
                  Add Promotion
                </Button>
              </Box>

              {promotions.length === 0 ? (
                <Typography color="text.secondary" textAlign="center" py={4}>
                  No promotions found. Create your first promotion!
                </Typography>
              ) : (
                <TableContainer component={Paper} variant="outlined">
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Name</TableCell>
                        <TableCell>Discount</TableCell>
                        <TableCell>Min Purchase</TableCell>
                        <TableCell>Valid Period</TableCell>
                        <TableCell>Usage</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell align="right">Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {promotions.map((promotion) => {
                        const status = getPromotionStatus(promotion);
                        return (
                          <TableRow key={promotion.id}>
                            <TableCell>
                              <Box>
                                <Typography variant="subtitle2">{promotion.name}</Typography>
                                <Typography variant="body2" color="text.secondary">
                                  {promotion.description}
                                </Typography>
                              </Box>
                            </TableCell>
                            <TableCell>
                              {promotion.discount_type === 'percentage' 
                                ? `${promotion.discount_value}%` 
                                : `$${promotion.discount_value.toFixed(2)}`
                              }
                            </TableCell>
                            <TableCell>${promotion.min_purchase.toFixed(2)}</TableCell>
                            <TableCell>
                              <Typography variant="body2">
                                {formatDate(promotion.start_date)} - {formatDate(promotion.end_date)}
                              </Typography>
                            </TableCell>
                            <TableCell>
                              {promotion.usage_limit 
                                ? `${promotion.usage_count}/${promotion.usage_limit}` 
                                : `${promotion.usage_count} uses`
                              }
                            </TableCell>
                            <TableCell>
                              <Chip
                                label={status.label}
                                color={status.color}
                                size="small"
                              />
                            </TableCell>
                            <TableCell align="right">
                              <IconButton
                                color="error"
                                onClick={() => handleDeletePromotion(promotion.id)}
                                size="small"
                              >
                                <Delete />
                              </IconButton>
                            </TableCell>
                          </TableRow>
                        );
                      })}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Loyalty Rules Section */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Box display="flex" alignItems="center" gap={1}>
                  <LocalOffer color="primary" />
                  <Typography variant="h6">
                    Loyalty Point Rules ({loyaltyRules.length})
                  </Typography>
                </Box>
                <Button
                  variant="contained"
                  startIcon={<Add />}
                  onClick={handleAddLoyaltyRule}
                >
                  Add Loyalty Rule
                </Button>
              </Box>

              {loyaltyRules.length === 0 ? (
                <Typography color="text.secondary" textAlign="center" py={4}>
                  No loyalty rules found. Create your first loyalty rule!
                </Typography>
              ) : (
                <TableContainer component={Paper} variant="outlined">
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Rule Name</TableCell>
                        <TableCell>Membership Type</TableCell>
                        <TableCell>Points per $1</TableCell>
                        <TableCell>Min Purchase</TableCell>
                        <TableCell>Bonus Multiplier</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell align="right">Actions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {loyaltyRules.map((rule) => (
                        <TableRow key={rule.id}>
                          <TableCell>
                            <Typography variant="subtitle2">{rule.name}</Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={rule.membership_type.toUpperCase()}
                              size="small"
                              color="primary"
                            />
                          </TableCell>
                          <TableCell>{rule.points_per_dollar}</TableCell>
                          <TableCell>${rule.min_purchase.toFixed(2)}</TableCell>
                          <TableCell>{rule.bonus_multiplier}x</TableCell>
                          <TableCell>
                            <Chip
                              label={rule.is_active ? 'Active' : 'Inactive'}
                              color={rule.is_active ? 'success' : 'default'}
                              size="small"
                            />
                          </TableCell>
                          <TableCell align="right">
                            <IconButton
                              color="error"
                              onClick={() => handleDeleteLoyaltyRule(rule.id)}
                              size="small"
                            >
                              <Delete />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Add Promotion Dialog */}
      <Dialog open={showPromotionDialog} onClose={() => setShowPromotionDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Promotion</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Promotion Name"
                fullWidth
                value={newPromotion.name}
                onChange={(e) => setNewPromotion({ ...newPromotion, name: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Description"
                fullWidth
                multiline
                rows={2}
                value={newPromotion.description}
                onChange={(e) => setNewPromotion({ ...newPromotion, description: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth>
                <InputLabel>Discount Type</InputLabel>
                <Select
                  value={newPromotion.discount_type}
                  onChange={(e) => setNewPromotion({ ...newPromotion, discount_type: e.target.value })}
                  label="Discount Type"
                >
                  <MenuItem value="percentage">Percentage</MenuItem>
                  <MenuItem value="fixed_amount">Fixed Amount</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Discount Value"
                type="number"
                fullWidth
                value={newPromotion.discount_value}
                onChange={(e) => setNewPromotion({ ...newPromotion, discount_value: e.target.value })}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      {newPromotion.discount_type === 'percentage' ? '%' : '$'}
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Minimum Purchase"
                type="number"
                fullWidth
                value={newPromotion.min_purchase}
                onChange={(e) => setNewPromotion({ ...newPromotion, min_purchase: e.target.value })}
                InputProps={{
                  startAdornment: <InputAdornment position="start">$</InputAdornment>,
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Usage Limit (Optional)"
                type="number"
                fullWidth
                value={newPromotion.usage_limit}
                onChange={(e) => setNewPromotion({ ...newPromotion, usage_limit: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Start Date"
                type="date"
                fullWidth
                value={newPromotion.start_date}
                onChange={(e) => setNewPromotion({ ...newPromotion, start_date: e.target.value })}
                InputLabelProps={{
                  shrink: true,
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="End Date"
                type="date"
                fullWidth
                value={newPromotion.end_date}
                onChange={(e) => setNewPromotion({ ...newPromotion, end_date: e.target.value })}
                InputLabelProps={{
                  shrink: true,
                }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowPromotionDialog(false)}>Cancel</Button>
          <Button onClick={addPromotion} variant="contained" color="secondary">
            Add Promotion
          </Button>
        </DialogActions>
      </Dialog>

      {/* Add Loyalty Rule Dialog */}
      <Dialog open={showLoyaltyDialog} onClose={() => setShowLoyaltyDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Loyalty Rule</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Rule Name"
                fullWidth
                value={newLoyaltyRule.name}
                onChange={(e) => setNewLoyaltyRule({ ...newLoyaltyRule, name: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth>
                <InputLabel>Membership Type</InputLabel>
                <Select
                  value={newLoyaltyRule.membership_type}
                  onChange={(e) => setNewLoyaltyRule({ ...newLoyaltyRule, membership_type: e.target.value })}
                  label="Membership Type"
                >
                  <MenuItem value="basic">Basic</MenuItem>
                  <MenuItem value="premium">Premium</MenuItem>
                  <MenuItem value="gold">Gold</MenuItem>
                  <MenuItem value="platinum">Platinum</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Points per $1"
                type="number"
                fullWidth
                value={newLoyaltyRule.points_per_dollar}
                onChange={(e) => setNewLoyaltyRule({ ...newLoyaltyRule, points_per_dollar: e.target.value })}
                inputProps={{ step: '0.1', min: '0' }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Minimum Purchase"
                type="number"
                fullWidth
                value={newLoyaltyRule.min_purchase}
                onChange={(e) => setNewLoyaltyRule({ ...newLoyaltyRule, min_purchase: e.target.value })}
                InputProps={{
                  startAdornment: <InputAdornment position="start">$</InputAdornment>,
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Bonus Multiplier"
                type="number"
                fullWidth
                value={newLoyaltyRule.bonus_multiplier}
                onChange={(e) => setNewLoyaltyRule({ ...newLoyaltyRule, bonus_multiplier: e.target.value })}
                inputProps={{ step: '0.1', min: '1' }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowLoyaltyDialog(false)}>Cancel</Button>
          <Button onClick={addLoyaltyRule} variant="contained">
            Add Loyalty Rule
          </Button>
        </DialogActions>
      </Dialog>

      {/* PIN Dialog */}
      {pinRequired && (
        <PinDialog
          open={showPinDialog}
          onClose={() => setShowPinDialog(false)}
          onSuccess={executePendingAction}
          title={
            pendingAction.type === 'add-promotion' 
              ? 'Add Promotion'
              : pendingAction.type === 'add-loyalty'
                ? 'Add Loyalty Rule'
                : pendingAction.type === 'delete-promotion'
                  ? 'Delete Promotion'
                  : 'Delete Loyalty Rule'
          }
        />
      )}
    </Container>
  );
};

export default LoyaltyManager;