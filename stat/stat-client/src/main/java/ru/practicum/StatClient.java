package ru.practicum;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

public class StatClient {
    private final RestClient restClient;
    private final String serverUrl;
    private static final String HIT_URI = "/hit";
    private static final String STATS_URI = "/stats";

    public StatClient(String serverUrl) {
        this.serverUrl = serverUrl;
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(serverUrl)
                .build();
    }

    public void addHit(HitRequestDto hitRequestDto) {
        hitRequestDto.setTimestamp(LocalDateTime.now());
        restClient.post()
                .uri(HIT_URI)
                .body(hitRequestDto)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve();
    }

    public Collection<StatResponseDto> getStat(LocalDateTime start,
                                               LocalDateTime end,
                                               List<String> uris,
                                               Boolean unique) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(STATS_URI)
                            .queryParam("start", start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .queryParam("end", end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .queryParam("uris", uris != null ? String.join(",", uris) : "")
                            .queryParam("unique", unique)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new RuntimeException("Ошибка выгрузки статистики: " + response.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException("Неудалось получить статистики", e);
        }
    }

}

