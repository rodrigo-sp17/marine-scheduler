package com.github.rodrigo_sp17.mscheduler.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//@Configuration
//@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                //.antMatchers("/api/user/signup").permitAll()
                .anyRequest().permitAll().and().httpBasic();
    }

    /*
    @Bean
    public PasswordEncoder getEncoder() {
        // Returns the encoder to be used with Spring Security
        // DelegatingPasswordEncoder is used to allow multiple algorithms and be ready to change
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
     */
}
