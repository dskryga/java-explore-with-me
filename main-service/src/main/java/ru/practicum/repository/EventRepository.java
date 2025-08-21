package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);
    Optional<Event> findByIdAndState(Long id, EventState state);
    boolean existsByCategoryId(Long categoryId);
}
