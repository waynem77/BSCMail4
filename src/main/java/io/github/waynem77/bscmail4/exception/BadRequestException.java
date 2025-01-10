package io.github.waynem77.bscmail4.exception;

/**
 * Indicates that a request is invalid.
 */
public class BadRequestException extends RuntimeException
{
    public BadRequestException(String message)
    {
        super(message);
    }

    public BadRequestException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
