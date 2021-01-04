package com.github.rodrigo_sp17.mscheduler.user;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.CreateUserRequest;
import com.github.rodrigo_sp17.mscheduler.user.data.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    public final static Logger log = LoggerFactory.getLogger(UserController.class.getSimpleName());

    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<AppUser> getLoggedUser(Authentication auth) {
        String username = auth.getName();
        AppUser user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<CreateUserRequest> signup(@RequestBody CreateUserRequest req) {
        AppUser user = new AppUser();
        user.setUserInfo(new UserInfo());
        String errorMsg = null;

        // Name validation
        String name = req.getName().trim();
        if (name.isBlank() || name.isEmpty()
                || !name.contains(" ") || !name.matches("^[a-zA-Z\\s]+$")) {
            errorMsg = "Names must: " +
                    "- Have at least first and last name, separated by space. " +
                    "- Have only alphabetic letters. " +
                    "- Not be empty ";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        user.getUserInfo().setName(name);

        // Username validation
        // Usernames are case-insensitive and unique
        // TODO - is username validation is part of spring security validation?
        String username = req.getUsername()
                .trim()
                .toLowerCase();
        if (username.length() < 6 || username.length() > 30) {
            errorMsg = "Usernames must be between 6 to 30 characters long!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        if (userService.getUserByUsername(username) != null) {
            errorMsg = "The username already exists. Choose another one!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        user.getUserInfo().setUserName(username);

        // Password validation
        String password = req.getPassword();
        if (!password.equals(req.getConfirmPassword())) {
            errorMsg = "Confirmed password and password are not the same! " +
                    "Please, check your values and try again.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        if (password.length() < 8 || password.length() > 64) {
            errorMsg = "Passwords must be between 8 and 64 characters long!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        user.getUserInfo().setPassword(password);

        // Email validation
        String email = req.getEmail()
                .trim();    // Trimming to avoid common typos
        // TODO - send email confirmation
        if (email.isBlank() || email.isEmpty() || !email.matches("^.+[@].+$")) {
            errorMsg = "Invalid email address!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        user.getUserInfo().setEmail(email);

        var addedUser = userService.saveUser(user);
        log.info("Created new user: " + addedUser.getUserInfo().getUserName());

        // TODO - convert to proper status
        return ResponseEntity.ok(getRequestFromUser(addedUser));
    }

    @PutMapping
    public ResponseEntity<CreateUserRequest> editUserInfo(@RequestBody CreateUserRequest req) {
        AppUser userToEdit = userService.getUserById(req.getUserId());
        if (req.getName() != null) {
            userToEdit.getUserInfo().setName(req.getName());
        }
        if (req.getEmail() != null) {
            userToEdit.getUserInfo().setEmail(req.getEmail());
        }
        AppUser editedUser = userService.saveUser(userToEdit);
        return ResponseEntity.ok(getRequestFromUser(editedUser));
    }

    /*@GetMapping()
    public ResponseEntity<AppUser> getUserByUsername(@RequestParam String username) {
        var user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    */

    /*
    private AppUser getUserFromRequest(CreateUserRequest req) {
        AppUser user = new AppUser();
        BeanUtils.copyProperties(req, user);
        return user;
    }*/

    private CreateUserRequest getRequestFromUser(AppUser user) {
        CreateUserRequest dto = new CreateUserRequest();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUserInfo().getUserName());
        dto.setName(user.getUserInfo().getName());
        dto.setEmail(user.getUserInfo().getEmail());
        return dto;
    }



}
