package io.github.waynem77.bscmail4.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * A wrapper for a Spring Page of {@link PermissionResponse}s.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PermissionsResponse extends ResponsePage<PermissionResponse>
{
    public PermissionsResponse(Page<PermissionResponse> page)
    {
        super(page);
    }
}
