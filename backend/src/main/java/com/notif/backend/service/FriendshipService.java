package com.notif.backend.service;

import com.notif.backend.entity.UserFriendship;
import com.notif.backend.enums.FriendshipStatus;
import com.notif.backend.repository.UserFrienshipRepository;
import com.notif.backend.repository.UserRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

@Service
public class FriendshipService {

    private final UserFrienshipRepository userFrienshipRepository;

    private final UserRepository userRepository;

    public FriendshipService(UserFrienshipRepository userFrienshipRepository, UserRepository userRepository) {
        this.userFrienshipRepository = userFrienshipRepository;
        this.userRepository = userRepository;
    }

    public void sendRequest(Long requesterId, Long addresseeId) throws Exception {
        if (requesterId.equals(addresseeId))
            throw new IllegalArgumentException("Can't friend yourself");

        // Check no relationship already exists in either direction
        boolean exists = userFrienshipRepository.existsByUsers(requesterId, addresseeId);
        if (exists) throw new Exception("Friendship already exists");

        UserFriendship friendship = new UserFriendship();
        friendship.setRequester(userRepository.getReferenceById(requesterId));
        friendship.setAddressee(userRepository.getReferenceById(addresseeId));
        friendship.setStatus(FriendshipStatus.PENDING);
        userFrienshipRepository.save(friendship);
    }

    public void respondToRequest(Long friendshipId, Long addresseeId, boolean accept) throws Exception {
        UserFriendship friendship = userFrienshipRepository.findById(friendshipId)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        // Only the addressee can respond
        if (!friendship.getAddressee().getId().equals(addresseeId))
            throw new Exception("Not your request to respond to");

        friendship.setStatus(accept ? FriendshipStatus.ACCEPTED : null);
        if (!accept) userFrienshipRepository.delete(friendship);  // decline = just delete the row
        else userFrienshipRepository.save(friendship);
    }
}
