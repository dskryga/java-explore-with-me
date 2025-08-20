package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.Location;

@UtilityClass
public class EventMapper {
    public Event mapToEvent (NewEventDto newEventDto) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .locationLat(newEventDto.getLocation().getLat())
                .locationLon(newEventDto.getLocation().getLon())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .build();
    }

    public EventFullDto mapToFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(UserMapper.mapToShortDto(event.getInitiator()))
                .location(new Location(event.getLocationLat(), event.getLocationLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public EventShortDto mapToShortDto(Event event){
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .category(CategoryMapper.mapToDto(event.getCategory()))
                .initiator(UserMapper.mapToShortDto(event.getInitiator()))
                .build();
    }
}
