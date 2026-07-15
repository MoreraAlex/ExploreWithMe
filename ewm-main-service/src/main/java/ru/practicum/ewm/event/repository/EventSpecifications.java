package ru.practicum.ewm.event.repository;

import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventSpecifications {

    public static Specification<Event> fetchRelations() {
        return (root, query, builder) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("initiator", JoinType.LEFT);
            }
            return builder.conjunction();
        };
    }

    public static Specification<Event> initiatorIn(List<Long> users) {
        return (root, query, builder) -> users == null || users.isEmpty()
                ? builder.conjunction()
                : root.get("initiator").get("id").in(users);
    }

    public static Specification<Event> statesIn(List<EventState> states) {
        return (root, query, builder) -> states == null || states.isEmpty()
                ? builder.conjunction()
                : root.get("state").in(states);
    }

    public static Specification<Event> categoriesIn(List<Long> categories) {
        return (root, query, builder) -> categories == null || categories.isEmpty()
                ? builder.conjunction()
                : root.get("category").get("id").in(categories);
    }

    public static Specification<Event> paid(Boolean paid) {
        return (root, query, builder) -> paid == null ? builder.conjunction() : builder.equal(root.get("paid"), paid);
    }

    public static Specification<Event> state(EventState state) {
        return (root, query, builder) -> builder.equal(root.get("state"), state);
    }

    public static Specification<Event> text(String text) {
        return (root, query, builder) -> {
            if (text == null || text.isBlank()) {
                return builder.conjunction();
            }
            String pattern = "%" + text.toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("annotation")), pattern),
                    builder.like(builder.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> eventDateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, builder) -> {
            if (start != null && end != null) {
                return builder.between(root.get("eventDate"), start, end);
            }
            if (start != null) {
                return builder.greaterThanOrEqualTo(root.get("eventDate"), start);
            }
            if (end != null) {
                return builder.lessThanOrEqualTo(root.get("eventDate"), end);
            }
            return builder.conjunction();
        };
    }
}
