package com.github.rodrigo_sp17.mscheduler.security;

import com.github.rodrigo_sp17.mscheduler.auth.OAuth2SuccessHandler;
import com.github.rodrigo_sp17.mscheduler.auth.SocialCredentialRepository;
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
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private final UserDetailsServiceImpl userDetailsService;
    @Autowired
    private SocialCredentialRepository socialCredentialRepository;
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
                .antMatchers(
                        HttpMethod.POST,
                        "/api/user/signup",
                        "/api/user/socialSignup",
                        "/api/user/recover",
                        "/api/user/changePassword",
                        "/api/user/resetPassword",
                        "/oauth2/delete"
                ).permitAll()
                .antMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/oauth2/delete-status"
                ).permitAll()
                .antMatchers(
                        "/",
                        "/loginSuccess",
                        "/signup",
                        "/socialSignup",
                        "/login",
                        "/changePassword",
                        "/**/*.{js,html,css}"
                ).permitAll()
                .anyRequest()
                .authenticated()
            .and()
                .oauth2Login(Customizer.withDefaults())
                .oauth2Login()
                    .loginPage("/login")
                    .successHandler(
                        new OAuth2SuccessHandler(
                                jwtSecret,
                                socialCredentialRepository))
            .and()
                .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint())
            .and()
            .addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtSecret))
            .addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtSecret))
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable();

                // TODO - cors and csrf
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

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://agendamaritima.herokuapp.com/", "http://localhost:8080/", "http://localhost:3000/"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Headers",
                "Access-Control-Allow-Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers", "Origin",
                "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
