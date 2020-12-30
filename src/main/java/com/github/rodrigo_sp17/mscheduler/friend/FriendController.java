package com.github.rodrigo_sp17.mscheduler.friend;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /**
     * Gets list of all friends of logged user
     * @return ResponseEntity containing all users registered as friends
     */
    @GetMapping
    public ResponseEntity<List<AppUser>> getFriends(Authentication auth) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @GetMapping("/search")
    public ResponseEntity<AppUser> searchFriendByUserName(@RequestParam String userName,
                                                          Authentication auth) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @PostMapping("/add")
    public ResponseEntity addFriendByUserName(@RequestBody String userName,
                                              Authentication auth) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @DeleteMapping("/remove")
    public ResponseEntity removeFriendByUserName(@RequestBody String userName,
                                                 Authentication auth) {
        // TODO
        throw new UnsupportedOperationException();
    }

}
