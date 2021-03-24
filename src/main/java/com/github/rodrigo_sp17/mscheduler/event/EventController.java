package com.github.rodrigo_sp17.mscheduler.event;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/event")
public class EventController {

    @GetMapping
    public ResponseEntity<CollectionModel<EventDTO>> getAllEvents(Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @GetMapping("/owned")
    public ResponseEntity<CollectionModel<EventDTO>> getOwnedEvents(Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id,
                                                 Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<EventDTO> confirmEvent(@PathVariable Long id,
                                              Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO dto,
                                             Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> editEvent(@PathVariable Long id,
                                           @RequestBody EventDTO dto,
                                           Authentication auth) {
        throw new UnsupportedOperationException();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id,
                                         Authentication auth) {
        throw new UnsupportedOperationException();
    }
}
