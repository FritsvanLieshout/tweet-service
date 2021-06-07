package com.kwetter.frits.tweetservice.exceptions;

public class SwearWordException extends RuntimeException {

    public SwearWordException() {
        super("Your tweet message contains a swear word");
    }

    public SwearWordException(String message) {
        super(message);
    }
}
