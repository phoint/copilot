package edu.ecommerce.service;

import edu.ecommerce.core.dto.ProductRequest;
import edu.ecommerce.core.dto.ProductResponse;
import edu.ecommerce.core.dto.ProductUpdateRequest;
import edu.ecommerce.core.entity.Category;
import edu.ecommerce.core.entity.Product;
import edu.ecommerce.core.exception.ProductNotFoundException;
import edu.ecommerce.service.repository.CategoryRepository;
import edu.ecommerce.service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("iPhone 13");
        testProduct.setDescription("Apple iPhone 13");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setStockQuantity(50);
        testProduct.setCategory(testCategory);
    }

    @Test
    void testCreateProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setName("Samsung Galaxy S23");
        request.setDescription("Samsung Galaxy S23");
        request.setPrice(new BigDecimal("899.99"));
        request.setStockQuantity(100);
        request.setCategoryId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("iPhone 13");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("Electronics");
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_CategoryNotFound() {
        ProductRequest request = new ProductRequest();
        request.setName("Samsung Galaxy S23");
        request.setPrice(new BigDecimal("899.99"));
        request.setStockQuantity(100);
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("iPhone 13");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(response.getStockQuantity()).isEqualTo(50);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void testGetAllProducts_Success() {
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Samsung Galaxy S23");
        product2.setPrice(new BigDecimal("899.99"));
        product2.setCategory(testCategory);

        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        products.add(product2);

        when(productRepository.findAll()).thenReturn(products);

        List<ProductResponse> responses = productService.getAllProducts();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("iPhone 13");
        assertThat(responses.get(1).getName()).isEqualTo("Samsung Galaxy S23");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetAllProducts_Empty() {
        when(productRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProductResponse> responses = productService.getAllProducts();

        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();
    }

    @Test
    void testListAllProducts_Success() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        Page<Product> page = new PageImpl<>(products);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Page<ProductResponse> responses = productService.listAllProducts(pageable);

        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testUpdateProduct_OnlyName() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("iPhone 14");
        updateRequest.setDescription(null);
        updateRequest.setPrice(null);
        updateRequest.setStockQuantity(null);
        updateRequest.setCategoryId(null);

        testProduct.setName("iPhone 14");
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("iPhone 14");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_OnlyPrice() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName(null);
        updateRequest.setDescription(null);
        updateRequest.setPrice(new BigDecimal("1099.99"));
        updateRequest.setStockQuantity(null);
        updateRequest.setCategoryId(null);

        testProduct.setPrice(new BigDecimal("1099.99"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("1099.99"));
    }

    @Test
    void testUpdateProduct_OnlyStockQuantity() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName(null);
        updateRequest.setDescription(null);
        updateRequest.setPrice(null);
        updateRequest.setStockQuantity(75);
        updateRequest.setCategoryId(null);

        testProduct.setStockQuantity(75);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStockQuantity()).isEqualTo(75);
    }

    @Test
    void testUpdateProduct_ChangeCategory() {
        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("Mobile Devices");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName(null);
        updateRequest.setDescription(null);
        updateRequest.setPrice(null);
        updateRequest.setStockQuantity(null);
        updateRequest.setCategoryId(2L);

        testProduct.setCategory(newCategory);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCategoryId()).isEqualTo(2L);
        verify(categoryRepository, times(1)).findById(2L);
    }

    @Test
    void testUpdateProduct_AllFields() {
        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("Mobile Devices");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("iPhone 14 Pro");
        updateRequest.setDescription("iPhone 14 Pro Max");
        updateRequest.setPrice(new BigDecimal("1099.99"));
        updateRequest.setStockQuantity(80);
        updateRequest.setCategoryId(2L);

        testProduct.setName("iPhone 14 Pro");
        testProduct.setDescription("iPhone 14 Pro Max");
        testProduct.setPrice(new BigDecimal("1099.99"));
        testProduct.setStockQuantity(80);
        testProduct.setCategory(newCategory);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("iPhone 14 Pro");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("1099.99"));
        assertThat(response.getStockQuantity()).isEqualTo(80);
        verify(categoryRepository, times(1)).findById(2L);
    }

    @Test
    void testUpdateProduct_NotFound() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Updated Product");

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, updateRequest))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void testUpdateProduct_InvalidCategory() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName(null);
        updateRequest.setCategoryId(999L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(testProduct);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(999L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void testSearchProducts_Success() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        Page<Product> page = new PageImpl<>(products);

        when(productRepository.searchProducts(
            "iPhone", 1L, new BigDecimal("500.00"), new BigDecimal("1500.00"), null
        )).thenReturn(page);

        Page<ProductResponse> responses = productService.searchProducts(
            "iPhone", 1L, new BigDecimal("500.00"), new BigDecimal("1500.00"), null
        );

        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        verify(productRepository, times(1)).searchProducts(
            "iPhone", 1L, new BigDecimal("500.00"), new BigDecimal("1500.00"), null
        );
    }

    @Test
    void testSearchProducts_NoResults() {
        List<Product> products = new ArrayList<>();
        Page<Product> page = new PageImpl<>(products);

        when(productRepository.searchProducts(
            "NonExistent", null, null, null, null
        )).thenReturn(page);

        Page<ProductResponse> responses = productService.searchProducts(
            "NonExistent", null, null, null, null
        );

        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).isEmpty();
    }
}
