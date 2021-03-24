package com.github.rodrigo_sp17.mscheduler.event;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException() {
    }

    public EventNotFoundException(String message) {
        super(message);
    }
}
