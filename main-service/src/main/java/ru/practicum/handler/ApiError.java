package ru.practicum.handler;

import lombok.*;

@Data
@Builder
public class ApiError {
    private String description;
    private Integer errorCode;
}
