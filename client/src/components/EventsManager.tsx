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
} from '@mui/material';
import { Add, Delete, Event as EventIcon, CalendarToday } from '@mui/icons-material';
import PinDialog from './PinDialog';

interface Event {
  id: number;
  title: string;
  description: string;
  event_date: string;
  capacity: number;
  booked: number;
  created_at: string;
}

interface EventsManagerProps {
  pinRequired?: boolean;
}

const EventsManager: React.FC<EventsManagerProps> = ({ pinRequired = true }) => {
  const [events, setEvents] = useState<Event[]>([]);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showPinDialog, setShowPinDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{ type: 'add' | 'delete'; eventId?: number }>({ type: 'add' });
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [newEvent, setNewEvent] = useState({
    title: '',
    description: '',
    event_date: '',
    capacity: '',
  });

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/events');
      const data = await response.json();
      setEvents(data);
    } catch (err) {
      console.error('Failed to fetch events:', err);
    }
  };

  const handleAddEvent = () => {
    setPendingAction({ type: 'add' });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowAddDialog(true);
    }
  };

  const handleDeleteEvent = (eventId: number) => {
    setPendingAction({ type: 'delete', eventId });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      deleteEvent(eventId);
    }
  };

  const executePendingAction = async () => {
    if (pendingAction.type === 'add') {
      setShowAddDialog(true);
    } else if (pendingAction.type === 'delete' && pendingAction.eventId) {
      await deleteEvent(pendingAction.eventId);
    }
  };

  const addEvent = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/events', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title: newEvent.title,
          description: newEvent.description,
          event_date: newEvent.event_date,
          capacity: parseInt(newEvent.capacity),
        }),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Event added successfully!' });
        setNewEvent({ title: '', description: '', event_date: '', capacity: '' });
        setShowAddDialog(false);
        fetchEvents();
      } else {
        setAlert({ type: 'error', message: 'Failed to add event' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const deleteEvent = async (eventId: number) => {
    try {
      const response = await fetch(`http://localhost:5000/api/events/${eventId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Event deleted successfully!' });
        fetchEvents();
      } else {
        setAlert({ type: 'error', message: 'Failed to delete event' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const handleAddDialogSubmit = () => {
    if (!newEvent.title || !newEvent.event_date || !newEvent.capacity) {
      setAlert({ type: 'error', message: 'Title, date, and capacity are required' });
      return;
    }
    
    addEvent();
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const getEventStatus = (event: Event) => {
    const now = new Date();
    const eventDate = new Date(event.event_date);
    const isFullyBooked = event.booked >= event.capacity;
    const isPast = eventDate < now;

    if (isPast) {
      return { label: 'Past', color: 'default' as const };
    } else if (isFullyBooked) {
      return { label: 'Full', color: 'error' as const };
    } else {
      return { label: 'Available', color: 'success' as const };
    }
  };

  const getAvailableSpots = (event: Event) => {
    return Math.max(0, event.capacity - event.booked);
  };

  return (
    <Container maxWidth="lg">
      <Box display="flex" alignItems="center" gap={2} mb={3}>
        <CalendarToday color="primary" />
        <Typography variant="h5" color="primary">
          Events & Calendar Management
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
              Events ({events.length})
            </Typography>
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={handleAddEvent}
            >
              Add Event
            </Button>
          </Box>

          {events.length === 0 ? (
            <Typography color="text.secondary" textAlign="center" py={4}>
              No events found. Schedule your first event!
            </Typography>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Event Title</TableCell>
                    <TableCell>Date & Time</TableCell>
                    <TableCell>Capacity</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Available Spots</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {events.map((event) => {
                    const status = getEventStatus(event);
                    const availableSpots = getAvailableSpots(event);
                    
                    return (
                      <TableRow key={event.id}>
                        <TableCell>
                          <Box>
                            <Typography variant="subtitle1">{event.title}</Typography>
                            {event.description && (
                              <Typography variant="body2" color="text.secondary">
                                {event.description}
                              </Typography>
                            )}
                          </Box>
                        </TableCell>
                        <TableCell>{formatDateTime(event.event_date)}</TableCell>
                        <TableCell>
                          {event.booked}/{event.capacity}
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={status.label}
                            color={status.color}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Box
                            component="span"
                            sx={{
                              color: availableSpots === 0 ? 'error.main' : 'text.primary',
                              fontWeight: availableSpots <= 5 ? 'bold' : 'normal',
                            }}
                          >
                            {availableSpots}
                            {availableSpots <= 5 && availableSpots > 0 && ' (Few left)'}
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          <IconButton
                            color="error"
                            onClick={() => handleDeleteEvent(event.id)}
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

      {/* Add Event Dialog */}
      <Dialog open={showAddDialog} onClose={() => setShowAddDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Event</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Event Title"
                fullWidth
                value={newEvent.title}
                onChange={(e) => setNewEvent({ ...newEvent, title: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Description"
                fullWidth
                multiline
                rows={3}
                value={newEvent.description}
                onChange={(e) => setNewEvent({ ...newEvent, description: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Event Date & Time"
                type="datetime-local"
                fullWidth
                value={newEvent.event_date}
                onChange={(e) => setNewEvent({ ...newEvent, event_date: e.target.value })}
                InputLabelProps={{
                  shrink: true,
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Capacity"
                type="number"
                fullWidth
                value={newEvent.capacity}
                onChange={(e) => setNewEvent({ ...newEvent, capacity: e.target.value })}
                inputProps={{ min: '1' }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowAddDialog(false)}>Cancel</Button>
          <Button onClick={handleAddDialogSubmit} variant="contained">
            Add Event
          </Button>
        </DialogActions>
      </Dialog>

      {/* PIN Dialog */}
      {pinRequired && (
        <PinDialog
          open={showPinDialog}
          onClose={() => setShowPinDialog(false)}
          onSuccess={executePendingAction}
          title={pendingAction.type === 'add' ? 'Add Event' : 'Delete Event'}
        />
      )}
    </Container>
  );
};

export default EventsManager;