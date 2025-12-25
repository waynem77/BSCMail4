package io.github.waynem77.bscmail4.server.model.response;

import io.github.waynem77.bscmail4.server.database.entity.Person;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonContainer extends Container<Person, PersonResponse>
{
    public PersonContainer(Page<Person> page)
    {
        super(page);
    }

    @Override
    protected PersonResponse transformEntityToResponse(Person entity)
    {
        return PersonResponse.fromPerson(entity);
    }
}
