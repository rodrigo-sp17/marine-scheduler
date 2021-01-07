package com.github.rodrigo_sp17.mscheduler;

import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.UserInfo;

import java.util.Arrays;
import java.util.List;

public class TestData {

    public static List<AppUser> getUsers() {
        AppUser user = new AppUser();
        UserInfo ui = new UserInfo();
        user.setUserInfo(ui);
        user.setUserId(1L);
        user.getUserInfo().setName("John Doe");
        user.getUserInfo().setEmail("john_doe@gmail.com");
        user.getUserInfo().setUsername("john@Doe123");
        user.getUserInfo().setPassword("testPassword");

        AppUser user2 = new AppUser();
        UserInfo ui2 = new UserInfo();
        user2.setUserInfo(ui2);
        user2.setUserId(2L);
        user2.getUserInfo().setName("Jane Doe");
        user2.getUserInfo().setEmail("jane_doe@gmail.com");
        user2.getUserInfo().setUsername("jane_girl18");
        user2.getUserInfo().setPassword("testPassword2");

        AppUser user3 = new AppUser();
        UserInfo ui3 = new UserInfo();
        user3.setUserInfo(ui3);
        user3.setUserId(3L);
        user3.getUserInfo().setName("Joao Silva");
        user3.getUserInfo().setEmail("joao_silva12@gmail.com");
        user3.getUserInfo().setUsername("joaozinn");
        user3.getUserInfo().setPassword("testPassword3");

        return Arrays.asList(user, user2, user3);
    }
}
