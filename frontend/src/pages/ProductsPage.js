import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Button,
  Grid,
  TextField,
  InputAdornment,
  Paper,
  Fab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
  Chip,
  Pagination
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  FilterList as FilterIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { toast } from 'react-toastify';

import ProductCard from '../components/ProductCard';
import ProductForm from '../components/ProductForm';
import { productService } from '../services/productService';

const ProductsPage = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(12);
  const [formOpen, setFormOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [productToDelete, setProductToDelete] = useState(null);
  const [viewDialogOpen, setViewDialogOpen] = useState(false);
  const [productToView, setProductToView] = useState(null);

  const queryClient = useQueryClient();

  // Fetch products with pagination
  const {
    data: productsData,
    isLoading,
    error,
    refetch
  } = useQuery(
    ['products', page, pageSize, searchTerm, minPrice, maxPrice],
    async () => {
      if (searchTerm) {
        return { content: await productService.searchProducts(searchTerm), totalPages: 1 };
      }
      if (minPrice && maxPrice) {
        return { content: await productService.getProductsByPriceRange(minPrice, maxPrice), totalPages: 1 };
      }
      return await productService.getProducts(page, pageSize);
    },
    {
      keepPreviousData: true,
      staleTime: 30000
    }
  );

  // Create product mutation
  const createMutation = useMutation(productService.createProduct, {
    onSuccess: () => {
      queryClient.invalidateQueries('products');
      toast.success('Product created successfully!');
    },
    onError: (error) => {
      toast.error(`Failed to create product: ${error.response?.data?.message || error.message}`);
    }
  });

  // Update product mutation
  const updateMutation = useMutation(
    ({ id, product }) => productService.updateProduct(id, product),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('products');
        toast.success('Product updated successfully!');
      },
      onError: (error) => {
        toast.error(`Failed to update product: ${error.response?.data?.message || error.message}`);
      }
    }
  );

  // Delete product mutation
  const deleteMutation = useMutation(productService.deleteProduct, {
    onSuccess: () => {
      queryClient.invalidateQueries('products');
      toast.success('Product deleted successfully!');
    },
    onError: (error) => {
      toast.error(`Failed to delete product: ${error.response?.data?.message || error.message}`);
    }
  });

  const handleCreateProduct = () => {
    setSelectedProduct(null);
    setFormOpen(true);
  };

  const handleEditProduct = (product) => {
    setSelectedProduct(product);
    setFormOpen(true);
  };

  const handleViewProduct = (product) => {
    setProductToView(product);
    setViewDialogOpen(true);
  };

  const handleDeleteProduct = (product) => {
    setProductToDelete(product);
    setDeleteDialogOpen(true);
  };

  const handleFormSubmit = async (productData) => {
    if (selectedProduct) {
      await updateMutation.mutateAsync({ id: selectedProduct.id, product: productData });
    } else {
      await createMutation.mutateAsync(productData);
    }
  };

  const confirmDelete = async () => {
    if (productToDelete) {
      await deleteMutation.mutateAsync(productToDelete.id);
      setDeleteDialogOpen(false);
      setProductToDelete(null);
    }
  };

  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  const clearFilters = () => {
    setSearchTerm('');
    setMinPrice('');
    setMaxPrice('');
    setPage(0);
  };

  const products = productsData?.content || [];
  const totalPages = productsData?.totalPages || 0;

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">
          Failed to load products. Please try again later.
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h3" component="h1" gutterBottom>
            Product Management
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Manage your product catalog
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreateProduct}
          size="large"
        >
          Add Product
        </Button>
      </Box>

      {/* Filters */}
      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          <FilterIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          Filters
        </Typography>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              label="Search products"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField
              fullWidth
              label="Min Price"
              type="number"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              InputProps={{
                startAdornment: <InputAdornment position="start">$</InputAdornment>,
              }}
            />
          </Grid>
          <Grid item xs={12} md={2}>
            <TextField
              fullWidth
              label="Max Price"
              type="number"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              InputProps={{
                startAdornment: <InputAdornment position="start">$</InputAdornment>,
              }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <Box display="flex" gap={1}>
              <Button variant="contained" onClick={handleSearch}>
                Search
              </Button>
              <Button variant="outlined" onClick={clearFilters}>
                Clear
              </Button>
              <Button variant="outlined" startIcon={<RefreshIcon />} onClick={() => refetch()}>
                Refresh
              </Button>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Products Grid */}
      {isLoading ? (
        <Box display="flex" justifyContent="center" py={8}>
          <CircularProgress size={60} />
        </Box>
      ) : products.length === 0 ? (
        <Paper sx={{ p: 8, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No products found
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={3}>
            {searchTerm || minPrice || maxPrice
              ? 'Try adjusting your search criteria'
              : 'Get started by adding your first product'}
          </Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreateProduct}>
            Add Product
          </Button>
        </Paper>
      ) : (
        <>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Typography variant="h6">
              {products.length} product{products.length !== 1 ? 's' : ''} found
            </Typography>
            {(searchTerm || minPrice || maxPrice) && (
              <Chip
                label="Filtered results"
                color="primary"
                variant="outlined"
                onDelete={clearFilters}
              />
            )}
          </Box>

          <Grid container spacing={3}>
            {products.map((product) => (
              <Grid item xs={12} sm={6} md={4} key={product.id}>
                <ProductCard
                  product={product}
                  onEdit={handleEditProduct}
                  onDelete={handleDeleteProduct}
                  onView={handleViewProduct}
                />
              </Grid>
            ))}
          </Grid>

          {/* Pagination */}
          {totalPages > 1 && (
            <Box display="flex" justifyContent="center" mt={4}>
              <Pagination
                count={totalPages}
                page={page + 1}
                onChange={(e, newPage) => setPage(newPage - 1)}
                color="primary"
                size="large"
              />
            </Box>
          )}
        </>
      )}

      {/* Product Form Dialog */}
      <ProductForm
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSubmit={handleFormSubmit}
        product={selectedProduct}
        isEditing={!!selectedProduct}
      />

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete "{productToDelete?.name}"? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={confirmDelete}
            color="error"
            variant="contained"
            disabled={deleteMutation.isLoading}
          >
            {deleteMutation.isLoading ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Product View Dialog */}
      <Dialog open={viewDialogOpen} onClose={() => setViewDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Product Details</DialogTitle>
        <DialogContent>
          {productToView && (
            <Box>
              <Typography variant="h6" gutterBottom>{productToView.name}</Typography>
              <Typography variant="body1" paragraph>{productToView.description}</Typography>
              <Typography variant="h5" color="primary" gutterBottom>
                ${productToView.price}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Product ID: {productToView.id}
              </Typography>
              {productToView.createdAt && (
                <Typography variant="caption" color="text.secondary" display="block">
                  Created: {new Date(productToView.createdAt).toLocaleString()}
                </Typography>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setViewDialogOpen(false)}>Close</Button>
          <Button
            onClick={() => {
              setViewDialogOpen(false);
              handleEditProduct(productToView);
            }}
            variant="contained"
          >
            Edit Product
          </Button>
        </DialogActions>
      </Dialog>

      {/* Floating Action Button */}
      <Fab
        color="primary"
        aria-label="add"
        sx={{ position: 'fixed', bottom: 16, right: 16 }}
        onClick={handleCreateProduct}
      >
        <AddIcon />
      </Fab>
    </Container>
  );
};

export default ProductsPage;