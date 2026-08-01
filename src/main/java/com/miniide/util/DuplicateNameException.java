package com.miniide.util;

public class DuplicateNameException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateNameException(String message) {
        super(message);
    }
}
