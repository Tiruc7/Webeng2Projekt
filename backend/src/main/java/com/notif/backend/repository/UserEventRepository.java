package com.notif.backend.repository;

import com.notif.backend.entity.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, String> {
    List<UserEvent> findAllByOrderByUser_IdAscEvent_IdAsc();
    List<UserEvent> findAllByOrderByEvent_IdAscUser_IdAsc();
    List<UserEvent> findAllByUser_Id(Long userId);
    List<UserEvent> findAllByEvent_Id(String eventId);
    boolean existsByUser_IdAndEvent_Id(Long userId, String eventId);

    boolean existsByEvent_Id(String eventId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserEvent ue WHERE ue.user.id = :userId AND ue.event.id = :eventId")
    void deleteByUserAndEvent(@Param("userId") Long userId, @Param("eventId") String eventId);
}
