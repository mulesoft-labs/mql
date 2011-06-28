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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
@Produces("application/json")
@Consumes("application/json")
public class UserResource {
    // a Map of Users keyed by their email
    private UserManager userManager = new UserManager();
   
    @GET
    public Collection<User> getUsers() {
        return userManager.getUsers();
    }
    
    @POST
    public User addUser(User user) {
        userManager.addUser(user);
        return user;
    }
}
