package com.notif.backend.controller;

import com.notif.backend.dto.UserEventRowDTO;
import com.notif.backend.entity.AppUser;
import com.notif.backend.entity.Event;
import com.notif.backend.repository.AppUserRepository;
import com.notif.backend.repository.EventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminDataController {

    private final AppUserRepository appUserRepository;
    private final EventRepository eventRepository;
    private final JdbcTemplate jdbcTemplate;

    public AdminDataController(
            AppUserRepository appUserRepository,
            EventRepository eventRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.appUserRepository = appUserRepository;
        this.eventRepository = eventRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/users")
    public List<AppUser> getUsers() {
        return appUserRepository.findAll();
    }

    @GetMapping("/events")
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/user-events")
    public List<UserEventRowDTO> getUserEvents() {
        return jdbcTemplate.query(
                "SELECT user_id, event_id FROM user_events ORDER BY user_id, event_id",
                (rs, rowNum) -> new UserEventRowDTO(
                        rs.getString("user_id"),
                        rs.getString("event_id")
                )
        );
    }
}