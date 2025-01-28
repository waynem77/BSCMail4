package io.github.waynem77.bscmail4.model.specification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

/**
 * Describes the sort direction of a page.
 */
@Getter
@RequiredArgsConstructor
public enum SortDirection
{
    ASC("ascending", Sort.Direction.ASC),
    DESC("descending", Sort.Direction.DESC);

    private final String value;
    private final Sort.Direction springDirection;

    /**
     * Converts a string to a SortDirection enum.
     *
     * @param value the string
     * @return the equivalent enum
     */
    public static SortDirection fromValue(String value)
    {
        return Arrays.stream(values())
                .filter(sortDirection -> sortDirection.getValue().equals(value))
                .findAny()
                .orElse(null);
    }
}
