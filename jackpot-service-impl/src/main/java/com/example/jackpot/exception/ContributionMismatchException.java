package com.example.jackpot.exception;

public class ContributionMismatchException extends RuntimeException {

    public ContributionMismatchException() {
        super("The supplied userId or jackpotId does not match the processed bet");
    }
}
