package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.dto.ProductDTO;
import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.service.ProductService;
import com.inventory.inventory_system.util.DTOMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Add product (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductDTO productDTO, 
                                       Authentication authentication,
                                       HttpServletRequest request) {
        try {
            Product product = DTOMapper.toProduct(productDTO);
            Product savedProduct = productService.addProduct(product, authentication.getName(), 
                                                           getClientIP(request));
            return ResponseEntity.ok(DTOMapper.toProductDTO(savedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Get all products
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            List<ProductDTO> productDTOs = DTOMapper.toProductDTOList(products);
            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            System.err.println("Error in getAllProducts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching products: " + e.getMessage());
        }
    }

    // Get products by category
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getProductsByCategory(@PathVariable String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);
            List<ProductDTO> productDTOs = DTOMapper.toProductDTOList(products);
            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            System.err.println("Error in getProductsByCategory: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching products by category: " + e.getMessage());
        }
    }

    // Get low stock products (Admin only)
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLowStockProducts() {
        try {
            List<Product> products = productService.getLowStockProducts();
            List<ProductDTO> productDTOs = DTOMapper.toProductDTOList(products);
            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            System.err.println("Error in getLowStockProducts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching low stock products: " + e.getMessage());
        }
    }

    // Get product by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                return ResponseEntity.ok(DTOMapper.toProductDTO(product));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Update product (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                          @Valid @RequestBody ProductDTO productDTO,
                                          Authentication authentication,
                                          HttpServletRequest request) {
        try {
            Product updatedProduct = productService.updateProduct(id, DTOMapper.toProduct(productDTO), 
                                                                 authentication.getName(), 
                                                                 getClientIP(request));
            if (updatedProduct != null) {
                return ResponseEntity.ok(DTOMapper.toProductDTO(updatedProduct));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Bulk update stock (Admin only)
    @PutMapping("/bulk-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkUpdateStock(@Valid @RequestBody List<ProductDTO> productDTOs,
                                            Authentication authentication,
                                            HttpServletRequest request) {
        try {
            List<Product> products = DTOMapper.toProductList(productDTOs);
            List<Product> updatedProducts = productService.bulkUpdateStock(products, 
                                                                          authentication.getName(), 
                                                                          getClientIP(request));
            return ResponseEntity.ok(DTOMapper.toProductDTOList(updatedProducts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Delete product (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id,
                                               Authentication authentication,
                                               HttpServletRequest request) {
        try {
            productService.deleteProduct(id, authentication.getName(), getClientIP(request));
            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Helper method to get client IP
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}