package com.github.rodrigo_sp17.mscheduler.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public Event getEventById(Long eventId) {
        throw new UnsupportedOperationException();
    }

    public List<Event> getAllEvents(String username) {
        throw new UnsupportedOperationException();
    }

    public List<Event> getOwnedEvents(String username) {
        throw new UnsupportedOperationException();
    }

    public Event createEvent(Event event,
                             String ownerUsername,
                             List<String> invitedUsernames) {
        throw new UnsupportedOperationException();
    }

    public Event editEvent(Event editedEvent,
                           String username) {
        throw new UnsupportedOperationException();
    }

    public void deleteEvent(Long eventId, String username) {
        throw new UnsupportedOperationException();
    }

}
