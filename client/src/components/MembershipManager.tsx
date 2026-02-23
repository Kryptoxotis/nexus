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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
} from '@mui/material';
import { Add, Delete, Edit, People, Email, Phone, CardGiftcard, AccountCircle, AddCircle, RemoveCircle } from '@mui/icons-material';
import PinDialog from './PinDialog';

interface Member {
  id: number;
  unique_id?: string;
  name: string;
  email: string;
  phone: string;
  membership_type: string;
  loyalty_points: number;
  total_spent: number;
  joined_date: string;
}

interface MembershipManagerProps {
  pinRequired?: boolean;
}

const MembershipManager: React.FC<MembershipManagerProps> = ({ pinRequired = true }) => {
  const [members, setMembers] = useState<Member[]>([]);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [showMemberDetailDialog, setShowMemberDetailDialog] = useState(false);
  const [showPinDialog, setShowPinDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{ type: 'add' | 'edit' | 'delete' | 'detail'; memberId?: number }>({ type: 'add' });
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [editingMember, setEditingMember] = useState<Member | null>(null);
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);
  const [pointsAdjustment, setPointsAdjustment] = useState('');
  const [pointsReason, setPointsReason] = useState('');
  const [giftCardAmount, setGiftCardAmount] = useState('');
  const [newMember, setNewMember] = useState({
    name: '',
    email: '',
    phone: '',
    membership_type: 'basic',
  });

  useEffect(() => {
    fetchMembers();
  }, []);

  const fetchMembers = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/members');
      const data = await response.json();
      setMembers(data);
    } catch (err) {
      console.error('Failed to fetch members:', err);
    }
  };

  const handleAddMember = () => {
    setPendingAction({ type: 'add' });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowAddDialog(true);
    }
  };

  const handleEditMember = (member: Member) => {
    setEditingMember(member);
    setPendingAction({ type: 'edit', memberId: member.id });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowEditDialog(true);
    }
  };

  const handleDeleteMember = (memberId: number) => {
    setPendingAction({ type: 'delete', memberId });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      deleteMember(memberId);
    }
  };

  const handleMemberDetail = (member: Member) => {
    setSelectedMember(member);
    setShowMemberDetailDialog(true);
  };

  const executePendingAction = async () => {
    if (pendingAction.type === 'add') {
      setShowAddDialog(true);
    } else if (pendingAction.type === 'edit') {
      setShowEditDialog(true);
    } else if (pendingAction.type === 'delete' && pendingAction.memberId) {
      await deleteMember(pendingAction.memberId);
    }
  };

  const addMember = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/members', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newMember),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Member added successfully!' });
        setNewMember({ name: '', email: '', phone: '', membership_type: 'basic' });
        setShowAddDialog(false);
        fetchMembers();
      } else {
        setAlert({ type: 'error', message: 'Failed to add member' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const updateMember = async () => {
    if (!editingMember) return;
    
    try {
      const response = await fetch(`http://localhost:5000/api/members/${editingMember.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: editingMember.name,
          email: editingMember.email,
          phone: editingMember.phone,
          membership_type: editingMember.membership_type,
        }),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Member updated successfully!' });
        setEditingMember(null);
        setShowEditDialog(false);
        fetchMembers();
      } else {
        setAlert({ type: 'error', message: 'Failed to update member' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const deleteMember = async (memberId: number) => {
    try {
      const response = await fetch(`http://localhost:5000/api/members/${memberId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Member deleted successfully!' });
        fetchMembers();
      } else {
        setAlert({ type: 'error', message: 'Failed to delete member' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const handleAddDialogSubmit = () => {
    if (!newMember.name || !newMember.email) {
      setAlert({ type: 'error', message: 'Name and email are required' });
      return;
    }
    
    addMember();
  };

  const adjustMemberPoints = async () => {
    if (!selectedMember || !pointsAdjustment) return;
    
    try {
      const response = await fetch(`http://localhost:5000/api/members/${selectedMember.id}/points`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          points_change: parseInt(pointsAdjustment),
          reason: pointsReason || 'Manual adjustment',
        }),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Points updated successfully!' });
        setPointsAdjustment('');
        setPointsReason('');
        fetchMembers();
        // Update selected member
        const updatedMember = { ...selectedMember, loyalty_points: selectedMember.loyalty_points + parseInt(pointsAdjustment) };
        setSelectedMember(updatedMember);
      } else {
        setAlert({ type: 'error', message: 'Failed to update points' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const createMemberGiftCard = async () => {
    if (!selectedMember || !giftCardAmount) return;
    
    try {
      const response = await fetch('http://localhost:5000/api/gift-cards', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          amount: parseFloat(giftCardAmount),
          recipient_name: selectedMember.name,
          notes: `Gift card created for member: ${selectedMember.name}`,
        }),
      });

      if (response.ok) {
        const result = await response.json();
        setAlert({ type: 'success', message: `Gift card created! Card number: ${result.card_number}` });
        setGiftCardAmount('');
      } else {
        setAlert({ type: 'error', message: 'Failed to create gift card' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const getMembershipColor = (type: string) => {
    switch (type) {
      case 'premium':
        return 'info';
      case 'gold':
        return 'warning';
      case 'platinum':
        return 'secondary';
      default:
        return 'success';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Container maxWidth="lg">
      <Box display="flex" alignItems="center" gap={2} mb={3}>
        <People color="primary" />
        <Typography variant="h5" color="primary">
          Membership Management
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

      <Card>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Typography variant="h6">
              Members ({members.length})
            </Typography>
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={handleAddMember}
            >
              Add Member
            </Button>
          </Box>

          {members.length === 0 ? (
            <Typography color="text.secondary" textAlign="center" py={4}>
              No members found. Add your first member!
            </Typography>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Contact</TableCell>
                    <TableCell>Membership Type</TableCell>
                    <TableCell>Points</TableCell>
                    <TableCell>Joined Date</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {members.map((member) => (
                    <TableRow 
                      key={member.id}
                      sx={{ 
                        cursor: 'pointer',
                        '&:hover': { backgroundColor: 'action.hover' }
                      }}
                      onClick={() => handleMemberDetail(member)}
                    >
                      <TableCell>{member.name}</TableCell>
                      <TableCell>
                        <Box>
                          <Box display="flex" alignItems="center" gap={1}>
                            <Email fontSize="small" color="action" />
                            <Typography variant="body2">{member.email}</Typography>
                          </Box>
                          {member.phone && (
                            <Box display="flex" alignItems="center" gap={1} mt={0.5}>
                              <Phone fontSize="small" color="action" />
                              <Typography variant="body2">{member.phone}</Typography>
                            </Box>
                          )}
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={member.membership_type.toUpperCase()}
                          color={getMembershipColor(member.membership_type)}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Box display="flex" alignItems="center" gap={1}>
                          <Typography variant="body2" fontWeight="bold" color="primary.main">
                            {member.loyalty_points || 0}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            pts
                          </Typography>
                        </Box>
                        <Typography variant="caption" color="text.secondary">
                          Total spent: ${(member.total_spent || 0).toFixed(2)}
                        </Typography>
                      </TableCell>
                      <TableCell>{formatDate(member.joined_date)}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          color="primary"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleEditMember(member);
                          }}
                          size="small"
                          sx={{ mr: 1 }}
                        >
                          <Edit />
                        </IconButton>
                        <IconButton
                          color="error"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteMember(member.id);
                          }}
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

      {/* Add Member Dialog */}
      <Dialog open={showAddDialog} onClose={() => setShowAddDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Member</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Full Name"
                fullWidth
                value={newMember.name}
                onChange={(e) => setNewMember({ ...newMember, name: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Email Address"
                type="email"
                fullWidth
                value={newMember.email}
                onChange={(e) => setNewMember({ ...newMember, email: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Phone Number"
                fullWidth
                value={newMember.phone}
                onChange={(e) => setNewMember({ ...newMember, phone: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <FormControl fullWidth>
                <InputLabel>Membership Type</InputLabel>
                <Select
                  value={newMember.membership_type}
                  onChange={(e) => setNewMember({ ...newMember, membership_type: e.target.value })}
                  label="Membership Type"
                >
                  <MenuItem value="basic">Basic</MenuItem>
                  <MenuItem value="premium">Premium</MenuItem>
                  <MenuItem value="gold">Gold</MenuItem>
                  <MenuItem value="platinum">Platinum</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowAddDialog(false)}>Cancel</Button>
          <Button onClick={handleAddDialogSubmit} variant="contained">
            Add Member
          </Button>
        </DialogActions>
      </Dialog>

      {/* Edit Member Dialog */}
      <Dialog open={showEditDialog} onClose={() => setShowEditDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Member</DialogTitle>
        <DialogContent>
          {editingMember && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid size={{ xs: 12 }}>
                <TextField
                  label="Full Name"
                  fullWidth
                  value={editingMember.name}
                  onChange={(e) => setEditingMember({ ...editingMember, name: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12 }}>
                <TextField
                  label="Email Address"
                  type="email"
                  fullWidth
                  value={editingMember.email}
                  onChange={(e) => setEditingMember({ ...editingMember, email: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12 }}>
                <TextField
                  label="Phone Number"
                  fullWidth
                  value={editingMember.phone}
                  onChange={(e) => setEditingMember({ ...editingMember, phone: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12 }}>
                <FormControl fullWidth>
                  <InputLabel>Membership Type</InputLabel>
                  <Select
                    value={editingMember.membership_type}
                    onChange={(e) => setEditingMember({ ...editingMember, membership_type: e.target.value })}
                    label="Membership Type"
                  >
                    <MenuItem value="basic">Basic</MenuItem>
                    <MenuItem value="premium">Premium</MenuItem>
                    <MenuItem value="gold">Gold</MenuItem>
                    <MenuItem value="platinum">Platinum</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowEditDialog(false)}>Cancel</Button>
          <Button onClick={updateMember} variant="contained">
            Update Member
          </Button>
        </DialogActions>
      </Dialog>

      {/* Member Detail Dialog */}
      <Dialog 
        open={showMemberDetailDialog} 
        onClose={() => setShowMemberDetailDialog(false)} 
        maxWidth="md" 
        fullWidth
      >
        {selectedMember && (
          <>
            <DialogTitle>
              <Box display="flex" alignItems="center" gap={2}>
                <AccountCircle color="primary" />
                <Box>
                  <Typography variant="h6">{selectedMember.name}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    ID: {selectedMember.unique_id} • {selectedMember.membership_type.toUpperCase()}
                  </Typography>
                </Box>
              </Box>
            </DialogTitle>
            <DialogContent>
              <Grid container spacing={3}>
                {/* Member Info Card */}
                <Grid size={{ xs: 12, md: 6 }}>
                  <Card variant="outlined" sx={{ 
                    p: 2, 
                    background: 'linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%)',
                    border: '1px solid #3498db'
                  }}>
                    <Typography variant="h6" gutterBottom sx={{ 
                      background: 'linear-gradient(45deg, #3498db, #5dade2)',
                      WebkitBackgroundClip: 'text',
                      WebkitTextFillColor: 'transparent',
                      backgroundClip: 'text'
                    }}>
                      Member Information
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">Email</Typography>
                      <Typography variant="body1">{selectedMember.email}</Typography>
                    </Box>
                    {selectedMember.phone && (
                      <Box sx={{ mb: 2 }}>
                        <Typography variant="body2" color="text.secondary">Phone</Typography>
                        <Typography variant="body1">{selectedMember.phone}</Typography>
                      </Box>
                    )}
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">Joined</Typography>
                      <Typography variant="body1">{formatDate(selectedMember.joined_date)}</Typography>
                    </Box>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">Total Spent</Typography>
                      <Typography variant="h6" color="primary">
                        ${(selectedMember.total_spent || 0).toFixed(2)}
                      </Typography>
                    </Box>
                  </Card>
                </Grid>

                {/* Points Management Card */}
                <Grid size={{ xs: 12, md: 6 }}>
                  <Card variant="outlined" sx={{ 
                    p: 2, 
                    background: 'linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%)',
                    border: '1px solid #8e44ad'
                  }}>
                    <Typography variant="h6" gutterBottom sx={{ 
                      background: 'linear-gradient(45deg, #8e44ad, #9b59b6)',
                      WebkitBackgroundClip: 'text',
                      WebkitTextFillColor: 'transparent',
                      backgroundClip: 'text'
                    }}>
                      Loyalty Points
                    </Typography>
                    <Box sx={{ mb: 3, textAlign: 'center' }}>
                      <Typography variant="h4" color="secondary.main" fontWeight="bold">
                        {selectedMember.loyalty_points || 0}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Available Points
                      </Typography>
                    </Box>
                    
                    <Typography variant="body2" gutterBottom>
                      Adjust Points
                    </Typography>
                    <Box display="flex" gap={1} mb={2}>
                      <TextField
                        size="small"
                        type="number"
                        placeholder="±points"
                        value={pointsAdjustment}
                        onChange={(e) => setPointsAdjustment(e.target.value)}
                        sx={{ flexGrow: 1 }}
                      />
                      <Button 
                        variant="outlined" 
                        onClick={adjustMemberPoints}
                        disabled={!pointsAdjustment}
                        startIcon={pointsAdjustment.startsWith('-') ? <RemoveCircle /> : <AddCircle />}
                      >
                        Apply
                      </Button>
                    </Box>
                    <TextField
                      size="small"
                      fullWidth
                      placeholder="Reason (optional)"
                      value={pointsReason}
                      onChange={(e) => setPointsReason(e.target.value)}
                      sx={{ mb: 2 }}
                    />
                  </Card>
                </Grid>

                {/* Gift Card Creation */}
                <Grid size={{ xs: 12 }}>
                  <Card variant="outlined" sx={{ 
                    p: 2, 
                    background: 'linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%)',
                    border: '1px solid #1dd1a1'
                  }}>
                    <Box display="flex" alignItems="center" gap={1} mb={2}>
                      <CardGiftcard sx={{ color: '#1dd1a1' }} />
                      <Typography variant="h6" sx={{ 
                        background: 'linear-gradient(45deg, #00ff88, #1dd1a1)',
                        WebkitBackgroundClip: 'text',
                        WebkitTextFillColor: 'transparent',
                        backgroundClip: 'text'
                      }}>
                        Create Gift Card
                      </Typography>
                    </Box>
                    <Box display="flex" gap={2} alignItems="center">
                      <TextField
                        label="Gift Card Amount"
                        type="number"
                        value={giftCardAmount}
                        onChange={(e) => setGiftCardAmount(e.target.value)}
                        inputProps={{ step: '0.01', min: '1' }}
                        sx={{ width: 200 }}
                        InputProps={{
                          startAdornment: <Typography>$</Typography>,
                        }}
                      />
                      <Button
                        variant="contained"
                        onClick={createMemberGiftCard}
                        disabled={!giftCardAmount}
                        startIcon={<CardGiftcard />}
                      >
                        Create Gift Card
                      </Button>
                    </Box>
                    <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                      Gift card will be created with member's name as recipient
                    </Typography>
                  </Card>
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setShowMemberDetailDialog(false)}>Close</Button>
              <Button 
                onClick={() => {
                  setShowMemberDetailDialog(false);
                  handleEditMember(selectedMember);
                }} 
                variant="contained" 
                startIcon={<Edit />}
              >
                Edit Member
              </Button>
            </DialogActions>
          </>
        )}
      </Dialog>

      {/* PIN Dialog */}
      {pinRequired && (
        <PinDialog
          open={showPinDialog}
          onClose={() => setShowPinDialog(false)}
          onSuccess={executePendingAction}
          title={
            pendingAction.type === 'add' 
              ? 'Add Member' 
              : pendingAction.type === 'edit' 
                ? 'Edit Member' 
                : 'Delete Member'
          }
        />
      )}
    </Container>
  );
};

export default MembershipManager;