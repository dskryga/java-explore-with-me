package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.request.*;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new InvalidRequestException("Запрос на участие уже был создан раннее");
        }
        Event event = getEventOrThrow(eventId);
        User requester = getUserOrThrow(userId);
        RequestStatus status = RequestStatus.PENDING;
        if (event.getInitiator().getId().equals(userId)) {
            throw new InvalidRequestException("Инициатор события не может отправлять запрос на участие");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new InvalidRequestException("Нельзя подать запрос на участие в неопубликованном событии");
        }

        int participants = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (participants >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new InvalidRequestException("В событии уже максимальное количество участников");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }

        Request request = Request.builder()
                .requester(requester)
                .event(event)
                .created(LocalDateTime.now())
                .status(status)
                .build();

        return RequestMapper.mapToDto(requestRepository.save(request));

    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        getUserOrThrow(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        getUserOrThrow(userId);

        if (requestRepository.existsById(requestId)) {
            Request request = getRequestOrThrow(requestId);
            if (!request.getRequester().getId().equals(userId)) {
                throw new InvalidRequestException(
                        String.format("Пользователь с id %d не является создателем запроса с id %d",
                                userId, requestId));
            }
            request.setStatus(RequestStatus.CANCELED);
            return RequestMapper.mapToDto(requestRepository.save(request));
        } else {
            throw new NotFoundException(String.format("Запрос с id %d не существует", requestId));
        }
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new InvalidRequestException("Только инициатор события может просматривать запросы на участие");
        }
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = getEventOrThrow(eventId);
        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return updateResult;
        }
        if (!isRequestsInPendingStatus(updateRequest.getRequestIds())) {
            throw new InvalidRequestException("Все запросы должны находиться в статусе PENDING");
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new InvalidRequestException("У события достигнут лимит участников");
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        List<Request> requestsToUpdate = requestRepository.findAllById(updateRequest.getRequestIds());

        int limit = event.getParticipantLimit();
        int requestsCount = event.getConfirmedRequests();

        for (Request request : requestsToUpdate) {
            if (requestsCount < limit) {
                if (updateRequest.getStatus() == RequestUpdateStatus.CONFIRMED) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(RequestMapper.mapToDto(request));
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejected.add(RequestMapper.mapToDto(request));
                }
                requestsCount++;
            } else {
                request.setStatus(RequestStatus.REJECTED);
            }
        }
        event.setConfirmedRequests(requestsCount);
        eventRepository.save(event);
        updateResult.setConfirmedRequests(confirmed);
        updateResult.setRejectedRequests(rejected);
        return updateResult;
    }

    private boolean isRequestsInPendingStatus(List<Long> requestsIds) {
        boolean isNotPending = requestRepository.findAllById(requestsIds).stream()
                .anyMatch(request -> request.getStatus() != RequestStatus.PENDING);
        return !isNotPending;
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id %d не найден", id)));
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id %d не найдено", id)));
    }

    private Request getRequestOrThrow(Long id) {
        return requestRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Запрос с id %d не найден", id))
        );
    }
}
