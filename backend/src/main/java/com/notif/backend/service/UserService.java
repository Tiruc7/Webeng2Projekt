package com.notif.backend.service;

import com.notif.backend.dto.UserDTO;
import com.notif.backend.dto.UserEventDTO;
import com.notif.backend.entity.User;
import com.notif.backend.entity.UserEvent;
import com.notif.backend.repository.UserEventRepository;
import com.notif.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserEventRepository userEventRepository;

    public UserService(UserRepository userRepository, UserEventRepository userEventRepository){
        this.userRepository = userRepository;
        this.userEventRepository = userEventRepository;
    }

    public List<UserDTO> getUserData() {
        return userRepository.findAll().stream().map(User::toDTO).toList();
    }

    public UserDTO updateUserData(UserDTO user) {
        return userRepository.save(new User(user)).toDTO();
    }

    public void deleteUserData(Long id) {
        userRepository.deleteById(id);
    }

}
