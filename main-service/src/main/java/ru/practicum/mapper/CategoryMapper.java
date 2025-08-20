package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

@UtilityClass
public class CategoryMapper {
    public Category mapToCategory(NewCategoryDto newCategoryDto){
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }
    public Category mapToCategory(CategoryDto categoryDto){
        return Category.builder()
                .name(categoryDto.getName())
                .id(categoryDto.getId())
                .build();
    }
    public CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
