package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.request.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Boolean existsByRequesterAndEvent(Long userId, Long eventId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.eventId = :eventId")
    int countByEventId(Long eventId);

    @Query("SELECT r FROM Request r WHERE r.eventId = :eventId")
    List<Request> findAllByEventId(Long eventId);

    @Query("SELECT r.event.id, COUNT(r) FROM Request r " +
            "WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event.id")
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Integer countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);
}
