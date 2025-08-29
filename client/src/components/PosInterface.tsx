import React, { useState, useEffect } from 'react';
import {
  Container,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Box,
  List,
  ListItem,
  ListItemText,
  Divider,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Chip,
} from '@mui/material';
import { Add, Remove, ShoppingCart, Payment, CardGiftcard } from '@mui/icons-material';

interface Product {
  id: number;
  name: string;
  price: number;
  category: string;
  stock: number;
}

interface CartItem {
  product: Product;
  quantity: number;
}

interface Member {
  id: number;
  name: string;
  email: string;
  loyalty_points: number;
  membership_type: string;
}

interface Promotion {
  id: number;
  name: string;
  description: string;
  discount_type: string;
  discount_value: number;
  min_purchase: number;
}

interface PosInterfaceProps {
  pinRequired?: boolean;
}

const TAX_RATE = 0.0825; // 8.25% Texas tax rate

const PosInterface: React.FC<PosInterfaceProps> = ({ pinRequired = true }) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [paymentMethod, setPaymentMethod] = useState('cash');
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [showGiftCardDialog, setShowGiftCardDialog] = useState(false);
  const [giftCardNumber, setGiftCardNumber] = useState('');
  const [giftCardBalance, setGiftCardBalance] = useState(0);
  const [giftCardUsed, setGiftCardUsed] = useState<string | null>(null);
  const [giftCardAmount, setGiftCardAmount] = useState(0);
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);
  const [memberSearch, setMemberSearch] = useState('');
  const [memberSearchResults, setMemberSearchResults] = useState<Member[]>([]);
  const [selectedPromotion, setSelectedPromotion] = useState<Promotion | null>(null);
  const [availablePromotions, setAvailablePromotions] = useState<Promotion[]>([]);
  const [promotionDiscount, setPromotionDiscount] = useState(0);

  useEffect(() => {
    fetchProducts();
    fetchPromotions();
  }, []);

  useEffect(() => {
    if (memberSearch.length > 2) {
      searchMembers();
    } else {
      setMemberSearchResults([]);
    }
  }, [memberSearch]);

  const fetchProducts = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/products');
      const data = await response.json();
      setProducts(data);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const fetchPromotions = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/promotions/active');
      const data = await response.json();
      setAvailablePromotions(data);
    } catch (err) {
      console.error('Failed to fetch promotions:', err);
    }
  };

  const searchMembers = async () => {
    try {
      const response = await fetch(`http://localhost:5000/api/members/search?q=${memberSearch}`);
      const data = await response.json();
      setMemberSearchResults(data);
    } catch (err) {
      console.error('Failed to search members:', err);
    }
  };

  const addToCart = (product: Product) => {
    if (product.stock <= 0) {
      setAlert({ type: 'error', message: 'Product out of stock' });
      return;
    }

    setCart(prevCart => {
      const existingItem = prevCart.find(item => item.product.id === product.id);
      if (existingItem) {
        if (existingItem.quantity >= product.stock) {
          setAlert({ type: 'error', message: 'Not enough stock available' });
          return prevCart;
        }
        return prevCart.map(item =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [...prevCart, { product, quantity: 1 }];
    });
  };

  const removeFromCart = (productId: number) => {
    setCart(prevCart =>
      prevCart
        .map(item =>
          item.product.id === productId
            ? { ...item, quantity: item.quantity - 1 }
            : item
        )
        .filter(item => item.quantity > 0)
    );
  };

  const getSubtotal = () => {
    return cart.reduce((total, item) => total + item.product.price * item.quantity, 0);
  };

  const getTaxAmount = () => {
    const subtotalAfterDiscounts = Math.max(0, getSubtotal() - promotionDiscount);
    return subtotalAfterDiscounts * TAX_RATE;
  };

  const getTotal = () => {
    const subtotal = getSubtotal();
    const discountAmount = promotionDiscount;
    const subtotalAfterDiscounts = Math.max(0, subtotal - discountAmount);
    const taxAmount = subtotalAfterDiscounts * TAX_RATE;
    const totalWithTax = subtotalAfterDiscounts + taxAmount;
    return Math.max(0, totalWithTax - giftCardAmount);
  };

  const calculatePromotionDiscount = () => {
    if (!selectedPromotion) return 0;
    
    const subtotal = getSubtotal();
    if (subtotal < selectedPromotion.min_purchase) return 0;
    
    if (selectedPromotion.discount_type === 'percentage') {
      return Math.min((subtotal * selectedPromotion.discount_value / 100), subtotal);
    } else {
      return Math.min(selectedPromotion.discount_value, subtotal);
    }
  };

  const applyPromotion = (promotion: Promotion) => {
    setSelectedPromotion(promotion);
    const discount = calculatePromotionDiscount();
    setPromotionDiscount(discount);
  };

  const removePromotion = () => {
    setSelectedPromotion(null);
    setPromotionDiscount(0);
  };

  const validateGiftCard = async () => {
    try {
      const response = await fetch('http://localhost:5000/api/gift-cards/validate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ card_number: giftCardNumber }),
      });

      const result = await response.json();
      
      if (response.ok) {
        setGiftCardBalance(result.balance);
        const maxUse = Math.min(result.balance, getSubtotal());
        setGiftCardAmount(maxUse);
        setAlert({ type: 'success', message: `Gift card validated! Balance: $${result.balance.toFixed(2)}` });
        return true;
      } else {
        setAlert({ type: 'error', message: result.error });
        return false;
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
      return false;
    }
  };

  const applyGiftCard = () => {
    if (giftCardAmount > 0) {
      setGiftCardUsed(giftCardNumber);
      setShowGiftCardDialog(false);
      setAlert({ type: 'success', message: `Gift card applied: $${giftCardAmount.toFixed(2)}` });
    }
  };

  const removeGiftCard = () => {
    setGiftCardUsed(null);
    setGiftCardAmount(0);
    setGiftCardNumber('');
    setGiftCardBalance(0);
    setAlert({ type: 'success', message: 'Gift card removed' });
  };

  const handleCheckout = async () => {
    if (cart.length === 0) {
      setAlert({ type: 'error', message: 'Cart is empty' });
      return;
    }

    try {
      // If using gift card, redeem it first
      if (giftCardUsed && giftCardAmount > 0) {
        const redeemResponse = await fetch('http://localhost:5000/api/gift-cards/redeem', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            card_number: giftCardUsed,
            amount: giftCardAmount,
          }),
        });

        if (!redeemResponse.ok) {
          const error = await redeemResponse.json();
          setAlert({ type: 'error', message: `Gift card error: ${error.error}` });
          return;
        }
      }

      const response = await fetch('http://localhost:5000/api/transactions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          items: cart,
          subtotal: getSubtotal(),
          tax_amount: getTaxAmount(),
          total: getTotal() + giftCardAmount, // Total before gift card deduction for server processing
          payment_method: paymentMethod,
          gift_card_used: giftCardUsed,
          member_id: selectedMember?.id,
          promotion_id: selectedPromotion?.id,
        }),
      });

      if (response.ok) {
        const result = await response.json();
        let successMessage = 'Transaction completed successfully!';
        if (result.points_earned && selectedMember) {
          successMessage += ` ${selectedMember.name} earned ${result.points_earned} points!`;
        }
        if (result.discount_applied > 0) {
          successMessage += ` Saved $${result.discount_applied.toFixed(2)} with promotion!`;
        }
        
        setAlert({ type: 'success', message: successMessage });
        setCart([]);
        setGiftCardUsed(null);
        setGiftCardAmount(0);
        setGiftCardNumber('');
        setGiftCardBalance(0);
        setSelectedMember(null);
        setMemberSearch('');
        setSelectedPromotion(null);
        setPromotionDiscount(0);
        fetchProducts(); // Refresh products to update stock
      } else {
        setAlert({ type: 'error', message: 'Transaction failed' });
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Connection error' });
    }
  };

  const clearCart = () => {
    setCart([]);
  };

  return (
    <Container maxWidth="lg">
      <Typography variant="h5" gutterBottom color="primary">
        Point of Sale
      </Typography>

      {alert && (
        <Alert
          severity={alert.type}
          onClose={() => setAlert(null)}
          sx={{ mb: 2 }}
        >
          {alert.message}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Products
              </Typography>
              <Grid container spacing={2}>
                {products.map((product) => (
                  <Grid size={{ xs: 12, sm: 6, md: 4 }} key={product.id}>
                    <Card
                      variant="outlined"
                      sx={{
                        cursor: 'pointer',
                        '&:hover': { backgroundColor: 'action.hover' },
                      }}
                      onClick={() => addToCart(product)}
                    >
                      <CardContent sx={{ textAlign: 'center', p: 2 }}>
                        <Typography variant="subtitle1" gutterBottom>
                          {product.name}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {product.category}
                        </Typography>
                        <Typography variant="h6" color="primary">
                          ${product.price.toFixed(2)}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Stock: {product.stock}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={1} mb={2}>
                <ShoppingCart />
                <Typography variant="h6">
                  Shopping Cart
                </Typography>
              </Box>

              {cart.length === 0 ? (
                <Typography color="text.secondary" textAlign="center" py={4}>
                  Cart is empty
                </Typography>
              ) : (
                <>
                  <List>
                    {cart.map((item) => (
                      <ListItem
                        key={item.product.id}
                        sx={{
                          px: 0,
                          display: 'flex',
                          justifyContent: 'space-between',
                        }}
                      >
                        <ListItemText
                          primary={item.product.name}
                          secondary={`$${item.product.price.toFixed(2)} x ${item.quantity}`}
                        />
                        <Box display="flex" alignItems="center" gap={1}>
                          <Button
                            size="small"
                            onClick={() => removeFromCart(item.product.id)}
                          >
                            <Remove />
                          </Button>
                          <Typography>{item.quantity}</Typography>
                          <Button
                            size="small"
                            onClick={() => addToCart(item.product)}
                          >
                            <Add />
                          </Button>
                        </Box>
                      </ListItem>
                    ))}
                  </List>

                  <Divider sx={{ my: 2 }} />

                  {/* Member Loyalty Section */}
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2" gutterBottom>
                      Customer Loyalty
                    </Typography>
                    {selectedMember ? (
                      <Box sx={{ p: 2, bgcolor: 'primary.dark', borderRadius: 1, mb: 1 }}>
                        <Box display="flex" justifyContent="space-between" alignItems="center">
                          <Box>
                            <Typography variant="body2" color="primary.light">
                              {selectedMember.name} ({selectedMember.membership_type})
                            </Typography>
                            <Typography variant="caption" color="primary.light">
                              {selectedMember.loyalty_points} points available
                            </Typography>
                          </Box>
                          <Button size="small" onClick={() => {setSelectedMember(null); setMemberSearch('');}}>
                            Remove
                          </Button>
                        </Box>
                      </Box>
                    ) : (
                      <Box>
                        <TextField
                          size="small"
                          placeholder="Search customer..."
                          value={memberSearch}
                          onChange={(e) => setMemberSearch(e.target.value)}
                          fullWidth
                          sx={{ mb: 1 }}
                        />
                        {memberSearchResults.map((member) => (
                          <Box
                            key={member.id}
                            sx={{
                              p: 1,
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
                            }}
                          >
                            <Typography variant="body2">
                              {member.name} - {member.loyalty_points} pts
                            </Typography>
                          </Box>
                        ))}
                      </Box>
                    )}
                  </Box>

                  {/* Promotions Section */}
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2" gutterBottom>
                      Promotions & Discounts
                    </Typography>
                    {selectedPromotion ? (
                      <Box sx={{ p: 2, bgcolor: 'warning.dark', borderRadius: 1 }}>
                        <Box display="flex" justifyContent="space-between" alignItems="center">
                          <Box>
                            <Typography variant="body2" color="warning.light">
                              {selectedPromotion.name}
                            </Typography>
                            <Typography variant="caption" color="warning.light">
                              Save ${promotionDiscount.toFixed(2)}
                            </Typography>
                          </Box>
                          <Button size="small" onClick={removePromotion}>
                            Remove
                          </Button>
                        </Box>
                      </Box>
                    ) : (
                      <Box>
                        {availablePromotions
                          .filter(promo => getSubtotal() >= promo.min_purchase)
                          .map((promotion) => (
                            <Box
                              key={promotion.id}
                              sx={{
                                p: 1,
                                bgcolor: 'background.paper',
                                border: '1px solid',
                                borderColor: 'warning.main',
                                borderRadius: 1,
                                cursor: 'pointer',
                                mb: 0.5,
                                '&:hover': { bgcolor: 'warning.dark' },
                              }}
                              onClick={() => applyPromotion(promotion)}
                            >
                              <Typography variant="body2" color="warning.main">
                                {promotion.name}
                              </Typography>
                              <Typography variant="caption" color="text.secondary">
                                {promotion.description}
                              </Typography>
                            </Box>
                          ))}
                        {availablePromotions.length === 0 && (
                          <Typography variant="caption" color="text.secondary">
                            No promotions available
                          </Typography>
                        )}
                      </Box>
                    )}
                  </Box>

                  {/* Gift Card Section */}
                  {giftCardUsed ? (
                    <Box sx={{ mb: 2, p: 2, bgcolor: 'success.light', borderRadius: 1 }}>
                      <Box display="flex" justifyContent="space-between" alignItems="center">
                        <Box display="flex" alignItems="center" gap={1}>
                          <CardGiftcard color="success" />
                          <Box>
                            <Typography variant="body2" color="success.dark">
                              Gift Card Applied
                            </Typography>
                            <Typography variant="caption" color="success.dark">
                              {giftCardUsed}
                            </Typography>
                          </Box>
                        </Box>
                        <Box textAlign="right">
                          <Typography variant="body2" color="success.dark">
                            -${giftCardAmount.toFixed(2)}
                          </Typography>
                          <Button size="small" onClick={removeGiftCard} color="error">
                            Remove
                          </Button>
                        </Box>
                      </Box>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      startIcon={<CardGiftcard />}
                      onClick={() => setShowGiftCardDialog(true)}
                      fullWidth
                      sx={{ mb: 2 }}
                    >
                      Apply Gift Card
                    </Button>
                  )}

                  {/* Total Breakdown */}
                  <Box sx={{ mb: 2 }}>
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="body1">Subtotal:</Typography>
                      <Typography variant="body1">${getSubtotal().toFixed(2)}</Typography>
                    </Box>
                    {selectedPromotion && promotionDiscount > 0 && (
                      <Box display="flex" justifyContent="space-between" color="warning.main">
                        <Typography variant="body1">Promotion:</Typography>
                        <Typography variant="body1">-${promotionDiscount.toFixed(2)}</Typography>
                      </Box>
                    )}
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="body1">Tax (8.25%):</Typography>
                      <Typography variant="body1">${getTaxAmount().toFixed(2)}</Typography>
                    </Box>
                    {giftCardUsed && (
                      <Box display="flex" justifyContent="space-between" color="success.main">
                        <Typography variant="body1">Gift Card:</Typography>
                        <Typography variant="body1">-${giftCardAmount.toFixed(2)}</Typography>
                      </Box>
                    )}
                    {selectedMember && (
                      <Box display="flex" justifyContent="space-between" color="primary.main">
                        <Typography variant="body1">Points Earned:</Typography>
                        <Typography variant="body1">+{getTotal().toFixed(0)} pts</Typography>
                      </Box>
                    )}
                    <Divider sx={{ my: 1 }} />
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="h6">Total:</Typography>
                      <Typography variant="h6" color="primary">
                        ${getTotal().toFixed(2)}
                      </Typography>
                    </Box>
                  </Box>

                  <FormControl fullWidth sx={{ mb: 2 }}>
                    <InputLabel>Payment Method</InputLabel>
                    <Select
                      value={paymentMethod}
                      onChange={(e) => setPaymentMethod(e.target.value)}
                      label="Payment Method"
                    >
                      <MenuItem value="cash">Cash</MenuItem>
                      <MenuItem value="card">Card</MenuItem>
                      <MenuItem value="digital">Digital Wallet</MenuItem>
                    </Select>
                  </FormControl>

                  <Box display="flex" gap={1}>
                    <Button
                      variant="outlined"
                      onClick={clearCart}
                      fullWidth
                    >
                      Clear
                    </Button>
                    <Button
                      variant="contained"
                      onClick={handleCheckout}
                      fullWidth
                      startIcon={<Payment />}
                    >
                      Checkout
                    </Button>
                  </Box>
                </>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Gift Card Dialog */}
      <Dialog open={showGiftCardDialog} onClose={() => setShowGiftCardDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Apply Gift Card</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <TextField
              label="Gift Card Number"
              fullWidth
              value={giftCardNumber}
              onChange={(e) => setGiftCardNumber(e.target.value)}
              sx={{ mb: 2 }}
              placeholder="Enter gift card number"
            />
            
            <Button
              variant="contained"
              onClick={validateGiftCard}
              fullWidth
              sx={{ mb: 2 }}
            >
              Validate Card
            </Button>

            {giftCardBalance > 0 && (
              <Box sx={{ p: 2, bgcolor: 'success.light', borderRadius: 1, mb: 2 }}>
                <Typography variant="body2" color="success.dark">
                  <strong>Card Balance:</strong> ${giftCardBalance.toFixed(2)}
                </Typography>
                <Typography variant="body2" color="success.dark">
                  <strong>Amount to Use:</strong> ${giftCardAmount.toFixed(2)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Maximum usage is the lesser of card balance or cart total
                </Typography>
              </Box>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setShowGiftCardDialog(false);
            setGiftCardNumber('');
            setGiftCardBalance(0);
            setGiftCardAmount(0);
          }}>
            Cancel
          </Button>
          <Button
            onClick={applyGiftCard}
            variant="contained"
            disabled={giftCardAmount <= 0}
          >
            Apply Gift Card
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default PosInterface;