package com.mulesoft.mql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static com.mulesoft.mql.ObjectBuilder.newObject;
import static com.mulesoft.mql.Restriction.*;

import junit.framework.Assert;

public class QueryTest extends Assert {
    
    @Test
    public void testQuery() throws Exception {
        List<User> persons = getPersons();
        
        Query query = new QueryBuilder()
            .as("p")
            .orderby("income")
            .max(3)
            .where(eq(property("division"), "Sales"))
            .select(newObject()
                      .set("name", "firstName + ' ' + lastName")
                      .set("income", "income")).build();
        
        Collection<Map> result = query.execute(persons);
        assertEquals(3, result.size());
    }
    
    
    private List<User> getPersons() {
        List<User> persons = new ArrayList<User>();
        
        persons.add(new User("Joe", "Schmoe", "Sales", 10000));
        persons.add(new User("Jane", "Schmoe", "Sales", 12000));
        persons.add(new User("Foo", "Bar", "Sales", 9000));
        persons.add(new User("Baz", "Bar", "Operations", 13000));
        persons.add(new User("Oof", "Fiz", "Operations", 20000));
        
        return persons;
    }
}
