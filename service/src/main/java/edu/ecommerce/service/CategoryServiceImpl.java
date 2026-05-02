package edu.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ecommerce.core.dto.CategoryRequest;
import edu.ecommerce.core.dto.CategoryResponse;
import edu.ecommerce.core.dto.CategoryUpdateRequest;
import edu.ecommerce.core.entity.Category;
import edu.ecommerce.core.exception.ProductNotFoundException;
import edu.ecommerce.service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

// service implementation for category management (CRUD operations, listing, etc.)
// This class will implement the CategoryService interface and contain the business logic for managing categories
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    // We will need to inject the CategoryRepository to perform database operations
    private final CategoryRepository categoryRepository;

    // -------------------------------------------------------------------------
    // Category CRUD
    // -------------------------------------------------------------------------

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        }

        return mapToCategoryDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Category not found with id: " + id));
        return mapToCategoryDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Category not found with id: " + id));

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        }

        return mapToCategoryDTO(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private CategoryResponse mapToCategoryDTO(Category category) {
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        String parentName = category.getParent() != null ? category.getParent().getName() : null;
        return new CategoryResponse(category.getId(), category.getName(), parentId, parentName);
    }
        
}
