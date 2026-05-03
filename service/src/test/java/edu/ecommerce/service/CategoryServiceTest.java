package edu.ecommerce.service;

import edu.ecommerce.core.dto.CategoryRequest;
import edu.ecommerce.core.dto.CategoryResponse;
import edu.ecommerce.core.dto.CategoryUpdateRequest;
import edu.ecommerce.core.entity.Category;
import edu.ecommerce.core.exception.ProductNotFoundException;
import edu.ecommerce.service.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private Category parentCategory;

    @BeforeEach
    void setUp() {
        parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setName("Electronics");
        parentCategory.setParent(null);

        testCategory = new Category();
        testCategory.setId(2L);
        testCategory.setName("Smartphones");
        testCategory.setParent(parentCategory);
    }

    @Test
    void testCreateCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Laptops");

        Category savedCategory = new Category();
        savedCategory.setId(3L);
        savedCategory.setName("Laptops");
        savedCategory.setParent(null);

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getName()).isEqualTo("Laptops");
        assertThat(response.getParentId()).isNull();
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testCreateCategory_WithParent() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Gaming Laptops");
        request.setParentId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        Category savedCategory = new Category();
        savedCategory.setId(4L);
        savedCategory.setName("Gaming Laptops");
        savedCategory.setParent(parentCategory);

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Gaming Laptops");
        assertThat(response.getParentId()).isEqualTo(1L);
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateCategory_ParentNotFound() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Gaming Laptops");
        request.setParentId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(request))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Parent category not found");
    }

    @Test
    void testGetCategoryById_Success() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));

        CategoryResponse response = categoryService.getCategoryById(2L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Smartphones");
        assertThat(response.getParentId()).isEqualTo(1L);
        assertThat(response.getParentName()).isEqualTo("Electronics");
        verify(categoryRepository, times(1)).findById(2L);
    }

    @Test
    void testGetCategoryById_NotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    void testGetCategoryById_NoParent() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getParentId()).isNull();
        assertThat(response.getParentName()).isNull();
    }

    @Test
    void testListAllCategories_Success() {
        List<Category> categories = new ArrayList<>();
        categories.add(parentCategory);
        categories.add(testCategory);

        when(categoryRepository.findAll()).thenReturn(categories);

        List<CategoryResponse> responses = categoryService.listAllCategories();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Electronics");
        assertThat(responses.get(1).getName()).isEqualTo("Smartphones");
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testListAllCategories_Empty() {
        when(categoryRepository.findAll()).thenReturn(new ArrayList<>());

        List<CategoryResponse> responses = categoryService.listAllCategories();

        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();
    }

    @Test
    void testUpdateCategory_OnlyName() {
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Updated Smartphones");
        updateRequest.setParentId(null);

        testCategory.setName("Updated Smartphones");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.updateCategory(2L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Smartphones");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_ChangeParent() {
        Category newParent = new Category();
        newParent.setId(5L);
        newParent.setName("Mobile Devices");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName(null);
        updateRequest.setParentId(5L);

        testCategory.setParent(newParent);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(newParent));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.updateCategory(2L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getParentId()).isEqualTo(5L);
        verify(categoryRepository, times(1)).findById(5L);
    }

    @Test
    void testUpdateCategory_RemoveParent() {
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName(null);

        testCategory.setParent(null);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.updateCategory(2L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getParentId()).isNull();
    }

    @Test
    void testUpdateCategory_NotFound() {
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Updated");

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(999L, updateRequest))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    void testUpdateCategory_InvalidParent() {
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName(null);
        updateRequest.setParentId(999L);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(2L, updateRequest))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Parent category not found");
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(testCategory);

        categoryService.deleteCategory(2L);

        verify(categoryRepository, times(1)).findById(2L);
        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    void testDeleteCategory_NotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Category not found");
    }
}
