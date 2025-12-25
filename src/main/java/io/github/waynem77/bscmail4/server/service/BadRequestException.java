package io.github.waynem77.bscmail4.server.service;

/**
 * Exception thrown when a request contains invalid parameters or data.
 */
public class BadRequestException extends RuntimeException
{
    /**
     * Constructs a new BadRequestException with the specified detail message.
     *
     * @param message the detail message
     */
    public BadRequestException(String message)
    {
        super(message);
    }
}

