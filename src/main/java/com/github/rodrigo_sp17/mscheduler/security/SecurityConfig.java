package com.github.rodrigo_sp17.mscheduler.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private final UserDetailsServiceImpl userDetailsService;

    private final String jwtSecret;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          @Value("${jwt.secret}") String secret) {
        this.userDetailsService = userDetailsService;
        this.jwtSecret = secret;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers(HttpMethod.POST,
                        "/api/user/signup",
                        "/api/user/recover",
                        "/api/user/changePassword",
                        "/api/user/resetPassword").permitAll()
                .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                .anyRequest().authenticated()
            .and()
            .addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtSecret))
            .addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtSecret))
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().csrf().disable();

                // TODO - cors?
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Bean
    public PasswordEncoder getEncoder() {
        // Returns the encoder to be used with Spring Security
        // DelegatingPasswordEncoder is used to allow multiple algorithms and be ready to change
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


/*    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods();
    }*/

}
