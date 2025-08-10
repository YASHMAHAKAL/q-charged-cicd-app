package com.example.productservice.service;

import com.example.productservice.exception.ProductAlreadyExistsException;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        logger.debug("Fetching all products with pagination: {}", pageable);
        return productRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Fetching all products");
        return productRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        logger.debug("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
    
    public Product createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());
        
        if (productRepository.existsByNameIgnoreCase(product.getName())) {
            throw new ProductAlreadyExistsException(product.getName());
        }
        
        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully with id: {}", savedProduct.getId());
        return savedProduct;
    }
    
    public Product updateProduct(Long id, Product updatedProduct) {
        logger.info("Updating product with id: {}", id);
        
        Product existingProduct = getProductById(id);
        
        if (!existingProduct.getName().equalsIgnoreCase(updatedProduct.getName()) &&
            productRepository.existsByNameIgnoreCase(updatedProduct.getName())) {
            throw new ProductAlreadyExistsException(updatedProduct.getName());
        }
        
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        
        Product savedProduct = productRepository.save(existingProduct);
        logger.info("Product updated successfully: {}", savedProduct.getId());
        return savedProduct;
    }
    
    public void deleteProduct(Long id) {
        logger.info("Deleting product with id: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        
        productRepository.deleteById(id);
        logger.info("Product deleted successfully: {}", id);
    }
    
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        logger.debug("Searching products with keyword: {}", keyword);
        return productRepository.searchByKeyword(keyword);
    }
    
    @Transactional(readOnly = true)
    public List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Finding products in price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
}