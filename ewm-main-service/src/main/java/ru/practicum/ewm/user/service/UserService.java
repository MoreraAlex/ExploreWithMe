package ru.practicum.ewm.user.service;

import java.util.List;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

public interface UserService {

    UserDto create(NewUserRequest request);

    List<UserDto> get(List<Long> ids, int from, int size);

    void delete(Long userId);
}
