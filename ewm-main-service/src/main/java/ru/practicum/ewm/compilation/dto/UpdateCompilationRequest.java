package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

@Data
public class UpdateCompilationRequest {

    private Set<Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50)
    private String title;
}
