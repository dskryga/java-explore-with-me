package ru.practicum.controller.admin.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@Slf4j
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId) {
        log.info("ADMIN: получен запрос на удаление комментария с id {}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @GetMapping("/{userId}")
    public List<CommentDto> getAllCommentsByUser(@PathVariable Long userId,
                                                 @RequestParam Integer from,
                                                 @RequestParam Integer size) {
        log.info("ADMIN: получен запрос на получение всех комментариев от пользователя c id {}", userId);
        return commentService.getAllCommentByUser(userId, from, size);
    }
}
