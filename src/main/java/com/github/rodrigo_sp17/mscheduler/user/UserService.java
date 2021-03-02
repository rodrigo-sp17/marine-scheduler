package com.github.rodrigo_sp17.mscheduler.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.rodrigo_sp17.mscheduler.security.SecurityConstants;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.UserRepository;
import com.github.rodrigo_sp17.mscheduler.user.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final String jwtSecret;

    @Autowired
    public UserService(UserRepository userRepository,
                       @Value("${jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.jwtSecret = jwtSecret;
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

    public AppUser getUserByEmail(String email) {
        var user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("Could not find user " + email);
        }
        return user;
    }

    public String createRecoveryToken(AppUser user, LocalDateTime time) {
        String token = JWT.create()
                .withSubject(user.getUserInfo().getUsername())
                .withExpiresAt(Timestamp.valueOf(time.plusMinutes(
                        SecurityConstants.RESET_TOKEN_MINUTES_TO_EXPIRE))
                )
                .sign(Algorithm.HMAC512(jwtSecret + user.getUserInfo()
                        .getPassword()));

        return token;
    }

    public DecodedJWT decodeRecoveryToken(String username, String token) {
        if (token == null) throw new JWTVerificationException("Token is null");

        AppUser user = getUserByUsername(username);

        return JWT.require(Algorithm.HMAC512(jwtSecret
                + user.getUserInfo().getPassword()))
                .build()
                .verify(token);
    }

    @Transactional
    public AppUser saveUser(AppUser user) {
        return userRepository.save(user);
    }
}
