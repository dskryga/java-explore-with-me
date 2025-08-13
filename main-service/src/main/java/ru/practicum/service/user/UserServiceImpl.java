package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        User created = userRepository.save(UserMapper.mapToUser(newUserRequest));
        return UserMapper.mapToDto(created);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (ids.isEmpty() || ids == null) {
            return userRepository.findAll(pageable)
                    .stream()
                    .map(UserMapper::mapToDto)
                    .collect(Collectors.toList());
        }
        return userRepository.findAllByIdPageable(ids, pageable)
                .stream()
                .map(UserMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
        userRepository.deleteById(userId);
    }

}
