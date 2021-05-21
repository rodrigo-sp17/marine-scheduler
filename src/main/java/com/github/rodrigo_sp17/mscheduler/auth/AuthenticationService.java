package com.github.rodrigo_sp17.mscheduler.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.rodrigo_sp17.mscheduler.security.SecurityConstants;
import com.github.rodrigo_sp17.mscheduler.user.UserService;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthenticationService {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * Generates a refreshed JWT token from a valid or expired one.
     * @param expToken the (possibly expired) token to refresh
     * @return an Optional containing the new token if successful, or else empty
     */
    public Optional<String> refreshToken(String expToken) {
        // checks if token is valid
        DecodedJWT decodedToken;
        try {
            decodedToken = decodeRefreshToken(expToken);
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }

        var username = decodedToken.getSubject();
        if (!isTokenRefreshable(decodedToken)) {
            return Optional.empty();
        }

        // removes the old one from redis whitelist
        var removed = redisTemplate.opsForSet().remove(username, expToken);
        if (removed == null || removed == 0) {
            return Optional.empty();
        }

        // generate new token and adds to redis whitelist
        var newToken = encodeToken(username);
        redisTemplate.opsForSet().add(username, newToken);
        redisTemplate.expire(username, Duration.ofDays(SecurityConstants.REFRESH_DAYS_TO_EXPIRE));

        // returns new token
        return Optional.of(newToken);
    }

    /**
     * Logs a user out.
     * @param username the username of the User to log out
     * @return true if successful, else false
     */
    public boolean logout(String username) {
        // logged out is represented by absence of user key on cache
        var loggedOut = redisTemplate.delete(username);
        return loggedOut != null && loggedOut;
    }

    /**
     * Logs in a user and returns its JWT token. Does not perform validation on the
     * user.
     * @param username the username of the user to login
     * @return the JWT for the logged in user
     */
    public String login(String username) {
        var token = encodeToken(username);
        redisTemplate.opsForSet().add(username, token);
        redisTemplate.expire(username,
                Duration.ofDays(SecurityConstants.REFRESH_DAYS_TO_EXPIRE));
        return token;
    }

    /**
     * Encodes a JWT authentication token with the given username.
     * @param username the subject claim to add to the JWT
     * @return the encoded JWT, ready to return to the client
     */
    public String encodeToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(Date.from(LocalDate.now()
                        .plusDays(SecurityConstants.JWT_DAYS_TO_EXPIRE)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()))
                .sign(Algorithm.HMAC512(jwtSecret));
    }

    /**
     * Checks if a JWT token is valid.
     * @param token the JWT token to validate
     * @return the decoded JWT, if valid
     * @throws JWTVerificationException if the JWT is not valid or expired
     */
    public DecodedJWT verifyJWTToken(String token) throws JWTVerificationException {
        return JWT.require(Algorithm.HMAC512(jwtSecret))
                .build()
                .verify(token);
    }


    public String createRecoveryToken(AppUser user, LocalDateTime time) {
        return JWT.create()
                .withSubject(user.getUserInfo().getUsername())
                .withExpiresAt(Timestamp.valueOf(time.plusMinutes(
                        SecurityConstants.RESET_TOKEN_MINUTES_TO_EXPIRE))
                )
                .sign(Algorithm.HMAC512(jwtSecret + user.getUserInfo()
                        .getPassword()));
    }

    public DecodedJWT decodeRecoveryToken(String username, String token) {
        if (token == null) throw new JWTVerificationException("Token is null");

        AppUser user = userService.getUserByUsername(username);

        return JWT.require(Algorithm.HMAC512(jwtSecret
                + user.getUserInfo().getPassword()))
                .build()
                .verify(token);
    }

    // Private methods
    private DecodedJWT decodeRefreshToken(String encodedToken) throws JWTVerificationException {
        var token = encodedToken.replace("Bearer ", "");
        try {
            return verifyJWTToken(token);
        } catch (TokenExpiredException e) {
            return JWT.decode(encodedToken);
        }
    }

    private boolean isTokenRefreshable(DecodedJWT decodedJWT) {
        var issuedDate = decodedJWT.getIssuedAt();
        var issuedLocalDate = LocalDate.ofInstant(issuedDate.toInstant(), ZoneId.systemDefault());
        return !issuedLocalDate.plusDays(SecurityConstants.REFRESH_DAYS_TO_EXPIRE).isAfter(LocalDate.now());
    }

}
