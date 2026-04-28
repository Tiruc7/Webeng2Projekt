package com.notif.backend.service;

import com.notif.backend.dto.UserDTO;
import com.notif.backend.entity.User;
import com.notif.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
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
