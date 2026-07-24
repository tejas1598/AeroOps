package com.aeroops.springdemo.exception;

public class EmployeeNotFoundException extends RuntimeException
{
    public EmployeeNotFoundException(String message)
    {
        super(message);     //Call the constructor of RuntimeException and give it this message.
    }
}
