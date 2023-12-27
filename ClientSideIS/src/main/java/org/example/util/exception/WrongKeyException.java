package org.example.util.exception;

public class WrongKeyException extends RuntimeException {
    public WrongKeyException(String message) {
        super(message);
    }
}
