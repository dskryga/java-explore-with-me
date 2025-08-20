package ru.practicum.controller.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.event.EventService;
import ru.practicum.service.request.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    public EventFullDto createEvent(@RequestBody @Valid NewEventDto newEventDto, @PathVariable Long userId) {
        return eventService.createEvent(newEventDto, userId);
    }

    @GetMapping
    public List<EventShortDto> getEventsByCurrentUser(@PathVariable Long userId,
                                                      @RequestParam(name = "from", defaultValue = "0", required = false) Integer from,
                                                      @RequestParam(name = "size", defaultValue = "10", required = false) Integer size) {
        return eventService.getEventsByCurrentUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getRequestsByEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return requestService.changeRequestStatus(userId, eventId, updateRequest);
    }
}
