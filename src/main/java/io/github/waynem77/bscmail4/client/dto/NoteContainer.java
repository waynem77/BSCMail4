package io.github.waynem77.bscmail4.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for paginated Note results from the server API.
 */
@Data
@NoArgsConstructor
public class NoteContainer
{
    private List<NoteResponse> elements = new ArrayList<>();
    private int pageNumber = 0;
    private int size = 5;
    private int numberOfElements = 0;
    private int totalPages = 0;
    private long totalElements = 0;
    private boolean isFirst = true;
    private boolean isLast = true;
    private boolean hasPrevious = false;
    private boolean hasNext = false;
}
