package com.capitalone.creditocr.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;

/**
 * This exception should be thrown by a controller that detects that a client attempted to upload a
 * file with an unsupported type. Throwing this will automatically set the HTTP status code to {@link HttpStatus#UNSUPPORTED_MEDIA_TYPE}
 */
@ResponseStatus(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
public class UnsupportedFileTypeException extends RuntimeException {

    private static final String MESSAGE_FMT = "Supplied type %s does not match one of the following acceptable types %s";

    private String[] acceptableTypes;
    private String suppliedTypes;

    public UnsupportedFileTypeException(String[] acceptableTypes, String suppliedType) {

        this.acceptableTypes = acceptableTypes;
        this.suppliedTypes = suppliedType;

    }

    @Override
    public String getMessage() {
        return String.format(MESSAGE_FMT, suppliedTypes, Arrays.toString(acceptableTypes));
    }
}
