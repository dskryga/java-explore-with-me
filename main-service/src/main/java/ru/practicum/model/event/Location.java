package ru.practicum.model.event;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {
    private Double lat;
    private Double lon;
}
