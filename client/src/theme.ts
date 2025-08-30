import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#00ff88', // Bright neon green
      dark: '#00cc6a',
      light: '#66ffaa',
      contrastText: '#000000',
    },
    secondary: {
      main: '#8e44ad', // Rich purple
      light: '#bb6bd9',
      dark: '#5d2c87',
      contrastText: '#ffffff',
    },
    info: {
      main: '#2ecc71', // Emerald green
      light: '#58d68d',
      dark: '#239b56',
    },
    success: {
      main: '#1dd1a1', // Teal green
      light: '#55efc4',
      dark: '#00b894',
    },
    warning: {
      main: '#9b59b6', // Amethyst purple
      light: '#d2b4de',
      dark: '#6c3483',
    },
    error: {
      main: '#e74c3c', // Softer red
      light: '#f1948a',
      dark: '#c0392b',
    },
    background: {
      default: '#0a0a0a',
      paper: '#1a1a1a',
    },
    text: {
      primary: '#ffffff',
      secondary: '#b0b0b0',
    },
  },
  typography: {
    h4: {
      fontWeight: 600,
      color: '#ffffff',
    },
    h5: {
      fontWeight: 500,
      color: '#00ff88',
    },
    h6: {
      fontWeight: 500,
      color: '#ffffff',
    },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(145deg, #1a1a1a 0%, #2a2a2a 100%)',
          border: '1px solid #333',
          borderRadius: '12px',
          transition: 'all 0.3s ease-in-out',
          '&:hover': {
            transform: 'translateY(-4px) scale(1.02)',
            boxShadow: '0 12px 24px rgba(29, 209, 161, 0.2), 0 6px 12px rgba(142, 68, 173, 0.1)',
            border: '1px solid',
            borderImage: 'linear-gradient(45deg, #00ff88, #1dd1a1, #8e44ad) 1',
          },
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: '8px',
          textTransform: 'none',
          fontWeight: 600,
        },
        contained: {
          background: 'linear-gradient(135deg, #00ff88 0%, #1dd1a1 50%, #2ecc71 100%)',
          boxShadow: '0 3px 5px 2px rgba(0, 255, 136, .3)',
          transition: 'all 0.3s ease-in-out',
          '&:hover': {
            background: 'linear-gradient(135deg, #66ffaa 0%, #55efc4 50%, #58d68d 100%)',
            transform: 'translateY(-2px)',
            boxShadow: '0 6px 10px 4px rgba(0, 255, 136, .4)',
          },
        },
        outlined: {
          borderColor: '#1dd1a1',
          color: '#1dd1a1',
          '&:hover': {
            borderColor: '#00ff88',
            color: '#00ff88',
            backgroundColor: 'rgba(0, 255, 136, 0.1)',
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: '16px',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            transform: 'scale(1.05)',
            boxShadow: '0 4px 8px rgba(0, 0, 0, 0.3)',
          },
        },
        colorPrimary: {
          background: 'linear-gradient(45deg, #00ff88, #1dd1a1)',
          color: '#000000',
          '&:hover': {
            background: 'linear-gradient(45deg, #66ffaa, #55efc4)',
          },
        },
        colorSecondary: {
          background: 'linear-gradient(45deg, #8e44ad, #9b59b6)',
          color: '#ffffff',
          '&:hover': {
            background: 'linear-gradient(45deg, #bb6bd9, #d2b4de)',
          },
        },
        colorInfo: {
          background: 'linear-gradient(45deg, #3498db, #5dade2)',
          color: '#ffffff',
        },
        colorSuccess: {
          background: 'linear-gradient(45deg, #2ecc71, #58d68d)',
          color: '#000000',
        },
        colorWarning: {
          background: 'linear-gradient(45deg, #9b59b6, #bb6bd9)',
          color: '#ffffff',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-head': {
            background: 'linear-gradient(90deg, #1a1a1a 0%, #2a2a2a 100%)',
            color: '#1dd1a1',
            fontWeight: 600,
            borderBottom: '2px solid #2ecc71',
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(90deg, #0a0a0a 0%, #1a1a1a 50%, #0a0a0a 100%)',
          boxShadow: '0 2px 10px rgba(0, 255, 136, 0.3)',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            '& fieldset': {
              borderColor: '#3498db',
            },
            '&:hover fieldset': {
              borderColor: '#1dd1a1',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#00ff88',
              boxShadow: '0 0 5px rgba(0, 255, 136, 0.3)',
            },
          },
          '& .MuiInputLabel-root': {
            color: '#9b59b6',
            '&.Mui-focused': {
              color: '#00ff88',
            },
          },
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        root: {
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: '#8e44ad',
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: '#00ff88',
            boxShadow: '0 0 5px rgba(0, 255, 136, 0.3)',
          },
        },
      },
    },
  },
});

export default theme;