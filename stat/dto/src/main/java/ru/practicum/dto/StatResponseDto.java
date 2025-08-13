package ru.practicum.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatResponseDto {
    private String app;
    private String uri;
    private Long hits;
}
