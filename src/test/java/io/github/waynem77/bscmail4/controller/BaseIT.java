package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.DbCleaner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Provides functionality common to integration tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIT
{
    @LocalServerPort
    protected String port;

    protected TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    protected DbCleaner dbCleaner;

    @BeforeEach
    public void setupForBaseIT()
    {
        dbCleaner = new DbCleaner(jdbcTemplate);

        // This creates a TestRestTemplate capable of issuing PATCH requests
        restTemplate = new TestRestTemplate();
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @AfterEach
    public void cleanupForBaseIT()
    {
//        dbCleaner.clean();
    }

    protected final String url(String endpoint)
    {
        return "http://localhost:" + port + endpoint;
    }

    protected final void addDbCleanup(String table, Object id)
    {
        dbCleaner.addCleanup(table, id);
    }
}
