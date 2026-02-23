import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#00d4ff', // Electric blue from logo
      dark: '#0099cc',
      light: '#66e6ff',
      contrastText: '#000000',
    },
    secondary: {
      main: '#8e44ad', // Rich purple
      light: '#bb6bd9',
      dark: '#5d2c87',
      contrastText: '#ffffff',
    },
    info: {
      main: '#00d4ff', // Electric blue matching primary
      light: '#66e6ff',
      dark: '#0099cc',
    },
    success: {
      main: '#00ff88', // Electric green from logo
      light: '#66ffaa',
      dark: '#00cc6a',
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
      color: '#00d4ff',
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
            boxShadow: '0 12px 24px rgba(0, 212, 255, 0.2), 0 6px 12px rgba(0, 255, 136, 0.1)',
            border: '1px solid',
            borderImage: 'linear-gradient(45deg, #00d4ff, #00ff88, #66e6ff) 1',
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
          background: 'linear-gradient(135deg, #00d4ff 0%, #00ff88 50%, #66e6ff 100%)',
          boxShadow: '0 3px 5px 2px rgba(0, 212, 255, .3)',
          transition: 'all 0.3s ease-in-out',
          '&:hover': {
            background: 'linear-gradient(135deg, #66e6ff 0%, #66ffaa 50%, #99f0ff 100%)',
            transform: 'translateY(-2px)',
            boxShadow: '0 6px 10px 4px rgba(0, 212, 255, .4)',
          },
        },
        outlined: {
          borderColor: '#00d4ff',
          color: '#00d4ff',
          '&:hover': {
            borderColor: '#00ff88',
            color: '#00ff88',
            backgroundColor: 'rgba(0, 212, 255, 0.1)',
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
          background: 'linear-gradient(45deg, #00d4ff, #00ff88)',
          color: '#000000',
          '&:hover': {
            background: 'linear-gradient(45deg, #66e6ff, #66ffaa)',
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
          background: 'linear-gradient(45deg, #00d4ff, #66e6ff)',
          color: '#000000',
        },
        colorSuccess: {
          background: 'linear-gradient(45deg, #00ff88, #66ffaa)',
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
            color: '#00d4ff',
            fontWeight: 600,
            borderBottom: '2px solid #00ff88',
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(90deg, #0a0a0a 0%, #1a1a1a 50%, #0a0a0a 100%)',
          boxShadow: '0 2px 10px rgba(0, 212, 255, 0.3)',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            '& fieldset': {
              borderColor: '#00d4ff',
            },
            '&:hover fieldset': {
              borderColor: '#00ff88',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#00ff88',
              boxShadow: '0 0 5px rgba(0, 255, 136, 0.3)',
            },
          },
          '& .MuiInputLabel-root': {
            color: '#00d4ff',
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
            borderColor: '#00d4ff',
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
