package edu.ecommerce.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for updating category information (e.g., name, parent category)
// This can be used in the updateCategory method of the CategoryService
// For simplicity, we can start with just the name and parentId fields, but this can be extended as needed
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    private String name;
    private Long parentId;
}
