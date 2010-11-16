package com.mulesoft.mql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static com.mulesoft.mql.Restriction.*;

import junit.framework.Assert;

public class QueryTest extends Assert {
    
    @Test
    public void testQuery() throws Exception {
        List<Person> persons = getPersons();
        
        Query query = new QueryBuilder()
            .as("p")
            .orderby("income")
            .max(3)
            .where(eq(property("division"), "Sales"))
            .select(new ObjectBuilder() 
                      .set("name", "p.firstName + \" \" p.lastName")
                      .set("income", "p.income")).build();
        
        Collection<Map> result = query.execute(persons);
        
        assertEquals(3, result.size());
        
    }
    
    
    private List<Person> getPersons() {
        List<Person> persons = new ArrayList<Person>();
        
        persons.add(new Person("Joe", "Schmoe", "Sales", 10000));
        persons.add(new Person("Jane", "Schmoe", "Sales", 12000));
        persons.add(new Person("Foo", "Bar", "Sales", 9000));
        persons.add(new Person("Baz", "Bar", "Operations", 13000));
        persons.add(new Person("Oof", "Fiz", "Operations", 20000));
        
        return persons;
    }
}
