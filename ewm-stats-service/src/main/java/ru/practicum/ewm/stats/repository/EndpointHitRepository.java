package ru.practicum.ewm.stats.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.entity.EndpointHit;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
            select new ru.practicum.ewm.stats.dto.ViewStatsDto(hit.app, hit.uri, count(hit.id))
            from EndpointHit hit
            where hit.timestamp between :start and :end
            group by hit.app, hit.uri
            order by count(hit.id) desc
            """)
    List<ViewStatsDto> findStats(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query("""
            select new ru.practicum.ewm.stats.dto.ViewStatsDto(hit.app, hit.uri, count(hit.id))
            from EndpointHit hit
            where hit.timestamp between :start and :end
            and hit.uri in :uris
            group by hit.app, hit.uri
            order by count(hit.id) desc
            """)
    List<ViewStatsDto> findStatsByUris(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("uris") List<String> uris);

    @Query("""
            select new ru.practicum.ewm.stats.dto.ViewStatsDto(hit.app, hit.uri, count(distinct hit.ip))
            from EndpointHit hit
            where hit.timestamp between :start and :end
            group by hit.app, hit.uri
            order by count(distinct hit.ip) desc
            """)
    List<ViewStatsDto> findUniqueStats(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("""
            select new ru.practicum.ewm.stats.dto.ViewStatsDto(hit.app, hit.uri, count(distinct hit.ip))
            from EndpointHit hit
            where hit.timestamp between :start and :end
            and hit.uri in :uris
            group by hit.app, hit.uri
            order by count(distinct hit.ip) desc
            """)
    List<ViewStatsDto> findUniqueStatsByUris(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("uris") List<String> uris);
}
