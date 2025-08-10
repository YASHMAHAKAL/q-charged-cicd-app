import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || '/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export const productService = {
  // Get all products with pagination
  getProducts: async (page = 0, size = 10, sortBy = 'id', sortDir = 'asc') => {
    const response = await api.get('/products', {
      params: { paginated: true, page, size, sortBy, sortDir }
    });
    return response.data;
  },

  // Get all products without pagination
  getAllProducts: async () => {
    const response = await api.get('/products');
    return response.data;
  },

  // Get product by ID
  getProductById: async (id) => {
    const response = await api.get(`/products/${id}`);
    return response.data;
  },

  // Create new product
  createProduct: async (product) => {
    const response = await api.post('/products', product);
    return response.data;
  },

  // Update product
  updateProduct: async (id, product) => {
    const response = await api.put(`/products/${id}`, product);
    return response.data;
  },

  // Delete product
  deleteProduct: async (id) => {
    await api.delete(`/products/${id}`);
  },

  // Search products
  searchProducts: async (keyword) => {
    const response = await api.get('/products/search', {
      params: { keyword }
    });
    return response.data;
  },

  // Get products by price range
  getProductsByPriceRange: async (minPrice, maxPrice) => {
    const response = await api.get('/products/price-range', {
      params: { minPrice, maxPrice }
    });
    return response.data;
  }
};