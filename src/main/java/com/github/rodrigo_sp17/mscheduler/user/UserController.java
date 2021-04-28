package com.github.rodrigo_sp17.mscheduler.user;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.rodrigo_sp17.mscheduler.auth.SocialCredential;
import com.github.rodrigo_sp17.mscheduler.auth.SocialUserDTO;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.UserDTO;
import com.github.rodrigo_sp17.mscheduler.user.data.PasswordRequestDTO;
import com.github.rodrigo_sp17.mscheduler.user.data.UserInfo;
import com.github.rodrigo_sp17.mscheduler.user.exceptions.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender javaMailSender;

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
    public ResponseEntity<UserDTO> signup(@Valid @RequestBody UserDTO req) {
        AppUser user = new AppUser();
        user.setUserInfo(new UserInfo());
        String errorMsg = null;

        // Name validation
        String name = req.getName().trim();
        user.getUserInfo().setName(name);

        // Usernames are case-insensitive and unique
        String username = req.getUsername()
                .trim()
                .toLowerCase();
        if (!userService.isUsernameAvailable(username)) {
            errorMsg = "The username already exists. Choose another one!";
            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMsg);
        }
        user.getUserInfo().setUsername(username);

        // Password validation
        String password = req.getPassword();
        if (!password.equals(req.getConfirmPassword())) {
            errorMsg = "Confirmed password and password are not the same! " +
                    "Please, check your values and try again.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
        }
        user.getUserInfo().setPassword(passwordEncoder.encode(password));

        // Email validation
        String email = req.getEmail().trim();    // Trimming to avoid common typos
        // TODO - send email confirmation
        user.getUserInfo().setEmail(email);

        var addedUser = userService.saveUser(user);
        log.info("Created new user: " + addedUser.getUserInfo().getUsername());

        UserDTO request = getDTOFromUser(addedUser);
        Link toNewUser = linkTo(methodOn(UserController.class).getLoggedUser(null))
                .withSelfRel();
        return ResponseEntity.created(toNewUser.toUri()).body(request);
    }

    @PostMapping("/socialSignup")
    public ResponseEntity<UserDTO> socialSignup(@Valid @RequestBody SocialUserDTO req)
             {
        AppUser user = new AppUser();
        user.setUserInfo(new UserInfo());

        String username = req.getUsername()
                .trim()
                .toLowerCase();
        if (!userService.isUsernameAvailable(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The username already exists. Choose another one");
        }
        String name = req.getName().trim();
        String email = req.getEmail().trim();

        user.getUserInfo().setName(name);
        user.getUserInfo().setEmail(email);
        user.getUserInfo().setUsername(username);

        var credential = new SocialCredential();
        credential.setSocialId(req.getSocialId());
        credential.setRegistrationId(req.getRegistrationId());
        credential.setSocialUser(user);

        user.setCredentials(List.of(credential));

        var addedUser = userService.saveUser(user);

        String addedUsername = addedUser.getUserInfo().getUsername();
        log.info("Created new user: " + addedUsername);
        String token = userService.encodeToken(addedUsername);

        UserDTO request = getDTOFromUser(addedUser);
        Link toNewUser = linkTo(methodOn(UserController.class).getLoggedUser(null))
                .withSelfRel();

        return ResponseEntity.created(toNewUser.toUri())
                .header("Authorization", "Bearer " + token)
                .body(request);
    }


    @PutMapping
    public ResponseEntity<UserDTO> editUserInfo(@RequestBody UserDTO req) {
        AppUser userToEdit = userService.getUserById(req.getUserId());
        if (req.getName() != null) {
            userToEdit.getUserInfo().setName(req.getName());
        }
        if (req.getEmail() != null) {
            userToEdit.getUserInfo().setEmail(req.getEmail());
        }
        AppUser editedUser = userService.saveUser(userToEdit);
        UserDTO request = getDTOFromUser(editedUser);
        request.add(linkTo(methodOn(UserController.class).getLoggedUser(null)).withSelfRel());
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<UserDTO> deleteUser(Authentication auth,
                                                        @RequestHeader String password) {
        var userToDelete = userService.getUserByUsername(auth.getName());
        var storedPassword = userToDelete.getUserInfo().getPassword();

        if (!passwordEncoder.matches(password, storedPassword)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account deletion unauthorized");
        }

        if (userService.deleteUser(userToDelete)) {
            log.info("Deleted user: " + userToDelete.getUserInfo().getUsername());
            return ResponseEntity.noContent().build();
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Deletion failed");
    }

    @PostMapping("/recover")
    public ResponseEntity<String> recoverAccount(@RequestParam String user) {
        AppUser appUser = null;

        // Attempts to fetch user
        try {
            appUser = userService.getUserByUsername(user);
        } catch (UserNotFoundException e) {
            log.info("Failed recovery attempt for user: " + user);
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
                                                @Valid @RequestBody PasswordRequestDTO passwordRequest) {
        String errorMsg;

        // Verifies token
        DecodedJWT decodedToken;
        try {
            decodedToken = userService
                    .decodeRecoveryToken(passwordRequest.getUsername(), token);
        } catch (JWTVerificationException e) {
            errorMsg = "You are not authorized to perform resets";
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMsg);
        }

        // Verifies passwords
        if (!passwordRequest.getPassword().equals(passwordRequest.getConfirmPassword())) {
            errorMsg = "Passwords do not match";
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
    private UserDTO getDTOFromUser(AppUser user) {
        UserDTO dto = new UserDTO();
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
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/changePassword")
                        .queryParam("reset", resetToken)
                        .toUriString()
        );

        var email = new SimpleMailMessage();
        email.setSubject("Agenda Marítima - Recuperação de Conta");
        email.setText(message);
        email.setTo(user.getUserInfo().getEmail());

        return email;
    }

}
