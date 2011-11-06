package com.mulesoft.mql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

public class ParserTest extends Assert {
    
    @Test 
    public void testSingleClause() {
        List<User> users = getUsers();
        
        Collection<User> result = 
            Query.execute("from users as u where u.division = 'Sales'", asMap("users", users));
        
        assertEquals(3, result.size());
        
        System.out.println(result.iterator().next().getClass());
    }
    
    private Map<String, Object> asMap(String key, Object value) {
        Map<String, Object> map = new HashMap<String,Object>();
        map.put(key, value);
        return map;
    }

    @Test
    public void testAnd() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute("from users as u where u.division = 'Sales' and u.lastName = 'Schmoe'", asMap("users", users));
        
        assertEquals(2, result.size());
    }
    
    @Test
    public void testOr() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute("from users as u where u.division = 'Sales' or u.lastName = 'Bar'", asMap("users", users));
        
        assertEquals(4, result.size());
    }
    
    @Test
    public void testParens() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute("from users as u where (u.division = 'Sales' and u.lastName = 'Schmoe')", asMap("users", users));
        
        assertEquals(2, result.size());
    }

    @Test
    public void testLike() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute("from users as u where u.division like 'Sal' and u.lastName = 'Schmoe'", asMap("users", users));
        
        assertEquals(2, result.size());
    }

    @Test
    public void testNullQuestion() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute("from users as u where u.?division = 'Sales' and u.lastName = 'Schmoe'", asMap("users", users));
        
        assertEquals(2, result.size());
    }
    @Test
    public void testNotEquals() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute("from users as u where u.?division != 'Sales'", asMap("users", users));
        
        assertEquals(2, result.size());
    }
    
    @Test
    public void testSelect() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute(
                    "from users as u where u.firstName = 'Joe' " +
            		"select new { " +
            		"  name = u.getFirstName() + ' ' + u.lastName, " +
            		"  division = u.division," +
            		"  value = Math.min(1,5)," + // test a function with arguments
            		"  char = u.firstName[0] " + // test an array
            		" }", asMap("users", users));
        
        assertEquals(1, result.size());
        
        Map newItem = result.iterator().next();
        assertEquals("Joe Schmoe", newItem.get("name"));
        assertEquals("Sales", newItem.get("division"));
        assertEquals(1, newItem.get("value"));
        assertEquals('J', newItem.get("char"));
    }
    
    @Test
    public void testSelectPojo() {  
        List<User> users = getUsers();
        Collection<UserDto> result = 
            Query.execute(
                    "from users as u where u.firstName = 'Joe' " +
                    "select new (com.mulesoft.mql.UserDto) { " +
                    "  name = u.getFirstName() + ' ' + u.lastName " + // test an array
                    " }", asMap("users", users));
        
        assertEquals(1, result.size());
     
        UserDto newPerson = result.iterator().next();
        assertEquals("Joe Schmoe", newPerson.getName());
    }

    @Test
    public void testSubObject() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute(
                    "from users as u where u.firstName = 'Joe' " +
                    "select new { " +
                    "  name = u.getFirstName() + ' ' + u.lastName, " +
                    "  companyInfo = {" +
                    "    division = u.division" +
                    "  }" +
                    "}", asMap("users", users));
        
        assertEquals(1, result.size());
        
        Map newItem = result.iterator().next();
        assertEquals("Joe Schmoe", newItem.get("name"));
        Map companyInfo = (Map) newItem.get("companyInfo");
        assertNotNull(companyInfo);
        assertEquals("Sales", companyInfo.get("division"));
    }

    @Test
    public void testSubQuery() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute(
                    "from users as u where u.firstName = 'Joe' " +
                    "select new { " +
                    "  name = u.getFirstName() + ' ' + u.lastName, " +
                    "  users = from users as child select new {" +
                    "    name = child.getFirstName() + ' ' + child.lastName " +
                    "  }" +
                    "}", asMap("users", users));
        
        assertEquals(1, result.size());
        
        Map newItem = result.iterator().next();
        assertEquals("Joe Schmoe", newItem.get("name"));
        List<Map> children = (List<Map>) newItem.get("users");
        assertNotNull(children);
        assertEquals(users.size(), children.size());
    }
    
    @Test
    public void testSelectWithExpression() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute(
                    "from users as u where u.firstName = 'Joe' " +
                    "select new { " +
                    "  name = u.firstName + \' \' + u.lastName, " +
                    "  division = u.division " +
                    "}", asMap("users", users));
        assertEquals(1, result.size());
        
        Map newItem = result.iterator().next();
        assertEquals("Joe Schmoe", newItem.get("name"));
        assertEquals("Sales", newItem.get("division"));
    }

    @Test 
    public void testSelectOnly() {
        List<User> users = getUsers();
        
        Collection<Map> result = 
            Query.execute("select new { name = firstName + \' \' + lastName }", users);
        
        assertEquals(5, result.size());
    }

    @Test 
    public void testSelectFromObjectMethod() {
        Map<String, Object> context = asMap("test", this);
        
        Collection<Map> result = 
            Query.execute("from test.users as u where u.division = 'Sales'", context);
        
        assertEquals(3, result.size());
    }
    
    @Test 
    public void testSingleQuote() {
        Query.create("from users as u join salesforce.query('SELECT Company FROM Lead where email = \\'foo\\'', 1) as sfuser");
    }

    @Test 
    public void testSingleQuote2() {
        Query.create("from users as u join salesforce.query('SELECT Company FROM Lead where email = \\'' + u.user + '\\'', 1) as sfuser");
    }


    @Test 
    public void testArrayBracket() {
        Query.create("from users as u join salesforce.query() as sfuser select new { company = sfuser[0].Company } ");
    }
    
    @Test 
    @Ignore
    public void testDoubleQuote() {
        Query.create("from users as u join salesforce.query(\"SELECT Company FROM Lead\", 1) as sfuser");
    }
    
    public List<User> getUsers() {
        List<User> users = new ArrayList<User>();
        
        users.add(new User("Joe", "Schmoe", "Sales", 10000));
        users.add(new User("Jane", "Schmoe", "Sales", 12000));
        users.add(new User("Foo", "Bar", "Sales", 9000));
        users.add(new User("Baz", "Bar", "Operations", 13000));
        users.add(new User("Oof", "Fiz", "Operations", 20000));
        
        return users;
    }
}
