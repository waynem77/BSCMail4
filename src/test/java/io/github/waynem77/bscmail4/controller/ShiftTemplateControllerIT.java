package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.DbCleaner;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.entity.ShiftTemplate;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.repository.ShiftTemplateRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdateShiftTemplateRequest;
import io.github.waynem77.bscmail4.model.response.ShiftTemplateResponse;
import io.github.waynem77.bscmail4.model.response.ShiftTemplatesResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Provides integration tests for {@link ShiftTemplateController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShiftTemplateControllerIT
{
    @LocalServerPort
    String port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    ShiftTemplateRepository shiftTemplateRepository;

    private DbCleaner dbCleaner;

    @BeforeEach
    public void setup()
    {
        dbCleaner = new DbCleaner(jdbcTemplate);
    }

    @AfterEach
    public void cleanup()
    {
        dbCleaner.clean();
    }

    @Test
    public void createShiftTemplateCreatesAShiftTemplate()
    {
        Permission permission = createPermission();

        CreateOrUpdateShiftTemplateRequest request = new CreateOrUpdateShiftTemplateRequest();
        request.setName(randomString());
        request.setRequiredPermissionId(permission.getId());

        ResponseEntity<ShiftTemplateResponse> responseEntity = restTemplate.postForEntity(
                url("/api/shift/template"),
                request,
                ShiftTemplateResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse response = responseEntity.getBody();
        assertThat(response, notNullValue());
        assertThat(response.getId(), notNullValue());
        addDbCleanup("shift_template", response.getId());
        assertThat(response.getName(), equalTo(request.getName()));
        assertThat(response.getRequiredPermissionId(), equalTo(request.getRequiredPermissionId()));

        Optional<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findById(response.getId());
        assertThat(shiftTemplate.isPresent(), equalTo(true));
        assertThat(shiftTemplate.get().getName(), equalTo(request.getName()));
        assertThat(shiftTemplate.get().getRequiredPermission(), equalTo(permission));
    }

    @Test
    public void createShiftTemplateWithoutPermissionCreatesAShiftTemplate()
    {
        CreateOrUpdateShiftTemplateRequest request = new CreateOrUpdateShiftTemplateRequest();
        request.setName(randomString());

        ResponseEntity<ShiftTemplateResponse> responseEntity = restTemplate.postForEntity(
                url("/api/shift/template"),
                request,
                ShiftTemplateResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse response = responseEntity.getBody();
        assertThat(response, notNullValue());
        assertThat(response.getId(), notNullValue());
        addDbCleanup("shift_template", response.getId());
        assertThat(response.getName(), equalTo(request.getName()));
        assertThat(response.getRequiredPermissionId(), nullValue());

        Optional<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findById(response.getId());
        assertThat(shiftTemplate.isPresent(), equalTo(true));
        assertThat(shiftTemplate.get().getName(), equalTo(request.getName()));
        assertThat(shiftTemplate.get().getRequiredPermission(), nullValue());
    }

    @Test
    public void createShiftTemplateReturnsBadRequestWhenRequestIsInvalid()
    {
        CreateOrUpdateShiftTemplateRequest requestWithoutName = new CreateOrUpdateShiftTemplateRequest();

        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutName = restTemplate.postForEntity(
                url("/api/shift/template"),
                requestWithoutName,
                ShiftTemplateResponse.class);

        assertThat(responseEntityWithoutName, notNullValue());
        assertThat(responseEntityWithoutName.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));

        CreateOrUpdateShiftTemplateRequest requestWithBadPermission = new CreateOrUpdateShiftTemplateRequest();
        requestWithBadPermission.setName(randomString());
        requestWithBadPermission.setRequiredPermissionId(randomLong());

        ResponseEntity<ShiftTemplateResponse> responseEntityWithBadPermission = restTemplate.postForEntity(
                url("/api/shift/template"),
                requestWithBadPermission,
                ShiftTemplateResponse.class);

        assertThat(responseEntityWithBadPermission, notNullValue());
        assertThat(responseEntityWithBadPermission.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updateShiftTemplateUpdatesShiftTemplateCorrectly()
    {
        ShiftTemplate originalShiftTemplate = createShiftTemplate();
        Long shiftTemplateId = originalShiftTemplate.getId();

        Permission newPermission = createPermission();

        CreateOrUpdateShiftTemplateRequest request = new CreateOrUpdateShiftTemplateRequest();
        request.setName(randomString());
        request.setRequiredPermissionId(newPermission.getId());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<ShiftTemplateResponse> responseEntity = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntity,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse shiftTemplateResponse = responseEntity.getBody();
        assertThat(shiftTemplateResponse, notNullValue());
        assertThat(shiftTemplateResponse.getId(), equalTo(shiftTemplateId));
        assertThat(shiftTemplateResponse.getName(), equalTo(request.getName()));
        assertThat(shiftTemplateResponse.getRequiredPermissionId(), equalTo(newPermission.getId()));

        Optional<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findById(shiftTemplateId);
        assertThat(shiftTemplate.isPresent(), equalTo(true));
        assertThat(shiftTemplate.get().getName(), equalTo(request.getName()));
        assertThat(shiftTemplate.get().getRequiredPermission(), equalTo(newPermission));


        CreateOrUpdateShiftTemplateRequest requestWithoutPermission = new CreateOrUpdateShiftTemplateRequest();
        requestWithoutPermission.setName(randomString());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithoutPermission = new HttpEntity<>(requestWithoutPermission);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutPermission = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithoutPermission,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithoutPermission, notNullValue());
        assertThat(responseEntityWithoutPermission.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse shiftTemplateResponseWithoutPermission = responseEntityWithoutPermission.getBody();
        assertThat(shiftTemplateResponseWithoutPermission, notNullValue());
        assertThat(shiftTemplateResponseWithoutPermission.getId(), equalTo(shiftTemplateId));
        assertThat(shiftTemplateResponseWithoutPermission.getName(), equalTo(requestWithoutPermission.getName()));
        assertThat(shiftTemplateResponseWithoutPermission.getRequiredPermissionId(), nullValue());

        Optional<ShiftTemplate> shiftTemplateWithoutPermission = shiftTemplateRepository.findById(shiftTemplateId);
        assertThat(shiftTemplateWithoutPermission.isPresent(), equalTo(true));
        assertThat(shiftTemplateWithoutPermission.get().getName(), equalTo(requestWithoutPermission.getName()));
    }

    @Test
    public void updateShiftTemplateCreatesShiftTemplateWhenOriginalDoesNotExist()
    {
        Long shiftTemplateId = randomLong();
        Permission newPermission = createPermission();

        CreateOrUpdateShiftTemplateRequest request = new CreateOrUpdateShiftTemplateRequest();
        request.setName(randomString());
        request.setRequiredPermissionId(newPermission.getId());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<ShiftTemplateResponse> responseEntity = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntity,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse shiftTemplateResponse = responseEntity.getBody();
        assertThat(shiftTemplateResponse, notNullValue());
        assertThat(shiftTemplateResponse.getId(), notNullValue());
        addDbCleanup("shift_template", shiftTemplateResponse.getId());
        assertThat(shiftTemplateResponse.getName(), equalTo(request.getName()));
        assertThat(shiftTemplateResponse.getRequiredPermissionId(), equalTo(newPermission.getId()));

        Optional<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findById(shiftTemplateResponse.getId());
        assertThat(shiftTemplate.isPresent(), equalTo(true));
        assertThat(shiftTemplate.get().getName(), equalTo(request.getName()));
        assertThat(shiftTemplate.get().getRequiredPermission(), equalTo(newPermission));


        Long shiftTemplateIdWithoutPermission = randomLong();

        CreateOrUpdateShiftTemplateRequest requestWithoutPermission = new CreateOrUpdateShiftTemplateRequest();
        requestWithoutPermission.setName(randomString());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithoutPermission = new HttpEntity<>(requestWithoutPermission);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutPermission = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithoutPermission,
                ShiftTemplateResponse.class,
                shiftTemplateIdWithoutPermission);

        assertThat(responseEntityWithoutPermission, notNullValue());
        assertThat(responseEntityWithoutPermission.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse shiftTemplateResponseWithoutPermission = responseEntityWithoutPermission.getBody();
        assertThat(shiftTemplateResponseWithoutPermission, notNullValue());
        assertThat(shiftTemplateResponseWithoutPermission.getId(), notNullValue());
        addDbCleanup("shift_template", shiftTemplateResponseWithoutPermission.getId());
        assertThat(shiftTemplateResponseWithoutPermission.getName(), equalTo(requestWithoutPermission.getName()));

        Optional<ShiftTemplate> shiftTemplateWithoutPermission = shiftTemplateRepository.findById(shiftTemplateResponseWithoutPermission.getId());
        assertThat(shiftTemplateWithoutPermission.isPresent(), equalTo(true));
        assertThat(shiftTemplateWithoutPermission.get().getName(), equalTo(requestWithoutPermission.getName()));
    }

    @Test
    public void updateShiftTemplateReturnsBadRequestWhenRequestIsInvalid()
    {
        Permission permission = createPermission();
        ShiftTemplate originalShiftTemplate = createShiftTemplate();
        Long shiftTemplateId = originalShiftTemplate.getId();

        CreateOrUpdateShiftTemplateRequest requestWithoutName = new CreateOrUpdateShiftTemplateRequest();
        requestWithoutName.setRequiredPermissionId(permission.getId());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithoutName = new HttpEntity<>(requestWithoutName);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutName = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithoutName,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithoutName, notNullValue());
        assertThat(responseEntityWithoutName.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));


        CreateOrUpdateShiftTemplateRequest requestWithBadPermission = new CreateOrUpdateShiftTemplateRequest();
        requestWithBadPermission.setName(randomString());
        requestWithBadPermission.setRequiredPermissionId(randomLong());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithBadPermission = new HttpEntity<>(requestWithBadPermission);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithBadPermission = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithBadPermission,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithBadPermission, notNullValue());
        assertThat(responseEntityWithBadPermission.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updateShiftTemplateReturnsBadRequestWhenOriginalDoesNotExistAndRequestIsInvalid()
    {
        Permission permission = createPermission();
        Long shiftTemplateId = randomLong();

        CreateOrUpdateShiftTemplateRequest requestWithoutName = new CreateOrUpdateShiftTemplateRequest();
        requestWithoutName.setRequiredPermissionId(permission.getId());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithoutName = new HttpEntity<>(requestWithoutName);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutName = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithoutName,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithoutName, notNullValue());
        assertThat(responseEntityWithoutName.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));


        CreateOrUpdateShiftTemplateRequest requestWithBadPermission = new CreateOrUpdateShiftTemplateRequest();
        requestWithBadPermission.setName(randomString());
        requestWithBadPermission.setRequiredPermissionId(randomLong());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithBadPermission = new HttpEntity<>(requestWithBadPermission);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithBadPermission = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithBadPermission,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithBadPermission, notNullValue());
        assertThat(responseEntityWithBadPermission.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void getShiftTemplateByIdReturnsShiftTemplate()
    {
        ShiftTemplate shiftTemplate = createShiftTemplate();

        ResponseEntity<ShiftTemplateResponse> responseEntity = restTemplate.getForEntity(
                url("/api/shift/template/{templateId}"),
                ShiftTemplateResponse.class,
                shiftTemplate.getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse shiftTemplateResponse = responseEntity.getBody();
        validateShiftTemplateResponse(shiftTemplateResponse, shiftTemplate);
    }

    @Test
    public void getShiftTemplateByIdReturnsNotFoundWhenShiftTemplateDoesNotExist()
    {
        Long shiftTemplateId = randomLong();
        ResponseEntity<ShiftTemplateResponse> responseEntity = restTemplate.getForEntity(
                url("/api/shift/template/{templateId}"),
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void deleteShiftTemplateDeletesShiftTemplate()
    {
        List<ShiftTemplate> shiftTemplates = List.of(
                createShiftTemplate(),
                createShiftTemplate(),
                createShiftTemplate());

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                shiftTemplates.get(0).getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(0).getId()).isPresent(), equalTo(false));
        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(1).getId()).isPresent(), equalTo(true));
        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(2).getId()).isPresent(), equalTo(true));
    }

    @Test
    public void deleteShiftTemplateIsIdempotent()
    {
        List<ShiftTemplate> shiftTemplates = List.of(
                createShiftTemplate(),
                createShiftTemplate(),
                createShiftTemplate());

        ResponseEntity<Void> responseEntityForExistingShiftTemplate = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                shiftTemplates.get(0).getId());

        assertThat(responseEntityForExistingShiftTemplate, notNullValue());
        assertThat(responseEntityForExistingShiftTemplate.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(0).getId()).isPresent(), equalTo(false));
        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(1).getId()).isPresent(), equalTo(true));
        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(2).getId()).isPresent(), equalTo(true));

        ResponseEntity<Void> responseEntityForDeletedShiftTemplate = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                shiftTemplates.get(0).getId());

        assertThat(responseEntityForDeletedShiftTemplate, notNullValue());
        assertThat(responseEntityForDeletedShiftTemplate.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(0).getId()).isPresent(), equalTo(false));
        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(1).getId()).isPresent(), equalTo(true));
        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(2).getId()).isPresent(), equalTo(true));

        ResponseEntity<Void> responseEntityForNonexistentShiftTemplate = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                shiftTemplates.get(0).getId());

        assertThat(responseEntityForNonexistentShiftTemplate, notNullValue());
        assertThat(responseEntityForNonexistentShiftTemplate.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(0).getId()).isPresent(), equalTo(false));
        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(1).getId()).isPresent(), equalTo(true));
        assertThat(shiftTemplateRepository.findById(shiftTemplates.get(2).getId()).isPresent(), equalTo(true));
    }

    @Test
    public void getAllShiftTemplatesReturnsTheCorrectValue()
    {
        shiftTemplateRepository.deleteAll();

        List<ShiftTemplate> shiftTemplates = List.of(
                createShiftTemplateWithPrefix("a"),
                createShiftTemplateWithPrefix("d"),
                createShiftTemplateWithPrefix("e"),
                createShiftTemplateWithPrefix("b"),
                createShiftTemplateWithPrefix("c"));
        List<ShiftTemplateResponse> shiftTemplateResponses = shiftTemplates.stream()
                .map(ShiftTemplateResponse::fromShiftTemplate)
                .toList();

        ResponseEntity<ShiftTemplatesResponse> defaultValuesResponseEntity = restTemplate.getForEntity(
                url("/api/shift/template"),
                ShiftTemplatesResponse.class);
        assertThat(defaultValuesResponseEntity, notNullValue());
        assertThat(defaultValuesResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplatesResponse defaultValuesPage = defaultValuesResponseEntity.getBody();
        assertThat(defaultValuesPage, notNullValue());
        assertThat(defaultValuesPage.getPageInfo(), notNullValue());
        assertThat(defaultValuesPage.getPageInfo().getNumber(), equalTo(0));
        assertThat(defaultValuesPage.getPageInfo().getSize(), equalTo(25));
        assertThat(defaultValuesPage.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(defaultValuesPage.getPageInfo().isFirst(), equalTo(true));
        assertThat(defaultValuesPage.getPageInfo().isLast(), equalTo(true));
        assertThat(defaultValuesPage.getContent(), equalTo(List.of(
                shiftTemplateResponses.get(0),
                shiftTemplateResponses.get(3),
                shiftTemplateResponses.get(4),
                shiftTemplateResponses.get(1),
                shiftTemplateResponses.get(2))));

        ResponseEntity<ShiftTemplatesResponse> page0ResponseEntity = restTemplate.getForEntity(
                url("/api/shift/template?page=0&size=2"),
                ShiftTemplatesResponse.class);
        assertThat(page0ResponseEntity, notNullValue());
        assertThat(page0ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplatesResponse page0 = page0ResponseEntity.getBody();
        assertThat(page0, notNullValue());
        assertThat(page0.getPageInfo(), notNullValue());
        assertThat(page0.getPageInfo().getNumber(), equalTo(0));
        assertThat(page0.getPageInfo().getSize(), equalTo(2));
        assertThat(page0.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page0.getPageInfo().isFirst(), equalTo(true));
        assertThat(page0.getPageInfo().isLast(), equalTo(false));
        assertThat(page0.getContent(), equalTo(List.of(
                shiftTemplateResponses.get(0),
                shiftTemplateResponses.get(3))));

        ResponseEntity<ShiftTemplatesResponse> page1ResponseEntity = restTemplate.getForEntity(
                url("/api/shift/template?page=1&size=2"),
                ShiftTemplatesResponse.class);
        assertThat(page1ResponseEntity, notNullValue());
        assertThat(page1ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplatesResponse page1 = page1ResponseEntity.getBody();
        assertThat(page1, notNullValue());
        assertThat(page1.getPageInfo(), notNullValue());
        assertThat(page1.getPageInfo().getNumber(), equalTo(1));
        assertThat(page1.getPageInfo().getSize(), equalTo(2));
        assertThat(page1.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page1.getPageInfo().isFirst(), equalTo(false));
        assertThat(page1.getPageInfo().isLast(), equalTo(false));
        assertThat(page1.getContent(), equalTo(List.of(
                shiftTemplateResponses.get(4),
                shiftTemplateResponses.get(1))));

        ResponseEntity<ShiftTemplatesResponse> page2ResponseEntity = restTemplate.getForEntity(
                url("/api/shift/template?page=2&size=2"),
                ShiftTemplatesResponse.class);
        assertThat(page2ResponseEntity, notNullValue());
        assertThat(page2ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplatesResponse page2 = page2ResponseEntity.getBody();
        assertThat(page2, notNullValue());
        assertThat(page2.getPageInfo(), notNullValue());
        assertThat(page2.getPageInfo().getNumber(), equalTo(2));
        assertThat(page2.getPageInfo().getSize(), equalTo(2));
        assertThat(page2.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page2.getPageInfo().isFirst(), equalTo(false));
        assertThat(page2.getPageInfo().isLast(), equalTo(true));
        assertThat(page2.getContent(), equalTo(List.of(
                shiftTemplateResponses.get(2))));
    }

    private String url(String endpoint)
    {
        return "http://localhost:" + port + endpoint;
    }

    private <T> void addDbCleanup(String table, T id)
    {
        dbCleaner.addCleanup(table, id);
    }

    private ShiftTemplate createShiftTemplate()
    {
        return createShiftTemplate(randomString());
    }

    private ShiftTemplate createShiftTemplateWithPrefix(String prefix)
    {
        return createShiftTemplate(randomStringWithPrefix(prefix));
    }

    private ShiftTemplate createShiftTemplate(String name)
    {
        Permission permission = createPermission();

        ShiftTemplate shiftTemplate = new ShiftTemplate();
        shiftTemplate.setName(name);
        shiftTemplate.setRequiredPermission(permission);
        shiftTemplate = shiftTemplateRepository.save(shiftTemplate);
        addDbCleanup("shift_template", shiftTemplate.getId());

        return shiftTemplate;
    }

    private Permission createPermission()
    {
        Permission permission = new Permission();
        permission.setName(randomString());
        permission = permissionRepository.save(permission);
        addDbCleanup("permission", permission.getId());

        return permission;
    }

    private void validateShiftTemplateResponse(ShiftTemplateResponse response, ShiftTemplate shiftTemplate)
    {
        if (shiftTemplate == null) {
            assertThat(response, nullValue());
            return;
        }

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(shiftTemplate.getId()));
        assertThat(response.getName(), equalTo(shiftTemplate.getName()));
        if (shiftTemplate.getRequiredPermission() == null) {
            assertThat(response.getRequiredPermissionId(), nullValue());
        } else {
            assertThat(response.getRequiredPermissionId(), equalTo(shiftTemplate.getRequiredPermission().getId()));
        }
    }
}