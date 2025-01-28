package io.github.waynem77.bscmail4.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * A wrapper for a Spring Page of {@link GroupResponse}s.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupsResponse extends ResponsePage<GroupResponse>
{
    public GroupsResponse(Page<GroupResponse> page)
    {
        super(page);
    }
}
