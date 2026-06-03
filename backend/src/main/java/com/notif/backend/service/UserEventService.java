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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserEventService {

    private static final Logger log = LoggerFactory.getLogger(UserEventService.class);

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
    public void removeEventFromUserProfile(Long userId, String eventId) {
        log.info("Removing event {} from profile of user {}", eventId, userId);
        userEventRepository.deleteByUserAndEvent(userId, eventId);
        // Remove the event itself if no user has it saved anymore
        if (!userEventRepository.existsByEvent_Id(eventId)) {
            log.info("Event {} has no remaining owners, deleting from DB", eventId);
            eventRepository.deleteById(eventId);
        }
    }

    @Transactional
    public void addEventToUserProfile(Long userId, com.notif.backend.dto.EventDTO eventDTO) {
        log.info("Adding event {} to profile of user {}", eventDTO.id(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Save event in DB if not yet existing (Upsert)
        Event event = eventRepository.findById(eventDTO.id()).orElse(new Event());
        event.setId(eventDTO.id());
        event.setName(eventDTO.title());
        event.setVenue(eventDTO.venue());
        event.setCity(eventDTO.city());
        event.setDate(eventDTO.date());
        event.setTime(eventDTO.time());
        event.setImageUrl(eventDTO.imageUrl());
        event.setTicketUrl(eventDTO.ticketUrl());
        event.setStatus(eventDTO.status());
        event = eventRepository.save(event);

        // No duplicate entries for same user-event combination
        if (userEventRepository.existsByUser_IdAndEvent_Id(userId, event.getId())) {
            log.debug("Event {} already saved for user {}, skipping", event.getId(), userId);
            return;
        }

        UserEvent connection = new UserEvent();
        connection.setId(UUID.randomUUID().toString());
        connection.setUser(user);
        connection.setEvent(event);
        userEventRepository.save(connection);
    }

    public List<UserEventDTO> getUserEventsByUser() {
        return userEventRepository.findAllByOrderByUser_IdAscEvent_IdAsc().stream().map(UserEvent::toDTO).toList();
    }

    public List<UserEventDTO> getUserEventsByEvent() {
        return userEventRepository.findAllByOrderByEvent_IdAscUser_IdAsc().stream().map(UserEvent::toDTO).toList();
    }
}