package io.github.waynem77.bscmail4.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * A wrapper for a Spring Page of {@link ShiftTemplateResponse}s.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShiftTemplatesResponse extends ResponsePage<ShiftTemplateResponse>
{
    public ShiftTemplatesResponse(Page<ShiftTemplateResponse> page)
    {
        super(page);
    }
}
