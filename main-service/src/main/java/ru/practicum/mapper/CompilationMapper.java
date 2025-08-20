package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {
    public Compilation mapToCompilation(NewCompilationDto newCompilationDto, Set<Event> events) {
        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .events(events)
                .build();
    }

    public CompilationDto mapToDto(Compilation compilation) {

        List<EventShortDto> shortEvents = compilation.getEvents()
                .stream()
                .map(EventMapper::mapToShortDto)
                .collect(Collectors.toList());

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(shortEvents)
                .build();
    }
}
