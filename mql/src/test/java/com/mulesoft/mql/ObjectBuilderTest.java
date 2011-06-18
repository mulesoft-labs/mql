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
        List<User> persons = getPersons();
        
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
        List<User> persons = getPersons();
        
        Query query = new QueryBuilder()
            .as("p")
            .where(eq(property("firstName"), "Joe"))
            .select(newObject(UserDto.class.getName())
                    .set("name", "p.firstName + \" \" + p.lastName"))
            .build();
        
        Collection<UserDto> result = query.execute(persons);
        
        assertEquals(1, result.size());
     
        UserDto newPerson = result.iterator().next();
        assertEquals("Joe Schmoe", newPerson.getName());
    }

    
    private List<User> getPersons() {
        List<User> persons = new ArrayList<User>();
        
        persons.add(new User("Joe", "Schmoe", "Sales", 10000));
        persons.add(new User("Jane", "Schmoe", "Sales", 12000));
        persons.add(new User("Foo", "Bar", "Sales", 9000));
        persons.add(new User("Baz", "Bar", "Operations", 13000));
        persons.add(new User("Oof", "Fiz", "Operations", 20000));
        persons.add(new User("Liz", "Biz", "Accounting", 20000));
        
        return persons;
    }
}
