package ru.practicum.ewm.category.service;

import java.util.List;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

public interface CategoryService {

    CategoryDto create(NewCategoryDto dto);

    CategoryDto update(Long catId, CategoryDto dto);

    void delete(Long catId);

    List<CategoryDto> get(int from, int size);

    CategoryDto getById(Long catId);
}
