package io.github.waynem77.bscmail4.client.service;

import io.github.waynem77.bscmail4.client.dto.CreateNoteRequest;
import io.github.waynem77.bscmail4.client.dto.CreatePersonRequest;
import io.github.waynem77.bscmail4.client.dto.NoteContainer;
import io.github.waynem77.bscmail4.client.dto.NoteResponse;
import io.github.waynem77.bscmail4.client.dto.PersonContainer;
import io.github.waynem77.bscmail4.client.dto.PersonResponse;
import io.github.waynem77.bscmail4.client.dto.UpdatePersonRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service for making REST API calls to the server for Person operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonClientService
{
    private final RestTemplate restTemplate;

    @Value("${server.api.url}")
    private String serverApiUrl;

    /**
     * Creates a new Person by calling the server API.
     *
     * @param request the request containing person data
     * @return the created person as a PersonResponse
     */
    public PersonResponse createPerson(CreatePersonRequest request)
    {
        log.info("Creating person via API. request={}", request);
        String url = serverApiUrl + "/person";
        HttpEntity<CreatePersonRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<PersonResponse> response = restTemplate.postForEntity(url, httpEntity, PersonResponse.class);
        return response.getBody();
    }

    /**
     * Retrieves a Person by ID from the server API.
     *
     * @param personId the ID of the person to retrieve
     * @return the person as a PersonResponse
     */
    public PersonResponse getPersonById(Long personId)
    {
        log.info("Getting person by ID via API. personId={}", personId);
        String url = serverApiUrl + "/person/" + personId;
        ResponseEntity<PersonResponse> response = restTemplate.getForEntity(url, PersonResponse.class);
        return response.getBody();
    }

    /**
     * Retrieves a paginated list of Persons with optional filtering and sorting from the server API.
     *
     * @param page      the page number (0-based)
     * @param size      the page size
     * @param sortBy    the field to sort by (name or emailAddress)
     * @param direction the sort direction (asc or desc)
     * @param isActive  filter by active status (null means no filtering)
     * @param search    filter by search string (null or empty means no filtering)
     * @return a PersonContainer containing the paginated results
     */
    public PersonContainer getPersons(
            int page,
            int size,
            String sortBy,
            String direction,
            Boolean isActive,
            String search)
    {
        log.info("Getting persons via API. page={}, size={}, sortBy={}, direction={}, isActive={}, search={}",
                page, size, sortBy, direction, isActive, search);

        try
        {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverApiUrl + "/person")
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .queryParam("sortBy", sortBy)
                    .queryParam("direction", direction);

            if (isActive != null)
            {
                builder.queryParam("isActive", isActive);
            }

            if (search != null && !search.trim().isEmpty())
            {
                builder.queryParam("search", search);
            }

            String url = builder.toUriString();
            log.debug("Calling server API: {}", url);
            ResponseEntity<PersonContainer> response = restTemplate.getForEntity(url, PersonContainer.class);
            return response.getBody();
        }
        catch (ResourceAccessException e)
        {
            log.error("Failed to connect to server API at {}. Is the server running?", serverApiUrl, e);
            throw new RuntimeException("Unable to connect to server. Please ensure the server is running on port 8080" +
                    ".", e);
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.error("Server API returned error. status={}, response={}", e.getStatusCode(),
                    e.getResponseBodyAsString(), e);
            throw new RuntimeException("Server API error: " + e.getStatusCode(), e);
        }
    }

    /**
     * Updates an existing Person by calling the server API.
     *
     * @param personId the ID of the person to update
     * @param request  the request containing updated person data
     * @return the updated person as a PersonResponse
     */
    public PersonResponse updatePerson(Long personId, UpdatePersonRequest request)
    {
        log.info("Updating person via API. personId={}, request={}", personId, request);
        String url = serverApiUrl + "/person/" + personId;
        HttpEntity<UpdatePersonRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<PersonResponse> response = restTemplate.postForEntity(url, httpEntity, PersonResponse.class);
        return response.getBody();
    }

    /**
     * Toggles the active status of a Person.
     *
     * @param personId the ID of the person to toggle
     * @return the updated person as a PersonResponse
     */
    public PersonResponse toggleActiveStatus(Long personId)
    {
        log.info("Toggling active status via API. personId={}", personId);
        PersonResponse person = getPersonById(personId);
        UpdatePersonRequest request = UpdatePersonRequest.builder()
                .name(person.getName())
                .emailAddress(person.getEmailAddress())
                .phone(person.getPhone())
                .isActive(!person.getIsActive())
                .build();
        return updatePerson(personId, request);
    }

    /**
     * Retrieves a paginated list of Notes for a Person from the server API.
     *
     * @param personId  the ID of the person to retrieve notes for
     * @param page      the page number (0-based)
     * @param size      the page size
     * @param direction the sort direction (asc or desc)
     * @return a NoteContainer containing the paginated results
     */
    public NoteContainer getNotes(Long personId, int page, int size, String direction)
    {
        log.info("Getting notes via API. personId={}, page={}, size={}, direction={}", personId, page, size, direction);

        try
        {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverApiUrl + "/person/" + personId + "/note")
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .queryParam("direction", direction);

            String url = builder.toUriString();
            log.debug("Calling server API: {}", url);
            ResponseEntity<NoteContainer> response = restTemplate.getForEntity(url, NoteContainer.class);
            return response.getBody();
        }
        catch (ResourceAccessException e)
        {
            log.error("Failed to connect to server API at {}. Is the server running?", serverApiUrl, e);
            throw new RuntimeException("Unable to connect to server. Please ensure the server is running.", e);
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.error("Server API returned error. status={}, response={}", e.getStatusCode(),
                    e.getResponseBodyAsString(), e);
            throw new RuntimeException("Server API error: " + e.getStatusCode(), e);
        }
    }

    /**
     * Creates a new Note for a Person by calling the server API.
     *
     * @param personId the ID of the person to create the note for
     * @param request  the request containing note data
     * @return the created note as a NoteResponse
     */
    public NoteResponse createNote(Long personId, CreateNoteRequest request)
    {
        log.info("Creating note via API. personId={}, request={}", personId, request);

        try
        {
            String url = serverApiUrl + "/person/" + personId + "/note";
            HttpEntity<CreateNoteRequest> httpEntity = new HttpEntity<>(request);
            ResponseEntity<NoteResponse> response = restTemplate.postForEntity(url, httpEntity, NoteResponse.class);
            return response.getBody();
        }
        catch (ResourceAccessException e)
        {
            log.error("Failed to connect to server API at {}. Is the server running?", serverApiUrl, e);
            throw new RuntimeException("Unable to connect to server. Please ensure the server is running.", e);
        }
        catch (HttpClientErrorException | HttpServerErrorException e)
        {
            log.error("Server API returned error. status={}, response={}", e.getStatusCode(),
                    e.getResponseBodyAsString(), e);
            throw new RuntimeException("Server API error: " + e.getStatusCode(), e);
        }
    }
}

