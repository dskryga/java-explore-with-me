package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.mapper.StatMapper;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {
    private final HitRepository hitRepository;

    @Override
    public void addHit(HitRequestDto hitRequestDto) {
        hitRepository.save(HitMapper.mapToHit(hitRequestDto));
    }

    @Override
    public List<StatResponseDto> getStat(LocalDateTime start,
                                         LocalDateTime end,
                                         ArrayList<String> uris,
                                         Boolean unique) {
        if(start!=null && end!=null && start.isAfter(end)) {
            throw new InvalidRequestException("Несоответсвие даты в фильтре");
        }

        if (unique) {
            if (uris == null || uris.isEmpty()) {
                return hitRepository.findUniqueStatsByDate(start, end).stream()
                        .map(StatMapper::mapToStatResponseDto)
                        .collect(Collectors.toList());
            }
            return hitRepository.findUniqueStatsByParameters(start, end, uris).stream()
                    .map(StatMapper::mapToStatResponseDto)
                    .collect(Collectors.toList());

        } else {
            if (uris == null || uris.isEmpty()) {
                return hitRepository.findAllStatsByDate(start, end).stream()
                        .map(StatMapper::mapToStatResponseDto)
                        .collect(Collectors.toList());
            }
            return hitRepository.findStatsByParameters(start, end, uris).stream()
                    .map(StatMapper::mapToStatResponseDto)
                    .collect(Collectors.toList());
        }
    }
}
