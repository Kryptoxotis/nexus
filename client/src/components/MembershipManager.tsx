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
import { Add, Delete, Edit, People, Email, Phone } from '@mui/icons-material';
import PinDialog from './PinDialog';

interface Member {
  id: number;
  name: string;
  email: string;
  phone: string;
  membership_type: string;
  joined_date: string;
}

interface MembershipManagerProps {
  pinRequired?: boolean;
}

const MembershipManager: React.FC<MembershipManagerProps> = ({ pinRequired = true }) => {
  const [members, setMembers] = useState<Member[]>([]);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [showPinDialog, setShowPinDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{ type: 'add' | 'edit' | 'delete'; memberId?: number }>({ type: 'add' });
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [editingMember, setEditingMember] = useState<Member | null>(null);
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

  const getMembershipColor = (type: string) => {
    switch (type) {
      case 'premium':
        return 'primary';
      case 'gold':
        return 'warning';
      case 'platinum':
        return 'secondary';
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
                    <TableCell>Joined Date</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {members.map((member) => (
                    <TableRow key={member.id}>
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
                      <TableCell>{formatDate(member.joined_date)}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          color="primary"
                          onClick={() => handleEditMember(member)}
                          size="small"
                          sx={{ mr: 1 }}
                        >
                          <Edit />
                        </IconButton>
                        <IconButton
                          color="error"
                          onClick={() => handleDeleteMember(member.id)}
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