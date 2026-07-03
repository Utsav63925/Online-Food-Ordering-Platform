package com.substring.foodies.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundException extends RuntimeException{

    public FileNotFoundException()
    {
        super("File Not Found");
    }
    public FileNotFoundException(String message)
    {
        super(message);
    }
}
