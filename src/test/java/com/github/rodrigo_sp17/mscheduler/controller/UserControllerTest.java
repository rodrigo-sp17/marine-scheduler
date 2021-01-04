package com.github.rodrigo_sp17.mscheduler.controller;

import com.github.rodrigo_sp17.mscheduler.user.*;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.CreateUserRequest;
import com.github.rodrigo_sp17.mscheduler.user.data.UserInfo;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// TODO - remove the security exclusions when functionality is thoroughly written
@WebMvcTest(value = UserController.class, excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class, SecurityAutoConfiguration.class})
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UserControllerTest {

    @MockBean
    private UserService userService;

    //@Autowired
    //private JacksonTester<CreateUserRequest> userRequestJacksonTester;

    @Autowired
    private MockMvc mvc;

    @Test
    public void testCreateUser() throws Exception {
        AppUser parsedUser = getUser();
        parsedUser.setUserId(null);

        when(userService.createUser(any())).thenReturn(getUser());

        // Manually parses the JSON to allow @JsonIgnore annotation on response
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("username", parsedUser.getUserInfo().getUserName());
        jsonRequest.put("name", parsedUser.getUserInfo().getName());
        jsonRequest.put("password", parsedUser.getUserInfo().getPassword());
        jsonRequest.put("confirmPassword", parsedUser.getUserInfo().getPassword());
        jsonRequest.put("email", parsedUser.getUserInfo().getEmail());

        //var userRequest = getUserRequest();

        var mvcResult = mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        var responseJson = mvcResult.getResponse().getContentAsString();

        assertTrue(responseJson.contains(parsedUser.getUserInfo().getName()));
        assertTrue(responseJson.contains(parsedUser.getUserInfo().getUserName()));
        assertTrue(responseJson.contains(parsedUser.getUserInfo().getEmail()));
        // Ensures password is not returned
        assertFalse(responseJson.contains(parsedUser.getUserInfo().getPassword()));
    }

    @Test
    public void testWrongSignup() throws Exception {
        AppUser parsedUser = getUser();
        parsedUser.setUserId(null);
        parsedUser.getUserInfo().setUserName("john@doe123");

        when(userService.getUserByUsername("john@doe123")).thenReturn(getUser());
        when(userService.createUser(parsedUser)).thenReturn(getUser());

        // Test wrong name
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("name", "");
        jsonRequest.put("username", parsedUser.getUserInfo().getUserName());
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
        jsonRequest.put("username", "john@DOE123 ");
        mvc.perform(post(new URI("/api/user/signup"))
                .content(jsonRequest.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("username already")));

        jsonRequest.put("username", "newUser");

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

    private CreateUserRequest getUserRequest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setName("John Doe");
        user.setEmail("john_doe@gmail.com");
        user.setUsername("john@Doe123");
        user.setPassword("testPassword");
        user.setConfirmPassword("testPassword");
        return user;
    }

    private AppUser getUser() {
        AppUser user = new AppUser();
        user.setUserInfo(new UserInfo());
        user.setUserId(1L);
        user.getUserInfo().setName("John Doe");
        user.getUserInfo().setEmail("john_doe@gmail.com");
        user.getUserInfo().setUserName("john@doe123");
        user.getUserInfo().setPassword("testPassword");
        return user;
    }



}
