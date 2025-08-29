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
} from '@mui/material';
import { Add, Delete, Edit, Inventory } from '@mui/icons-material';
import PinDialog from './PinDialog';

interface Product {
  id: number;
  name: string;
  price: number;
  category: string;
  stock: number;
  created_at: string;
}

interface InventoryManagerProps {
  pinRequired?: boolean;
}

const InventoryManager: React.FC<InventoryManagerProps> = ({ pinRequired = true }) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showPinDialog, setShowPinDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{ type: 'add' | 'delete'; productId?: number }>({ type: 'add' });
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [newProduct, setNewProduct] = useState({
    name: '',
    price: '',
    category: '',
    stock: '',
  });

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/products');
      const data = await response.json();
      setProducts(data);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const handleAddProduct = () => {
    setPendingAction({ type: 'add' });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowAddDialog(true);
    }
  };

  const handleDeleteProduct = (productId: number) => {
    setPendingAction({ type: 'delete', productId });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      deleteProduct(productId);
    }
  };

  const executePendingAction = async () => {
    if (pendingAction.type === 'add') {
      await addProduct();
      setShowAddDialog(true);
    } else if (pendingAction.type === 'delete' && pendingAction.productId) {
      await deleteProduct(pendingAction.productId);
    }
  };

  const addProduct = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/products', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: newProduct.name,
          price: parseFloat(newProduct.price),
          category: newProduct.category,
          stock: parseInt(newProduct.stock),
        }),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Product added successfully!' });
        setNewProduct({ name: '', price: '', category: '', stock: '' });
        setShowAddDialog(false);
        fetchProducts();
      } else {
        setAlert({ type: 'error', message: 'Failed to add product' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const deleteProduct = async (productId: number) => {
    try {
      const response = await fetch(`http://localhost:5000/api/products/${productId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Product deleted successfully!' });
        fetchProducts();
      } else {
        setAlert({ type: 'error', message: 'Failed to delete product' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const handleAddDialogSubmit = () => {
    if (!newProduct.name || !newProduct.price || !newProduct.category || !newProduct.stock) {
      setAlert({ type: 'error', message: 'Please fill in all fields' });
      return;
    }
    
    addProduct();
  };

  return (
    <Container maxWidth="lg">
      <Box display="flex" alignItems="center" gap={2} mb={3}>
        <Inventory color="primary" />
        <Typography variant="h5" color="primary">
          Inventory Management
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
              Products ({products.length})
            </Typography>
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={handleAddProduct}
            >
              Add Product
            </Button>
          </Box>

          {products.length === 0 ? (
            <Typography color="text.secondary" textAlign="center" py={4}>
              No products found. Add your first product!
            </Typography>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell align="right">Price</TableCell>
                    <TableCell align="right">Stock</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {products.map((product) => (
                    <TableRow key={product.id}>
                      <TableCell>{product.name}</TableCell>
                      <TableCell>{product.category}</TableCell>
                      <TableCell align="right">${product.price.toFixed(2)}</TableCell>
                      <TableCell align="right">
                        <Box
                          component="span"
                          sx={{
                            color: product.stock <= 5 ? 'error.main' : 'text.primary',
                            fontWeight: product.stock <= 5 ? 'bold' : 'normal',
                          }}
                        >
                          {product.stock}
                          {product.stock <= 5 && ' (Low)'}
                        </Box>
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          color="error"
                          onClick={() => handleDeleteProduct(product.id)}
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

      {/* Add Product Dialog */}
      <Dialog open={showAddDialog} onClose={() => setShowAddDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Product</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Product Name"
                fullWidth
                value={newProduct.name}
                onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Category"
                fullWidth
                value={newProduct.category}
                onChange={(e) => setNewProduct({ ...newProduct, category: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Price"
                type="number"
                fullWidth
                value={newProduct.price}
                onChange={(e) => setNewProduct({ ...newProduct, price: e.target.value })}
                inputProps={{ step: '0.01', min: '0' }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Stock Quantity"
                type="number"
                fullWidth
                value={newProduct.stock}
                onChange={(e) => setNewProduct({ ...newProduct, stock: e.target.value })}
                inputProps={{ min: '0' }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowAddDialog(false)}>Cancel</Button>
          <Button onClick={handleAddDialogSubmit} variant="contained">
            Add Product
          </Button>
        </DialogActions>
      </Dialog>

      {/* PIN Dialog */}
      {pinRequired && (
        <PinDialog
          open={showPinDialog}
          onClose={() => setShowPinDialog(false)}
          onSuccess={executePendingAction}
          title={pendingAction.type === 'add' ? 'Add Product' : 'Delete Product'}
        />
      )}
    </Container>
  );
};

export default InventoryManager;