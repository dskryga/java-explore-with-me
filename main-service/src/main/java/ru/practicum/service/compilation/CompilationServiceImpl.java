package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new InvalidRequestException(String.format("Подборка с именем %s уже существует",
                    newCompilationDto.getTitle()));
        }

        Set<Event> events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));

        Compilation compilation = CompilationMapper.mapToCompilation(newCompilationDto, events);
        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.mapToDto(saved);
    }

    @Override
    public void deleteCompilation(Long compId) {
        getCompilationOrThrow(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilationByAdmin(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompilationOrThrow(compId);
        if (updateCompilationRequest.getTitle() != null && !updateCompilationRequest.getTitle().isBlank()) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationRequest.getEvents()));
            compilation.setEvents(events);
        }
        Compilation updated = compilationRepository.save(compilation);
        return CompilationMapper.mapToDto(updated);
    }

    @Override
    public List<CompilationDto> getCompilationsWithFilters(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations = List.of();
        Pageable pageable = PageRequest.of(from / size, size);

        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }
        return compilations.stream()
                .map(CompilationMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return CompilationMapper.mapToDto(getCompilationOrThrow(compId));
    }

    private Compilation getCompilationOrThrow(Long id) {
        return compilationRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Подборка с id %d не найдена", id)));
    }
}
