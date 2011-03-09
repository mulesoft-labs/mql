package com.mulesoft.mule.mql;

import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import org.mule.client.DefaultLocalMuleClient;
import org.mule.tck.FunctionalTestCase;

import com.mulesoft.mql.Person;

public class MqlTransformerTest extends FunctionalTestCase {

    @Override
    protected String getConfigResources() {
        return "mql-conf.xml";
    }

    public void testSelect() throws Exception {
        LocalMuleClient client = getClient();
        
        Person person = new Person();
        person.setFirstName("first");
        person.setLastName("last");
        
        MuleMessage result = client.send("vm://select", new DefaultMuleMessage(person, muleContext));
        
        Object payload = result.getPayload();
        System.out.println(payload);
        assertTrue(payload instanceof Map);
        
    }

    public void testJoin() throws Exception {
        LocalMuleClient client = getClient();
        
        Person person = new Person();
        person.setFirstName("first");
        person.setLastName("last");
        person.setTwitterId("123");
        
        MuleMessage result = client.send("vm://join", new DefaultMuleMessage(person, muleContext));
        
        Object payload = result.getPayload();
        System.out.println(payload);
        assertTrue(payload instanceof Map);
        
        Map<String,Object> map = (Map<String,Object>) payload;
        assertEquals("1000", map.get("tweets"));
    }

    public void testSelectOnly() throws Exception {
        LocalMuleClient client = getClient();
        
        MuleMessage result = client.send("vm://twitter", new DefaultMuleMessage("123", muleContext));
        
        Object payload = result.getPayload();
        System.out.println(payload);
        assertTrue(payload instanceof Map);
        
        Map<String,Object> map = (Map<String,Object>) payload;
        assertEquals("1000", map.get("totalTweets"));
    }

    protected LocalMuleClient getClient() {
        LocalMuleClient client = new DefaultLocalMuleClient(muleContext);
        return client;
    }
}
