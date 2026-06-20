package com.notif.backend.entity;


import com.notif.backend.dto.UserFriendshipDTO;
import com.notif.backend.enums.FriendshipStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_friendships")
public class UserFriendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "addressee_id")
    private User addressee;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    private LocalDateTime createdAt;

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public User getRequester() {
        return requester;
    }

    public User getAddressee() {
        return addressee;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserFriendshipDTO toDTO(){
        return new UserFriendshipDTO(
                this.getId(),
                this.getRequester().toDTO(),
                this.getAddressee().toDTO(),
                this.getStatus(),
                this.getCreatedAt()
        );
    }

    public void setId(Long id) {
        this.id = id;
    }
}

