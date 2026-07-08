package ru.practicum.ewm.compilation.service;

import java.util.List;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

public interface CompilationService {

    CompilationDto create(NewCompilationDto dto);

    void delete(Long compId);

    CompilationDto update(Long compId, UpdateCompilationRequest dto);

    List<CompilationDto> get(Boolean pinned, int from, int size);

    CompilationDto getById(Long compId);
}
