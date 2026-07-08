package ru.practicum.ewm.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.common.Validation;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(NewUserRequest request) {
        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser != null) {
            if (existingUser.getName().equals(request.getName())) {
                throw new ConflictException("User with email=" + request.getEmail() + " already exists");
            }
            return UserMapper.toDto(existingUser);
        }
        return UserMapper.toDto(userRepository.save(UserMapper.toEntity(request)));
    }

    @Override
    public List<UserDto> get(List<Long> ids, int from, int size) {
        Validation.checkPage(from, size);
        List<User> users = ids == null || ids.isEmpty()
                ? userRepository.findAll(new OffsetPageRequest(from, size)).getContent()
                : userRepository.findByIdIn(ids, new OffsetPageRequest(from, size));
        return users.stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
    }
}
