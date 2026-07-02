package ru.practicum.ewm.stats.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.StatsDateTimePattern;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

public class StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(StatsDateTimePattern.PATTERN);

    private final String serverUrl;
    private final RestTemplate restTemplate;

    public StatsClient(String serverUrl) {
        this(serverUrl, new RestTemplate());
    }

    public StatsClient(String serverUrl, RestTemplate restTemplate) {
        this.serverUrl = serverUrl;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Void> hit(EndpointHitDto endpointHitDto) {
        return restTemplate.postForEntity(serverUrl + "/hit", endpointHitDto, Void.class);
    }

    public ResponseEntity<List<ViewStatsDto>> getStats(LocalDateTime start,
                                                       LocalDateTime end,
                                                       List<String> uris,
                                                       Boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER));

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris.toArray());
        }

        if (unique != null) {
            builder.queryParam("unique", unique);
        }

        return restTemplate.exchange(
                builder.encode().toUriString(),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<ViewStatsDto>>() {
                }
        );
    }
}
