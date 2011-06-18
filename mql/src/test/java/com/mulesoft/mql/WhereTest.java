package com.mulesoft.mql;

import static com.mulesoft.mql.Restriction.and;
import static com.mulesoft.mql.Restriction.eq;
import static com.mulesoft.mql.Restriction.gt;
import static com.mulesoft.mql.Restriction.gte;
import static com.mulesoft.mql.Restriction.lt;
import static com.mulesoft.mql.Restriction.like;
import static com.mulesoft.mql.Restriction.lte;
import static com.mulesoft.mql.Restriction.or;
import static com.mulesoft.mql.Restriction.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class WhereTest extends Assert {
    
    @Test
    public void testSimple() throws Exception {
        List<User> persons = getPersons();
        
        Query query = new QueryBuilder()
            .as("p")
            .where(eq(property("division"), "Sales"))
            .build();
        
        Collection<User> result = query.execute(persons);
        
        assertEquals(3, result.size());
    }

    @Test
    public void testOr() throws Exception {
        List<User> persons = getPersons();
        
        Query query = new QueryBuilder()
            .where(or(eq(property("division"), "Sales"), eq(property("division"), "Operations")))
            .build();
        
        Collection<User> result = query.execute(persons);
        
        assertEquals(5, result.size());
    }
    
    @Test
    public void testAnd() throws Exception {
        List<User> persons = getPersons();
        
        Query query = new QueryBuilder()
            .where(and(eq(property("division"), "Sales"), 
                       eq(property("firstName"), "Joe")))
            .orderby("income")
            .max(3)
            .build();
        
        Collection<User> result = query.execute(persons);
        
        assertEquals(1, result.size());
    }
    
    @Test
    public void testGreaterAndLessThan() throws Exception {
        List<User> persons = getPersons();
        
        Query query = new QueryBuilder()
            .where(gt(property("income"), 9500))
            .build();
        
        Collection<User> result = query.execute(persons);
        
        assertEquals(5, result.size());
        
        // less than
        query = new QueryBuilder()
            .where(lt(property("income"), 9500))
        .build();
    
        result = query.execute(persons);
        
        assertEquals(1, result.size());
        
        // greater than equals
        query = new QueryBuilder()
            .where(gte(property("income"), 10000))
        .build();
    
        result = query.execute(persons);
        
        assertEquals(5, result.size());

        // greater than equals
        query = new QueryBuilder()
            .where(lte(property("income"), 10000))
        .build();
    
        result = query.execute(persons);
        
        assertEquals(2, result.size());
    }

    @Test
    public void testLike() throws Exception {
        List<User> persons = getPersons();
        
        Query query = new QueryBuilder()
            .as("p")
            .where(like(property("division"), "Sal"))
            .build();
        
        Collection<User> result = query.execute(persons);
        
        assertEquals(3, result.size());
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
