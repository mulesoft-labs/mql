package com.mulesoft.mql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class ParserTest extends Assert {
    
    @Test 
    public void testSingleClause() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from payload as p where p.division = 'Sales'", persons);
        
        assertEquals(3, result.size());
    }
    
    @Test
    public void testAnd() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from persons as p where p.division = 'Sales' and p.lastName = 'Schmoe'", persons);
        
        assertEquals(2, result.size());
    }
    
    @Test
    public void testOr() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from persons as p where p.division = 'Sales' or p.lastName = 'Bar'", persons);
        
        assertEquals(4, result.size());
    }
    
    @Test
    public void testParens() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from persons as p where (p.division = 'Sales' and p.lastName = 'Schmoe')", persons);
        
        assertEquals(2, result.size());
    }
    
    @Test
    public void testSelect() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute(
                    "from persons as p where p.firstName = 'Joe' " +
            		"select new { " +
            		"  name = p.firstName, " +
            		"  division = p.division " +
            		"}", persons);
        
        assertEquals(1, result.size());
        
        Map newItem = result.iterator().next();
        assertEquals("Joe", newItem.get("name"));
        assertEquals("Sales", newItem.get("division"));
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
