package com.github.rodrigo_sp17.mscheduler.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.github.rodrigo_sp17.mscheduler.TestData;
import com.github.rodrigo_sp17.mscheduler.auth.data.AuthenticationService;
import com.github.rodrigo_sp17.mscheduler.friend.data.FriendRequestRepository;
import com.github.rodrigo_sp17.mscheduler.security.SecurityConstants;
import com.github.rodrigo_sp17.mscheduler.user.UserService;
import com.github.rodrigo_sp17.mscheduler.user.data.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository = Mockito.mock(UserRepository.class);
    @Mock
    private FriendRequestRepository requestRepository = Mockito.mock(FriendRequestRepository.class);
    @Mock
    private UserService userService = Mockito.mock(UserService.class);
    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void testEncodeDecodeRecoveryToken() {
        var user = TestData.getUsers().get(0);
        user.getUserInfo().setPassword("test");
        var expMinutes = SecurityConstants.RESET_TOKEN_MINUTES_TO_EXPIRE;

        when(userService.getUserByUsername(any())).thenReturn(user);

        var resultToken = authenticationService.createRecoveryToken(user, LocalDateTime.now());
        assertNotNull(resultToken);

        var decodedToken = authenticationService
                .decodeRecoveryToken(user.getUserInfo().getUsername(), resultToken);

        assertEquals(user.getUserInfo().getUsername(), decodedToken.getSubject());

        // Ensures expiration is working
        assertThrows(JWTVerificationException.class, () -> authenticationService
                .decodeRecoveryToken(user.getUserInfo().getUsername(),
                        authenticationService.createRecoveryToken(user,
                                LocalDateTime.now().minusMinutes(expMinutes + 1))
                ));


        LocalDateTime timeOfJwt = LocalDateTime.of(2021, 02, 23,
                12, 10);

        // Tests determinism
        var firstToken = authenticationService.createRecoveryToken(user, timeOfJwt);
        var secondToken = authenticationService.createRecoveryToken(user, timeOfJwt);

        // Tests password dependency of JWT
        user.getUserInfo().setPassword("newpassword");
        var thirdToken = authenticationService.createRecoveryToken(user, timeOfJwt);

        assertEquals(firstToken, secondToken);
        assertNotEquals(firstToken, thirdToken);

    }
}
