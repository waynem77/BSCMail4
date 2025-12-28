package io.github.waynem77.bscmail4.server.model.response;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class PersonContainerTest
{
    @Test
    void fromPageWithValidPageShouldCreateContainerWithAllProperties()
    {
        Long personId1 = randomLong();
        Long personId2 = randomLong();
        int pageNumber = 0; // Use 0 for simplicity
        int size = 20;
        long totalElements = 2L; // Total elements matches the 2 people

        Person person1 = Person.builder()
                .id(personId1)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .isActive(randomBool())
                .build();
        Person person2 = Person.builder()
                .id(personId2)
                .name(randomString())
                .emailAddress(randomStringWithSuffix("@example.com"))
                .isActive(randomBool())
                .build();
        List<Person> people = List.of(person1, person2);

        // Use PageImpl instead of mocking Page (Java 23 inline mocking doesn't support mocking interfaces)
        PageRequest pageRequest = PageRequest.of(pageNumber, size);
        Page<Person> page = new PageImpl<>(people, pageRequest, totalElements);

        // When
        PersonContainer container = new PersonContainer(page);

        // Then
        assertThat(container, notNullValue());
        assertThat(container.getElements(), notNullValue());
        assertThat(container.getElements().size(), equalTo(2));
        assertThat(container.getElements().get(0).getId(), equalTo(personId1));
        assertThat(container.getElements().get(1).getId(), equalTo(personId2));
        assertThat(container.getElements().get(0).getNumberOfNotes(), equalTo(0L));
        assertThat(container.getElements().get(1).getNumberOfNotes(), equalTo(0L));
        assertThat(container.getPageNumber(), equalTo(pageNumber));
        assertThat(container.getSize(), equalTo(size));
        assertThat(container.getNumberOfElements(), equalTo(people.size()));
        assertThat(container.getTotalPages(), equalTo(page.getTotalPages()));
        assertThat(container.getTotalElements(), equalTo(totalElements));
        assertThat(container.isFirst(), equalTo(page.isFirst()));
        assertThat(container.isLast(), equalTo(page.isLast()));
        assertThat(container.hasNext(), equalTo(page.hasNext()));
        assertThat(container.hasPrevious(), equalTo(page.hasPrevious()));
    }
}
