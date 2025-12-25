package io.github.waynem77.bscmail4.server.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration of valid sort fields for Person entities.
 */
@RequiredArgsConstructor
@Getter
public enum PersonSortBy
{
    /**
     * Sort by person name.
     */
    NAME("name"),

    /**
     * Sort by email address.
     */
    EMAIL_ADDRESS("emailAddress"),

    /**
     * Sort by creation timestamp.
     */
    CREATED_AT("createdAt");

    private final String value;

    /**
     * Converts a string to a PersonSortBy enum value.
     * If the string does not match any valid enum value, an empty Optional is returned.
     *
     * @param value the string value to convert (e.g., "name", "emailAddress", "createdAt")
     * @return an Optional containing the corresponding PersonSortBy enum value, or empty if invalid
     */
    public static Optional<PersonSortBy> fromString(String value)
    {
        return Arrays.stream(values()).filter(dir -> dir.getValue().equalsIgnoreCase(value)).findFirst();
    }

    /**
     * Returns the database field name for this sort field.
     *
     * @return the database field name (e.g., "name", "emailAddress", "createdAt")
     */
    public String getFieldName()
    {
        return value;
    }
}

