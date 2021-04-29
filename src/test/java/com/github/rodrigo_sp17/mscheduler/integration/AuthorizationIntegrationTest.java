package com.github.rodrigo_sp17.mscheduler.integration;

import org.json.JSONObject;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
public class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private JavaMailSender mailSender;
    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    public void testSignupAndAuthorization() throws Exception {
        mvc.perform(get(new URI("/api/user")))
                .andExpect(status().isForbidden());

        mvc.perform(get(new URI("/api/shifts")))
                .andExpect(status().isForbidden());

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("name", "Maria Silva");
        jsonRequest.put("username", "Maria_silva12");
        jsonRequest.put("password", "pasWdssh@#!123");
        jsonRequest.put("confirmPassword", "pasWdssh@#!123");
        jsonRequest.put("email", "maria_silva@email.com");

        mvc.perform(post(new URI("/api/user/signup"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest.toString()))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Maria")))
                .andExpect(content().string(containsString("maria_silva12")))
                .andExpect(content().string(containsString("maria_silva@email.com")))
                .andExpect(content().string(not(containsString("password"))))
                .andExpect(content().string(not(containsString("confirmPassword"))));

        // Attempts wrong login
        var jsonLogin = new JSONObject();
        jsonLogin.put("username", "maria_silva12")
                .put("password", "wrong password");

        mvc.perform(post(new URI("/login"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonLogin.toString()))
                .andExpect(status().isUnauthorized());

        // Rightful login
        jsonLogin.put("password", "pasWdssh@#!123");
        var result = mvc.perform(post(new URI("/login"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonLogin.toString()))
                .andExpect(status().isOk())
                .andReturn();

        String token = result
                .getResponse()
                .getHeader("Authorization");

        mvc.perform(get(new URI("/api/user"))
                .header("Authorization", token))
                .andExpect(status().isOk());
    }
}
