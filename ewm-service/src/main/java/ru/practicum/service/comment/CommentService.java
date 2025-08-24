package ru.practicum.service.comment;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(NewCommentDto newCommentDto, Long userId, Long eventId);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getComments(Integer from, Integer size, Long eventId);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getAllCommentByUser(Long userId, Integer from, Integer size);
}
