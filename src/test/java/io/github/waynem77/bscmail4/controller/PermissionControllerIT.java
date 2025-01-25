package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.DbCleaner;
import io.github.waynem77.bscmail4.model.entity.Permission;
import io.github.waynem77.bscmail4.model.repository.PermissionRepository;
import io.github.waynem77.bscmail4.model.request.CreatePermissionRequest;
import io.github.waynem77.bscmail4.model.response.PermissionResponse;
import io.github.waynem77.bscmail4.model.response.PermissionsResponse;
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
 * Provides integration tests for the {@link PermissionController}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PermissionControllerIT
{
    @LocalServerPort
    String port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PermissionRepository permissionRepository;

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
    public void getAllPermissionsReturnsTheCorrectValue()
    {
        List<Permission> permissions = List.of(
                createPermissionWithPrefix("a"),
                createPermissionWithPrefix("d"),
                createPermissionWithPrefix("e"),
                createPermissionWithPrefix("b"),
                createPermissionWithPrefix("c"));
        List<PermissionResponse> permissionRespons = permissions.stream()
                .map(PermissionResponse::fromPermission)
                .toList();

        ResponseEntity<PermissionsResponse> defaultValuesResponseEntity = testRestTemplate.getForEntity(
                url("/api/permission"),
                PermissionsResponse.class);
        assertThat(defaultValuesResponseEntity, notNullValue());
        assertThat(defaultValuesResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PermissionsResponse defaultValuesPage = defaultValuesResponseEntity.getBody();
        assertThat(defaultValuesPage, notNullValue());
        assertThat(defaultValuesPage.getPageInfo(), notNullValue());
        assertThat(defaultValuesPage.getPageInfo().getNumber(), equalTo(0));
        assertThat(defaultValuesPage.getPageInfo().getSize(), equalTo(25));
        assertThat(defaultValuesPage.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(defaultValuesPage.getPageInfo().isFirst(), equalTo(true));
        assertThat(defaultValuesPage.getPageInfo().isLast(), equalTo(true));
        assertThat(defaultValuesPage.getContent(), equalTo(List.of(
                permissionRespons.get(0),
                permissionRespons.get(3),
                permissionRespons.get(4),
                permissionRespons.get(1),
                permissionRespons.get(2))));

        ResponseEntity<PermissionsResponse> page0ResponseEntity = testRestTemplate.getForEntity(
                url("/api/permission?page=0&size=2"),
                PermissionsResponse.class);
        assertThat(page0ResponseEntity, notNullValue());
        assertThat(page0ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PermissionsResponse page0 = page0ResponseEntity.getBody();
        assertThat(page0, notNullValue());
        assertThat(page0.getPageInfo(), notNullValue());
        assertThat(page0.getPageInfo().getNumber(), equalTo(0));
        assertThat(page0.getPageInfo().getSize(), equalTo(2));
        assertThat(page0.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page0.getPageInfo().isFirst(), equalTo(true));
        assertThat(page0.getPageInfo().isLast(), equalTo(false));
        assertThat(page0.getContent(), equalTo(List.of(
                permissionRespons.get(0),
                permissionRespons.get(3))));

        ResponseEntity<PermissionsResponse> page1ResponseEntity = testRestTemplate.getForEntity(
                url("/api/permission?page=1&size=2"),
                PermissionsResponse.class);
        assertThat(page1ResponseEntity, notNullValue());
        assertThat(page1ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PermissionsResponse page1 = page1ResponseEntity.getBody();
        assertThat(page1, notNullValue());
        assertThat(page1.getPageInfo(), notNullValue());
        assertThat(page1.getPageInfo().getNumber(), equalTo(1));
        assertThat(page1.getPageInfo().getSize(), equalTo(2));
        assertThat(page1.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page1.getPageInfo().isFirst(), equalTo(false));
        assertThat(page1.getPageInfo().isLast(), equalTo(false));
        assertThat(page1.getContent(), equalTo(List.of(
                permissionRespons.get(4),
                permissionRespons.get(1))));

        ResponseEntity<PermissionsResponse> page2ResponseEntity = testRestTemplate.getForEntity(
                url("/api/permission?page=2&size=2"),
                PermissionsResponse.class);
        assertThat(page2ResponseEntity, notNullValue());
        assertThat(page2ResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PermissionsResponse page2 = page2ResponseEntity.getBody();
        assertThat(page2, notNullValue());
        assertThat(page2.getPageInfo(), notNullValue());
        assertThat(page2.getPageInfo().getNumber(), equalTo(2));
        assertThat(page2.getPageInfo().getSize(), equalTo(2));
        assertThat(page2.getPageInfo().getTotalElements(), equalTo(5L));
        assertThat(page2.getPageInfo().isFirst(), equalTo(false));
        assertThat(page2.getPageInfo().isLast(), equalTo(true));
        assertThat(page2.getContent(), equalTo(List.of(
                permissionRespons.get(2))));
    }

    @Test
    public void createPermissionAddsAPermission()
    {
        CreatePermissionRequest request = new CreatePermissionRequest()
                .withName(randomString());

        ResponseEntity<PermissionResponse> responseEntity = testRestTemplate.postForEntity(
                url("/api/permission"),
                request,
                PermissionResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PermissionResponse permissionResponse = responseEntity.getBody();
        assertThat(permissionResponse, notNullValue());
        assertThat(permissionResponse.getId(), notNullValue());
        addDbCleanup("permission", permissionResponse.getId());
        assertThat(permissionResponse.getName(), equalTo(request.getName()));

        Optional<Permission> permission = permissionRepository.findById(permissionResponse.getId());
        assertThat(permission.isPresent(), equalTo(true));
        assertThat(permission.get().getName(), equalTo(request.getName()));
    }

    @Test
    public void createPermissionReturnsBadRequestWhenRequstIsInvalid()
    {
        CreatePermissionRequest request = new CreatePermissionRequest();

        ResponseEntity<PermissionResponse> responseEntity = testRestTemplate.postForEntity(
                url("/api/permission"),
                request,
                PermissionResponse.class);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void createPermissionReturnsBadRequestWhenPermissionNameIsNotUnique()
    {
        CreatePermissionRequest request = new CreatePermissionRequest()
                .withName(randomString());

        ResponseEntity<PermissionResponse> originalResponseEntity = testRestTemplate.postForEntity(
                url("/api/permission"),
                request,
                PermissionResponse.class,
                request);

        assertThat(originalResponseEntity, notNullValue());
        assertThat(originalResponseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));
        addDbCleanup("permission", originalResponseEntity.getBody().getId());

        ResponseEntity<PermissionResponse> duplicateResponseEntity = testRestTemplate.postForEntity(
                url("/api/permission"),
                request,
                PermissionResponse.class,
                request);

        assertThat(duplicateResponseEntity, notNullValue());
        assertThat(duplicateResponseEntity.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void getPermissionByIdReturnsPermission()
    {
        Permission permission = createPermission();

        ResponseEntity<PermissionResponse> responseEntity = testRestTemplate.getForEntity(
                url("/api/permission/{permissionId}"),
                PermissionResponse.class,
                permission.getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        PermissionResponse permissionResponse = responseEntity.getBody();
        verifyPermissionReponse(permissionResponse, permission);
    }

    @Test
    public void getPermissionByIdReturnsNotFoundWhenPermissionDoesNotExist()
    {
        Long permissionId = randomLong();
        ResponseEntity<PermissionResponse> responseEntity = testRestTemplate.getForEntity(
                url("/api/permission/{permissionId}"),
                PermissionResponse.class,
                permissionId);

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void deletePermissionDeletesPermission()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                url("/api/permission/{permissionId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                permissions.get(0).getId());

        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(permissionRepository.findById(permissions.get(0).getId()).isPresent(), equalTo(false));
        assertThat(permissionRepository.findById(permissions.get(1).getId()).isPresent(), equalTo(true));
        assertThat(permissionRepository.findById(permissions.get(2).getId()).isPresent(), equalTo(true));
    }

    @Test
    public void deletePermissionIsIdempotent()
    {
        List<Permission> permissions = List.of(
                createPermission(),
                createPermission(),
                createPermission());

        ResponseEntity<Void> responseEntityForExistingPermission = testRestTemplate.exchange(
                url("/api/permission/{permissionId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                permissions.get(0).getId());

        assertThat(responseEntityForExistingPermission, notNullValue());
        assertThat(responseEntityForExistingPermission.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(permissionRepository.findById(permissions.get(0).getId()).isPresent(), equalTo(false));
        assertThat(permissionRepository.findById(permissions.get(1).getId()).isPresent(), equalTo(true));
        assertThat(permissionRepository.findById(permissions.get(2).getId()).isPresent(), equalTo(true));

        ResponseEntity<Void> responseEntityForDeletedPermission = testRestTemplate.exchange(
                url("/api/permission/{permissionId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                permissions.get(0).getId());

        assertThat(responseEntityForDeletedPermission, notNullValue());
        assertThat(responseEntityForDeletedPermission.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(permissionRepository.findById(permissions.get(0).getId()).isPresent(), equalTo(false));
        assertThat(permissionRepository.findById(permissions.get(1).getId()).isPresent(), equalTo(true));
        assertThat(permissionRepository.findById(permissions.get(2).getId()).isPresent(), equalTo(true));

        ResponseEntity<Void> responseEntityForNonexistentPermission = testRestTemplate.exchange(
                url("/api/permission/{permissionId}"),
                HttpMethod.DELETE,
                null,
                Void.class,
                permissions.get(0).getId());

        assertThat(responseEntityForNonexistentPermission, notNullValue());
        assertThat(responseEntityForNonexistentPermission.getStatusCode().is2xxSuccessful(), equalTo(true));

        assertThat(permissionRepository.findById(permissions.get(0).getId()).isPresent(), equalTo(false));
        assertThat(permissionRepository.findById(permissions.get(1).getId()).isPresent(), equalTo(true));
        assertThat(permissionRepository.findById(permissions.get(2).getId()).isPresent(), equalTo(true));
    }


    private String url(String endpoint)
    {
        return "http://localhost:" + port + endpoint;
    }

    private <T> void addDbCleanup(String table, T id)
    {
        dbCleaner.addCleanup(table, id);
    }

    private void verifyPermissionReponse(PermissionResponse received, Permission expected)
    {
        if (expected == null) {
            assertThat(received, nullValue());
            return;
        }

        assertThat(received, notNullValue());
        assertThat(received.getId(), equalTo(expected.getId()));
        assertThat(received.getName(), equalTo(expected.getName()));
    }

    private Permission createPermission()
    {
        Permission permission = new Permission();
        permission.setName(randomString());
        permission = permissionRepository.save(permission);
        addDbCleanup("permission", permission.getId());

        return permission;
    }

    private Permission createPermissionWithPrefix(String prefix)
    {
        Permission permission = new Permission();
        permission.setName(randomStringWithPrefix(prefix));
        permission = permissionRepository.save(permission);
        addDbCleanup("permission", permission.getId());

        return permission;
    }
}