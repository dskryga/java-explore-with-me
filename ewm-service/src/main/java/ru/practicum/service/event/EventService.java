package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;
import ru.practicum.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(NewEventDto newEventDto, Long userId);

    List<EventShortDto> getEventsByCurrentUser(Long userId, Integer from, Integer size);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventShortDto> getEventsWithFilters(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            int from,
            int size,
            HttpServletRequest request);

    EventFullDto getEvent(Long id, HttpServletRequest request);

    List<EventFullDto> getEventsForAdmin(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size);

    EventFullDto updateEventByAdmin(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);
}
