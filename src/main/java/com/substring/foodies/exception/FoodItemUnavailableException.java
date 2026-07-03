package com.substring.foodies.exception;

public class FoodItemUnavailableException extends RuntimeException {
    public FoodItemUnavailableException(String message) {
        super(message);
    }
}
