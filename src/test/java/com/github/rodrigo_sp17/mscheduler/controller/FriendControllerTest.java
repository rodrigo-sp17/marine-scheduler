package com.github.rodrigo_sp17.mscheduler.controller;

import com.github.rodrigo_sp17.mscheduler.friend.FriendController;
import com.github.rodrigo_sp17.mscheduler.friend.FriendService;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@WebMvcTest(value = FriendController.class)
@AutoConfigureMockMvc
public class FriendControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FriendService friendService;

    @Test
    @WithMockUser(username = "john@doe123")
    public void testFriendsHappyPath() throws Exception {
        mvc.perform(get(new URI("/api/friends")))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(not(containsString("Jane Doe"))));

        mvc.perform(get(new URI("/api/friends/search"))
                .param("username", "jane_girl18"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Jane Doe")));

        mvc.perform(post(new URI("/api/friends/add"))
                .requestAttr("username", "jane_girl18"))
                .andExpect(status().isOk());

        mvc.perform(get(new URI("/api/friends")))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("Jane Doe")));

        mvc.perform(delete(new URI("/api/friends/remove"))
                .requestAttr("userName", "jane_girl18"))
                .andExpect(status().isOk());
    }


    private List<AppUser> getUsers() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.getUserInfo().setName("John Doe");
        user.getUserInfo().setEmail("john_doe@gmail.com");
        user.getUserInfo().setUsername("john@Doe123");
        user.getUserInfo().setPassword("testPassword");

        AppUser user2 = new AppUser();
        user2.setUserId(2L);
        user2.getUserInfo().setName("Jane Doe");
        user2.getUserInfo().setEmail("jane_doe@gmail.com");
        user2.getUserInfo().setUsername("jane_girl18");
        user2.getUserInfo().setPassword("testPassword2");

        AppUser user3 = new AppUser();
        user3.setUserId(3L);
        user3.getUserInfo().setName("Joao Silva");
        user3.getUserInfo().setEmail("joao_silva12@gmail.com");
        user3.getUserInfo().setUsername("joaozinn");
        user3.getUserInfo().setPassword("testPassword3");

        return Arrays.asList(user, user2, user3);
    }

}
