package io.github.waynem77.bscmail4.server.model.response;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import org.junit.jupiter.api.Test;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for PersonResponse.
 */
class PersonResponseTest
{

    @Test
    void fromPersonWithAllFieldsShouldReturnPersonResponseWithAllFields()
    {
        // Given
        Long id = randomLong();
        String name = randomString();
        String emailAddress = randomStringWithSuffix("@example.com");
        String phone = randomString();
        Boolean isActive = randomBool();

        Person person = Person.builder()
                .id(id)
                .name(name)
                .emailAddress(emailAddress)
                .phone(phone)
                .isActive(isActive)
                .build();

        // When
        PersonResponse response = PersonResponse.fromPerson(person);

        // Then
        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(id));
        assertThat(response.getName(), equalTo(name));
        assertThat(response.getEmailAddress(), equalTo(emailAddress));
        assertThat(response.getPhone(), equalTo(phone));
        assertThat(response.getIsActive(), equalTo(isActive));
    }

    @Test
    void fromPersonWithNullOptionalFieldsShouldReturnPersonResponseWithNullOptionalFields()
    {
        // Given
        Long id = randomLong();
        String name = randomString();

        Person person = Person.builder()
                .id(id)
                .name(name)
                .emailAddress(null)
                .phone(null)
                .isActive(randomBool())
                .build();

        // When
        PersonResponse response = PersonResponse.fromPerson(person);

        // Then
        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(id));
        assertThat(response.getName(), equalTo(name));
        assertThat(response.getEmailAddress(), nullValue());
        assertThat(response.getPhone(), nullValue());
        assertThat(response.getIsActive(), notNullValue());
    }

    @Test
    void fromPersonWithNullPersonShouldReturnNull()
    {
        // When
        PersonResponse response = PersonResponse.fromPerson(null);

        // Then
        assertThat(response, nullValue());
    }
}

