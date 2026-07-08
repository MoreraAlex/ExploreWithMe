package ru.practicum.ewm.stats.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.stats.dto.StatsDateTimePattern;

@Data
@Builder
@AllArgsConstructor
public class ApiError {

    private String status;

    private String reason;

    private String message;

    @JsonFormat(pattern = StatsDateTimePattern.PATTERN)
    private LocalDateTime timestamp;
}
