package ru.practicum.ewm.request.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.request.model.RequestStatus;

@Data
@Builder
public class ParticipationRequestDto {

    private Long id;

    private LocalDateTime created;

    private Long event;

    private Long requester;

    private RequestStatus status;
}
