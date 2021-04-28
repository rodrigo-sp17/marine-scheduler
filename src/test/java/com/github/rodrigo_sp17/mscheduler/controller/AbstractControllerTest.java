package com.github.rodrigo_sp17.mscheduler.controller;

import com.github.rodrigo_sp17.mscheduler.auth.SocialCredentialRepository;
import com.github.rodrigo_sp17.mscheduler.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;


public abstract class AbstractControllerTest {
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private SocialCredentialRepository socialCredentialRepository;
    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    protected MockMvc mvc;
}
