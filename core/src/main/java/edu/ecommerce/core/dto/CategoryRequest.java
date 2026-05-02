package edu.ecommerce.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for category creation and update requests
// includes validation annotations to ensure data integrity
// lombok annotations for boilerplate code reduction (getters, setters, constructors)


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;
    // parentId is optional for top-level categories, but can be used to create subcategories
    private Long parentId;
}
