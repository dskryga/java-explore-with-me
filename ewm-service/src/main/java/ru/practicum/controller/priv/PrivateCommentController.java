package ru.practicum.controller.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@Slf4j
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    CommentDto addComment(@RequestBody @Valid NewCommentDto newCommentDto,
                          @PathVariable Long userId,
                          @PathVariable Long eventId) {
        log.info("Получен запрос на добавление комментария от пользователся с id {} к событию с id {}", userId, eventId);
        return commentService.addComment(newCommentDto, userId, eventId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(@PathVariable Long userId,
                       @PathVariable Long commentId) {
        log.info("Получен запрос от пользователя с id {} на удаление комментария с id {}", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/{eventId}")
    List<CommentDto> getComments(@RequestParam(defaultValue = "0") Integer from,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 @PathVariable Long eventId) {
        log.info("Получен запрос на получение комментариев к событию c id {}", eventId);
        return commentService.getComments(from, size, eventId);
    }
}
