package io.github.waynem77.bscmail4.model.specification;

import io.github.waynem77.bscmail4.DbCleaner;
import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.repository.PersonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static io.github.waynem77.bscmail4.TestUtils.randomStringContaining;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PersonSpecification.
 */
@DataJpaTest
class PersonSpecificationIT
{
    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private DbCleaner dbCleaner;

    @BeforeEach
    void setUp()
    {
        dbCleaner = new DbCleaner(jdbcTemplate);
    }

    @AfterEach
    void tearDown()
    {
        dbCleaner.clean();
    }

    @Test
    void hasActiveStatusWithTrueReturnsOnlyActivePersons()
    {
        // Given
        Person activePerson1 = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person activePerson2 = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person inactivePerson = createAndSavePerson(randomString(), randomString(), randomString(), false);

        // When
        Specification<Person> specification = PersonSpecification.hasActiveStatus(true);
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(activePerson1));
        assertTrue(result.contains(activePerson2));
        assertFalse(result.contains(inactivePerson));
    }

    @Test
    void hasActiveStatusWithFalseReturnsOnlyInactivePersons()
    {
        // Given
        Person activePerson = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person inactivePerson1 = createAndSavePerson(randomString(), randomString(), randomString(), false);
        Person inactivePerson2 = createAndSavePerson(randomString(), randomString(), randomString(), false);

        // When
        Specification<Person> specification = PersonSpecification.hasActiveStatus(false);
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(inactivePerson1));
        assertTrue(result.contains(inactivePerson2));
        assertFalse(result.contains(activePerson));
    }

    @Test
    void hasActiveStatusWithNullReturnsAllPersons()
    {
        // Given
        Person activePerson = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person inactivePerson = createAndSavePerson(randomString(), randomString(), randomString(), false);

        // When
        Specification<Person> specification = PersonSpecification.hasActiveStatus(null);
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(activePerson));
        assertTrue(result.contains(inactivePerson));
    }

    @Test
    void hasNameLikeReturnsMatchingPersons()
    {
        // Given
        String searchTerm = randomString();
        Person person1 = createAndSavePerson(randomStringContaining(searchTerm), randomString(), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person person3 = createAndSavePerson(randomStringContaining(searchTerm), randomString(), randomString(), true);

        // When
        Specification<Person> specification = PersonSpecification.hasNameLike(searchTerm);
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(person1));
        assertTrue(result.contains(person3));
        assertFalse(result.contains(person2));
    }

    @Test
    void hasNameLikeIsCaseInsensitive()
    {
        // Given
        String searchTerm = randomString();
        Person person1 = createAndSavePerson(randomStringContaining(searchTerm), randomString(), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);

        // When
        Specification<Person> specification = PersonSpecification.hasNameLike(searchTerm.toUpperCase());
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(person1));
    }

    @Test
    void hasNameLikeWithNullReturnsAllPersons()
    {
        // Given
        Person person1 = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);

        // When
        Specification<Person> specification = PersonSpecification.hasNameLike(null);
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(person1));
        assertTrue(result.contains(person2));
    }

    @Test
    void hasNameLikeWithEmptyStringReturnsAllPersons()
    {
        // Given
        Person person1 = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);

        // When
        Specification<Person> specification = PersonSpecification.hasNameLike("");
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(person1));
        assertTrue(result.contains(person2));
    }

    @Test
    void hasEmailLikeReturnsMatchingPersons()
    {
        // Given
        String searchTerm = randomString();
        Person person1 = createAndSavePerson(randomString(), randomStringContaining(searchTerm), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomStringContaining(searchTerm), randomString(), true);
        Person person3 = createAndSavePerson(randomString(), randomString(), randomString(), true);

        // When
        Specification<Person> specification = PersonSpecification.hasEmailLike(searchTerm);
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(person1));
        assertTrue(result.contains(person2));
        assertFalse(result.contains(person3));
    }

    @Test
    void hasEmailLikeIsCaseInsensitive()
    {
        // Given
        String searchTerm = randomString();
        Person person1 = createAndSavePerson(randomString(), randomStringContaining(searchTerm), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);

        // When
        Specification<Person> specification = PersonSpecification.hasEmailLike(searchTerm.toUpperCase());
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(person1));
    }

    @Test
    void hasPhoneLikeReturnsMatchingPersons()
    {
        // Given
        String searchTerm = randomString();
        Person person1 = createAndSavePerson(randomString(), randomString(), randomStringContaining(searchTerm), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person person3 = createAndSavePerson(randomString(), randomString(), randomStringContaining(searchTerm), true);

        // When
        Specification<Person> specification = PersonSpecification.hasPhoneLike(searchTerm);
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(person1));
        assertTrue(result.contains(person3));
        assertFalse(result.contains(person2));
    }

    @Test
    void hasPhoneLikeIsCaseInsensitive()
    {
        // Given
        String searchTerm = randomString();
        Person person1 = createAndSavePerson(randomString(), randomString(), randomStringContaining(searchTerm), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);

        // When
        Specification<Person> specification = PersonSpecification.hasPhoneLike(searchTerm.toUpperCase());
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(person1));
    }

    @Test
    void hasTextSearchReturnsPersonsMatchingAnyField()
    {
        // Given
        String searchTerm1 = randomString();
        String searchTerm2 = randomString();
        String searchTerm3 = randomString();

        Person person1 = createAndSavePerson(randomStringContaining(searchTerm1), randomStringContaining(searchTerm2)
                , randomStringContaining(searchTerm3), true);
        Person person2 = createAndSavePerson(randomString(), randomStringContaining(searchTerm2), randomString(), true);
        Person person3 = createAndSavePerson(randomStringContaining(searchTerm1), randomString(),
                randomStringContaining(searchTerm3), true);

        // When - search by name
        Specification<Person> specification1 = PersonSpecification.hasTextSearch(searchTerm1);
        List<Person> result1 = personRepository.findAll(specification1);

        // When - search by email
        Specification<Person> specification2 = PersonSpecification.hasTextSearch(searchTerm2);
        List<Person> result2 = personRepository.findAll(specification2);

        // When - search by phone
        Specification<Person> specification3 = PersonSpecification.hasTextSearch(searchTerm3);
        List<Person> result3 = personRepository.findAll(specification3);

        // Then
        assertEquals(2, result1.size()); // person1 and person3 have searchTerm1 in name
        assertTrue(result1.contains(person1));
        assertTrue(result1.contains(person3));

        assertEquals(2, result2.size()); // person1 and person2 have searchTerm2 in email
        assertTrue(result2.contains(person1));
        assertTrue(result2.contains(person2));

        assertEquals(2, result3.size()); // person1 and person3 have searchTerm3 in phone
        assertTrue(result3.contains(person1));
        assertTrue(result3.contains(person3));
    }

    @Test
    void hasTextSearchIsCaseInsensitive()
    {
        // Given
        String searchTerm = randomString();
        Person person1 = createAndSavePerson(randomStringContaining(searchTerm), randomString(), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);

        // When
        Specification<Person> specification = PersonSpecification.hasTextSearch(searchTerm.toUpperCase());
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(person1));
    }

    @Test
    void andCombinesMultipleSpecifications()
    {
        // Given
        String searchTerm = randomString();
        Person person1 = createAndSavePerson(randomStringContaining(searchTerm), randomString(), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomString(), randomString(), true);
        Person person3 = createAndSavePerson(randomStringContaining(searchTerm), randomString(), randomString(), false);

        // When
        Specification<Person> specification = PersonSpecification.and(
                PersonSpecification.hasActiveStatus(true),
                PersonSpecification.hasNameLike(searchTerm)
        );
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(person1));
        assertFalse(result.contains(person2));
        assertFalse(result.contains(person3));
    }

    @Test
    void andWithMultipleSpecificationsFiltersCorrectly()
    {
        // Given
        String nameSearchTerm = randomString();
        String emailSearchTerm = randomString();
        Person person1 = createAndSavePerson(randomStringContaining(nameSearchTerm),
                randomStringContaining(emailSearchTerm), randomString(), true);
        Person person2 = createAndSavePerson(randomString(), randomStringContaining(emailSearchTerm), randomString(),
                true);
        Person person3 = createAndSavePerson(randomStringContaining(nameSearchTerm), randomString(), randomString(),
                true);

        // When
        Specification<Person> specification = PersonSpecification.and(
                PersonSpecification.hasActiveStatus(true),
                PersonSpecification.hasEmailLike(emailSearchTerm),
                PersonSpecification.hasNameLike(nameSearchTerm)
        );
        List<Person> result = personRepository.findAll(specification);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(person1));
    }

    /**
     * Creates and saves a Person entity to the database for testing.
     * Also adds it to the DbCleaner for cleanup after the test.
     *
     * @param name         the person's name
     * @param emailAddress the person's email address
     * @param phone        the person's phone number
     * @param active       whether the person is active
     * @return the saved Person entity
     */
    private Person createAndSavePerson(String name, String emailAddress, String phone, Boolean active)
    {
        Person person = new Person();
        person.setName(name);
        person.setEmailAddress(emailAddress);
        person.setPhone(phone);
        person.setActive(active);

        Person savedPerson = personRepository.save(person);
        dbCleaner.addCleanup("person", savedPerson.getId());
        return savedPerson;
    }
}