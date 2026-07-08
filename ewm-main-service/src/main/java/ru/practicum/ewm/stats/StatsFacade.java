package ru.practicum.ewm.stats;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsFacade {

    private static final String APP = "ewm-main-service";

    private final StatsClient statsClient;

    public void saveHit(HttpServletRequest request) {
        try {
            statsClient.hit(EndpointHitDto.builder()
                    .app(APP)
                    .uri(request.getRequestURI())
                    .ip(getClientIp(request))
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (RuntimeException exception) {
            log.warn("Could not save stats hit for uri={}", request.getRequestURI(), exception);
        }
    }

    public Map<String, Long> getViews(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Map.of();
        }
        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    LocalDateTime.of(1970, 1, 1, 0, 0),
                    LocalDateTime.now().plusSeconds(1),
                    uris,
                    true
            ).getBody();
            if (stats == null) {
                return Map.of();
            }
            return stats.stream()
                    .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits, Long::sum));
        } catch (RuntimeException exception) {
            log.warn("Could not load stats for uris={}", uris, exception);
            return Map.of();
        }
    }

    public long getViews(String uri) {
        return getViews(List.of(uri)).getOrDefault(uri, 0L);
    }

    public static Function<Long, String> eventUri() {
        return eventId -> "/events/" + eventId;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        return forwardedFor.split(",")[0].trim();
    }
}
