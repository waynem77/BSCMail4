package io.github.waynem77.bscmail4.server.config;

import io.github.waynem77.bscmail4.server.service.BadRequestException;
import io.github.waynem77.bscmail4.server.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Controller advice for handling service layer exceptions.
 */
@ControllerAdvice
public class ServiceControllerAdvice
{
    /**
     * Handles NotFoundException and returns a 404 NOT FOUND response.
     *
     * @param exception the NotFoundException that was thrown
     * @return a ResponseEntity with HTTP status 404 NOT FOUND
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    /**
     * Handles BadRequestException and returns a 400 BAD REQUEST response.
     *
     * @param exception the BadRequestException that was thrown
     * @return a ResponseEntity with HTTP status 400 BAD REQUEST
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}

