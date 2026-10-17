package com.Data_Processing_Backend.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException() {
        super();
    }

    public InventoryNotFoundException(String message) {
        super(message);
    }
}

