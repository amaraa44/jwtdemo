package com.davidhalma.jwtdemo.jwtframework.exception;

public class JwtTokenException extends RuntimeException {
    public JwtTokenException(String message) {
        super(message);
    }
}
