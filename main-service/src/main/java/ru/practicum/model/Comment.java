package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.model.event.Event;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    @Column(name = "event_id")
    private Long eventId;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column
    private String text;
}
