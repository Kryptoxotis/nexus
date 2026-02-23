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
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import { Add, Delete, Event as EventIcon, CalendarToday, ViewList, CalendarMonth } from '@mui/icons-material';
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
  const [viewMode, setViewMode] = useState<'table' | 'calendar'>('table');
  const [currentMonth, setCurrentMonth] = useState(new Date());
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

  const getDaysInMonth = (date: Date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    const days = [];
    // Add empty cells for days before the first day of the month
    for (let i = 0; i < startingDayOfWeek; i++) {
      days.push(null);
    }
    // Add days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      days.push(day);
    }
    return days;
  };

  const getEventsForDate = (date: Date, day: number) => {
    const targetDate = new Date(date.getFullYear(), date.getMonth(), day);
    return events.filter(event => {
      const eventDate = new Date(event.event_date);
      return eventDate.toDateString() === targetDate.toDateString();
    });
  };

  const navigateMonth = (direction: 'prev' | 'next') => {
    const newMonth = new Date(currentMonth);
    if (direction === 'prev') {
      newMonth.setMonth(newMonth.getMonth() - 1);
    } else {
      newMonth.setMonth(newMonth.getMonth() + 1);
    }
    setCurrentMonth(newMonth);
  };

  const renderCalendarView = () => {
    const days = getDaysInMonth(currentMonth);
    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    return (
      <Box>
        {/* Calendar Header */}
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Button onClick={() => navigateMonth('prev')} variant="outlined" size="small">
            ← Previous
          </Button>
          <Typography variant="h6">
            {monthNames[currentMonth.getMonth()]} {currentMonth.getFullYear()}
          </Typography>
          <Button onClick={() => navigateMonth('next')} variant="outlined" size="small">
            Next →
          </Button>
        </Box>

        {/* Calendar Grid */}
        <Paper variant="outlined">
          <Grid container>
            {/* Day headers */}
            {dayNames.map((dayName) => (
              <Grid size={{ xs: 12/7 }} key={dayName}>
                <Box 
                  p={1} 
                  textAlign="center" 
                  sx={{ 
                    backgroundColor: 'grey.100',
                    fontWeight: 'bold',
                    borderBottom: '1px solid',
                    borderColor: 'divider'
                  }}
                >
                  <Typography variant="caption">{dayName}</Typography>
                </Box>
              </Grid>
            ))}
            
            {/* Calendar days */}
            {days.map((day, index) => {
              const dayEvents = day ? getEventsForDate(currentMonth, day) : [];
              
              return (
                <Grid size={{ xs: 12/7 }} key={index}>
                  <Box
                    p={1}
                    minHeight={100}
                    sx={{
                      border: '1px solid',
                      borderColor: 'divider',
                      backgroundColor: day ? 'background.paper' : 'grey.50',
                      cursor: day ? 'pointer' : 'default',
                      '&:hover': day ? { backgroundColor: 'grey.50' } : {},
                    }}
                    onClick={() => {
                      if (day) {
                        const selectedDate = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
                        const isoString = selectedDate.toISOString().slice(0, 16);
                        setNewEvent({ ...newEvent, event_date: isoString });
                        handleAddEvent();
                      }
                    }}
                  >
                    {day && (
                      <>
                        <Typography variant="body2" mb={0.5}>
                          {day}
                        </Typography>
                        {dayEvents.map((event) => (
                          <Box
                            key={event.id}
                            sx={{
                              backgroundColor: 'primary.main',
                              color: 'primary.contrastText',
                              borderRadius: 1,
                              p: 0.5,
                              mb: 0.5,
                              fontSize: '0.75rem',
                              cursor: 'pointer',
                              '&:hover': { backgroundColor: 'primary.dark' }
                            }}
                            onClick={(e) => {
                              e.stopPropagation();
                              // Could add event details popup here
                            }}
                          >
                            <Typography variant="caption" sx={{ display: 'block', lineHeight: 1.2 }}>
                              {event.title}
                            </Typography>
                            <Typography variant="caption" sx={{ opacity: 0.8 }}>
                              {new Date(event.event_date).toLocaleTimeString([], {
                                hour: '2-digit',
                                minute: '2-digit'
                              })}
                            </Typography>
                          </Box>
                        ))}
                      </>
                    )}
                  </Box>
                </Grid>
              );
            })}
          </Grid>
        </Paper>
      </Box>
    );
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
            <Box display="flex" gap={2} alignItems="center">
              <ToggleButtonGroup
                value={viewMode}
                exclusive
                onChange={(e, newView) => {
                  if (newView !== null) {
                    setViewMode(newView);
                  }
                }}
                size="small"
              >
                <ToggleButton value="table">
                  <ViewList sx={{ mr: 1 }} />
                  Table
                </ToggleButton>
                <ToggleButton value="calendar">
                  <CalendarMonth sx={{ mr: 1 }} />
                  Calendar
                </ToggleButton>
              </ToggleButtonGroup>
              <Button
                variant="contained"
                startIcon={<Add />}
                onClick={handleAddEvent}
              >
                Add Event
              </Button>
            </Box>
          </Box>

          {events.length === 0 ? (
            <Typography color="text.secondary" textAlign="center" py={4}>
              No events found. Schedule your first event!
            </Typography>
          ) : (
            <>
              {viewMode === 'table' ? (
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
              ) : (
                renderCalendarView()
              )}
            </>
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