package ru.practicum.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CommentDto {
    private Long id;
    private UserShortDto author;
    private Long eventId;
    private LocalDateTime publishedOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String text;
}
