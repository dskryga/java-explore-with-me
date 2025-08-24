package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public Comment mapToComment(NewCommentDto newCommentDto) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .publishedOn(LocalDateTime.now())
                .build();
    }

    public CommentDto mapToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .eventId(comment.getEventId())
                .author(UserMapper.mapToShortDto(comment.getAuthor()))
                .publishedOn(comment.getPublishedOn())
                .text(comment.getText())
                .build();
    }
}
