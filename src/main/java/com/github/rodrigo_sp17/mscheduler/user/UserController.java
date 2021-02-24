package com.github.rodrigo_sp17.mscheduler.user;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.CreateUserRequest;
import com.github.rodrigo_sp17.mscheduler.user.data.PasswordRequest;
import com.github.rodrigo_sp17.mscheduler.user.data.UserInfo;
import com.github.rodrigo_sp17.mscheduler.user.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/user")
public class UserController {

    public final static Logger log = LoggerFactory.getLogger(UserController.class.getSimpleName());

    @Autowired
    private final UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JavaMailSender javaMailSender;

    public UserController(UserService userService, PasswordEncoder passwordEncoder,
                          JavaMailSender javaMailSender) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.javaMailSender = javaMailSender;
    }

    @GetMapping
    public CollectionModel<String> getUsernames() {
        List<String> usernames = userService.getUsernames();
        Link selfLink = linkTo(methodOn(UserController.class).getUsernames()).withSelfRel();
        return CollectionModel.of(usernames).add(selfLink);
    }

    @GetMapping("/me")
    public ResponseEntity<AppUser> getLoggedUser(Authentication auth) {
        String username = auth.getName();
        AppUser user = userService.getUserByUsername(username);
        user.add(linkTo(methodOn(UserController.class).editUserInfo(null)).withRel("edit"));
        user.add(linkTo(methodOn(UserController.class).getLoggedUser(auth)).withSelfRel());
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
        String username = req.getUsername()
                .trim()
                .toLowerCase();
        if (username.length() < 6 || username.length() > 30) {
            errorMsg = "Usernames must be between 6 to 30 characters long!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        if (!userService.isUsernameAvailable(username)) {
            errorMsg = "The username already exists. Choose another one!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        user.getUserInfo().setUsername(username);

        // Password validation
        String password = req.getPassword();
        if (!password.equals(req.getConfirmPassword())) {
            errorMsg = "Confirmed password and password are not the same! " +
                    "Please, check your values and try again.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        if (!isValidPassword(password)) {
            errorMsg = "Password does not meet requirements!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        user.getUserInfo().setPassword(passwordEncoder.encode(password));

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
        log.info("Created new user: " + addedUser.getUserInfo().getUsername());

        CreateUserRequest request = getRequestFromUser(addedUser);
        Link toNewUser = linkTo(methodOn(UserController.class).getLoggedUser(null))
                .withSelfRel();
        return ResponseEntity.created(toNewUser.toUri()).body(request);
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
        CreateUserRequest request = getRequestFromUser(editedUser);
        request.add(linkTo(methodOn(UserController.class).getLoggedUser(null)).withSelfRel());
        return ResponseEntity.ok(request);
    }

    @PostMapping("/recover")
    public ResponseEntity<String> recoverAccount(@RequestParam String user) {
        AppUser appUser = null;

        // Attempts to fetch user
        try {
            appUser = userService.getUserByUsername(user);
        } catch (UserNotFoundException e) {
            try {
                appUser = userService.getUserByEmail(user);
            } catch (UserNotFoundException d) {
                log.info("Failed recovery attempt for user: " + user);
            }
        }

        if (appUser != null) {
            String resetToken = userService.createRecoveryToken(appUser,
                    LocalDateTime.now());

            // Send email
            var email = createRecoveryEmail(resetToken, appUser);
            javaMailSender.send(email);
            log.info("Recovery e-mail sent to: " + appUser.getUserInfo().getEmail());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestParam String token) {
        // validates
        // returns change password window
        // TODO - couple with React page. Unused for API password reset.
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestHeader("reset") String token,
                                                @RequestBody PasswordRequest passwordRequest) {
        String errorMsg;

        // Verifies token
        DecodedJWT decodedToken;
        try {
            decodedToken = userService
                    .decodeRecoveryToken(passwordRequest.getUsername(), token);
        } catch (JWTVerificationException e) {
            errorMsg = "You are not authorized to perform resets.";
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMsg);
        }

        // Verifies passwords
        if (!passwordRequest.getPassword().equals(passwordRequest.getConfirmPassword())) {
            errorMsg = "Passwords do not match.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }

        if (!isValidPassword(passwordRequest.getPassword())) {
            errorMsg = "Password does not meet requirements!";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }

        // Edits the password for user
        var user = userService.getUserByUsername(decodedToken.getSubject());
        user.getUserInfo().setPassword(passwordEncoder
                .encode(passwordRequest.getPassword()));

        userService.saveUser(user);

        return ResponseEntity.ok().build();
    }


    // Helper methods
    private CreateUserRequest getRequestFromUser(AppUser user) {
        CreateUserRequest dto = new CreateUserRequest();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUserInfo().getUsername());
        dto.setName(user.getUserInfo().getName());
        dto.setEmail(user.getUserInfo().getEmail());
        return dto;
    }

    private SimpleMailMessage createRecoveryEmail(String resetToken, AppUser user) {
        String message = String.format(
                "Olá, %s! %n%n" +
                        "Recebemos uma solicitação de recuperação de conta em seu nome. " +
                        "Caso realmente o tenha feito, siga o link fornecido abaixo para trocar " +
                        "sua senha: %n%n" +
                        "%s %n%n" +
                        "Atenciosamente, %n%n" +
                        "Equipe Agenda Marítima",
                user.getUserInfo().getName(),
                "http://localhost:8080/api/user/changePassword?reset=" + resetToken
        );

        var email = new SimpleMailMessage();
        email.setSubject("Agenda Marítima - Recuperação de Conta");
        email.setText(message);
        email.setTo(user.getUserInfo().getEmail());

        return email;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8 || password.length() > 64) {
            return false;
        }
        return true;
    }

}
