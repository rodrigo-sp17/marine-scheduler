package com.github.rodrigo_sp17.mscheduler.friend;

import com.github.rodrigo_sp17.mscheduler.user.UserService;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {

    public static final Logger log = LoggerFactory.getLogger(FriendService.class);

    @Autowired
    private final FriendRequestRepository requestRepository;

    @Autowired
    private final UserService userService;

    public FriendService(FriendRequestRepository requestRepository, UserService userService) {
        this.requestRepository = requestRepository;
        this.userService = userService;
    }

    public List<AppUser> getFriendsByUser(String username) {
        AppUser user = userService.getUserByUsername(username);
        return user.getFriends();
    }

    public List<FriendRequest> getFriendRequestsForUser(String username) {
        AppUser user = userService.getUserByUsername(username);
        return user.getFriendRequests();
    }

    @Transactional
    public FriendRequest requestFriendship(String friendName, String username) {
        // check if there is a request already
        AppUser user = userService.getUserByUsername(username);

        List<FriendRequest> requests = user.getFriendRequests();
        List<Long> requestIds = requests.stream()
                .filter(fr -> fr.getTarget().getUserInfo().getUsername().equals(friendName))
                .map(fr -> fr.getId())
                .collect(Collectors.toList());

        if (!requestIds.isEmpty()) {
            if (requestIds.size() == 1) {
                return requestRepository.findById(requestIds.get(0))
                        .orElseThrow(FriendRequestNotFoundException::new);
            } else {
                log.error("2 requests for same username were detected");
                throw new AssertionError("There should not be more than 1 request " +
                        "between users");
            }
        }

        // if not, build request, save and return
        AppUser friend = userService.getUserByUsername(friendName);
        var friendRequest = new FriendRequest();
        friendRequest.setSource(user);
        friendRequest.setTarget(friend);
        friendRequest.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));

        // persists all
        var result = requestRepository.save(friendRequest);
        user.getFriendRequests().add(friendRequest);
        friend.getFriendRequests().add(friendRequest);
        userService.saveUser(user);
        userService.saveUser(friend);

        log.info("Friend request created");
        return result;
    }

    @Transactional
    public AppUser acceptFriendship(String friendName, String username) {
        FriendRequest request = requestRepository.findBySourceUsernameAndTargetUsername(
                username,
                friendName);
        if (request == null) {
            throw new FriendRequestNotFoundException();
        }

        AppUser user = request.getSource();
        AppUser friend = request.getTarget();

        // Mutually adds friends
        user.getFriends().add(friend);
        friend.getFriends().add(user);

        // Persists users
        userService.saveUser(user);
        AppUser savedFriend = userService.saveUser(friend);

        // Removes request since friendship was established
        requestRepository.delete(request);

        return savedFriend;
    }

    @Transactional
    public void removeFriendByUsername(String friendUsername, String username) {
        AppUser owner = userService.getUserByUsername(username);
        AppUser friend = userService.getUserByUsername(friendUsername);

        owner.getFriends().remove(friend);
        friend.getFriends().remove(owner);

        userService.saveUser(owner);
        userService.saveUser(friend);
        log.info("Friendship removed");
    }
}
