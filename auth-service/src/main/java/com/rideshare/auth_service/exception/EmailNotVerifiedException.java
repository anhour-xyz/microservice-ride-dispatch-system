package com.rideshare.auth_service.exception;

public class EmailNotVerifiedException extends RuntimeException{
    public EmailNotVerifiedException(){
        super("Email address has not been verified");
    }
}
