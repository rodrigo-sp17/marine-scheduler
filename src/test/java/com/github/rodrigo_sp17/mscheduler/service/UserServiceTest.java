package com.github.rodrigo_sp17.mscheduler.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.github.rodrigo_sp17.mscheduler.TestData;
import com.github.rodrigo_sp17.mscheduler.friend.data.FriendRequestRepository;
import com.github.rodrigo_sp17.mscheduler.security.SecurityConstants;
import com.github.rodrigo_sp17.mscheduler.user.UserService;
import com.github.rodrigo_sp17.mscheduler.user.data.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private UserRepository userRepository = Mockito.mock(UserRepository.class);

    private FriendRequestRepository requestRepository = Mockito.mock(FriendRequestRepository.class);

    private UserService userService = new UserService(userRepository,
            requestRepository, "testsecret");

    @Test
    public void testEncodeDecodeRecoveryToken() {
        var user = TestData.getUsers().get(0);
        user.getUserInfo().setPassword("test");
        var expMinutes = SecurityConstants.RESET_TOKEN_MINUTES_TO_EXPIRE;

        when(userRepository.findByUsername(any())).thenReturn(user);

        var resultToken = userService.createRecoveryToken(user, LocalDateTime.now());
        assertNotNull(resultToken);

        var decodedToken = userService
                .decodeRecoveryToken(user.getUserInfo().getUsername(), resultToken);

        assertEquals(user.getUserInfo().getUsername(), decodedToken.getSubject());

        // Ensures expiration is working
        assertThrows(JWTVerificationException.class, () -> userService
                .decodeRecoveryToken(user.getUserInfo().getUsername(),
                        userService.createRecoveryToken(user,
                                LocalDateTime.now().minusMinutes(expMinutes + 1))
                ));


        LocalDateTime timeOfJwt = LocalDateTime.of(2021, 02, 23,
                12, 10);

        // Tests determinism
        var firstToken = userService.createRecoveryToken(user, timeOfJwt);
        var secondToken = userService.createRecoveryToken(user, timeOfJwt);

        // Tests password dependency of JWT
        user.getUserInfo().setPassword("newpassword");
        var thirdToken = userService.createRecoveryToken(user, timeOfJwt);

        assertEquals(firstToken, secondToken);
        assertNotEquals(firstToken, thirdToken);

    }
}
