package com.github.rodrigo_sp17.mscheduler.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.rodrigo_sp17.mscheduler.TestData;
import com.github.rodrigo_sp17.mscheduler.security.UserDetailsServiceImpl;
import com.github.rodrigo_sp17.mscheduler.user.UserController;
import com.github.rodrigo_sp17.mscheduler.user.UserService;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.exceptions.UserNotFoundException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(value = UserController.class)
@AutoConfigureJsonTesters
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private JavaMailSender mailSender;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    public void testCreateUser() throws Exception {
        AppUser parsedUser = TestData.getUsers().get(0);
        parsedUser.setUserId(null);

        when(userService.saveUser(any())).thenReturn(TestData.getUsers().get(0));
        when(userService.isUsernameAvailable("john@doe123")).thenReturn(true);

        // Manually parses the JSON to allow @JsonIgnore annotation on response
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("username", parsedUser.getUserInfo().getUsername());
        jsonRequest.put("name", parsedUser.getUserInfo().getName());
        jsonRequest.put("password", parsedUser.getUserInfo().getPassword());
        jsonRequest.put("confirmPassword", parsedUser.getUserInfo().getPassword());
        jsonRequest.put("email", parsedUser.getUserInfo().getEmail());

        var mvcResult = mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        var responseJson = mvcResult.getResponse().getContentAsString();

        assertTrue(responseJson.contains(parsedUser.getUserInfo().getName()));
        assertTrue(responseJson.contains(parsedUser.getUserInfo().getUsername()));
        assertTrue(responseJson.contains(parsedUser.getUserInfo().getEmail()));
        // Ensures password is not returned
        assertFalse(responseJson.contains(parsedUser.getUserInfo().getPassword()));
    }

    @Test
    public void testWrongSignup() throws Exception {
        AppUser parsedUser = TestData.getUsers().get(0);
        parsedUser.setUserId(null);
        parsedUser.getUserInfo().setUsername("john@doe123");

        when(userService.getUserByUsername("john@doe123")).thenReturn(TestData.getUsers().get(0));
        when(userService.saveUser(parsedUser)).thenReturn(TestData.getUsers().get(0));
        when(userService.isUsernameAvailable(any())).thenReturn(false);

        // Test wrong name
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("name", "");
        jsonRequest.put("username", parsedUser.getUserInfo().getUsername());
        jsonRequest.put("password", parsedUser.getUserInfo().getPassword());
        jsonRequest.put("confirmPassword", parsedUser.getUserInfo().getPassword());
        jsonRequest.put("email", parsedUser.getUserInfo().getEmail());

        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("Names must:")));

        jsonRequest.put("name", "Rodrigo");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("Names must:")));

        jsonRequest.put("name", "Rodrigo1 Rodrigues");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("Names must:")));

        jsonRequest.put("name", "John Doe");

        // Test wrong username
        // Test too short
        jsonRequest.put("username", "john1");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("Usernames must be")));

        // Test case-insensitive and wrong space
        when(userService.isUsernameAvailable(eq("john@doe123"))).thenReturn(false);
        jsonRequest.put("username", "john@DOE123 ");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(header().string("error", containsString("username already")));

        jsonRequest.put("username", "newUser");

        when(userService.isUsernameAvailable(any())).thenReturn(true);
        // Test wrong password
        jsonRequest.put("confirmPassword", "testPassword1");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("Confirmed password")));

        // Test too short
        jsonRequest.put("password", "short");
        jsonRequest.put("confirmPassword", "short");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("Password does not")));

        jsonRequest.put("password", "short1234");
        jsonRequest.put("confirmPassword", "short1234");

        // Test wrong email
        jsonRequest.put("email", "notanemail@");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("Invalid email")));
        jsonRequest.put("email", "");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("Invalid email")));
    }

    @Test
    @WithMockUser(username = "john@doe123")
    public void testEditUser() throws Exception {
        AppUser originalUser = TestData.getUsers().get(0);
        AppUser editedUser = TestData.getUsers().get(0);
        editedUser.getUserInfo().setEmail("newmail@hotmail.com");
        var request = new JSONObject();
        request.put("userId", originalUser.getUserId());
        request.put("email", "newmail@hotmail.com");

        when(userService.getUserById(1L)).thenReturn(originalUser);
        when(userService.saveUser(any())).thenReturn(editedUser);

        mvc.perform(put(new URI("/api/user"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("newmail@hotmail.com")));
    }

    @Test
    public void testRecoverAccount() throws Exception {
        AppUser user = TestData.getUsers().get(0);
        when(userService.getUserByUsername(eq(user.getUserInfo().getUsername()))).thenReturn(user);
        when(userService.getUserByUsername(eq(user.getUserInfo().getEmail())))
                .thenThrow(new UserNotFoundException());
        when(userService.getUserByEmail(eq(user.getUserInfo().getEmail()))).thenReturn(user);
        when(userService.getUserByEmail(eq("ababwa")))
                .thenThrow(new UserNotFoundException());

        mvc.perform(post(new URI("/api/user/recover"))
                .param("user", "ababwa"))
                .andExpect(status().isOk());

        mvc.perform(post(new URI("/api/user/recover"))
                .param("user", user.getUserInfo().getUsername()))
                .andExpect(status().isOk());

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testResetPassword() throws Exception {
        AppUser user = TestData.getUsers().get(0);
        String token = JWT.create()
                .withSubject(user.getUserInfo().getUsername())
                .withExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)))
                .sign(Algorithm.HMAC512("secret"));

        DecodedJWT decoded = JWT.decode(token);

        when(userService.decodeRecoveryToken(any(), eq(token)))
                .thenThrow(JWTVerificationException.class);
        when(userService.decodeRecoveryToken(eq(user.getUserInfo().getUsername()), eq(token)))
                .thenReturn(decoded);
        when(userService.decodeRecoveryToken(any(), eq("random")))
                .thenThrow(JWTVerificationException.class);

        when(userService.getUserByUsername(user.getUserInfo().getUsername())).thenReturn(user);

        JSONObject json = new JSONObject();
        json.put("username", user.getUserInfo().getUsername());
        json.put("password", "newPassword");
        json.put("confirmPassword", "newPassword");

        mvc.perform(post(new URI("/api/user/resetPassword"))
                .header("reset", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isOk());
        verify(userService, times(1)).saveUser(any());

        mvc.perform(post(new URI("/api/user/resetPassword"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isBadRequest());
        verify(userService, times(1)).saveUser(any());

        mvc.perform(post(new URI("/api/user/resetPassword"))
                .header("reset", "random")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isForbidden());
        verify(userService, times(1)).saveUser(any());

        json.put("username", "wrongUsername");
        mvc.perform(post(new URI("/api/user/resetPassword"))
                .header("reset", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isForbidden());
        verify(userService, times(1)).saveUser(any());

        json.put("username", user.getUserInfo().getUsername());
        json.put("confirmPassword", "wrong");
        mvc.perform(post(new URI("/api/user/resetPassword"))
                .header("reset", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isBadRequest());

        json.put("password", "small");
        json.put("confirmPassword", "small");
        mvc.perform(post(new URI("/api/user/resetPassword"))
                .header("reset", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("error", containsString("requirements")));
        verify(userService, times(1)).saveUser(any());
    }


}
