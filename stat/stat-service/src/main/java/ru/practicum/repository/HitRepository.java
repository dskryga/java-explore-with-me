package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Hit;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {
    //Подсчет просмотров только с уникальных ip, если uri!=null
    @Query("SELECT new ru.practicum.model.Stat(s.app, s.uri, " +
            "COUNT(DISTINCT s.ip)) " +
            "FROM Hit s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "AND s.uri IN :uris " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<Stat> findUniqueStatsByParameters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.model.Stat(s.app, s.uri, COUNT(s)) " +
            "FROM Hit s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "AND s.uri IN :uris " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s) DESC")
    List<Stat> findStatsByParameters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    //Подсчет просмотров только с уникальных ip для всех страниц
    @Query("SELECT new ru.practicum.model.Stat(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Stat> findUniqueStatsByDate(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.model.Stat(h.app, h.uri, COUNT(h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<Stat> findAllStatsByDate(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
