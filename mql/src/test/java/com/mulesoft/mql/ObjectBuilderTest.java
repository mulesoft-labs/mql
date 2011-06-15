package com.mulesoft.mql;

import static com.mulesoft.mql.ObjectBuilder.newObject;
import static com.mulesoft.mql.Restriction.eq;
import static com.mulesoft.mql.Restriction.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class ObjectBuilderTest extends Assert {
    
    @Test
    public void testSimpleTransform() throws Exception {
        List<Person> persons = getPersons();
        
        Query query = new QueryBuilder()
            .as("p")
            .where(eq(property("firstName"), "Joe"))
            .select(newObject().set("name", "p.firstName + \" \" + p.lastName"))
            .build();
        
        Collection<Map> result = query.execute(persons);
        
        assertEquals(1, result.size());

        Map newPerson = result.iterator().next();
        assertEquals(1, newPerson.size());
        assertEquals("Joe Schmoe", newPerson.get("name"));
    }
    
    @Test
    public void testPOJOTransform() throws Exception {
        List<Person> persons = getPersons();
        
        Query query = new QueryBuilder()
            .as("p")
            .where(eq(property("firstName"), "Joe"))
            .select(newObject(PersonDto.class.getName())
                    .set("name", "p.firstName + \" \" + p.lastName"))
            .build();
        
        Collection<PersonDto> result = query.execute(persons);
        
        assertEquals(1, result.size());
     
        PersonDto newPerson = result.iterator().next();
        assertEquals("Joe Schmoe", newPerson.getName());
    }

    
    private List<Person> getPersons() {
        List<Person> persons = new ArrayList<Person>();
        
        persons.add(new Person("Joe", "Schmoe", "Sales", 10000));
        persons.add(new Person("Jane", "Schmoe", "Sales", 12000));
        persons.add(new Person("Foo", "Bar", "Sales", 9000));
        persons.add(new Person("Baz", "Bar", "Operations", 13000));
        persons.add(new Person("Oof", "Fiz", "Operations", 20000));
        persons.add(new Person("Liz", "Biz", "Accounting", 20000));
        
        return persons;
    }
}
