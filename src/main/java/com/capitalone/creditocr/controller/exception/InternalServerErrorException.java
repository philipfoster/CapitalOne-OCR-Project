package com.capitalone.creditocr.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception should be thrown by a controller when it detects that an unrecoverable error has occurred
 * Throwing this will automatically set the HTTP status code to {@link HttpStatus#UNSUPPORTED_MEDIA_TYPE}
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
