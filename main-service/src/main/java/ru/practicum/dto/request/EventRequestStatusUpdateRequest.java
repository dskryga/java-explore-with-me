package ru.practicum.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    List<Long> requestsId;
    RequestUpdateStatus status;
}
