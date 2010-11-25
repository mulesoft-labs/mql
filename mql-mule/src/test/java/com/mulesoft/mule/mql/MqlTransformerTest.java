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

    public void testTransformer() throws Exception {
        LocalMuleClient client = new DefaultLocalMuleClient(muleContext);
        
        Person person = new Person();
        person.setFirstName("first");
        person.setLastName("last");
        
        MuleMessage result = client.send("vm://test", new DefaultMuleMessage(person, muleContext));
        
        Object payload = result.getPayload();
        System.out.println(payload);
        assertTrue(payload instanceof Map);
        
    }
}
