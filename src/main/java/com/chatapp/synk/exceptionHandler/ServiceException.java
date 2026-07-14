package com.chatapp.synk.exceptionHandler;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Constructs a new service exception with a default 500 status.
     *
     * @param message the detail message
     */
    public ServiceException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Constructs a new service exception with a cause and default 500 status.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Constructs a new service exception with an explicit HTTP status.
     *
     * @param message the detail message
     * @param status  the HTTP status code associated with this exception
     */
    public ServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status associated with this exception, never null.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
