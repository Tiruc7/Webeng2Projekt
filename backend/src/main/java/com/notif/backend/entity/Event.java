package com.notif.backend.entity;

import com.notif.backend.dto.EventDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "events")
public class Event {

    @Id
    private String id;

    private String name;
    private String venue;
    private String city;
    private String date;
    private String time;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "ticket_url", length = 1000)
    private String ticketUrl;

    private String status;

    public Event() {
    }

    public Event(String id, String name, String venue, String city, String date, String time,
                 String imageUrl, String ticketUrl, String status) {
        this.id = id;
        this.name = name;
        this.venue = venue;
        this.city = city;
        this.date = date;
        this.time = time;
        this.imageUrl = imageUrl;
        this.ticketUrl = ticketUrl;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTicketUrl() {
        return ticketUrl;
    }

    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EventDTO toDTO(){
        return new EventDTO(id, name, venue, city, date, time,
                imageUrl, ticketUrl, status);
    }

}