package com.github.rodrigo_sp17.mscheduler.controller;

import com.github.rodrigo_sp17.mscheduler.user.*;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.CreateUserRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO - remove the security exclusions when functionality is thoroughly written
@WebMvcTest(value = UserController.class, excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class, SecurityAutoConfiguration.class})
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UserControllerTest {

    //@Autowired
    //private UserController userController;

    @MockBean
    private UserService userService;

    @Autowired
    private JacksonTester<CreateUserRequest> userRequestJacksonTester;

    @Autowired
    private MockMvc mvc;

    @Test
    public void testCreateUser() throws Exception {
        AppUser parsedUser = getUser();
        parsedUser.setUserId(null);

        when(userService.createUser(parsedUser)).thenReturn(getUser());

        // Manually parses the JSON to allow @JsonIgnore annotation on response
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("userName", parsedUser.getUserInfo().getUserName());
        jsonRequest.put("name", parsedUser.getUserInfo().getName());
        jsonRequest.put("password", parsedUser.getUserInfo().getPassword());
        jsonRequest.put("confirmPassword", parsedUser.getUserInfo().getPassword());
        jsonRequest.put("email", parsedUser.getUserInfo().getEmail());

        var userRequest = getUserRequest();

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

    private CreateUserRequest getUserRequest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setName("John Doe");
        user.setEmail("john_doe@gmail.com");
        user.setUserName("john@Doe123");
        user.setPassword("testPassword");
        user.setConfirmPassword("testPassword");
        return user;
    }

    private AppUser getUser() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.getUserInfo().setName("John Doe");
        user.getUserInfo().setEmail("john_doe@gmail.com");
        user.getUserInfo().setUserName("john@Doe123");
        user.getUserInfo().setPassword("testPassword");
        return user;
    }



}
