package io.github.waynem77.bscmail4.server.model.response;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;

import java.util.List;

import static io.github.waynem77.bscmail4.TestUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class PersonContainerTest
{
    @Test
    void fromPageWithValidPageShouldCreateContainerWithAllProperties()
    {
        Long personId1 = randomLong();
        Long personId2 = randomLong();
        int pageNumber = randomInt();
        int size = randomInt();
        int numberOfElements = randomInt();
        int totalPages = randomInt();
        long totalElements = randomLong();
        boolean isFirst = randomBool();
        boolean isLast = randomBool();
        boolean hasNext = randomBool();
        boolean hasPrevious = randomBool();

        List<Person> people = List.of(mock(Person.class), mock(Person.class));
        given(people.get(0).getId()).willReturn(personId1);
        given(people.get(1).getId()).willReturn(personId2);

        List<PersonResponse> personResponses = List.of(mock(PersonResponse.class), mock(PersonResponse.class));
        given(personResponses.get(0).getId()).willReturn(personId1);
        given(personResponses.get(1).getId()).willReturn(personId2);

        Page<Person> page = mock();

        given(page.getContent()).willReturn(people);
        given(page.getNumber()).willReturn(pageNumber);
        given(page.getSize()).willReturn(size);
        given(page.getNumberOfElements()).willReturn(numberOfElements);
        given(page.getTotalPages()).willReturn(totalPages);
        given(page.getTotalElements()).willReturn(totalElements);
        given(page.isFirst()).willReturn(isFirst);
        given(page.isLast()).willReturn(isLast);
        given(page.hasNext()).willReturn(hasNext);
        given(page.hasPrevious()).willReturn(hasPrevious);

        // When
        try (MockedStatic<PersonResponse> mockedPersonResponse = mockStatic(PersonResponse.class))
        {
            mockedPersonResponse.when(() -> PersonResponse.fromPerson(people.get(0))).thenReturn(personResponses.get(0));
            mockedPersonResponse.when(() -> PersonResponse.fromPerson(people.get(1))).thenReturn(personResponses.get(1));

            PersonContainer container = new PersonContainer(page);

            // Then
            assertThat(container, notNullValue());
            assertThat(container.getElements(), equalTo(personResponses));
            assertThat(container.getPageNumber(), equalTo(pageNumber));
            assertThat(container.getSize(), equalTo(size));
            assertThat(container.getNumberOfElements(), equalTo(numberOfElements));
            assertThat(container.getTotalPages(), equalTo(totalPages));
            assertThat(container.getTotalElements(), equalTo(totalElements));
            assertThat(container.isFirst(), equalTo(isFirst));
            assertThat(container.isLast(), equalTo(isLast));
            assertThat(container.hasNext(), equalTo(hasNext));
            assertThat(container.hasPrevious(), equalTo(hasPrevious));
        }
    }
}