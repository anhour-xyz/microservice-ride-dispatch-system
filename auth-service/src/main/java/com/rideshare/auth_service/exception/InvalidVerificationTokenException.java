package com.rideshare.auth_service.exception;

public class InvalidVerificationTokenException extends RuntimeException{
    
    public InvalidVerificationTokenException(){
        super("Verification token is invalid or already used");
    }
}
