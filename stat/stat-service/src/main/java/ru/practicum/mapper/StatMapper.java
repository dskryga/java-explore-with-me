package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.model.Stat;

@UtilityClass
public class StatMapper {
    public StatResponseDto mapToStatResponseDto(Stat stat) {
        return StatResponseDto.builder()
                .app(stat.getApp())
                .uri(stat.getUri())
                .hits(stat.getHits())
                .build();
    }
}
