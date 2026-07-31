package com.rideshare.auth_service.exception;


public class VerificationTokenExpiredException extends RuntimeException{
    public VerificationTokenExpiredException(){
        super("Verification token has expired");
    }
}
