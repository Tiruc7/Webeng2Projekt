package com.notif.backend.service;

import com.notif.backend.dto.UserDTO;
import com.notif.backend.entity.User;
import com.notif.backend.repository.UserEventRepository;
import com.notif.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserEventRepository userEventRepository;

    public UserService(UserRepository userRepository, UserEventRepository userEventRepository){
        this.userRepository = userRepository;
        this.userEventRepository = userEventRepository;
    }

    public List<UserDTO> getUserData() {
        return userRepository.findAll().stream().map(User::toDTO).toList();
    }

    @Transactional
    public UserDTO updateUserData(UserDTO user) {
        log.info("Updating user id={}", user.id());
        return userRepository.save(new User(user)).toDTO();
    }

    @Transactional
    public void deleteUserData(Long id) {
        log.info("Deleting user id={}", id);
        userRepository.deleteById(id);
    }

    public UserDTO getUserByID(Long userId) {
        return userRepository.getReferenceById(userId).toDTO();
    }

    @Transactional
    public UserDTO getOrCreateUser(Jwt jwt) {
        String externalId = jwt.getClaimAsString("sub");
        String username = jwt.getClaimAsString("preferred_username");

        User user = userRepository.findByExternalId(externalId)
                .orElseGet(() -> {
                    // JIT Provisioning: User existiert noch nicht in unserer DB
                    User newUser = new User();
                    newUser.setExternalId(externalId);
                    newUser.setUserName(username);
                    User saved = userRepository.save(newUser);
                    log.info("JIT provisioning: created new user externalId={}, username={}", externalId, username);
                    return saved;
                });

        return user.toDTO();
    }
}
