package com.mulesoft.mql;

import static com.mulesoft.mql.ObjectBuilder.newObject;
import static com.mulesoft.mql.Restriction.*;
import static com.mulesoft.mql.JoinBuilder.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import junit.framework.Assert;

import org.junit.Test;

public class JoinTest extends Assert {
    
    private int executionCount;
    
    @Test
    public void findTotalTweets() throws Exception {
        List<Person> persons = getPersons();
        
        Twitter twitter = getMockTwitter();
        Query query = new QueryBuilder()
            .from("persons")
            .as("p")
            .join("twitter.getUserInfo(p.twitterId)", "twitterInfo")
            .select(newObject()
                        .set("name", "p.firstName + \" \" + p.lastName")
                        .set("tweets", "twitterInfo.totalTweets"))
            .build();
        
        Map<String,Object> context = new HashMap<String,Object>();
        context.put("persons", persons);
        context.put("twitter", twitter);
        Collection<Map> result = query.execute(context);
        
        assertEquals(2, result.size());

        Iterator<Map> itr = result.iterator();
        Map newPerson = itr.next();
        assertEquals(2, newPerson.size());
        assertEquals("Joe Schmoe", newPerson.get("name"));
        assertEquals(4, newPerson.get("tweets"));

        newPerson = itr.next();
        assertEquals(2, newPerson.size());
        assertEquals("Jane Schmoe", newPerson.get("name"));
        assertEquals(5, newPerson.get("tweets"));
    }

    @Test
    public void findTotalTweetsWithWhere() throws Exception {
        List<Person> persons = getPersons();
        
        Twitter twitter = getMockTwitter();
        Query query = new QueryBuilder()
            .from("persons")
            .as("p")
            .join("twitter.getUserInfo(p.twitterId)", "twitterInfo")
            .where(lt(property("twitterInfo.totalTweets"), 5))
            .select(newObject()
                        .set("name", "p.firstName + \" \" + p.lastName")
                        .set("tweets", "twitterInfo.totalTweets"))
            .build();
        
        Map<String,Object> context = new HashMap<String,Object>();
        context.put("persons", persons);
        context.put("twitter", twitter);
        Collection<Map> result = query.execute(context);
        
        assertEquals(1, result.size());

        Iterator<Map> itr = result.iterator();
        Map newPerson = itr.next();
        assertEquals(2, newPerson.size());
        assertEquals("Joe Schmoe", newPerson.get("name"));
        assertEquals(4, newPerson.get("tweets"));
    }


    @Test
    public void testQueryText() throws Exception {
        List<Person> persons = getPersons();
        Twitter twitter = getMockTwitter();
        
        Map<String,Object> context = new HashMap<String,Object>();
        context.put("persons", persons);
        context.put("twitter", twitter);
        
        Collection<Map> result = Query.execute(
            "from persons as p join twitter.getUserInfo(p.twitterId) as twitterInfo where twitterInfo.totalTweets < 5" +
            "select new { name = p.firstName + ' ' + p.lastName, tweets = twitterInfo.totalTweets }", 
            context);
        
        assertEquals(1, result.size());

        Iterator<Map> itr = result.iterator();
        Map newPerson = itr.next();
        assertEquals(2, newPerson.size());
        assertEquals("Joe Schmoe", newPerson.get("name"));
        assertEquals(4, newPerson.get("tweets"));
    }

    protected Twitter getMockTwitter() {
        Twitter twitter = createMock(Twitter.class);
        
        UserInfo joe = createMock(UserInfo.class);
        expect(twitter.getUserInfo("joeschmoe")).andStubReturn(joe);
        expect(joe.getTotalTweets()).andStubReturn(4);
        
        UserInfo jane = createMock(UserInfo.class);
        expect(twitter.getUserInfo("janeschmoe")).andStubReturn(jane);
        expect(jane.getTotalTweets()).andStubReturn(5);
        
        replay(twitter, joe, jane);
        return twitter;
    }
    
    private List<Person> getPersons() {
        List<Person> persons = new ArrayList<Person>();
        
        persons.add(new Person("Joe", "Schmoe", "Sales", 10000, "joeschmoe"));
        persons.add(new Person("Jane", "Schmoe", "Sales", 12000, "janeschmoe"));
        
        return persons;
    }

    @Test
    public void asyncJoin() throws Exception {
        List<Person> persons = getPersons();
        
        Executor executor = new Executor() {
            
            public void execute(Runnable command) {
                command.run();
                executionCount++;
            }
        };
        
        Twitter twitter = getMockTwitter();
        Query query = new QueryBuilder()
            .from("persons")
            .as("p")
            .join(expression("twitter.getUserInfo(p.twitterId)", "twitterInfo")
                  .async().executor(executor))
            .select(newObject()
                        .set("name", "p.firstName + \" \" + p.lastName")
                        .set("tweets", "twitterInfo.totalTweets"))
            .build();
        
        Map<String,Object> context = new HashMap<String,Object>();
        context.put("persons", persons);
        context.put("twitter", twitter);
        Collection<Map> result = query.execute(context);
        
        assertEquals(2, executionCount);
        assertEquals(2, result.size());

        Iterator<Map> itr = result.iterator();
        Map newPerson = itr.next();
        assertEquals(2, newPerson.size());
        assertEquals("Joe Schmoe", newPerson.get("name"));
        assertEquals(4, newPerson.get("tweets"));

        newPerson = itr.next();
        assertEquals(2, newPerson.size());
        assertEquals("Jane Schmoe", newPerson.get("name"));
        assertEquals(5, newPerson.get("tweets"));
    }
    
    public interface Twitter {
        UserInfo getUserInfo(String twitterId);
    }

    public interface UserInfo {
        int getTotalTweets();
    }
}
