package com.notif.backend.service;

import com.notif.backend.dto.UserFriendshipDTO;
import com.notif.backend.entity.User;
import com.notif.backend.entity.UserFriendship;
import com.notif.backend.enums.FriendshipStatus;
import com.notif.backend.repository.UserFriendshipRepository;
import com.notif.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipService {

    private final UserFriendshipRepository userFriendshipRepository;

    private final UserRepository userRepository;

    public FriendshipService(UserFriendshipRepository userFriendshipRepository, UserRepository userRepository) {
        this.userFriendshipRepository = userFriendshipRepository;
        this.userRepository = userRepository;
    }

    public void sendRequest(Long requesterId, Long addresseeId) throws Exception {
        if (requesterId.equals(addresseeId))
            throw new IllegalArgumentException("Can't friend yourself");

        boolean exists = userFriendshipRepository.existsByUsers(requesterId, addresseeId);
        if (exists) throw new Exception("Friendship already exists");

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new EntityNotFoundException("Addressee not found"));

        UserFriendship friendship = new UserFriendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        userFriendshipRepository.save(friendship);
    }

    public void respondToRequest(Long friendshipId, boolean accept) throws Exception {
        UserFriendship friendship = userFriendshipRepository.findById(friendshipId)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        friendship.setStatus(accept ? FriendshipStatus.ACCEPTED : null);
        if (!accept) userFriendshipRepository.delete(friendship);
        else userFriendshipRepository.save(friendship);
    }

    public List<UserFriendshipDTO> getFriends(Long userId) {
        return userFriendshipRepository.findAcceptedFriendships(userId)
                .stream()
                .map(UserFriendshipDTO::from)
                .toList();
    }

    public List<UserFriendshipDTO> getPendingRequests(Long userId) {
        return userFriendshipRepository.findPendingRequestsForAddressee(userId)
                .stream()
                .map(UserFriendshipDTO::from)
                .toList();
    }

    public void deleteFriendship(Long friendshipId) {
        UserFriendship friendship = userFriendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));
        userFriendshipRepository.delete(friendship);
    }
}
