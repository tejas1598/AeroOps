package com.aeroops.springdemo.exception;

public class RoleNotFoundException extends RuntimeException
{
    public RoleNotFoundException(String message)
    {
        super(message);
    }
}
