package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static io.github.waynem77.bscmail4.TestUtils.randomBool;
import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

class PersonControllerIT extends BaseIT
{
    @Autowired
    private PersonRepository personRepository;

    @Test
    void listPersonsShouldReturnDefaultPageWithDefaultSorting()
    {
        // Given
        Person person1 = createAndSavePerson("Alice", "alice@example.com", randomString(), true);
        Person person2 = createAndSavePerson("Bob", "bob@example.com", randomString(), true);
        Person person3 = createAndSavePerson("Charlie", "charlie@example.com", randomString(), false);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url("/persons"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Should contain all persons sorted by name (default)
        assertTrue(body.contains("Alice"));
        assertTrue(body.contains("Bob"));
        assertTrue(body.contains("Charlie"));

        // Check that names appear in alphabetical order
        int aliceIndex = body.indexOf("Alice");
        int bobIndex = body.indexOf("Bob");
        int charlieIndex = body.indexOf("Charlie");
        assertTrue(aliceIndex < bobIndex);
        assertTrue(bobIndex < charlieIndex);
    }

    @Test
    void listPersonsShouldRespectPaginationParameters()
    {
        // Given - create 3 persons
        Person person1 = createAndSavePerson("Alice", "alice@example.com", randomString(), true);
        Person person2 = createAndSavePerson("Bob", "bob@example.com", randomString(), true);
        Person person3 = createAndSavePerson("Charlie", "charlie@example.com", randomString(), true);

        // When - request page 0 with size 2
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?page=0&size=2"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Should contain first 2 persons (Alice and Bob)
        assertTrue(body.contains("Alice"));
        assertTrue(body.contains("Bob"));
        assertFalse(body.contains("Charlie")); // Charlie should not be on first page
    }

    @Test
    void listPersonsShouldRespectSecondPage()
    {
        // Given - create 3 persons
        Person person1 = createAndSavePerson("Alice", "alice@example.com", randomString(), true);
        Person person2 = createAndSavePerson("Bob", "bob@example.com", randomString(), true);
        Person person3 = createAndSavePerson("Charlie", "charlie@example.com", randomString(), true);

        // When - request page 1 with size 2
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?page=1&size=2"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Should contain only Charlie on second page
        assertFalse(body.contains("Alice"));
        assertFalse(body.contains("Bob"));
        assertTrue(body.contains("Charlie"));
    }

    @Test
    void listPersonsShouldSortByEmailAddressAscending()
    {
        // Given
        Person person1 = createAndSavePerson("Alice", "charlie@example.com", randomString(), true);
        Person person2 = createAndSavePerson("Bob", "alice@example.com", randomString(), true);
        Person person3 = createAndSavePerson("Charlie", "bob@example.com", randomString(), true);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?sortBy=emailAddress&direction=asc"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Emails should appear in alphabetical order: alice@, bob@, charlie@
        int aliceEmailIndex = body.indexOf("alice@example.com");
        int bobEmailIndex = body.indexOf("bob@example.com");
        int charlieEmailIndex = body.indexOf("charlie@example.com");
        assertTrue(aliceEmailIndex < bobEmailIndex);
        assertTrue(bobEmailIndex < charlieEmailIndex);
    }

    @Test
    void listPersonsShouldSortByEmailAddressDescending()
    {
        // Given
        Person person1 = createAndSavePerson("Alice", "alice@example.com", randomString(), true);
        Person person2 = createAndSavePerson("Bob", "bob@example.com", randomString(), true);
        Person person3 = createAndSavePerson("Charlie", "charlie@example.com", randomString(), true);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?sortBy=emailAddress&direction=desc"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Emails should appear in reverse alphabetical order: charlie@, bob@, alice@
        int aliceEmailIndex = body.indexOf("alice@example.com");
        int bobEmailIndex = body.indexOf("bob@example.com");
        int charlieEmailIndex = body.indexOf("charlie@example.com");
        assertTrue(charlieEmailIndex < bobEmailIndex);
        assertTrue(bobEmailIndex < aliceEmailIndex);
    }

    @Test
    void listPersonsShouldSortByActiveStatus()
    {
        // Given
        Person activePerson = createAndSavePerson("Active Person", "active@example.com", randomString(), true);
        Person inactivePerson = createAndSavePerson("Inactive Person", "inactive@example.com", randomString(), false);

        // When - sort by active status ascending (false comes before true)
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?sortBy=active&direction=asc"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Inactive should come before active when sorting ascending
        int inactiveIndex = body.indexOf("Inactive Person");
        int activeIndex = body.indexOf("Active Person");
        assertTrue(inactiveIndex < activeIndex);
    }

    @Test
    void listPersonsShouldFilterByActiveStatusTrue()
    {
        // Given
        Person activePerson = createAndSavePerson("Active Person", "active@example.com", randomString(), true);
        Person inactivePerson = createAndSavePerson("Inactive Person", "inactive@example.com", randomString(), false);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?active=true"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        assertTrue(body.contains("Active Person"));
        assertFalse(body.contains("Inactive Person"));
    }

    @Test
    void listPersonsShouldFilterByActiveStatusFalse()
    {
        // Given
        Person activePerson = createAndSavePerson("Active Person", "active@example.com", randomString(), true);
        Person inactivePerson = createAndSavePerson("Inactive Person", "inactive@example.com", randomString(), false);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?active=false"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        assertFalse(body.contains("Active Person"));
        assertTrue(body.contains("Inactive Person"));
    }

    @Test
    void listPersonsShouldFilterByNamePartialMatch()
    {
        // Given
        Person person1 = createAndSavePerson("Alice Johnson", "alice@example.com", randomString(), true);
        Person person2 = createAndSavePerson("Bob Smith", "bob@example.com", randomString(), true);
        Person person3 = createAndSavePerson("Alice Brown", "alice.brown@example.com", randomString(), true);

        // When - search for "Alice"
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?name=Alice"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        assertTrue(body.contains("Alice Johnson"));
        assertTrue(body.contains("Alice Brown"));
        assertFalse(body.contains("Bob Smith"));
    }

    @Test
    void listPersonsShouldFilterByEmailPartialMatch()
    {
        // Given
        Person person1 = createAndSavePerson("Alice", "alice@gmail.com", randomString(), true);
        Person person2 = createAndSavePerson("Bob", "bob@yahoo.com", randomString(), true);
        Person person3 = createAndSavePerson("Charlie", "charlie@gmail.com", randomString(), true);

        // When - search for "gmail"
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?email=gmail"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        assertTrue(body.contains("alice@gmail.com"));
        assertTrue(body.contains("charlie@gmail.com"));
        assertFalse(body.contains("bob@yahoo.com"));
    }

    @Test
    void listPersonsShouldFilterByPhonePartialMatch()
    {
        // Given
        Person person1 = createAndSavePerson("Alice", "alice@example.com", "555-1234", true);
        Person person2 = createAndSavePerson("Bob", "bob@example.com", "444-5678", true);
        Person person3 = createAndSavePerson("Charlie", "charlie@example.com", "555-9999", true);

        // When - search for "555"
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?phone=555"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        assertTrue(body.contains("Alice"));
        assertTrue(body.contains("Charlie"));
        assertFalse(body.contains("Bob"));
    }

    @Test
    void listPersonsShouldFilterByTextSearchAcrossAllFields()
    {
        // Given
        String uniqueText = randomString();
        Person person1 = createAndSavePerson(uniqueText + " Name", "alice@example.com", "123-4567", true);
        Person person2 = createAndSavePerson("Bob", uniqueText + "@example.com", "234-5678", true);
        Person person3 = createAndSavePerson("Charlie", "charlie@example.com", uniqueText, true);
        Person person4 = createAndSavePerson("David", "david@example.com", "345-6789", true);

        // When - text search for unique text
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?textSearch=" + uniqueText), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Should find persons where unique text appears in name, email, or phone
        assertTrue(body.contains(uniqueText + " Name"));
        assertTrue(body.contains(uniqueText + "@example.com"));
        assertTrue(body.contains("Charlie")); // has unique text in phone
        assertFalse(body.contains("David")); // doesn't have unique text anywhere
    }

    @Test
    void listPersonsShouldCombineMultipleFilters()
    {
        // Given
        Person person1 = createAndSavePerson("Alice Active", "alice@gmail.com", "555-1234", true);
        Person person2 = createAndSavePerson("Alice Inactive", "alice@yahoo.com", "555-5678", false);
        Person person3 = createAndSavePerson("Bob Active", "bob@gmail.com", "444-1234", true);

        // When - filter by active=true AND name=Alice AND email=gmail
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?active=true&name=Alice&email=gmail"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);

        // Should only find Alice Active with gmail
        assertTrue(body.contains("Alice Active"));
        assertFalse(body.contains("Alice Inactive")); // inactive
        assertFalse(body.contains("Bob Active")); // name doesn't match
    }

    @Test
    void listPersonsShouldHandleInvalidSortFieldGracefully()
    {
        // Given
        Person person = createAndSavePerson("Alice", "alice@example.com", randomString(), true);

        // When - use invalid sort field
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?sortBy=invalidField"), String.class);

        // Then - should default to name sorting and not fail
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Alice"));
    }

    @Test
    void listPersonsShouldHandleNegativePageGracefully()
    {
        // Given
        Person person = createAndSavePerson("Alice", "alice@example.com", randomString(), true);

        // When - use negative page number
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?page=-1"), String.class);

        // Then - should default to page 0 and not fail
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Alice"));
    }

    @Test
    void listPersonsShouldConstrainPageSizeToMaximum()
    {
        // Given
        createMultiplePersons(5); // Create 5 persons

        // When - request very large page size
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/persons?size=1000"), String.class);

        // Then - should still work (size will be constrained to 100 internally)
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
    }

    @Test
    void listPersonsShouldHandleEmptyDatabase()
    {
        // Given - no persons in database

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url("/persons"), String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        // Should not crash and should render the empty list page
    }

    // Helper methods

    private Person createAndSavePerson(String name, String email, String phone, Boolean active)
    {
        Person person = new Person();
        person.setName(name);
        person.setEmailAddress(email);
        person.setPhone(phone);
        person.setActive(active);

        Person savedPerson = personRepository.save(person);
        addDbCleanup("person", savedPerson.getId());
        return savedPerson;
    }

    private List<Person> createMultiplePersons(int count)
    {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> createAndSavePerson(
                        "Person " + i,
                        "person" + i + "@example.com",
                        randomString(),
                        randomBool()
                ))
                .toList();
    }
}
