package com.mulesoft.mql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class ParserTest extends Assert {
    
    @Test 
    public void testSingleClause() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from persons as p where p.division = 'Sales'", asMap("persons", persons));
        
        assertEquals(3, result.size());
    }
    
    private Map<String, Object> asMap(String key, Object value) {
        Map<String, Object> map = new HashMap<String,Object>();
        map.put(key, value);
        return map;
    }

    @Test
    public void testAnd() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from persons as p where p.division = 'Sales' and p.lastName = 'Schmoe'", asMap("persons", persons));
        
        assertEquals(2, result.size());
    }
    
    @Test
    public void testOr() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from persons as p where p.division = 'Sales' or p.lastName = 'Bar'", asMap("persons", persons));
        
        assertEquals(4, result.size());
    }
    
    @Test
    public void testParens() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from persons as p where (p.division = 'Sales' and p.lastName = 'Schmoe')", asMap("persons", persons));
        
        assertEquals(2, result.size());
    }

    @Test
    public void testLike() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("from persons as p where p.division like 'Sal' and p.lastName = 'Schmoe'", asMap("persons", persons));
        
        assertEquals(2, result.size());
    }
    
    @Test
    public void testSelect() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute(
                    "from persons as p where p.firstName = 'Joe' " +
            		"select new { " +
            		"  name = p.getFirstName() + ' ' + p.lastName, " +
            		"  division = p.division," +
            		"  value = Math.min(1,5)," + // test a function with arguments
            		"  char = p.firstName[0] " + // test an array
            		" }", asMap("persons", persons));
        
        assertEquals(1, result.size());
        
        Map newItem = result.iterator().next();
        assertEquals("Joe Schmoe", newItem.get("name"));
        assertEquals("Sales", newItem.get("division"));
        assertEquals(1, newItem.get("value"));
        assertEquals('J', newItem.get("char"));
    }

    @Test
    public void testSelectWithExpression() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute(
                    "from persons as p where p.firstName = 'Joe' " +
                    "select new { " +
                    "  name = p.firstName + \' \' + p.lastName, " +
                    "  division = p.division " +
                    "}", asMap("persons", persons));
        assertEquals(1, result.size());
        
        Map newItem = result.iterator().next();
        assertEquals("Joe Schmoe", newItem.get("name"));
        assertEquals("Sales", newItem.get("division"));
    }

    @Test 
    public void testSelectOnly() {
        List<Person> persons = getPersons();
        
        Collection<Map> result = 
            Query.execute("select new { name = firstName + \' \' + lastName }", persons);
        
        assertEquals(5, result.size());
    }

    @Test 
    public void testSelectFromObjectMethod() {
        Map<String, Object> context = asMap("test", this);
        
        Collection<Map> result = 
            Query.execute("from test.persons as p where p.division = 'Sales'", context);
        
        assertEquals(3, result.size());
    }
    
    public List<Person> getPersons() {
        List<Person> persons = new ArrayList<Person>();
        
        persons.add(new Person("Joe", "Schmoe", "Sales", 10000));
        persons.add(new Person("Jane", "Schmoe", "Sales", 12000));
        persons.add(new Person("Foo", "Bar", "Sales", 9000));
        persons.add(new Person("Baz", "Bar", "Operations", 13000));
        persons.add(new Person("Oof", "Fiz", "Operations", 20000));
        
        return persons;
    }
}
