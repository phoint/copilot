package edu.ecommerce.service;

import edu.ecommerce.core.dto.ProductRequest;
import edu.ecommerce.core.dto.ProductResponse;
import edu.ecommerce.core.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    Page<ProductResponse> listAllProducts(Pageable pageable);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    void deleteProduct(Long id);

    Page<ProductResponse> searchProducts(String keyword, Long categoryId,
                                    BigDecimal minPrice, BigDecimal maxPrice,
                                    Pageable pageable);
}
