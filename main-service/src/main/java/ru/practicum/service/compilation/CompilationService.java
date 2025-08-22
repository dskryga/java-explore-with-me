package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilationByAdmin(Long compId, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> getCompilationsWithFilters(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);
}
