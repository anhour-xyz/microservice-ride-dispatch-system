package com.rideshare.auth_service.exception;
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() { super("Refresh token is invalid or expired"); }
}
