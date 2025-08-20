package ru.practicum.controller.admin.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.category.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public CategoryDto createCategory(@RequestBody NewCategoryDto newCategoryDto) {
        log.info("Получен запрос на создание категории: {}", newCategoryDto.getName());
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Получен запрос на удаление категории с id {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@RequestBody NewCategoryDto newCategoryDto, @PathVariable Long catId) {
        log.info("Получен запрос на изменение категории с id {}", catId);
        return categoryService.updateCategory(newCategoryDto, catId);
    }
}
