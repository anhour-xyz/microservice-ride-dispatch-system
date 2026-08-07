package com.rideshare.user_service.exception;
public class PhoneNumberAlreadyExistsException extends RuntimeException { public PhoneNumberAlreadyExistsException() { super("Phone number is already in use"); } }
