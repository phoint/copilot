package edu.ecommerce.api.repository;

import edu.ecommerce.core.entity.Category;
import edu.ecommerce.core.entity.Product;
import edu.ecommerce.service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ProductRepositoryJpaTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Category category;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();
        category = new Category();
        category.setName("TestElectronics_" + timestamp);

        product1 = new Product();
        product1.setName("TestLaptop_" + timestamp);
        product1.setDescription("High performance laptop");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setStockQuantity(10);
        product1.setCategory(category);

        product2 = new Product();
        product2.setName("TestMouse_" + timestamp);
        product2.setDescription("Wireless mouse");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setStockQuantity(50);
        product2.setCategory(category);

        product3 = new Product();
        product3.setName("TestLaptopStand_" + timestamp);
        product3.setDescription("Aluminum laptop stand");
        product3.setPrice(new BigDecimal("49.99"));
        product3.setStockQuantity(30);
        product3.setCategory(category);
    }

    @Test
    void testSaveProduct_ShouldPersistAndGenerateId() {
        testEntityManager.persistAndFlush(category);
        Product saved = productRepository.save(product1);
        testEntityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
    }

    @Test
    void testSearchProducts_ByKeyword_ShouldReturnMatchingProducts() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);
        testEntityManager.persistAndFlush(product3);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Product> results = productRepository.searchProducts("laptop", null, null, null, pageable);

        assertThat(results.getContent())
            .hasSizeGreaterThanOrEqualTo(2)
            .anyMatch(p -> p.getName().contains("Laptop") && p.getCategory().getId().equals(category.getId()))
            .anyMatch(p -> p.getName().contains("Stand") && p.getCategory().getId().equals(category.getId()));
    }

    @Test
    void testSearchProducts_ByKeywordInDescription_ShouldReturnMatchingProducts() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);
        testEntityManager.persistAndFlush(product3);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Product> results = productRepository.searchProducts("Wireless", null, null, null, pageable);

        assertThat(results.getContent())
            .hasSizeGreaterThanOrEqualTo(1)
            .anyMatch(p -> p.getName().contains("Mouse") && p.getCategory().getId().equals(category.getId()));
    }

    @Test
    void testSearchProducts_ByCategoryId_ShouldReturnProductsInCategory() {
        long timestamp = System.currentTimeMillis();
        Category anotherCategory = new Category();
        anotherCategory.setName("TestFurniture_" + timestamp);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(anotherCategory);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        Product productInAnotherCategory = new Product();
        productInAnotherCategory.setName("TestDesk_" + timestamp);
        productInAnotherCategory.setDescription("Wooden desk");
        productInAnotherCategory.setPrice(new BigDecimal("299.99"));
        productInAnotherCategory.setStockQuantity(5);
        productInAnotherCategory.setCategory(anotherCategory);
        testEntityManager.persistAndFlush(productInAnotherCategory);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Product> results = productRepository.searchProducts(null, category.getId(), null, null, pageable);

        assertThat(results.getContent())
            .hasSizeGreaterThanOrEqualTo(2)
            .allMatch(p -> p.getCategory().getId().equals(category.getId()));
    }

    @Test
    void testSearchProducts_ByMinPrice_ShouldReturnProductsGreaterThanOrEqual() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);
        testEntityManager.persistAndFlush(product3);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Product> results = productRepository.searchProducts(null, null, new BigDecimal("900.00"), null, pageable);

        assertThat(results.getContent())
            .hasSizeGreaterThanOrEqualTo(1)
            .allMatch(p -> p.getPrice().compareTo(new BigDecimal("900.00")) >= 0)
            .anyMatch(p -> p.getId().equals(product1.getId()));
    }

    @Test
    void testSearchProducts_ByMaxPrice_ShouldReturnProductsLessThanOrEqual() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);
        testEntityManager.persistAndFlush(product3);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Product> results = productRepository.searchProducts(null, null, null, new BigDecimal("100.00"), pageable);

        assertThat(results.getContent())
            .hasSizeGreaterThanOrEqualTo(2)
            .allMatch(p -> p.getPrice().compareTo(new BigDecimal("100.00")) <= 0)
            .anyMatch(p -> p.getId().equals(product2.getId()))
            .anyMatch(p -> p.getId().equals(product3.getId()));
    }

    @Test
    void testSearchProducts_ByPriceRange_ShouldReturnProductsBetween() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);
        testEntityManager.persistAndFlush(product3);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Product> results = productRepository.searchProducts(null, null, new BigDecimal("20.00"), new BigDecimal("100.00"), pageable);

        assertThat(results.getContent())
            .hasSizeGreaterThanOrEqualTo(2)
            .allMatch(p -> p.getPrice().compareTo(new BigDecimal("20.00")) >= 0
                    && p.getPrice().compareTo(new BigDecimal("100.00")) <= 0)
            .anyMatch(p -> p.getId().equals(product3.getId()))
            .anyMatch(p -> p.getId().equals(product2.getId()));
    }

    @Test
    void testSearchProducts_WithAllParameters_ShouldReturnFilteredProducts() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);
        testEntityManager.persistAndFlush(product3);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Product> results = productRepository.searchProducts("laptop", category.getId(),
                                                                 new BigDecimal("40.00"),
                                                                 new BigDecimal("1000.00"), pageable);

        assertThat(results.getContent())
            .hasSizeGreaterThanOrEqualTo(1)
            .anyMatch(p -> p.getCategory().getId().equals(category.getId())
                    && p.getPrice().compareTo(new BigDecimal("40.00")) >= 0
                    && p.getPrice().compareTo(new BigDecimal("1000.00")) <= 0);
    }

    @Test
    void testSearchProducts_WithNullParameters_ShouldReturnAllProducts() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);
        testEntityManager.persistAndFlush(product3);

        Pageable pageable = PageRequest.of(0, 1000);
        Page<Product> results = productRepository.searchProducts(null, null, null, null, pageable);

        assertThat(results.getTotalElements()).isGreaterThanOrEqualTo(3L);
    }

    @Test
    void testSearchProducts_WithKeywordNotFound_ShouldReturnEmptyPage() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> results = productRepository.searchProducts("nonexistent", null, null, null, pageable);

        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void testSearchProducts_Pagination_ShouldReturnCorrectPage() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);
        testEntityManager.persistAndFlush(product3);

        Pageable pageZero = PageRequest.of(0, 2);
        Page<Product> pageOne = productRepository.searchProducts(null, null, null, null, pageZero);

        assertThat(pageOne.getContent()).hasSize(2);
        assertThat(pageOne.getTotalElements()).isGreaterThanOrEqualTo(3L);
    }

    @Test
    void testFindById_ShouldReturnProduct() {
        testEntityManager.persistAndFlush(category);
        Product saved = testEntityManager.persistAndFlush(product1);

        Optional<Product> found = productRepository.findById(saved.getId());

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(p -> assertThat(p.getId()).isEqualTo(saved.getId()));
    }

    @Test
    void testFindAll_ShouldReturnAllProducts() {
        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        assertThat(productRepository.findAll())
            .hasSizeGreaterThanOrEqualTo(2)
            .anyMatch(p -> p.getName().contains("TestLaptop"))
            .anyMatch(p -> p.getName().contains("TestMouse"));
    }

    @Test
    void testCount_ShouldReturnTotalProducts() {
        testEntityManager.persistAndFlush(category);
        long beforeCount = productRepository.count();
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        long count = productRepository.count();

        assertThat(count).isGreaterThan(beforeCount);
    }

    @Test
    void testUpdate_ShouldModifyExistingProduct() {
        testEntityManager.persistAndFlush(category);
        Product saved = testEntityManager.persistAndFlush(product1);

        saved.setPrice(new BigDecimal("899.99"));
        saved.setStockQuantity(5);
        productRepository.save(saved);
        testEntityManager.flush();

        Product updated = testEntityManager.find(Product.class, saved.getId());

        assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("899.99"));
        assertThat(updated.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void testDelete_ShouldRemoveProduct() {
        testEntityManager.persistAndFlush(category);
        Product saved = testEntityManager.persistAndFlush(product1);
        Long productId = saved.getId();

        productRepository.deleteById(productId);
        testEntityManager.flush();

        Product deleted = testEntityManager.find(Product.class, productId);

        assertThat(deleted).isNull();
    }
}
