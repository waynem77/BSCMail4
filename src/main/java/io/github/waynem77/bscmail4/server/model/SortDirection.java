package io.github.waynem77.bscmail4.server.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration of valid sort directions.
 */
@RequiredArgsConstructor
@Getter
public enum SortDirection
{
    /**
     * Sort in ascending order.
     */
    ASC("asc"),

    /**
     * Sort in descending order.
     */
    DESC("desc");

    private final String value;

    /**
     * Converts a string to a SortDirection enum value.
     * The string is matched case-insensitively.
     * If the string does not match any valid enum value, an empty Optional is returned.
     *
     * @param value the string value to convert (e.g., "asc", "desc")
     * @return the corresponding SortDirection enum value
     */
    public static Optional<SortDirection> fromString(String value)
    {
        return Arrays.stream(values()).filter(dir -> dir.getValue().equalsIgnoreCase(value)).findFirst();
    }

    /**
     * Converts this SortDirection to Spring Data's Sort.Direction.
     *
     * @return the corresponding Spring Data Sort.Direction
     */
    public org.springframework.data.domain.Sort.Direction toSpringSortDirection()
    {
        return switch (this)
        {
            case ASC -> org.springframework.data.domain.Sort.Direction.ASC;
            case DESC -> org.springframework.data.domain.Sort.Direction.DESC;
        };
    }
}

