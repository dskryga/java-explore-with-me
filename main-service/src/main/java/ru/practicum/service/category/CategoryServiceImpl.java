package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        validateAvailableName(newCategoryDto.getName());
        Category created = categoryRepository.save(CategoryMapper.mapToCategory(newCategoryDto));
        return CategoryMapper.mapToDto(created);
    }

    @Override
    public void deleteCategory(Long catId) {
        if (eventRepository.existsByCategoryId(catId)) {
            throw new InvalidRequestException("Нельзя удалить связанную с событием категорию");
        }
        getCategoryOrThrow(catId);
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId) {
        Category category = getCategoryOrThrow(catId);
        if (category.getName().equals(newCategoryDto.getName())) {
            return CategoryMapper.mapToDto(category);
        }
        validateAvailableName(newCategoryDto.getName());
        category.setName(newCategoryDto.getName());
        Category saved = categoryRepository.save(category);
        return CategoryMapper.mapToDto(saved);
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = getCategoryOrThrow(catId);
        return CategoryMapper.mapToDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::mapToDto)
                .collect(Collectors.toList());
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Категория с id %d не существует", id)));
    }

    private void validateAvailableName(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new InvalidRequestException(String.format("Имя категории %s уже занято", name));
        }
    }
}
