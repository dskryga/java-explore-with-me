package ru.practicum.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentDto addComment(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new InvalidRequestException(String.format("Событие с id %d не опубликовано", eventId));
        }
        Comment comment = CommentMapper.mapToComment(newCommentDto);
        comment.setAuthor(user);
        comment.setEventId(eventId);
        return CommentMapper.mapToDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        getUserOrThrow(userId);
        Comment comment = getCommentOrThrow(commentId);
        Event event = getEventOrThrow(comment.getEventId());
        if (event.getInitiator().getId().equals(userId) || comment.getAuthor().getId().equals(userId)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new InvalidRequestException("Только авторы комментариев и авторы поста могут удалить комментарий");
        }
    }

    @Override
    public List<CommentDto> getComments(Integer from, Integer size, Long eventId) {
        Pageable pageable = PageRequest.of(from / size, size);
        getEventOrThrow(eventId);
        return commentRepository.findByEventIdOrderByPublishedOnAsc(eventId, pageable).stream()
                .map(CommentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        getCommentOrThrow(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getAllCommentByUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return commentRepository.findByAuthorId(userId, pageable).stream()
                .map(CommentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(()
                -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException(String.format("Событие с id %d не найдено", eventId)));
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException(String.format("Комментарий с id %d не найден", commentId)));
    }
}
