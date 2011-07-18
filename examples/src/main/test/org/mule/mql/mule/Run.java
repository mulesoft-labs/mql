/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.mql.mule;

import org.mule.api.MuleContext;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.api.context.MuleContextBuilder;
import org.mule.api.context.MuleContextFactory;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;

import java.util.ArrayList;
import java.util.List;

public class Run  {
    public static void main(String[] args) throws Exception{
        MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
        List<ConfigurationBuilder> builders = new ArrayList<ConfigurationBuilder>();
        builders.add(getBuilder());
        MuleContextBuilder contextBuilder = new DefaultMuleContextBuilder();
        MuleContext ctx = muleContextFactory.createMuleContext(builders, contextBuilder);
        ctx.start();
        
        System.out.println("selecting");
    }

    protected static String getConfigResources() {
        return "./src/main/app/mule-config.xml";
    }


    protected static ConfigurationBuilder getBuilder() throws Exception
    {
        return new SpringXmlConfigurationBuilder(getConfigResources());
    }
}
