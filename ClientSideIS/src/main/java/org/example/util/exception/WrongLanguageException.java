package org.example.util.exception;

public class WrongLanguageException extends RuntimeException {
    public WrongLanguageException(String message) {
        super(message);
    }
}
