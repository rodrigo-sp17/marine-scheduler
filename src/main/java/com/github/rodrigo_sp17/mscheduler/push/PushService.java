package com.github.rodrigo_sp17.mscheduler.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PushService {
    /*@Autowired
    private RedisTemplate<String, SseEmitter> redisTemplate;*/

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    /**
     * Subscribes a user to SSE events
     * @param username the username of the AppUser to subscribe
     * @return the SSE emitter for the user
     */
    public SseEmitter subscribe(String username) {
        var key = getKey(username);
        var emitter = new SseEmitter(Duration.ofMinutes(10).toMillis());
        /*emitter.onCompletion(() -> redisTemplate.delete(key));
        emitter.onError(e -> {
            log.error("Exception creating emitter", e);
            redisTemplate.delete(key);
        });

        redisTemplate.opsForValue().set(key, emitter);*/
        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onError(e -> {
            log.error("Exception creating emitter", e);
            emitters.remove(key);
        });

        emitters.put(key, emitter);
        return emitter;
    }

    /**
     * Pushes a SSE to a subscribed user
     * @param username the username of the AppUser to send the event to
     * @param event the PushEvent object of the specific event to send
     */
    @Async
    public void pushNotification(String username, PushEvent event) {
        var key = getKey(username);
        //var emitter = redisTemplate.opsForValue().get(key);
        var emitter = emitters.get(key);
        if (emitter == null) {
            return;
        }

        var sseEvent = SseEmitter.event().name(event.getType()).data(event.getBody());
/*
        try {
            emitter.send(sseEvent);
        } catch (IOException e) {
            log.warn("Could not send event for user " + username);
            redisTemplate.delete(key);
        }
*/
        try {
            emitter.send(sseEvent);
            log.debug("Event sent for {}", username);
        } catch (IOException e) {
            log.warn("Could not send event for user " + username);
            emitters.remove(key);
        }

    }

    private String getKey(String username) {
        return "emitter:" + username;
    }

}
