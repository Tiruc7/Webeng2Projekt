package com.notif.backend.service;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.entity.Event;
import com.notif.backend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

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

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> saveOrUpdateEvent(List<EventDTO> eventDTOS) {
        return eventDTOS.stream()
                .map(this::saveOrUpdateEvent)
                .toList();
    }
}