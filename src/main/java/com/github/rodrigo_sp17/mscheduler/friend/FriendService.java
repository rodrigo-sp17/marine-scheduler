package com.github.rodrigo_sp17.mscheduler.friend;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendService {

    @Autowired
    private final UserRepository userRepository;

    public FriendService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser getFriendByUsername(String username, Long ownerId) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public AppUser getFriendByName(String name, Long ownerId) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Transactional
    public boolean addFriendByUsername(String username, Long ownerId) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Transactional
    public boolean addFriendById(Long friendId, Long ownerId) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Transactional
    public boolean removeFriendById(Long friendId, Long ownerId) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
