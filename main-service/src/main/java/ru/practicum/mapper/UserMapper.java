package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.model.User;

@UtilityClass
public class UserMapper {
    public User mapToUser(NewUserRequest newUserRequest){
        return User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }

    public UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
