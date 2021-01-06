package com.github.rodrigo_sp17.mscheduler.shift.exception;

public class ShiftNotFoundException extends RuntimeException{

    public ShiftNotFoundException() {
    }

    public ShiftNotFoundException(String message) {
        super(message);
    }
}
