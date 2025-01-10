package io.github.waynem77.bscmail4.exception;

/**
 * Indicates that a resource was not found.
 */
public class NotFoundException extends RuntimeException
{
    public NotFoundException(String message)
    {
        super(message);
    }

    public NotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
