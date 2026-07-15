package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.common.DateTimePattern;
import ru.practicum.ewm.event.model.ReactionType;

@Data
@Builder
public class EventReactionDto {

    private Long eventId;

    private Long userId;

    private ReactionType reaction;

    @JsonFormat(pattern = DateTimePattern.PATTERN)
    private LocalDateTime created;

    @JsonFormat(pattern = DateTimePattern.PATTERN)
    private LocalDateTime updated;
}
