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
import { Add, Delete, Edit, Inventory, Save, Cancel, Check, Close } from '@mui/icons-material';
import PinDialog from './PinDialog';

interface Product {
  id: number;
  name: string;
  price: number;
  category: string;
  category_id?: number;
  stock: number;
  created_at: string;
}

interface Category {
  id: number;
  name: string;
  description: string;
  parent_id?: number;
  is_active: boolean;
}

interface InventoryManagerProps {
  pinRequired?: boolean;
}

const InventoryManager: React.FC<InventoryManagerProps> = ({ pinRequired = true }) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showCategoryDialog, setShowCategoryDialog] = useState(false);
  const [showPinDialog, setShowPinDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{ type: 'add' | 'delete' | 'add-category'; productId?: number }>({ type: 'add' });
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [editingProduct, setEditingProduct] = useState<number | null>(null);
  const [editValues, setEditValues] = useState<{ [key: number]: Partial<Product> }>({});
  const [newProduct, setNewProduct] = useState({
    name: '',
    price: '',
    category: '',
    category_id: '',
    stock: '',
  });
  const [newCategory, setNewCategory] = useState({
    name: '',
    description: '',
  });

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, []);

  const fetchProducts = async () => {
    try {
      let url = 'http://localhost:5000/api/products';
      if (selectedCategory) {
        url = `http://localhost:5000/api/products/category/${selectedCategory}`;
      }
      const response = await fetch(url);
      const data = await response.json();
      setProducts(data);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/categories');
      const data = await response.json();
      setCategories(data);
    } catch (err) {
      console.error('Failed to fetch categories:', err);
    }
  };

  // Update products when category filter changes
  useEffect(() => {
    fetchProducts();
  }, [selectedCategory]);

  const handleAddProduct = () => {
    setPendingAction({ type: 'add' });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowAddDialog(true);
    }
  };

  const handleAddCategory = () => {
    setPendingAction({ type: 'add-category' });
    if (pinRequired) {
      setShowPinDialog(true);
    } else {
      setShowCategoryDialog(true);
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
    } else if (pendingAction.type === 'add-category') {
      setShowCategoryDialog(true);
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
          category_id: newProduct.category_id ? parseInt(newProduct.category_id) : null,
          stock: parseInt(newProduct.stock),
        }),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Product added successfully!' });
        setNewProduct({ name: '', price: '', category: '', category_id: '', stock: '' });
        setShowAddDialog(false);
        fetchProducts();
      } else {
        setAlert({ type: 'error', message: 'Failed to add product' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const addCategory = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/categories', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newCategory),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Category added successfully!' });
        setNewCategory({ name: '', description: '' });
        setShowCategoryDialog(false);
        fetchCategories();
      } else {
        setAlert({ type: 'error', message: 'Failed to add category' });
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

  const startEditing = (product: Product) => {
    setEditingProduct(product.id);
    setEditValues({
      ...editValues,
      [product.id]: { ...product }
    });
  };

  const cancelEditing = () => {
    setEditingProduct(null);
    setEditValues({});
  };

  const saveProduct = async (productId: number) => {
    const updatedData = editValues[productId];
    if (!updatedData) return;

    try {
      const response = await fetch(`http://localhost:5000/api/products/${productId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updatedData),
      });

      if (response.ok) {
        setAlert({ type: 'success', message: 'Product updated successfully!' });
        setEditingProduct(null);
        setEditValues({});
        fetchProducts();
      } else {
        setAlert({ type: 'error', message: 'Failed to update product' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const updateEditValue = (productId: number, field: keyof Product, value: any) => {
    setEditValues({
      ...editValues,
      [productId]: {
        ...editValues[productId],
        [field]: value
      }
    });
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
            <Box display="flex" gap={2}>
              <Button
                variant="outlined"
                startIcon={<Add />}
                onClick={handleAddCategory}
              >
                Add Category
              </Button>
              <Button
                variant="contained"
                startIcon={<Add />}
                onClick={handleAddProduct}
              >
                Add Product
              </Button>
            </Box>
          </Box>

          {/* Category Filter */}
          <Box sx={{ mb: 3 }}>
            <FormControl variant="outlined" sx={{ minWidth: 250, mb: 2 }}>
              <InputLabel>Filter by Category</InputLabel>
              <Select
                value={selectedCategory || ''}
                onChange={(e) => setSelectedCategory(e.target.value ? Number(e.target.value) : null)}
                label="Filter by Category"
                sx={{
                  '& .MuiOutlinedInput-root': {
                    borderRadius: 2,
                  }
                }}
              >
                <MenuItem value="">
                  <Box display="flex" alignItems="center" gap={1}>
                    <Box
                      sx={{
                        width: 12,
                        height: 12,
                        borderRadius: '50%',
                        background: 'linear-gradient(45deg, #00ff88, #8e44ad, #3498db)',
                      }}
                    />
                    <em>All Categories</em>
                  </Box>
                </MenuItem>
                {categories.map((category, index) => (
                  <MenuItem key={category.id} value={category.id}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <Box
                        sx={{
                          width: 12,
                          height: 12,
                          borderRadius: '50%',
                          backgroundColor: [
                            '#00ff88', // Neon green
                            '#1dd1a1', // Teal green  
                            '#2ecc71', // Emerald green
                            '#8e44ad', // Rich purple
                            '#9b59b6', // Amethyst purple
                            '#3498db', // Bright blue
                            '#5dade2', // Sky blue
                            '#85c1e9', // Light blue
                            '#bb6bd9', // Light purple
                            '#58d68d', // Light emerald
                          ][index % 10],
                        }}
                      />
                      {category.name}
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            
            {/* Category Pills */}
            <Box display="flex" gap={1} flexWrap="wrap">
              <Chip
                label="All Categories"
                onClick={() => setSelectedCategory(null)}
                color={selectedCategory === null ? 'primary' : 'default'}
                variant={selectedCategory === null ? 'filled' : 'outlined'}
                size="small"
              />
              {categories.map((category) => (
                <Chip
                  key={category.id}
                  label={category.name}
                  onClick={() => setSelectedCategory(category.id)}
                  color={selectedCategory === category.id ? 'secondary' : 'default'}
                  variant={selectedCategory === category.id ? 'filled' : 'outlined'}
                  size="small"
                />
              ))}
            </Box>
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
                  {products.map((product) => {
                    const isEditing = editingProduct === product.id;
                    const editData = editValues[product.id] || product;
                    
                    return (
                      <TableRow key={product.id}>
                        <TableCell>
                          {isEditing ? (
                            <TextField
                              size="small"
                              value={editData.name}
                              onChange={(e) => updateEditValue(product.id, 'name', e.target.value)}
                              fullWidth
                            />
                          ) : (
                            product.name
                          )}
                        </TableCell>
                        <TableCell>
                          {isEditing ? (
                            <FormControl size="small" fullWidth>
                              <Select
                                value={editData.category_id || ''}
                                onChange={(e) => {
                                  const categoryId = Number(e.target.value);
                                  const category = categories.find(c => c.id === categoryId);
                                  updateEditValue(product.id, 'category_id', categoryId);
                                  updateEditValue(product.id, 'category', category?.name || '');
                                }}
                              >
                                {categories.map((category) => (
                                  <MenuItem key={category.id} value={category.id}>
                                    {category.name}
                                  </MenuItem>
                                ))}
                              </Select>
                            </FormControl>
                          ) : (
                            product.category
                          )}
                        </TableCell>
                        <TableCell align="right">
                          {isEditing ? (
                            <TextField
                              size="small"
                              type="number"
                              value={editData.price}
                              onChange={(e) => updateEditValue(product.id, 'price', parseFloat(e.target.value))}
                              inputProps={{ step: '0.01', min: '0' }}
                              sx={{ width: 100 }}
                            />
                          ) : (
                            `$${product.price.toFixed(2)}`
                          )}
                        </TableCell>
                        <TableCell align="right">
                          {isEditing ? (
                            <TextField
                              size="small"
                              type="number"
                              value={editData.stock}
                              onChange={(e) => updateEditValue(product.id, 'stock', parseInt(e.target.value))}
                              inputProps={{ min: '0' }}
                              sx={{ width: 80 }}
                            />
                          ) : (
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
                          )}
                        </TableCell>
                        <TableCell align="right">
                          {isEditing ? (
                            <Box display="flex" gap={0.5}>
                              <IconButton
                                color="success"
                                onClick={() => saveProduct(product.id)}
                                size="small"
                              >
                                <Check />
                              </IconButton>
                              <IconButton
                                color="error"
                                onClick={cancelEditing}
                                size="small"
                              >
                                <Close />
                              </IconButton>
                            </Box>
                          ) : (
                            <Box display="flex" gap={0.5}>
                              <IconButton
                                color="primary"
                                onClick={() => startEditing(product)}
                                size="small"
                              >
                                <Edit />
                              </IconButton>
                              <IconButton
                                color="error"
                                onClick={() => handleDeleteProduct(product.id)}
                                size="small"
                              >
                                <Delete />
                              </IconButton>
                            </Box>
                          )}
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
              <FormControl fullWidth>
                <InputLabel>Category</InputLabel>
                <Select
                  value={newProduct.category_id}
                  onChange={(e) => {
                    const categoryId = e.target.value;
                    const category = categories.find(c => c.id === Number(categoryId));
                    setNewProduct({ 
                      ...newProduct, 
                      category_id: categoryId,
                      category: category?.name || ''
                    });
                  }}
                  label="Category"
                >
                  <MenuItem value="">
                    <em>Select Category</em>
                  </MenuItem>
                  {categories.map((category) => (
                    <MenuItem key={category.id} value={category.id}>
                      {category.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
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

      {/* Add Category Dialog */}
      <Dialog open={showCategoryDialog} onClose={() => setShowCategoryDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Category</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Category Name"
                fullWidth
                value={newCategory.name}
                onChange={(e) => setNewCategory({ ...newCategory, name: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Description (Optional)"
                fullWidth
                multiline
                rows={2}
                value={newCategory.description}
                onChange={(e) => setNewCategory({ ...newCategory, description: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowCategoryDialog(false)}>Cancel</Button>
          <Button onClick={addCategory} variant="contained">
            Add Category
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
              ? 'Add Product' 
              : pendingAction.type === 'add-category' 
                ? 'Add Category' 
                : 'Delete Product'
          }
        />
      )}
    </Container>
  );
};

export default InventoryManager;