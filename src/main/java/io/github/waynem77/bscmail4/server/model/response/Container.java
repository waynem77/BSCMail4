package io.github.waynem77.bscmail4.server.model.response;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
abstract public class Container<E, R>
{
    @Getter
    private List<R> elements;
    @Getter
    private int pageNumber;
    @Getter
    private int size;
    @Getter
    private int numberOfElements;
    @Getter
    private int totalPages;
    @Getter
    private long totalElements;
    @Getter
    private boolean isFirst;
    @Getter
    private boolean isLast;
    private boolean hasPrevious;
    private boolean hasNext;

    public Container(Page<E> page)
    {
        elements = page.getContent().stream().map(this::transformEntityToResponse).collect(Collectors.toList());
        pageNumber = page.getNumber();
        size = page.getSize();
        numberOfElements = page.getNumberOfElements();
        totalPages = page.getTotalPages();
        totalElements = page.getTotalElements();
        isFirst = page.isFirst();
        isLast = page.isLast();
        hasPrevious = page.hasPrevious();
        hasNext = page.hasNext();
    }

    public boolean hasNext()
    {
        return hasNext;
    }

    public boolean hasPrevious()
    {
        return hasPrevious;
    }

    protected abstract R transformEntityToResponse(E entity);
}
