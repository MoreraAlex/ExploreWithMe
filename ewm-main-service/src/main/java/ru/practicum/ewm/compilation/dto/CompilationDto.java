package ru.practicum.ewm.compilation.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.event.dto.EventShortDto;

@Data
@Builder
public class CompilationDto {

    private Long id;

    private Set<EventShortDto> events;

    private Boolean pinned;

    private String title;
}
