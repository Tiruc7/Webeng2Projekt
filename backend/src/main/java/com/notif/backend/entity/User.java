package com.notif.backend.entity;


import com.notif.backend.dto.UserDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private String external_id;

    @Column(name = "username")
    private String userName;

    public User(){};

    public User(UserDTO user) {
        this.id = user.id();
        this.external_id = user.external_id();
        this.userName = user.username();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
    }

    public UserDTO toDTO() {
        return new UserDTO(this.id,this.userName,this.external_id);
    }
}
