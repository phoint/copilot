package edu.ecommerce.service;

import java.util.List;

import edu.ecommerce.core.dto.CategoryRequest;
import edu.ecommerce.core.dto.CategoryResponse;
import edu.ecommerce.core.dto.CategoryUpdateRequest;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse getCategoryById(Long id);
    List<CategoryResponse> listAllCategories();
    CategoryResponse updateCategory(Long id, CategoryUpdateRequest request);
    void deleteCategory(Long id);
}
