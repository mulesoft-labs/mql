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

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.client.MuleClient;
import org.mule.api.construct.FlowConstruct;
import org.mule.construct.Flow;

/**
 * Provides a simple API for the query context around the MuleClient.
 */
public class MuleClientWrapper {
    private final MuleClient muleClient;
    private final MuleContext muleContext;
    private final MuleEvent event;

    public MuleClientWrapper(MuleEvent event, MuleClient muleClient, MuleContext muleContext) {
        super();
        this.event = event;
        this.muleClient = muleClient;
        this.muleContext = muleContext;
    }
    
    public Object send(String url, Object payload) throws MuleException {
        return muleClient.send(url, payload, null).getPayload();
    }
    
    public Object flow(String flowName) throws MuleException {
        return flow(flowName, null);
    }

    public Object flow(String flowName, Object payload) throws MuleException {
        FlowConstruct flowConstruct = muleContext.getRegistry().lookupFlowConstruct(flowName);
        if (flowConstruct == null) {
            throw new DefaultMuleException("Could not find flow " + flowName);
        }
        
        Flow flow = (Flow) flowConstruct;
        
        if (payload == null) {
            payload = event.getMessage().getPayload();
        }
        
        DefaultMuleEvent subEvent = new DefaultMuleEvent(new DefaultMuleMessage(payload, muleContext), event);
        MuleEvent result = flow.process(subEvent);
        return result.getMessage().getPayload();
    }


    public MuleClient getMuleClient() {
        return muleClient;
    }

}
