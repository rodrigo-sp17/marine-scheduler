package com.github.rodrigo_sp17.mscheduler.user;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    public final static Logger log = LoggerFactory.getLogger(UserController.class.getSimpleName());

    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<CreateUserRequest> signup(@RequestBody CreateUserRequest req) {
        // TODO - password and username validation
        AppUser user = getUserFromRequest(req);
        var addedUser = userService.createUser(user);
        log.info("Created new user");
        return ResponseEntity.ok(getRequestFromUser(addedUser));
    }

    @GetMapping()
    public ResponseEntity<AppUser> getUserByUsername(@RequestParam String userName) {
        // TODO
        throw new UnsupportedOperationException();
    }


    private AppUser getUserFromRequest(CreateUserRequest req) {
        AppUser user = new AppUser();
        BeanUtils.copyProperties(req, user);
        return user;
    }

    private CreateUserRequest getRequestFromUser(AppUser user) {
        CreateUserRequest dto = new CreateUserRequest();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }



}
