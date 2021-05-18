package com.github.rodrigo_sp17.mscheduler.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.rodrigo_sp17.mscheduler.auth.data.SocialCredentialRepository;
import com.github.rodrigo_sp17.mscheduler.security.SecurityConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * This handler is responsible for processing information from a successful OAuth2 login,
 * integrating it with the current authentication schema
 */
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final String jwtSecret;
    private final SocialCredentialRepository socialCredentialRepository;

    public OAuth2SuccessHandler(String jwtSecret,
                                SocialCredentialRepository socialCredentialRepository) {
        this.jwtSecret = jwtSecret;
        this.socialCredentialRepository = socialCredentialRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        var token = (OAuth2AuthenticationToken) authentication;
        var socialId = authentication.getName();
        var registrationId = token.getAuthorizedClientRegistrationId();

        var credential = socialCredentialRepository
                .findBySocialIdAndRegistrationId(socialId, registrationId);

        if (credential.isPresent()) {
            var user = credential.get().getSocialUser();

            // creates jwt
            String jwtToken = JWT.create()
                    .withSubject(user.getUserInfo().getUsername())
                    .withExpiresAt(Date.from(LocalDate.now()
                            .plusDays(SecurityConstants.JWT_DAYS_TO_EXPIRE)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()))
                    .sign(Algorithm.HMAC512(jwtSecret));

            // returns page with jwt to user
            String url = String.format("/loginSuccess?token=%s&user=%s",
                    jwtToken,
                    user.getUserInfo().getUsername());

            response.sendRedirect(url);
        } else {
            var oAuth2User = (OAuth2User) authentication.getPrincipal();
            String name = oAuth2User.getAttribute("name");
            String email = oAuth2User.getAttribute("email");

            // Redirect to new signup
            String url = String.format("/socialSignup?name=%s&email=%s&socialId=%s&registrationId=%s",
                    name,
                    email,
                    socialId,
                    registrationId);

            response.sendRedirect(url);
        }
    }
}
