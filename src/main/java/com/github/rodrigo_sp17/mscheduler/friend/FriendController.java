package com.github.rodrigo_sp17.mscheduler.friend;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public ResponseEntity<List<AppUser>> getFriends(Authentication auth) {
        var user = friendService.getFriendsByUser(auth.getName());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/request")
    public ResponseEntity<List<FriendRequest>> getFriendRequests(Authentication auth) {
        var request = friendService.getFriendRequestsForUser(auth.getName());
        return ResponseEntity.ok(request);
    }

    @PostMapping("/request")
    public ResponseEntity<FriendRequest> requestFriendship(@RequestParam String username,
                                                           Authentication auth) {
        var request = friendService.requestFriendship(username, auth.getName());
        return ResponseEntity.ok(request);
    }

    @PostMapping("/accept")
    public ResponseEntity<AppUser> acceptFriendship(@RequestParam String username,
                                                    Authentication auth) {
        var user = friendService.acceptFriendship(username, auth.getName());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<AppUser> removeFriendByUsername(@RequestParam String username,
                                                 Authentication auth) {
        friendService.removeFriendByUsername(username, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
