package ru.practicum.service;

import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface HitService {
    void addHit(HitRequestDto hitRequestDto);

    List<StatResponseDto> getStat(LocalDateTime start,
                                  LocalDateTime end,
                                  ArrayList<String> uris,
                                  Boolean unique);
}
