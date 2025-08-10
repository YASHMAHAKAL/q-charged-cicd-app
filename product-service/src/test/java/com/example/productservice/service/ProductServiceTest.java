package com.example.productservice.service;

import com.example.productservice.exception.ProductAlreadyExistsException;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Test Product", "Test Description", new BigDecimal("99.99"));
        testProduct.setId(1L);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(testProduct.getName(), result.get(0).getName());
        verify(productRepository).findAll();
    }

    @Test
    void getAllProductsWithPagination_ShouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<Product> result = productService.getAllProducts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(testProduct.getName(), result.getContent().get(0).getName());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.getProductById(1L);

        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1L));
        verify(productRepository).findById(1L);
    }

    @Test
    void createProduct_WhenProductNameNotExists_ShouldCreateProduct() {
        when(productRepository.existsByNameIgnoreCase(testProduct.getName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository).existsByNameIgnoreCase(testProduct.getName());
        verify(productRepository).save(testProduct);
    }

    @Test
    void createProduct_WhenProductNameExists_ShouldThrowException() {
        when(productRepository.existsByNameIgnoreCase(testProduct.getName())).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.createProduct(testProduct));
        verify(productRepository).existsByNameIgnoreCase(testProduct.getName());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateProduct() {
        Product existingProduct = new Product("Old Name", "Old Description", new BigDecimal("50.00"));
        existingProduct.setId(1L);
        Product updatedProduct = new Product("New Name", "New Description", new BigDecimal("100.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByNameIgnoreCase("New Name")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        assertEquals(new BigDecimal("100.00"), result.getPrice());
        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_WhenProductNotExists_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(1L, testProduct));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_WhenNewNameAlreadyExists_ShouldThrowException() {
        Product existingProduct = new Product("Old Name", "Old Description", new BigDecimal("50.00"));
        existingProduct.setId(1L);
        Product updatedProduct = new Product("Existing Name", "New Description", new BigDecimal("100.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByNameIgnoreCase("Existing Name")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.updateProduct(1L, updatedProduct));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenProductNotExists_ShouldThrowException() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(1L));
        verify(productRepository).existsById(1L);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.searchByKeyword("test")).thenReturn(products);

        List<Product> result = productService.searchProducts("test");

        assertEquals(1, result.size());
        assertEquals(testProduct.getName(), result.get(0).getName());
        verify(productRepository).searchByKeyword("test");
    }

    @Test
    void findProductsByPriceRange_ShouldReturnProductsInRange() {
        List<Product> products = Arrays.asList(testProduct);
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("150.00");
        when(productRepository.findByPriceBetween(minPrice, maxPrice)).thenReturn(products);

        List<Product> result = productService.findProductsByPriceRange(minPrice, maxPrice);

        assertEquals(1, result.size());
        assertEquals(testProduct.getName(), result.get(0).getName());
        verify(productRepository).findByPriceBetween(minPrice, maxPrice);
    }
}