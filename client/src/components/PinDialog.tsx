import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Typography,
  Box
} from '@mui/material';
import LockIcon from '@mui/icons-material/Lock';

interface PinDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  title: string;
}

const PinDialog: React.FC<PinDialogProps> = ({ open, onClose, onSuccess, title }) => {
  const [pin, setPin] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/verify-pin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ pin }),
      });

      const data = await response.json();
      
      if (data.success) {
        setPin('');
        setError('');
        onSuccess();
        onClose();
      } else {
        setError('Invalid PIN');
      }
    } catch (err) {
      setError('Connection error');
    }
  };

  const handleClose = () => {
    setPin('');
    setError('');
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <LockIcon color="primary" />
          {title}
        </Box>
      </DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Enter PIN to proceed with this action
        </Typography>
        <TextField
          autoFocus
          label="PIN"
          type="password"
          fullWidth
          variant="outlined"
          value={pin}
          onChange={(e) => setPin(e.target.value)}
          error={!!error}
          helperText={error}
          onKeyPress={(e) => {
            if (e.key === 'Enter') {
              handleSubmit();
            }
          }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={!pin}>
          Verify
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default PinDialog;