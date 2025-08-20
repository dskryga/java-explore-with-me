package ru.practicum.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInvalidRequestException(final InvalidRequestException e) {
        return ApiError.builder().description(e.getMessage()).errorCode(409).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDeniedException(final AccessDeniedException e) {
        return ApiError.builder().description(e.getMessage()).errorCode(403).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e){
        return ApiError.builder().description(e.getMessage()).errorCode(404).build();
    }
}
