package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Person;
import org.junit.jupiter.api.Test;

import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

class PersonResponseTest
{
    @Test
    void fromPersonReturnsNullWhenPersonIsNull()
    {
        PersonResponse result = PersonResponse.fromPerson(null);

        assertNull(result);
    }

    @Test
    void fromPersonReturnsCorrectResponse()
    {
        // Arrange
        Person person = new Person();
        person.setId(1L);
        person.setName(randomString());
        person.setEmailAddress(randomString());
        person.setPhone(randomString());
        person.setActive(true);

        // Act
        PersonResponse result = PersonResponse.fromPerson(person);

        // Assert
        assertNotNull(result);
        assertEquals(person.getId(), result.getId());
        assertEquals(person.getName(), result.getName());
        assertEquals(person.getEmailAddress(), result.getEmailAddress());
        assertEquals(person.getPhone(), result.getPhone());
        assertEquals(person.getActive(), result.getActive());
    }

    @Test
    void fromPersonReturnsCorrectResponseWithNullFields()
    {
        // Arrange
        Person person = new Person();
        person.setId(2L);
        person.setName(null);
        person.setEmailAddress(null);
        person.setPhone(null);
        person.setActive(null);

        // Act
        PersonResponse result = PersonResponse.fromPerson(person);

        // Assert
        assertNotNull(result);
        assertEquals(person.getId(), result.getId());
        assertNull(result.getName());
        assertNull(result.getEmailAddress());
        assertNull(result.getPhone());
        assertNull(result.getActive());
    }
}