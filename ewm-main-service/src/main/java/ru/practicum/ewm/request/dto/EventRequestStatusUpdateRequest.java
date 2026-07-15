package ru.practicum.ewm.request.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import ru.practicum.ewm.request.model.RequestStatus;

@Data
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;

    @NotNull
    private RequestStatus status;
}
