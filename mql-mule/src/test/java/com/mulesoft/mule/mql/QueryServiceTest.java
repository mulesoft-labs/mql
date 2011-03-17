package com.mulesoft.mule.mql;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import org.mule.client.DefaultLocalMuleClient;
import org.mule.tck.FunctionalTestCase;

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

    protected LocalMuleClient getClient() {
        LocalMuleClient client = new DefaultLocalMuleClient(muleContext);
        return client;
    }
}
