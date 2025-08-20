package ru.practicum.model.request;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    @Column
    private RequestStatus status;
}
