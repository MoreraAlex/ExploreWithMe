package ru.practicum.ewm.user.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByIdIn(Collection<Long> ids, Pageable pageable);
}
