package com.github.rodrigo_sp17.mscheduler.friend;

public class FriendRequestNotFoundException extends RuntimeException {

    public FriendRequestNotFoundException() {
    }

    public FriendRequestNotFoundException(String message) {
        super(message);
    }
}
