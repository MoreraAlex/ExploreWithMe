package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException exception) {
        return buildError(HttpStatus.NOT_FOUND, "The required object was not found.", exception);
    }

    @ExceptionHandler({
            ConflictException.class,
            DataIntegrityViolationException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(Exception exception) {
        return buildError(HttpStatus.CONFLICT, "Integrity constraint has been violated.", exception);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleForbidden(ForbiddenException exception) {
        return buildError(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.", exception);
    }

    @ExceptionHandler({
            BadRequestException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception exception) {
        return buildError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", exception);
    }

    private ApiError buildError(HttpStatus status, String reason, Exception exception) {
        log.warn("{}: {}", status, exception.getMessage(), exception);
        return ApiError.builder()
                .errors(List.of(exception.getClass().getName()))
                .message(exception.getMessage())
                .reason(reason)
                .status(status.name())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
