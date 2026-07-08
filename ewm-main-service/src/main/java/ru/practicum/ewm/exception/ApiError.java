package ru.practicum.ewm.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.common.DateTimePattern;

@Data
@Builder
public class ApiError {

    private List<String> errors;

    private String message;

    private String reason;

    private String status;

    @JsonFormat(pattern = DateTimePattern.PATTERN)
    private LocalDateTime timestamp;
}
