package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.service.HitService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HitController {

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final HitService hitService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@RequestBody @Valid HitRequestDto hitRequestDto) {
        log.info("Получен запрос на hit, {}", hitRequestDto);
        hitService.addHit(hitRequestDto);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public Collection<StatResponseDto> getStat(
            @RequestParam @NotNull @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime start,
            @RequestParam @NotNull @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime end,
            @RequestParam(required = false) ArrayList<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получен запрос на выгрузку статистики для {}", uris);
        return hitService.getStat(start, end, uris, unique);
    }
}
