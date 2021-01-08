package com.github.rodrigo_sp17.mscheduler.friend;

import com.github.rodrigo_sp17.mscheduler.friend.data.FriendRequest;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<AppUser>> getFriends(Authentication auth) {
        var friends = friendService.getFriendsByUser(auth.getName());
        Link self = linkTo(methodOn(FriendController.class).getFriends(null)).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(friends).add(self));
    }

    @GetMapping("/request")
    public ResponseEntity<CollectionModel<FriendRequest>> getFriendRequests(Authentication auth) {
        var requests = friendService.getFriendRequestsForUser(auth.getName());
        Link self = linkTo(methodOn(FriendController.class).getFriendRequests(null))
                .withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(requests).add(self));
    }

    @GetMapping("/request/{id}")
    public ResponseEntity<FriendRequest> getFriendRequestById(@PathVariable Long id,
                                                              Authentication auth) {
    FriendRequest request = friendService.getRequestById(id, auth.getName());
    request.add(linkTo(methodOn(FriendController.class)
            .getFriendRequestById(id, null)).withSelfRel());
    request.add(linkTo(methodOn(FriendController.class).getFriendRequests(null))
            .withRel("allRequests"));
    return ResponseEntity.ok(request);
    }

    @PostMapping("/request")
    public ResponseEntity<FriendRequest> requestFriendship(@RequestParam String username,
                                                           Authentication auth) {
        var request = friendService.requestFriendship(username, auth.getName());
        Link toCreated = linkTo(methodOn(FriendController.class)
                .getFriendRequestById(request.getId(), null)).withSelfRel();
        request.add(linkTo(methodOn(FriendController.class).getFriendRequests(null))
                .withRel("allRequests"));
        return ResponseEntity.created(toCreated.toUri()).body(request);
    }

    @PostMapping("/accept")
    public ResponseEntity<AppUser> acceptFriendship(@RequestParam String username,
                                                    Authentication auth) {
        var acceptedUser = friendService.acceptFriendship(username, auth.getName());
        acceptedUser.add(linkTo(methodOn(FriendController.class).getFriends(null))
                .withRel("allFriends"));
        acceptedUser.add(linkTo(methodOn(FriendController.class).getFriendRequests(null))
                .withRel("allRequests"));
        return ResponseEntity.ok(acceptedUser);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<AppUser> removeFriendByUsername(@RequestParam String username,
                                                 Authentication auth) {
        friendService.removeFriendByUsername(username, auth.getName());
        Link allFriends = linkTo(methodOn(FriendController.class).getFriends(null))
                .withRel("allFriends");
        return ResponseEntity.noContent().header("Link", allFriends.getHref()).build();
    }
}
