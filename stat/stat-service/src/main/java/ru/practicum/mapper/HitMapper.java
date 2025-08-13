package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.model.Hit;

@UtilityClass
public class HitMapper {
    public Hit mapToHit(HitRequestDto hitRequestDto) {
        return Hit.builder()
                .app(hitRequestDto.getApp())
                .uri(hitRequestDto.getUri())
                .ip(hitRequestDto.getIp())
                .timestamp(hitRequestDto.getTimestamp())
                .build();
    }
}
