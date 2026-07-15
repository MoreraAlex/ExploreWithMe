package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.ewm.event.model.ReactionType;

@Data
public class EventReactionRequest {

    @NotNull
    private ReactionType reaction;
}
