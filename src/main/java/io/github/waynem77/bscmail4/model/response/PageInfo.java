package io.github.waynem77.bscmail4.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * A summary of page info from a Spring Page object.
 */
@Data
@NoArgsConstructor
public class PageInfo
{
    private int size;
    private int number;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;

    public <T> PageInfo(Page<T> page)
    {
        size = page.getSize();
        number = page.getNumber();
        totalElements = page.getTotalElements();
        totalPages = page.getTotalPages();
        isFirst = page.isFirst();
        isLast = page.isLast();
    }
}
