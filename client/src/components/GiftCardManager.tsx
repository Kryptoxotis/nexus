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
  InputAdornment,
} from '@mui/material';
import { Add, Delete, CardGiftcard, Search, ContentCopy, Visibility } from '@mui/icons-material';
import PinDialog from './PinDialog';

interface GiftCard {
  id: number;
  card_number: string;
  balance: number;
  original_amount: number;
  status: string;
  created_at: string;
  recipient_name: string;
  notes: string;
}

interface Member {
  id: number;
  unique_id: string;
  name: string;
  email: string;
  phone: string;
  membership_type: string;
}

interface GiftCardManagerProps {
  pinRequired?: boolean;
}

const GiftCardManager: React.FC<GiftCardManagerProps> = ({ pinRequired = true }) => {
  const [giftCards, setGiftCards] = useState<GiftCard[]>([]);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showPinDialog, setShowPinDialog] = useState(false);
  const [showValidateDialog, setShowValidateDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{ type: 'add' | 'delete'; cardId?: number }>({ type: 'add' });
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [validationResult, setValidationResult] = useState<any>(null);
  const [newGiftCard, setNewGiftCard] = useState({
    amount: '',
    recipient_name: '',
    notes: '',
  });
  const [validateCardNumber, setValidateCardNumber] = useState('');
  const [memberSearch, setMemberSearch] = useState('');
  const [memberSearchResults, setMemberSearchResults] = useState<Member[]>([]);
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);

  useEffect(() => {
    fetchGiftCards();
  }, []);

  useEffect(() => {
    if (memberSearch.length > 2) {
      searchMembers();
    } else {
      setMemberSearchResults([]);
    }
  }, [memberSearch]);

  const fetchGiftCards = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/gift-cards');
      const data = await response.json();
      setGiftCards(data);
    } catch (err) {
      console.error('Failed to fetch gift cards:', err);
    }
  };

  const searchMembers = async () => {
    try {
      const response = await fetch(`http://localhost:5000/api/members/search?q=${encodeURIComponent(memberSearch)}`);
      const data = await response.json();
      setMemberSearchResults(data);
    } catch (err) {
      console.error('Failed to search members:', err);
    }
  };

  const handleAddGiftCard = () => {
    setPendingAction({ type: 'add' });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowAddDialog(true);
    }
  };

  const handleDeleteGiftCard = (cardId: number) => {
    setPendingAction({ type: 'delete', cardId });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      deleteGiftCard(cardId);
    }
  };

  const executePendingAction = async () => {
    if (pendingAction.type === 'add') {
      setShowAddDialog(true);
    } else if (pendingAction.type === 'delete' && pendingAction.cardId) {
      await deleteGiftCard(pendingAction.cardId);
    }
  };

  const addGiftCard = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/gift-cards', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          amount: parseFloat(newGiftCard.amount),
          recipient_name: newGiftCard.recipient_name,
          notes: newGiftCard.notes,
        }),
      });

      if (response.ok) {
        const result = await response.json();
        setAlert({ 
          type: 'success', 
          message: `Gift card created! Card Number: ${result.card_number}` 
        });
        setNewGiftCard({ amount: '', recipient_name: '', notes: '' });
        setShowAddDialog(false);
        fetchGiftCards();
      } else {
        setAlert({ type: 'error', message: 'Failed to create gift card' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const deleteGiftCard = async (cardId: number) => {
    try {
      const response = await fetch(`http://localhost:5000/api/gift-cards/${cardId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Gift card cancelled successfully!' });
        fetchGiftCards();
      } else {
        setAlert({ type: 'error', message: 'Failed to cancel gift card' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const validateGiftCard = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/gift-cards/validate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ card_number: validateCardNumber }),
      });

      const result = await response.json();
      
      if (response.ok) {
        setValidationResult(result);
      } else {
        setValidationResult({ error: result.error });
      }
    } catch (err) {
      setValidationResult({ error: 'Connection error' });
    }
  };

  const handleAddDialogSubmit = () => {
    if (!newGiftCard.amount || !newGiftCard.recipient_name) {
      setAlert({ type: 'error', message: 'Amount and recipient name are required' });
      return;
    }
    
    addGiftCard();
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    setAlert({ type: 'success', message: 'Copied to clipboard!' });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active':
        return 'success';
      case 'used':
        return 'default';
      case 'cancelled':
        return 'error';
      default:
        return 'default';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Container maxWidth="lg">
      <Box display="flex" alignItems="center" gap={2} mb={3}>
        <CardGiftcard color="primary" />
        <Typography variant="h5" color="primary">
          Gift Card Management
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

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Create New Gift Card
              </Typography>
              <Button
                variant="contained"
                startIcon={<Add />}
                onClick={handleAddGiftCard}
                fullWidth
              >
                Create Gift Card
              </Button>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Validate Gift Card
              </Typography>
              <Box display="flex" gap={1}>
                <TextField
                  size="small"
                  placeholder="Enter card number"
                  value={validateCardNumber}
                  onChange={(e) => setValidateCardNumber(e.target.value)}
                  sx={{ flexGrow: 1 }}
                />
                <Button
                  variant="outlined"
                  onClick={() => setShowValidateDialog(true)}
                  startIcon={<Search />}
                >
                  Check
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Typography variant="h6">
              Gift Cards ({giftCards.length})
            </Typography>
          </Box>

          {giftCards.length === 0 ? (
            <Typography color="text.secondary" textAlign="center" py={4}>
              No gift cards found. Create your first gift card!
            </Typography>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Card Number</TableCell>
                    <TableCell>Recipient</TableCell>
                    <TableCell align="right">Original Amount</TableCell>
                    <TableCell align="right">Balance</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {giftCards.map((card) => (
                    <TableRow key={card.id}>
                      <TableCell>
                        <Box display="flex" alignItems="center" gap={1}>
                          <Typography variant="body2" fontFamily="monospace">
                            {card.card_number}
                          </Typography>
                          <IconButton
                            size="small"
                            onClick={() => copyToClipboard(card.card_number)}
                          >
                            <ContentCopy fontSize="small" />
                          </IconButton>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Box>
                          <Typography variant="body2">{card.recipient_name}</Typography>
                          {card.notes && (
                            <Typography variant="caption" color="text.secondary">
                              {card.notes}
                            </Typography>
                          )}
                        </Box>
                      </TableCell>
                      <TableCell align="right">${card.original_amount.toFixed(2)}</TableCell>
                      <TableCell align="right">
                        <Typography
                          variant="body2"
                          color={card.balance > 0 ? 'success.main' : 'text.secondary'}
                          fontWeight={card.balance > 0 ? 'bold' : 'normal'}
                        >
                          ${card.balance.toFixed(2)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={card.status.toUpperCase()}
                          color={getStatusColor(card.status)}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{formatDate(card.created_at)}</TableCell>
                      <TableCell align="right">
                        {card.status === 'active' && (
                          <IconButton
                            color="error"
                            onClick={() => handleDeleteGiftCard(card.id)}
                            size="small"
                          >
                            <Delete />
                          </IconButton>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* Add Gift Card Dialog */}
      <Dialog open={showAddDialog} onClose={() => setShowAddDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Gift Card</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Amount"
                type="number"
                fullWidth
                value={newGiftCard.amount}
                onChange={(e) => setNewGiftCard({ ...newGiftCard, amount: e.target.value })}
                InputProps={{
                  startAdornment: <InputAdornment position="start">$</InputAdornment>,
                }}
                inputProps={{ step: '0.01', min: '1' }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <Typography variant="body2" gutterBottom>
                Recipient
              </Typography>
              {selectedMember ? (
                <Box sx={{ p: 2, bgcolor: 'primary.light', borderRadius: 1, mb: 1 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Box>
                      <Typography variant="body2" fontWeight="bold">
                        {selectedMember.name}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        ID: {selectedMember.unique_id} | {selectedMember.email}
                      </Typography>
                      {selectedMember.phone && (
                        <Typography variant="caption" color="text.secondary" display="block">
                          Phone: {selectedMember.phone}
                        </Typography>
                      )}
                    </Box>
                    <Button 
                      size="small" 
                      onClick={() => {
                        setSelectedMember(null);
                        setMemberSearch('');
                        setNewGiftCard({ ...newGiftCard, recipient_name: '' });
                      }}
                    >
                      Remove
                    </Button>
                  </Box>
                </Box>
              ) : (
                <Box>
                  <TextField
                    size="small"
                    placeholder="Search by name, ID, email, or phone..."
                    value={memberSearch}
                    onChange={(e) => setMemberSearch(e.target.value)}
                    fullWidth
                    sx={{ mb: 1 }}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <Search />
                        </InputAdornment>
                      ),
                    }}
                  />
                  {memberSearchResults.map((member) => (
                    <Box
                      key={member.id}
                      sx={{
                        p: 1.5,
                        bgcolor: 'background.paper',
                        border: '1px solid',
                        borderColor: 'divider',
                        borderRadius: 1,
                        cursor: 'pointer',
                        mb: 0.5,
                        '&:hover': { bgcolor: 'action.hover' },
                      }}
                      onClick={() => {
                        setSelectedMember(member);
                        setMemberSearch('');
                        setMemberSearchResults([]);
                        setNewGiftCard({ ...newGiftCard, recipient_name: member.name });
                      }}
                    >
                      <Typography variant="body2" fontWeight="bold">
                        {member.name} ({member.membership_type})
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        ID: {member.unique_id} | {member.email}
                      </Typography>
                      {member.phone && (
                        <Typography variant="caption" color="text.secondary" display="block">
                          Phone: {member.phone}
                        </Typography>
                      )}
                    </Box>
                  ))}
                  <TextField
                    label="Or enter name manually"
                    fullWidth
                    value={newGiftCard.recipient_name}
                    onChange={(e) => setNewGiftCard({ ...newGiftCard, recipient_name: e.target.value })}
                    sx={{ mt: 1 }}
                  />
                </Box>
              )}
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Notes (Optional)"
                fullWidth
                multiline
                rows={2}
                value={newGiftCard.notes}
                onChange={(e) => setNewGiftCard({ ...newGiftCard, notes: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowAddDialog(false)}>Cancel</Button>
          <Button onClick={handleAddDialogSubmit} variant="contained">
            Create Gift Card
          </Button>
        </DialogActions>
      </Dialog>

      {/* Validate Gift Card Dialog */}
      <Dialog open={showValidateDialog} onClose={() => setShowValidateDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Gift Card Validation</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <TextField
              label="Card Number"
              fullWidth
              value={validateCardNumber}
              onChange={(e) => setValidateCardNumber(e.target.value)}
              sx={{ mb: 2 }}
            />
            <Button
              variant="contained"
              onClick={validateGiftCard}
              fullWidth
              startIcon={<Visibility />}
            >
              Validate Card
            </Button>
            
            {validationResult && (
              <Box sx={{ mt: 2 }}>
                {validationResult.error ? (
                  <Alert severity="error">{validationResult.error}</Alert>
                ) : (
                  <Alert severity="success">
                    <Typography variant="body2">
                      <strong>Card Number:</strong> {validationResult.card_number}<br/>
                      <strong>Balance:</strong> ${validationResult.balance.toFixed(2)}<br/>
                      <strong>Original Amount:</strong> ${validationResult.original_amount.toFixed(2)}
                    </Typography>
                  </Alert>
                )}
              </Box>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setShowValidateDialog(false);
            setValidationResult(null);
            setValidateCardNumber('');
          }}>
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* PIN Dialog */}
      {pinRequired && (
        <PinDialog
          open={showPinDialog}
          onClose={() => setShowPinDialog(false)}
          onSuccess={executePendingAction}
          title={pendingAction.type === 'add' ? 'Create Gift Card' : 'Cancel Gift Card'}
        />
      )}
    </Container>
  );
};

export default GiftCardManager;