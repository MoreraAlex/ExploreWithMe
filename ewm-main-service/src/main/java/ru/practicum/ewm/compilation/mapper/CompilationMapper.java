package ru.practicum.ewm.compilation.mapper;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {

    public static Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        return Compilation.builder()
                .events(events)
                .pinned(Boolean.TRUE.equals(dto.getPinned()))
                .title(dto.getTitle())
                .build();
    }

    public static CompilationDto toDto(Compilation compilation, Set<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(events)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}
