package io.github.waynem77.bscmail4.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A wrapper for a Spring Page object, suitable for returning as an API response.
 *
 * @param <T> the type of object content
 */
@Data
@NoArgsConstructor
public class ResponsePage<T>
{
    private List<T> content;
    private PageInfo pageInfo;

    public ResponsePage(Page<T> page)
    {
        this.content = page.getContent();
        this.pageInfo = new PageInfo(page);
    }
}
