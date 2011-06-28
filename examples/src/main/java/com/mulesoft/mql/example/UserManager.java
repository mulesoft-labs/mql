/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.mql.example;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    // a Map of Users keyed by their email
    private Map<String,User> users = new HashMap<String,User>();
    
    public UserManager() {
        super();
        
        User user = new User();
        user.setName("Dan Diephouse");
        user.setEmail("dan@mulesoft.com");
        
        Address a = new Address();
        a.setAddress("123 Main St");
        a.setCity("San Francisco");
        a.setState("CA");
        a.setCountry("USA");
        user.setAddress(a);
        
        users.put(user.getEmail(), user);
    }
    
    public Collection<User> getUsers() {
        return users.values();
    }
    
    public void addUser(User user) {
        if (user.getEmail() == null) {
            throw new NullPointerException("User email cannot be null");
        }
        users.put(user.getEmail(), user);
    }
}
