package edu.ecommerce.api.repository;

import edu.ecommerce.core.entity.Category;
import edu.ecommerce.service.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryJpaTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();
        parentCategory = new Category();
        parentCategory.setName("TestCategory_" + timestamp);
        parentCategory.setParent(null);

        childCategory = new Category();
        childCategory.setName("TestSubCategory_" + timestamp);
        childCategory.setParent(parentCategory);
    }

    @Test
    void testSaveCategory_ShouldPersistAndGenerateId() {
        Category saved = categoryRepository.save(parentCategory);
        testEntityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
    }

    @Test
    void testFindByParentIsNull_ShouldReturnRootCategories() {
        testEntityManager.persistAndFlush(parentCategory);

        Category child = new Category();
        child.setName("TestPhones_" + System.currentTimeMillis());
        child.setParent(parentCategory);
        testEntityManager.persistAndFlush(child);

        List<Category> rootCategories = categoryRepository.findByParentIsNull();

        assertThat(rootCategories)
            .hasSizeGreaterThanOrEqualTo(1)
            .anyMatch(cat -> cat.getId().equals(parentCategory.getId()));
    }

    @Test
    void testFindByParentIsNull_ShouldReturnEmptyWhenNoRootCategories() {
        testEntityManager.persistAndFlush(parentCategory);
        testEntityManager.persistAndFlush(childCategory);

        childCategory.setParent(null);
        testEntityManager.merge(childCategory);
        testEntityManager.persistAndFlush(parentCategory);

        Category anotherChild = new Category();
        anotherChild.setName("Phones");
        anotherChild.setParent(parentCategory);
        testEntityManager.persistAndFlush(anotherChild);

        List<Category> result = categoryRepository.findByParentIsNull();

        assertThat(result).isNotEmpty();
    }

    @Test
    void testFindByParentId_ShouldReturnChildCategories() {
        Category savedParent = testEntityManager.persistAndFlush(parentCategory);

        Category child1 = new Category();
        long timestamp = System.currentTimeMillis();
        child1.setName("TestLaptops_" + timestamp);
        child1.setParent(savedParent);

        Category child2 = new Category();
        child2.setName("TestTablets_" + timestamp);
        child2.setParent(savedParent);

        testEntityManager.persistAndFlush(child1);
        testEntityManager.persistAndFlush(child2);

        List<Category> children = categoryRepository.findByParentId(savedParent.getId());

        assertThat(children)
            .hasSize(2)
            .allMatch(cat -> cat.getParent().getId().equals(savedParent.getId()));
    }

    @Test
    void testFindByParentId_ShouldReturnEmptyWhenNoChildren() {
        Category savedParent = testEntityManager.persistAndFlush(parentCategory);

        List<Category> children = categoryRepository.findByParentId(savedParent.getId());

        assertThat(children).isEmpty();
    }

    @Test
    void testFindByName_ShouldReturnCategory() {
        testEntityManager.persistAndFlush(parentCategory);

        Optional<Category> found = categoryRepository.findByName(parentCategory.getName());

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(cat -> assertThat(cat.getId()).isEqualTo(parentCategory.getId()));
    }

    @Test
    void testFindByName_ShouldReturnEmptyWhenNotFound() {
        testEntityManager.persistAndFlush(parentCategory);

        Optional<Category> found = categoryRepository.findByName("NotExisting");

        assertThat(found).isEmpty();
    }

    @Test
    void testExistsByName_ShouldReturnTrueWhenExists() {
        testEntityManager.persistAndFlush(parentCategory);

        boolean exists = categoryRepository.existsByName("Electronics");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByName_ShouldReturnFalseWhenNotExists() {
        testEntityManager.persistAndFlush(parentCategory);

        boolean exists = categoryRepository.existsByName("NotExisting");

        assertThat(exists).isFalse();
    }

    @Test
    void testFindById_ShouldReturnCategory() {
        Category saved = testEntityManager.persistAndFlush(parentCategory);

        Optional<Category> found = categoryRepository.findById(saved.getId());

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(cat -> assertThat(cat.getId()).isEqualTo(saved.getId()));
    }

    @Test
    void testFindAll_ShouldReturnAllCategories() {
        testEntityManager.persistAndFlush(parentCategory);
        testEntityManager.persistAndFlush(childCategory);

        List<Category> all = categoryRepository.findAll();

        assertThat(all)
            .hasSizeGreaterThanOrEqualTo(2)
            .anyMatch(c -> c.getId().equals(parentCategory.getId()))
            .anyMatch(c -> c.getId().equals(childCategory.getId()));
    }

    @Test
    void testCount_ShouldReturnTotalCategories() {
        testEntityManager.persistAndFlush(parentCategory);
        testEntityManager.persistAndFlush(childCategory);

        long count = categoryRepository.count();

        assertThat(count).isGreaterThanOrEqualTo(2L);
    }

    @Test
    void testUpdate_ShouldModifyExistingCategory() {
        Category saved = testEntityManager.persistAndFlush(parentCategory);

        saved.setName("Updated Electronics");
        categoryRepository.save(saved);
        testEntityManager.flush();

        Category updated = testEntityManager.find(Category.class, saved.getId());

        assertThat(updated.getName()).isEqualTo("Updated Electronics");
    }

    @Test
    void testDelete_ShouldRemoveCategory() {
        Category saved = testEntityManager.persistAndFlush(parentCategory);
        Long categoryId = saved.getId();

        categoryRepository.deleteById(categoryId);
        testEntityManager.flush();

        Category deleted = testEntityManager.find(Category.class, categoryId);

        assertThat(deleted).isNull();
    }
}
