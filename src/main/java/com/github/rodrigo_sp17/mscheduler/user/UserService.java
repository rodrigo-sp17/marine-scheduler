package com.github.rodrigo_sp17.mscheduler.user;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.UserRepository;
import com.github.rodrigo_sp17.mscheduler.user.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<String> getUsernames() {
        return userRepository.findUsernames();
    }


    public AppUser getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    public boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username) == null;
    }

    public AppUser getUserByUsername(String username) {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("Could not find user " + username);
        }
        return user;
    }

    @Transactional
    public AppUser saveUser(AppUser user) {
        return userRepository.save(user);
    }
}
