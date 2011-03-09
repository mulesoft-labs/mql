/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.mql.spring;

import com.mulesoft.mql.LazyQueryContext;

import org.springframework.context.ApplicationContext;

public class SpringQueryContext extends LazyQueryContext {
    private ApplicationContext applicationContext;

    public SpringQueryContext(ApplicationContext applicationContext) {
        super();
        this.applicationContext = applicationContext;
    }

    @Override
    public Object load(String key) {
        if (applicationContext.containsBean(key)) {
            return applicationContext.getBean(key);
        }
        return null;
    }    
}
