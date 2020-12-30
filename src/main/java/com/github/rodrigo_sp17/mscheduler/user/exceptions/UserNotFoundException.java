package com.github.rodrigo_sp17.mscheduler.user.exceptions;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException() {
    }
}
