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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Chip,
  LinearProgress,
} from '@mui/material';
import {
  Assessment,
  TrendingUp,
  Inventory,
  MonetizationOn,
  Download,
  DateRange,
  Warning,
} from '@mui/icons-material';

interface SalesReport {
  transactions: any[];
  summary: {
    total_sales: number;
    total_transactions: number;
    average_transaction: number;
    payment_methods: Record<string, { count: number; total: number }>;
  };
}

interface InventoryReport {
  products: any[];
  summary: {
    total_products: number;
    total_inventory_value: number;
    low_stock_count: number;
    out_of_stock_count: number;
    categories: Record<string, { count: number; total_stock: number; total_value: number }>;
  };
  alerts: {
    low_stock_items: any[];
    out_of_stock_items: any[];
  };
}

const ReportsManager: React.FC = () => {
  const [salesReport, setSalesReport] = useState<SalesReport | null>(null);
  const [inventoryReport, setInventoryReport] = useState<InventoryReport | null>(null);
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [dateRange, setDateRange] = useState({
    start_date: '',
    end_date: '',
  });
  const [reportType, setReportType] = useState('sales');

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    setLoading(true);
    try {
      // Fetch sales report
      let salesUrl = 'http://localhost:5000/api/reports/sales';
      if (dateRange.start_date || dateRange.end_date) {
        const params = new URLSearchParams();
        if (dateRange.start_date) params.append('start_date', dateRange.start_date);
        if (dateRange.end_date) params.append('end_date', dateRange.end_date);
        salesUrl += `?${params.toString()}`;
      }
      
      const [salesResponse, inventoryResponse] = await Promise.all([
        fetch(salesUrl),
        fetch('http://localhost:5000/api/reports/inventory')
      ]);

      const salesData = await salesResponse.json();
      const inventoryData = await inventoryResponse.json();

      setSalesReport(salesData);
      setInventoryReport(inventoryData);
    } catch (err) {
      setAlert({ type: 'error', message: 'Failed to fetch reports' });
      console.error('Failed to fetch reports:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDateRangeChange = () => {
    fetchReports();
  };

  const formatCurrency = (amount: number) => {
    return `$${amount.toFixed(2)}`;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  const exportReport = (format: 'csv' | 'json') => {
    const data = reportType === 'sales' ? salesReport : inventoryReport;
    if (!data) return;

    let content = '';
    let filename = '';
    let mimeType = '';

    if (format === 'csv') {
      if (reportType === 'sales') {
        const headers = ['Date', 'Items', 'Total', 'Payment Method', 'Gift Card Used'];
        const rows = salesReport!.transactions.map(t => [
          formatDate(t.created_at),
          JSON.parse(t.items).map((i: any) => `${i.product.name} (${i.quantity})`).join('; '),
          formatCurrency(t.total),
          t.payment_method,
          t.gift_card_used || 'None'
        ]);
        content = [headers, ...rows].map(row => row.join(',')).join('\n');
      } else {
        const headers = ['Name', 'Category', 'Price', 'Stock', 'Value'];
        const rows = inventoryReport!.products.map(p => [
          p.name,
          p.category,
          formatCurrency(p.price),
          p.stock.toString(),
          formatCurrency(p.price * p.stock)
        ]);
        content = [headers, ...rows].map(row => row.join(',')).join('\n');
      }
      mimeType = 'text/csv';
      filename = `${reportType}-report-${new Date().toISOString().split('T')[0]}.csv`;
    } else {
      content = JSON.stringify(data, null, 2);
      mimeType = 'application/json';
      filename = `${reportType}-report-${new Date().toISOString().split('T')[0]}.json`;
    }

    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    setAlert({ type: 'success', message: `Report exported as ${format.toUpperCase()}!` });
  };

  return (
    <Container maxWidth="lg">
      <Box display="flex" alignItems="center" gap={2} mb={3}>
        <Assessment color="primary" />
        <Typography variant="h5" color="primary">
          Reports & Analytics
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

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      {/* Date Range Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Report Filters
          </Typography>
          <Grid container spacing={2} alignItems="center">
            <Grid size={{ xs: 12, md: 3 }}>
              <FormControl fullWidth>
                <InputLabel>Report Type</InputLabel>
                <Select
                  value={reportType}
                  onChange={(e) => setReportType(e.target.value)}
                  label="Report Type"
                >
                  <MenuItem value="sales">Sales Report</MenuItem>
                  <MenuItem value="inventory">Inventory Report</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            {reportType === 'sales' && (
              <>
                <Grid size={{ xs: 12, md: 3 }}>
                  <TextField
                    label="Start Date"
                    type="date"
                    fullWidth
                    value={dateRange.start_date}
                    onChange={(e) => setDateRange({ ...dateRange, start_date: e.target.value })}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 3 }}>
                  <TextField
                    label="End Date"
                    type="date"
                    fullWidth
                    value={dateRange.end_date}
                    onChange={(e) => setDateRange({ ...dateRange, end_date: e.target.value })}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
              </>
            )}
            <Grid size={{ xs: 12, md: 3 }}>
              <Box display="flex" gap={1}>
                <Button
                  variant="contained"
                  onClick={handleDateRangeChange}
                  startIcon={<DateRange />}
                >
                  Refresh
                </Button>
                <Button
                  variant="outlined"
                  onClick={() => exportReport('csv')}
                  startIcon={<Download />}
                >
                  CSV
                </Button>
                <Button
                  variant="outlined"
                  onClick={() => exportReport('json')}
                  startIcon={<Download />}
                >
                  JSON
                </Button>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Sales Report */}
      {reportType === 'sales' && salesReport && (
        <>
          {/* Sales Summary Cards */}
          <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid size={{ xs: 12, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <MonetizationOn color="primary" />
                    <Box>
                      <Typography variant="h6" color="primary">
                        {formatCurrency(salesReport.summary.total_sales)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Total Sales
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <TrendingUp color="success" />
                    <Box>
                      <Typography variant="h6" color="success.main">
                        {salesReport.summary.total_transactions}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Transactions
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <Assessment color="info" />
                    <Box>
                      <Typography variant="h6" color="info.main">
                        {formatCurrency(salesReport.summary.average_transaction)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Avg Transaction
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Card>
                <CardContent>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Payment Methods
                  </Typography>
                  {Object.entries(salesReport.summary.payment_methods).map(([method, data]) => (
                    <Chip
                      key={method}
                      label={`${method}: ${data.count}`}
                      size="small"
                      sx={{ mr: 0.5, mb: 0.5 }}
                    />
                  ))}
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Sales Transactions Table */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Transactions ({salesReport.transactions.length})
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Date</TableCell>
                      <TableCell>Items</TableCell>
                      <TableCell align="right">Total</TableCell>
                      <TableCell>Payment Method</TableCell>
                      <TableCell>Gift Card</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {salesReport.transactions.slice(0, 10).map((transaction) => (
                      <TableRow key={transaction.id}>
                        <TableCell>{formatDate(transaction.created_at)}</TableCell>
                        <TableCell>
                          {JSON.parse(transaction.items).map((item: any, idx: number) => (
                            <Typography key={idx} variant="body2">
                              {item.product.name} x{item.quantity}
                            </Typography>
                          ))}
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" fontWeight="bold">
                            {formatCurrency(transaction.total)}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={transaction.payment_method} size="small" />
                        </TableCell>
                        <TableCell>
                          {transaction.gift_card_used ? (
                            <Chip label={transaction.gift_card_used} size="small" color="secondary" />
                          ) : (
                            '-'
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </>
      )}

      {/* Inventory Report */}
      {reportType === 'inventory' && inventoryReport && (
        <>
          {/* Inventory Summary Cards */}
          <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid size={{ xs: 12, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <Inventory color="primary" />
                    <Box>
                      <Typography variant="h6" color="primary">
                        {inventoryReport.summary.total_products}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Total Products
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <MonetizationOn color="success" />
                    <Box>
                      <Typography variant="h6" color="success.main">
                        {formatCurrency(inventoryReport.summary.total_inventory_value)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Inventory Value
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <Warning color="warning" />
                    <Box>
                      <Typography variant="h6" color="warning.main">
                        {inventoryReport.summary.low_stock_count}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Low Stock Items
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <Warning color="error" />
                    <Box>
                      <Typography variant="h6" color="error.main">
                        {inventoryReport.summary.out_of_stock_count}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Out of Stock
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Category Breakdown */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Category Breakdown
              </Typography>
              <Grid container spacing={2}>
                {Object.entries(inventoryReport.summary.categories).map(([category, data]) => (
                  <Grid size={{ xs: 12, sm: 6, md: 4 }} key={category}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="subtitle1">{category}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          Products: {data.count}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Stock: {data.total_stock}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Value: {formatCurrency(data.total_value)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>

          {/* Inventory Alerts */}
          {(inventoryReport.alerts.low_stock_items.length > 0 || inventoryReport.alerts.out_of_stock_items.length > 0) && (
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Inventory Alerts
                </Typography>
                {inventoryReport.alerts.out_of_stock_items.length > 0 && (
                  <Alert severity="error" sx={{ mb: 2 }}>
                    <Typography variant="subtitle2">Out of Stock Items:</Typography>
                    {inventoryReport.alerts.out_of_stock_items.map(item => item.name).join(', ')}
                  </Alert>
                )}
                {inventoryReport.alerts.low_stock_items.length > 0 && (
                  <Alert severity="warning">
                    <Typography variant="subtitle2">Low Stock Items:</Typography>
                    {inventoryReport.alerts.low_stock_items.map(item => `${item.name} (${item.stock})`).join(', ')}
                  </Alert>
                )}
              </CardContent>
            </Card>
          )}

          {/* Inventory Table */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Product Inventory
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Product Name</TableCell>
                      <TableCell>Category</TableCell>
                      <TableCell align="right">Price</TableCell>
                      <TableCell align="right">Stock</TableCell>
                      <TableCell align="right">Value</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {inventoryReport.products.map((product) => (
                      <TableRow key={product.id}>
                        <TableCell>{product.name}</TableCell>
                        <TableCell>{product.category}</TableCell>
                        <TableCell align="right">{formatCurrency(product.price)}</TableCell>
                        <TableCell align="right">
                          <Box
                            component="span"
                            sx={{
                              color: product.stock <= 5 ? (product.stock === 0 ? 'error.main' : 'warning.main') : 'text.primary',
                              fontWeight: product.stock <= 5 ? 'bold' : 'normal',
                            }}
                          >
                            {product.stock}
                            {product.stock <= 5 && product.stock > 0 && ' (Low)'}
                            {product.stock === 0 && ' (Out)'}
                          </Box>
                        </TableCell>
                        <TableCell align="right">
                          {formatCurrency(product.price * product.stock)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </>
      )}
    </Container>
  );
};

export default ReportsManager;