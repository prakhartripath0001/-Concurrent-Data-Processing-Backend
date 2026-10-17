package com.Data_Processing_Backend.exception;

public class DuplicateInventoryException extends RuntimeException {
    public DuplicateInventoryException() {
        super();
    }

    public DuplicateInventoryException(String message) {
        super(message);
    }
}

