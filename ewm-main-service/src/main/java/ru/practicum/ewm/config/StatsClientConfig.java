package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StatsClient statsClient(@Value("${stats-server.url}") String statsServerUrl, RestTemplate restTemplate) {
        return new StatsClient(statsServerUrl, restTemplate);
    }
}
