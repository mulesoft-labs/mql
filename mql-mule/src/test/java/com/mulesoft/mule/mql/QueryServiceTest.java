package com.mulesoft.mule.mql;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import org.mule.client.DefaultLocalMuleClient;
import org.mule.tck.FunctionalTestCase;

import com.mulesoft.mql.Person;

import java.util.Map;

public class QueryServiceTest extends FunctionalTestCase {

    @Override
    protected String getConfigResources() {
        return "service-conf.xml";
    }

    public void testSelectSingle() throws Exception {
        LocalMuleClient client = getClient();
        
        String payload = "{ \"firstName\" : \"first\", \"lastName\" : \"last\"}";
        
        MuleMessage result = client.send("vm://select", new DefaultMuleMessage(payload, muleContext));
        
        payload = result.getPayloadAsString();
        assertEquals("{\"name\":\"first last\"}", payload);
    }

    public void testSelectCollection() throws Exception {
        LocalMuleClient client = getClient();
        
        String payload = "[{ \"firstName\" : \"first\", \"lastName\" : \"last\"}]";
        
        MuleMessage result = client.send("vm://select", new DefaultMuleMessage(payload, muleContext));
        
        payload = result.getPayloadAsString();
        assertEquals("[{\"name\":\"first last\"}]", payload);
    }
    
    public void testPojo() throws Exception {
        LocalMuleClient client = getClient();

        Person person = new Person();
        person.setFirstName("first");
        person.setLastName("last");

        MuleMessage result = client.send("vm://selectpojo", new DefaultMuleMessage(person, muleContext));
        
        Object payload = result.getPayload();
        System.out.println(payload);
        assertTrue(payload instanceof Map);
    }

    protected LocalMuleClient getClient() {
        LocalMuleClient client = new DefaultLocalMuleClient(muleContext);
        return client;
    }
}
