package com.notif.backend.entity;

import com.notif.backend.dto.UserEventDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "user_event")
public class UserEvent {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Optional: Hier könnten weitere Felder stehen
    // private LocalDateTime joinedAt;

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public UserEventDTO toDTO(){
        return new UserEventDTO(id,user.toDTO(),event.toDTO());
    }
}
