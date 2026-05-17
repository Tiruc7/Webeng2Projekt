package com.notif.backend.service;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.UserEventDTO;
import com.notif.backend.entity.Event;
import com.notif.backend.entity.UserEvent;
import com.notif.backend.repository.EventRepository;
import com.notif.backend.repository.UserEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    private final UserEventRepository userEventRepository;

    public EventService(EventRepository eventRepository, UserEventRepository userEventRepository) {
        this.eventRepository = eventRepository;
        this.userEventRepository = userEventRepository;
    }

    @Transactional
    public Event saveOrUpdateEvent(EventDTO dto) {
        Event event = eventRepository.findById(dto.id())
                .orElse(new Event());

        event.setId(dto.id());
        event.setName(dto.title());
        event.setVenue(dto.venue());
        event.setCity(dto.city());
        event.setDate(dto.date());
        event.setTime(dto.time());
        event.setImageUrl(dto.imageUrl());
        event.setTicketUrl(dto.ticketUrl());
        event.setStatus(dto.status());

        return eventRepository.save(event);
    }

    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll().stream().map(Event::toDTO).toList();
    }

    @Transactional
    public List<Event> saveOrUpdateEvent(List<EventDTO> eventDTOS) {
        return eventDTOS.stream()
                .map(this::saveOrUpdateEvent)
                .toList();
    }
}