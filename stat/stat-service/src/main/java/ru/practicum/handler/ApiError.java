package ru.practicum.handler;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {
    private String description;
    private int errorCode;
}
