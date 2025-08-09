package ru.practicum.model;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stat {
    private String app;
    private String uri;
    private Long hits;
}
