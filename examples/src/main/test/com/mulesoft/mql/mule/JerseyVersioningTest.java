/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.mql.mule;

import org.mule.tck.FunctionalTestCase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;

public class JerseyVersioningTest extends FunctionalTestCase {

    public void testGetUsers() {
        Client client = getClient();
        
        Map[] users = client.resource("http://localhost:9002/api/v1/users/").get(Map[].class);
        
        assertEquals(1, users.length);
        
        assertEquals("Dan Diephouse", users[0].get("name"));
        assertEquals("123 Main St", users[0].get("address"));
    }

    public void testAddUser() {
        Client client = getClient();
        
        Map<String, String> user = new HashMap<String,String>();
        user.put("name", "Joe Schmoe");
        user.put("email", "joe@schmoe.com");
        user.put("address", "10 Foo");
        user.put("city", "New York");
        user.put("state", "NY");
        
        ClientResponse response = 
            client.resource("http://localhost:9002/api/v1/users/")
                .type("application/json")
                .accept("application/json")
                .post(ClientResponse.class, user);
            
        assertEquals(200, response.getStatus());
        
        Map[] users = client.resource("http://localhost:9002/api/v1/users/").get(Map[].class);
        assertEquals(2, users.length);
        
    }

    protected Client getClient() {
        DefaultClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJsonProvider.class);
        JacksonJsonProvider jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        config.getSingletons().add(jsonProvider);
        
        Client client = Client.create(config);
        client.addFilter(new LoggingFilter());
        return client;
    }
    
    @Override
    protected String getConfigResources() {
        return "versioning.xml";
    }

}
