package com.substring.foodies.exception;

public class BadItemRequestException extends RuntimeException {

    public BadItemRequestException(String message) {
        super(message);
    }

    public BadItemRequestException() {
        super("Cart already contains items from another restaurant. Please clear the cart before adding new items.");
    }

}
