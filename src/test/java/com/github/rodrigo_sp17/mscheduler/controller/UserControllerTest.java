package com.github.rodrigo_sp17.mscheduler.controller;

import com.github.rodrigo_sp17.mscheduler.TestData;
import com.github.rodrigo_sp17.mscheduler.security.UserDetailsServiceImpl;
import com.github.rodrigo_sp17.mscheduler.user.UserController;
import com.github.rodrigo_sp17.mscheduler.user.UserService;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = UserController.class)
@AutoConfigureJsonTesters
public class UserControllerTest {

    @MockBean
    private UserService userService;

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
                .andExpect(status().isOk())
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
                .andExpect(status().reason(containsString("Names must:")));

        jsonRequest.put("name", "Rodrigo");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Names must:")));

        jsonRequest.put("name", "Rodrigo1 Rodrigues");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Names must:")));

        jsonRequest.put("name", "John Doe");

        // Test wrong username
        // Test too short
        jsonRequest.put("username", "john1");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Usernames must be")));

        // Test case-insensitive and wrong space
        when(userService.isUsernameAvailable(eq("john@doe123"))).thenReturn(false);
        jsonRequest.put("username", "john@DOE123 ");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("username already")));

        jsonRequest.put("username", "newUser");

        when(userService.isUsernameAvailable(any())).thenReturn(true);
        // Test wrong password
        jsonRequest.put("confirmPassword", "testPassword1");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Confirmed password")));

        // Test too short
        jsonRequest.put("password", "short");
        jsonRequest.put("confirmPassword", "short");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Passwords must be")));

        jsonRequest.put("password", "short1234");
        jsonRequest.put("confirmPassword", "short1234");

        // Test wrong email
        jsonRequest.put("email", "notanemail@");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Invalid email")));
        jsonRequest.put("email", "");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Invalid email")));
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





}
