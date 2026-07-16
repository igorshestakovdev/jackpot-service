package com.example.jackpot.exception;

import java.util.UUID;

public class ContributionNotFoundException extends RuntimeException {

    public ContributionNotFoundException(UUID betId) {
        super("No processed contribution exists for bet " + betId);
    }
}
