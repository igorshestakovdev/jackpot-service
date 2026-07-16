package com.example.jackpot.exception;

public class JackpotNotFoundException extends RuntimeException {

    public JackpotNotFoundException(String jackpotId) {
        super("Jackpot not found: " + jackpotId);
    }
}
