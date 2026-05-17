package com.notif.backend.repository;

import com.notif.backend.entity.User;
import com.notif.backend.entity.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, String> {
    List<UserEvent> findAllByOrderByUser_IdAscEvent_IdAsc();
    List<UserEvent> findAllByOrderByEvent_IdAscUser_IdAsc();
    List<UserEvent> findAllByUser_Id(Long userId);
    List<UserEvent> findAllByEvent_Id(String eventId);
}
