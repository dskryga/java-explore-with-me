package ru.practicum.controller.admin.event;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.model.event.EventState;
import ru.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    EventService eventService;

    @GetMapping
    List<EventFullDto> getEvents(@RequestParam(name = "users", required = false) List<Long> users,
                                 @RequestParam(name = "states", required = false) List<EventState> states,
                                 @RequestParam(name = "categories", required = false) List<Long> categories,
                                 @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                 @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                 @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
                                 @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        return eventService.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/eventId")
    EventFullDto updateEvent(@RequestBody UpdateEventAdminRequest updateEventAdminRequest,
                             @PathVariable Long eventId) {
        return eventService.updateEventByAdmin(updateEventAdminRequest, eventId);
    }
}
