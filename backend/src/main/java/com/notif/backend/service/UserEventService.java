package com.notif.backend.service;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.UserDTO;
import com.notif.backend.dto.UserEventDTO;
import com.notif.backend.entity.Event;
import com.notif.backend.entity.User;
import com.notif.backend.entity.UserEvent;
import com.notif.backend.repository.EventRepository;
import com.notif.backend.repository.UserEventRepository;
import com.notif.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserEventService {

    private final UserEventRepository userEventRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public UserEventService(UserEventRepository userEventRepository,
                            UserRepository userRepository,
                            EventRepository eventRepository) {
        this.userEventRepository = userEventRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }


    public List<EventDTO> getEventsForUser(Long userId) {
        return userEventRepository.findAllByUser_Id(userId)
                .stream()
                .map(u -> u.getEvent().toDTO())
                .toList();
    }

    public List<UserDTO> getUserForEvents(String eventId) {
        return userEventRepository.findAllByEvent_Id(eventId)
                .stream()
                .map(u -> u.getUser().toDTO())
                .toList();
    }


    @Transactional
    public UserEventDTO register(Long userId, String eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        UserEvent connection = new UserEvent();
        connection.setId(UUID.randomUUID().toString());
        connection.setUser(user);
        connection.setEvent(event);

        return userEventRepository.save(connection).toDTO();
    }

    public List<UserEventDTO> getUserEventsByUser() {
        return userEventRepository.findAllByOrderByUser_IdAscEvent_IdAsc().stream().map(UserEvent::toDTO).toList();
    }

    public List<UserEventDTO> getUserEventsByEvent() {
        return userEventRepository.findAllByOrderByEvent_IdAscUser_IdAsc().stream().map(UserEvent::toDTO).toList();
    }
}