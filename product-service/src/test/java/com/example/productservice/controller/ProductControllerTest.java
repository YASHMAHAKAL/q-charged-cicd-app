package com.example.productservice.controller;

import com.example.productservice.exception.ProductAlreadyExistsException;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Test Product", "Test Description", new BigDecimal("99.99"));
        testProduct.setId(1L);
    }

    @Test
    void getAllProducts_WithoutPagination_ShouldReturnAllProducts() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(99.99));

        verify(productService).getAllProducts();
    }

    @Test
    void getAllProducts_WithPagination_ShouldReturnPagedProducts() throws Exception {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
        when(productService.getAllProducts(any(PageRequest.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/v1/products")
                .param("paginated", "true")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));

        verify(productService).getAllProducts(any(PageRequest.class));
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(productService).getProductById(1L);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(1L)).thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Product Not Found"));

        verify(productService).getProductById(1L);
    }

    @Test
    void createProduct_WithValidData_ShouldCreateProduct() throws Exception {
        Product newProduct = new Product("New Product", "New Description", new BigDecimal("149.99"));
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Product invalidProduct = new Product("", "Description", new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(productService, never()).createProduct(any());
    }

    @Test
    void createProduct_WhenProductAlreadyExists_ShouldReturnConflict() throws Exception {
        Product newProduct = new Product("Existing Product", "Description", new BigDecimal("99.99"));
        when(productService.createProduct(any(Product.class)))
                .thenThrow(new ProductAlreadyExistsException("Existing Product"));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Product Already Exists"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void updateProduct_WithValidData_ShouldUpdateProduct() throws Exception {
        Product updatedProduct = new Product("Updated Product", "Updated Description", new BigDecimal("199.99"));
        updatedProduct.setId(1L);
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Product"));

        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void updateProduct_WhenProductNotExists_ShouldReturnNotFound() throws Exception {
        Product updatedProduct = new Product("Updated Product", "Updated Description", new BigDecimal("199.99"));
        when(productService.updateProduct(eq(1L), any(Product.class)))
                .thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_WhenProductNotExists_ShouldReturnNotFound() throws Exception {
        doThrow(new ProductNotFoundException(1L)).when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(productService).deleteProduct(1L);
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.searchProducts("test")).thenReturn(products);

        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).searchProducts("test");
    }

    @Test
    void findByPriceRange_ShouldReturnProductsInRange() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.findProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(products);

        mockMvc.perform(get("/api/v1/products/price-range")
                .param("minPrice", "50.00")
                .param("maxPrice", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).findProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class));
    }
}