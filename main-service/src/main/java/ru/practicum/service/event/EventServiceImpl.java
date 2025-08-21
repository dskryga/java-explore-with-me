package ru.practicum.service.event;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.StatClient;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.dto.event.*;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.event.Location;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final StatClient statsClient;
    private final RequestRepository requestRepository;

    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, Long userId) {
        Category category = getCategoryOrThrow(newEventDto.getCategory());
        User initiator = getUserOrThrow(userId);
        validateEventDate(newEventDto.getEventDate());
        Event createdEvent = EventMapper.mapToEvent(newEventDto);
        createdEvent.setCategory(category);
        createdEvent.setInitiator(initiator);
        createdEvent = eventRepository.save(createdEvent);
        return EventMapper.mapToFullDto(createdEvent);
    }

    @Override
    public List<EventShortDto> getEventsByCurrentUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::mapToShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = getEventOrThrow(eventId);

        getUserOrThrow(userId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new AccessDeniedException(String.format("Пользователь с id %d не является создателем события с id %d",
                    userId, eventId));
        }
        return EventMapper.mapToFullDto(event);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = getEventOrThrow(eventId);
        getUserOrThrow(userId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new AccessDeniedException(String.format("Пользователь с id %d не является создателем события с id %d",
                    userId, eventId));
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new InvalidRequestException("Событие в статусе Опубликовано изменить нельзя");
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            Category category = getCategoryOrThrow(updateEventUserRequest.getCategory());
            event.setCategory(category);
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            validateEventDate(updateEventUserRequest.getEventDate());
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.getLocation() != null) {
            Location location = updateEventUserRequest.getLocation();
            event.setLocationLat(location.getLat());
            event.setLocationLon(location.getLon());
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction() == UpdateEventUserRequest.StateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return EventMapper.mapToFullDto(updatedEvent);
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException(String.format("Категория с id %d не существует", categoryId)));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User с id %d не существует", userId)));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id %d не найдено", eventId)));
    }

    private void validateEventDate(LocalDateTime eventDate) {
        LocalDateTime validTime = LocalDateTime.now().plusHours(2);
        if (eventDate.isBefore(validTime))
            throw new InvalidRequestException("Событие должно начинаться не раньше, чем два часа от текущего времени");
    }

    @Override
    public List<EventShortDto> getEventsWithFilters(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            int from,
            int size,
            HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Время начала не может быть позже времени конца при фильтрации");
        }

        // Сохраняем информацию о запросе в статистику
        saveHitStats(request);

        // Устанавливаем диапазон дат по умолчанию
        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime end = rangeEnd;

        // Строим спецификацию для фильтрации
        Specification<Event> spec = buildEventSpecification(text, categories, paid, start, end, onlyAvailable);

        //  Получаем события с пагинацией
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> eventsPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventsPage.getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        // Получаем просмотры из статистики
        Map<Long, Long> viewsMap = getViewsForEvents(events);

        // Получаем подтвержденные заявки
        Map<Long, Integer> confirmedRequestsMap = getConfirmedRequestsForEvents(
                events.stream().map(Event::getId).collect(Collectors.toList())
        );

        // Обновляем события с актуальными данными
        events.forEach(event -> {
            event.setViews(viewsMap.getOrDefault(event.getId(), 0L));
            event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0));
        });

        List<EventShortDto> result = events.stream()
                .map(EventMapper::mapToShortDto)
                .collect(Collectors.toList());

        // Сортировка
        if ("EVENT_DATE".equals(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        } else if ("VIEWS".equals(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return result;
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        // Сохраняем информацию о запросе в статистику
        saveHitStats(request);

        // Находим событие (только опубликованное)
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено или не опубликовано"));

        // Получаем количество просмотров
        Long views = getViewsForEvent(id);
        event.setViews(views);

        // Получаем количество подтвержденных запросов
        Integer confirmedRequests = getConfirmedRequestsForEvent(id);
        event.setConfirmedRequests(confirmedRequests);

        // Преобразуем в DTO
        return EventMapper.mapToFullDto(event);
    }

    @Override
    public List<EventFullDto> getEventsForAdmin(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size) {

        // Строим спецификацию
        Specification<Event> spec = buildAdminSpecification(users, states, categories, rangeStart, rangeEnd);

        // Получаем события с пагинацией
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> eventsPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventsPage.getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        // просмотры из статистики
        Map<Long, Long> viewsMap = getViewsForEvents(events);

        // подтвержденные заявки
        Map<Long, Integer> confirmedRequestsMap = getConfirmedRequestsForEvents(
                events.stream().map(Event::getId).collect(Collectors.toList())
        );

        // 5. Обновляем события с актуальными данными
        events.forEach(event -> {
            event.setViews(viewsMap.getOrDefault(event.getId(), 0L));
            event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0));
        });

        return events.stream()
                .map(EventMapper::mapToFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest newEvent, Long eventId) {
        Event event = getEventOrThrow(eventId);

        if (newEvent.getEventDate() != null) {
            validateEventDate(newEvent.getEventDate());
            event.setEventDate(newEvent.getEventDate());
        }

        if (newEvent.getTitle() != null) {
            event.setTitle(newEvent.getTitle());
        }
        if (newEvent.getAnnotation() != null) {
            event.setAnnotation(newEvent.getAnnotation());
        }
        if (newEvent.getDescription() != null) {
            event.setDescription(newEvent.getDescription());
        }
        if (newEvent.getPaid() != null) {
            event.setPaid(newEvent.getPaid());
        }
        if (newEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(newEvent.getParticipantLimit());
        }
        if (newEvent.getLocation() != null) {
            event.setLocationLon(newEvent.getLocation().getLon());
            event.setLocationLat(newEvent.getLocation().getLat());
        }
        if (newEvent.getRequestModeration() != null) {
            event.setRequestModeration(newEvent.getRequestModeration());
        }

        if (newEvent.getStateAction() != null) {
            switch (newEvent.getStateAction()) {
                case REJECT_EVENT -> rejectEvent(event);
                case PUBLISH_EVENT -> publishEvent(event);
            }
        }


        Event updated = eventRepository.save(event);
        return EventMapper.mapToFullDto(updated);
    }

    private void rejectEvent(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new InvalidRequestException("Опубликованное событие не может быть отклонено");
        }
        event.setState(EventState.CANCELED);
    }

    private void publishEvent(Event event) {
        if (event.getState() != EventState.PENDING) {
            throw new InvalidRequestException("Можно опубликовать только события в статусе PENDING");
        }
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private Specification<Event> buildAdminSpecification(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтр по пользователям
            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }

            // Фильтр по состояниям
            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }

            // Фильтр по категориям
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            // Фильтр по датам
            if (rangeStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Event> buildEventSpecification(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Только опубликованные события
            predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

            // Текстовый поиск (без учета регистра)
            if (text != null && !text.isBlank()) {
                String searchText = "%" + text.toLowerCase() + "%";
                Predicate annotationPred = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("annotation")), searchText);
                Predicate descriptionPred = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchText);
                predicates.add(criteriaBuilder.or(annotationPred, descriptionPred));
            }

            // Фильтр по категориям
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            // Фильтр по paid
            if (paid != null) {
                predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
            }

            // Фильтр по датам
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            // Фильтр по доступности
            if (Boolean.TRUE.equals(onlyAvailable)) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("participantLimit"), 0),
                        criteriaBuilder.greaterThan(
                                criteriaBuilder.diff(root.get("participantLimit"), root.get("confirmedRequests")), 0)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void saveHitStats(HttpServletRequest request) {
        try {
            HitRequestDto hitRequest = HitRequestDto.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.addHit(hitRequest);
        } catch (Exception e) {
            log.warn("Не удалось сохранить статистику запроса: {}", e.getMessage());
        }
    }

    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        try {
            LocalDateTime start = events.stream()
                    .map(Event::getCreatedOn)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now().minusYears(1));

            Collection<StatResponseDto> stats = statsClient.getStat(
                    start, LocalDateTime.now(), uris, true);

            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> Long.parseLong(stat.getUri().substring("/events/".length())),
                            StatResponseDto::getHits
                    ));
        } catch (Exception e) {
            log.warn("Не удалось получить статистику просмотров: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, Integer> getConfirmedRequestsForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> results = requestRepository.countConfirmedRequestsByEventIds(eventIds);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Number) result[1]).intValue()
                ));
    }

    private Integer getConfirmedRequestsForEvent(Long eventId) {
        try {
            return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        } catch (Exception e) {
            log.warn("Не удалось получить количество подтвержденных запросов для события {}: {}", eventId, e.getMessage());
            return 0;
        }
    }

    private Long getViewsForEvent(Long eventId) {
        try {
            String uri = "/events/" + eventId;
            LocalDateTime start = LocalDateTime.now().minusYears(1); // Берем статистику за последний год
            LocalDateTime end = LocalDateTime.now().plusYears(1);
            Collection<StatResponseDto> stats = statsClient.getStat(
                    start, end, List.of(uri), true);

            return stats.stream()
                    .findFirst()
                    .map(StatResponseDto::getHits)
                    .orElse(0L);
        } catch (Exception e) {
            log.warn("Не удалось получить статистику просмотров для события {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }
}
