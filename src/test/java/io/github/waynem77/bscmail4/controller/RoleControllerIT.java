package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.DbCleaner;
import io.github.waynem77.bscmail4.model.entity.Role;
import io.github.waynem77.bscmail4.model.repository.RoleRepository;
import io.github.waynem77.bscmail4.model.request.CreateRoleRequest;
import io.github.waynem77.bscmail4.model.response.RoleResponse;
import io.github.waynem77.bscmail4.model.response.RolesResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
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
 * Provides integration tests for the {@link RoleController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleControllerIT
{
    @LocalServerPort
    String port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

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
    public void getAllRolesReturnsTheCorrectValue()
    {
        List<Role> roles = List.of(
                createRoleWithPrefix("a"),
                createRoleWithPrefix("d"),
                createRoleWithPrefix("e"),
                createRoleWithPrefix("b"),
                createRoleWithPrefix("c"));
        List<RoleResponse> roleResponses = roles.stream()
                .map(RoleResponse::fromRole)
                .toList();

        ResponseEntity<RolesResponse> defaultValuesResponseEntity = testRestTemplate.getForEntity(
                url("/api/role"),
                RolesResponse.class);
        assertThat(defaultValuesResponseEntity, notNullValue());
        assertThat(defaultValuesResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        RolesResponse defaultValuesPage = defaultValuesResponseEntity.getBody();
        assertThat(defaultValuesPage, notNullValue());
        assertThat(defaultValuesPage.getPageInfo(), notNullValue());
        assertThat(defaultValuesPage.getPageInfo().getNumber(), equalTo(0));
        assertThat(defaultValuesPage.getPageInfo().getSize(), equalTo(25));
        assertThat(defaultValuesPage.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(defaultValuesPage.getPageInfo().isFirst(), equalTo(true));
        assertThat(defaultValuesPage.getPageInfo().isLast(), equalTo(true));
        assertThat(defaultValuesPage.getContent(), equalTo(List.of(
                roleResponses.get(0),
                roleResponses.get(3),
                roleResponses.get(4),
                roleResponses.get(1),
                roleResponses.get(2))));

        ResponseEntity<RolesResponse> page0ResponseEntity = testRestTemplate.getForEntity(
                url("/api/role?page=0&size=2"),
                RolesResponse.class);
        assertThat(page0ResponseEntity, notNullValue());
        assertThat(page0ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        RolesResponse page0 = page0ResponseEntity.getBody();
        assertThat(page0, notNullValue());
        assertThat(page0.getPageInfo(), notNullValue());
        assertThat(page0.getPageInfo().getNumber(), equalTo(0));
        assertThat(page0.getPageInfo().getSize(), equalTo(2));
        assertThat(page0.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page0.getPageInfo().isFirst(), equalTo(true));
        assertThat(page0.getPageInfo().isLast(), equalTo(false));
        assertThat(page0.getContent(), equalTo(List.of(
                roleResponses.get(0),
                roleResponses.get(3))));

        ResponseEntity<RolesResponse> page1ResponseEntity = testRestTemplate.getForEntity(
                url("/api/role?page=1&size=2"),
                RolesResponse.class);
        assertThat(page1ResponseEntity, notNullValue());
        assertThat(page1ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        RolesResponse page1 = page1ResponseEntity.getBody();
        assertThat(page1, notNullValue());
        assertThat(page1.getPageInfo(), notNullValue());
        assertThat(page1.getPageInfo().getNumber(), equalTo(1));
        assertThat(page1.getPageInfo().getSize(), equalTo(2));
        assertThat(page1.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page1.getPageInfo().isFirst(), equalTo(false));
        assertThat(page1.getPageInfo().isLast(), equalTo(false));
        assertThat(page1.getContent(), equalTo(List.of(
                roleResponses.get(4),
                roleResponses.get(1))));

        ResponseEntity<RolesResponse> page2ResponseEntity = testRestTemplate.getForEntity(
                url("/api/role?page=2&size=2"),
                RolesResponse.class);
        assertThat(page2ResponseEntity, notNullValue());
        assertThat(page2ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        RolesResponse page2 = page2ResponseEntity.getBody();
        assertThat(page2, notNullValue());
        assertThat(page2.getPageInfo(), notNullValue());
        assertThat(page2.getPageInfo().getNumber(), equalTo(2));
        assertThat(page2.getPageInfo().getSize(), equalTo(2));
        assertThat(page2.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page2.getPageInfo().isFirst(), equalTo(false));
        assertThat(page2.getPageInfo().isLast(), equalTo(true));
        assertThat(page2.getContent(), equalTo(List.of(
                roleResponses.get(2))));
    }

    @Test
    public void createRoleAddsARole()
    {
        CreateRoleRequest request = new CreateRoleRequest()
                .withName(randomString());

        ResponseEntity<RoleResponse> responseEntity = testRestTemplate.postForEntity(
                url("/api/role"),
                request,
                RoleResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        RoleResponse roleResponse = responseEntity.getBody();
        assertThat(roleResponse, notNullValue());
        assertThat(roleResponse.getId(), notNullValue());
        addDbCleanup("role", roleResponse.getId());
        assertThat(roleResponse.getName(), equalTo(request.getName()));

        Optional<Role> role = roleRepository.findById(roleResponse.getId());
        assertThat(role.isPresent(), equalTo(true));
        assertThat(role.get().getName(), equalTo(request.getName()));
    }

    @Test
    public void createRoleReturnsBadRequestWhenRequstIsInvalid()
    {
        CreateRoleRequest request = new CreateRoleRequest();

        ResponseEntity<RoleResponse> responseEntity = testRestTemplate.postForEntity(
                url("/api/role"),
                request,
                RoleResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void createRoleReturnsBadRequestWhenRoleNameIsNotUnique()
    {
        CreateRoleRequest request = new CreateRoleRequest()
                .withName(randomString());

        ResponseEntity<RoleResponse> originalResponseEntity = testRestTemplate.postForEntity(
                url("/api/role"),
                request,
                RoleResponse.class,
                request);

        assertThat(originalResponseEntity, notNullValue());
        assertThat(originalResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
        addDbCleanup("role", originalResponseEntity.getBody().getId());

        ResponseEntity<RoleResponse> duplicateResponseEntity = testRestTemplate.postForEntity(
                url("/api/role"),
                request,
                RoleResponse.class,
                request);

        assertThat(duplicateResponseEntity, notNullValue());
        assertThat(duplicateResponseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void getRoleByIdReturnsRole()
    {
        Role role = createRandomRoleInDb();

        ResponseEntity<RoleResponse> responseEntity = testRestTemplate.getForEntity(
                url("/api/role/{roleId}"),
                RoleResponse.class,
                role.getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        RoleResponse roleResponse = responseEntity.getBody();
        verifyRoleReponse(roleResponse, role);
    }

    @Test
    public void getRoleByIdReturnsNotFoundWhenRoleDoesNotExist()
    {
        Long roleId = randomLong();
        ResponseEntity<RoleResponse> responseEntity = testRestTemplate.getForEntity(
                url("/api/role/{roleId}"),
                RoleResponse.class,
                roleId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void deleteRoleDeletesRole()
    {
        List<Role> roles = List.of(
                createRandomRoleInDb(),
                createRandomRoleInDb(),
                createRandomRoleInDb());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                url("/api/role/{roleId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                roles.get(0).getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(roleRepository.findById(roles.get(0).getId()).isPresent(), equalTo(false));
        assertThat(roleRepository.findById(roles.get(1).getId()).isPresent(), equalTo(true));
        assertThat(roleRepository.findById(roles.get(2).getId()).isPresent(), equalTo(true));
    }

    @Test
    public void deleteRoleIsIdempotent()
    {
        List<Role> roles = List.of(
                createRandomRoleInDb(),
                createRandomRoleInDb(),
                createRandomRoleInDb());

        ResponseEntity<Void> responseEntityForExistingRole = testRestTemplate.exchange(
                url("/api/role/{roleId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                roles.get(0).getId());

        assertThat(responseEntityForExistingRole, notNullValue());
        assertThat(responseEntityForExistingRole.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(roleRepository.findById(roles.get(0).getId()).isPresent(), equalTo(false));
        assertThat(roleRepository.findById(roles.get(1).getId()).isPresent(), equalTo(true));
        assertThat(roleRepository.findById(roles.get(2).getId()).isPresent(), equalTo(true));

        ResponseEntity<Void> responseEntityForDeletedRole = testRestTemplate.exchange(
                url("/api/role/{roleId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                roles.get(0).getId());

        assertThat(responseEntityForDeletedRole, notNullValue());
        assertThat(responseEntityForDeletedRole.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(roleRepository.findById(roles.get(0).getId()).isPresent(), equalTo(false));
        assertThat(roleRepository.findById(roles.get(1).getId()).isPresent(), equalTo(true));
        assertThat(roleRepository.findById(roles.get(2).getId()).isPresent(), equalTo(true));

        ResponseEntity<Void> responseEntityForNonexistentRole = testRestTemplate.exchange(
                url("/api/role/{roleId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                roles.get(0).getId());

        assertThat(responseEntityForNonexistentRole, notNullValue());
        assertThat(responseEntityForNonexistentRole.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(roleRepository.findById(roles.get(0).getId()).isPresent(), equalTo(false));
        assertThat(roleRepository.findById(roles.get(1).getId()).isPresent(), equalTo(true));
        assertThat(roleRepository.findById(roles.get(2).getId()).isPresent(), equalTo(true));
    }


    private String url(String endpoint)
    {
        return "http://localhost:" + port + endpoint;
    }

    private <T> void addDbCleanup(String table, T id)
    {
        dbCleaner.addCleanup(table, id);
    }

    private void verifyRoleReponse(RoleResponse received, Role expected)
    {
        if (expected == null)
        {
            assertThat(received, nullValue());
            return;
        }

        assertThat(received, notNullValue());
        assertThat(received.getId(), equalTo(expected.getId()));
        assertThat(received.getName(), equalTo(expected.getName()));
    }

    private Role createRandomRoleInDb()
    {
        Role role = new Role();
        role.setName(randomString());
        role = roleRepository.save(role);
        addDbCleanup("role", role.getId());

        return role;
    }

    private Role createRoleWithPrefix(String prefix)
    {
        Role role = new Role();
        role.setName(randomStringWithPrefix(prefix));
        role = roleRepository.save(role);
        addDbCleanup("role", role.getId());

        return role;
    }
}