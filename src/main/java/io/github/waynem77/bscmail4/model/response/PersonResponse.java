package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.Person;
import lombok.Data;

/**
 * Response object for Person entities.
 */
@Data
public class PersonResponse
{
    private Long id;
    private String name;
    private String emailAddress;
    private String phone;
    private Boolean active;

    /**
     * Creates a PersonResponse from a Person entity.
     *
     * @param person The Person entity to convert
     * @return A new PersonResponse instance with data from the Person entity
     */
    public static PersonResponse fromPerson(Person person)
    {
        if (person == null)
        {
            return null;
        }

        PersonResponse response = new PersonResponse();
        response.setId(person.getId());
        response.setName(person.getName());
        response.setEmailAddress(person.getEmailAddress());
        response.setPhone(person.getPhone());
        response.setActive(person.getActive());

        return response;
    }
}