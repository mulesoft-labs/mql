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

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    public List<User> getUsers() {
        List<User> users = new ArrayList<User>();
        
        User user = new User();
        user.setName("Dan Diephouse");
        user.setEmail("dan@mulesoft.com");
        users.add(user);
        
        return users;
    }
}
