package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.DbCleaner;
import io.github.waynem77.bscmail4.model.entity.Role;
import io.github.waynem77.bscmail4.model.entity.ShiftTemplate;
import io.github.waynem77.bscmail4.model.repository.RoleRepository;
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
    RoleRepository roleRepository;

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
        Role role = createRole();

        CreateOrUpdateShiftTemplateRequest request = new CreateOrUpdateShiftTemplateRequest();
        request.setName(randomString());
        request.setRequiredRoleId(role.getId());

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
        assertThat(response.getRequiredRoleId(), equalTo(request.getRequiredRoleId()));

        Optional<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findById(response.getId());
        assertThat(shiftTemplate.isPresent(), equalTo(true));
        assertThat(shiftTemplate.get().getName(), equalTo(request.getName()));
        assertThat(shiftTemplate.get().getRequiredRole(), equalTo(role));
    }

    @Test
    public void createShiftTemplateWithoutRoleCreatesAShiftTemplate()
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
        assertThat(response.getRequiredRoleId(), nullValue());

        Optional<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findById(response.getId());
        assertThat(shiftTemplate.isPresent(), equalTo(true));
        assertThat(shiftTemplate.get().getName(), equalTo(request.getName()));
        assertThat(shiftTemplate.get().getRequiredRole(), nullValue());
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

        CreateOrUpdateShiftTemplateRequest requestWithBadRole = new CreateOrUpdateShiftTemplateRequest();
        requestWithBadRole.setName(randomString());
        requestWithBadRole.setRequiredRoleId(randomLong());

        ResponseEntity<ShiftTemplateResponse> responseEntityWithBadRole = restTemplate.postForEntity(
                url("/api/shift/template"),
                requestWithBadRole,
                ShiftTemplateResponse.class);

        assertThat(responseEntityWithBadRole, notNullValue());
        assertThat(responseEntityWithBadRole.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updateShiftTemplateUpdatesShiftTemplateCorrectly()
    {
        ShiftTemplate originalShiftTemplate = createShiftTemplate();
        Long shiftTemplateId = originalShiftTemplate.getId();

        Role newRole = createRole();

        CreateOrUpdateShiftTemplateRequest request = new CreateOrUpdateShiftTemplateRequest();
        request.setName(randomString());
        request.setRequiredRoleId(newRole.getId());

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
        assertThat(shiftTemplateResponse.getRequiredRoleId(), equalTo(newRole.getId()));

        Optional<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findById(shiftTemplateId);
        assertThat(shiftTemplate.isPresent(), equalTo(true));
        assertThat(shiftTemplate.get().getName(), equalTo(request.getName()));
        assertThat(shiftTemplate.get().getRequiredRole(), equalTo(newRole));


        CreateOrUpdateShiftTemplateRequest requestWithoutRole = new CreateOrUpdateShiftTemplateRequest();
        requestWithoutRole.setName(randomString());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithoutRole = new HttpEntity<>(requestWithoutRole);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutRole = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithoutRole,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithoutRole, notNullValue());
        assertThat(responseEntityWithoutRole.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse shiftTemplateResponseWithoutRole = responseEntityWithoutRole.getBody();
        assertThat(shiftTemplateResponseWithoutRole, notNullValue());
        assertThat(shiftTemplateResponseWithoutRole.getId(), equalTo(shiftTemplateId));
        assertThat(shiftTemplateResponseWithoutRole.getName(), equalTo(requestWithoutRole.getName()));
        assertThat(shiftTemplateResponseWithoutRole.getRequiredRoleId(), nullValue());

        Optional<ShiftTemplate> shiftTemplateWithoutRole = shiftTemplateRepository.findById(shiftTemplateId);
        assertThat(shiftTemplateWithoutRole.isPresent(), equalTo(true));
        assertThat(shiftTemplateWithoutRole.get().getName(), equalTo(requestWithoutRole.getName()));
    }

    @Test
    public void updateShiftTemplateCreatesShiftTemplateWhenOriginalDoesNotExist()
    {
        Long shiftTemplateId = randomLong();
        Role newRole = createRole();

        CreateOrUpdateShiftTemplateRequest request = new CreateOrUpdateShiftTemplateRequest();
        request.setName(randomString());
        request.setRequiredRoleId(newRole.getId());

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
        assertThat(shiftTemplateResponse.getRequiredRoleId(), equalTo(newRole.getId()));

        Optional<ShiftTemplate> shiftTemplate = shiftTemplateRepository.findById(shiftTemplateResponse.getId());
        assertThat(shiftTemplate.isPresent(), equalTo(true));
        assertThat(shiftTemplate.get().getName(), equalTo(request.getName()));
        assertThat(shiftTemplate.get().getRequiredRole(), equalTo(newRole));


        Long shiftTemplateIdWithoutRole = randomLong();

        CreateOrUpdateShiftTemplateRequest requestWithoutRole = new CreateOrUpdateShiftTemplateRequest();
        requestWithoutRole.setName(randomString());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithoutRole = new HttpEntity<>(requestWithoutRole);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutRole = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithoutRole,
                ShiftTemplateResponse.class,
                shiftTemplateIdWithoutRole);

        assertThat(responseEntityWithoutRole, notNullValue());
        assertThat(responseEntityWithoutRole.getStatusCode().is2xxSuccessful(), equalTo(true));

        ShiftTemplateResponse shiftTemplateResponseWithoutRole = responseEntityWithoutRole.getBody();
        assertThat(shiftTemplateResponseWithoutRole, notNullValue());
        assertThat(shiftTemplateResponseWithoutRole.getId(), notNullValue());
        addDbCleanup("shift_template", shiftTemplateResponseWithoutRole.getId());
        assertThat(shiftTemplateResponseWithoutRole.getName(), equalTo(requestWithoutRole.getName()));

        Optional<ShiftTemplate> shiftTemplateWithoutRole = shiftTemplateRepository.findById(shiftTemplateResponseWithoutRole.getId());
        assertThat(shiftTemplateWithoutRole.isPresent(), equalTo(true));
        assertThat(shiftTemplateWithoutRole.get().getName(), equalTo(requestWithoutRole.getName()));
    }

    @Test
    public void updateShiftTemplateReturnsBadRequestWhenRequestIsInvalid()
    {
        Role role = createRole();
        ShiftTemplate originalShiftTemplate = createShiftTemplate();
        Long shiftTemplateId = originalShiftTemplate.getId();

        CreateOrUpdateShiftTemplateRequest requestWithoutName = new CreateOrUpdateShiftTemplateRequest();
        requestWithoutName.setRequiredRoleId(role.getId());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithoutName = new HttpEntity<>(requestWithoutName);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutName = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithoutName,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithoutName, notNullValue());
        assertThat(responseEntityWithoutName.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));


        CreateOrUpdateShiftTemplateRequest requestWithBadRole = new CreateOrUpdateShiftTemplateRequest();
        requestWithBadRole.setName(randomString());
        requestWithBadRole.setRequiredRoleId(randomLong());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithBadRole = new HttpEntity<>(requestWithBadRole);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithBadRole = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithBadRole,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithBadRole, notNullValue());
        assertThat(responseEntityWithBadRole.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updateShiftTemplateReturnsBadRequestWhenOriginalDoesNotExistAndRequestIsInvalid()
    {
        Role role = createRole();
        Long shiftTemplateId = randomLong();

        CreateOrUpdateShiftTemplateRequest requestWithoutName = new CreateOrUpdateShiftTemplateRequest();
        requestWithoutName.setRequiredRoleId(role.getId());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithoutName = new HttpEntity<>(requestWithoutName);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithoutName = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithoutName,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithoutName, notNullValue());
        assertThat(responseEntityWithoutName.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));


        CreateOrUpdateShiftTemplateRequest requestWithBadRole = new CreateOrUpdateShiftTemplateRequest();
        requestWithBadRole.setName(randomString());
        requestWithBadRole.setRequiredRoleId(randomLong());

        HttpEntity<CreateOrUpdateShiftTemplateRequest> httpEntityWithBadRole = new HttpEntity<>(requestWithBadRole);
        ResponseEntity<ShiftTemplateResponse> responseEntityWithBadRole = restTemplate.exchange(
                url("/api/shift/template/{templateId}"),
                HttpMethod.PUT,
                httpEntityWithBadRole,
                ShiftTemplateResponse.class,
                shiftTemplateId);

        assertThat(responseEntityWithBadRole, notNullValue());
        assertThat(responseEntityWithBadRole.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
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
        Role role = createRole();

        ShiftTemplate shiftTemplate = new ShiftTemplate();
        shiftTemplate.setName(name);
        shiftTemplate.setRequiredRole(role);
        shiftTemplate = shiftTemplateRepository.save(shiftTemplate);
        addDbCleanup("shift_template", shiftTemplate.getId());

        return shiftTemplate;
    }

    private Role createRole()
    {
        Role role = new Role();
        role.setName(randomString());
        role = roleRepository.save(role);
        addDbCleanup("role", role.getId());

        return role;
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
        if (shiftTemplate.getRequiredRole() == null) {
            assertThat(response.getRequiredRoleId(), nullValue());
        } else {
            assertThat(response.getRequiredRoleId(), equalTo(shiftTemplate.getRequiredRole().getId()));
        }
    }
}