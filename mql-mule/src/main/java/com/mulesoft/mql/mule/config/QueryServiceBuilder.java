/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.mql.mule.config;

import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.construct.builder.AbstractFlowConstructWithSingleInboundEndpointBuilder;

import com.mulesoft.mql.mule.QueryService;

public class QueryServiceBuilder extends AbstractFlowConstructWithSingleInboundEndpointBuilder<QueryServiceBuilder, QueryService>{

    private String query;
    
    public QueryServiceBuilder query(String query) {
        this.query = query;
        return this;
    }
    
    @Override
    protected MessageExchangePattern getInboundMessageExchangePattern() {
        return MessageExchangePattern.REQUEST_RESPONSE;
    }

    @Override
    protected QueryService buildFlowConstruct(MuleContext muleContext) throws MuleException {
        return new QueryService(name, query, getOrBuildInboundEndpoint(muleContext), muleContext);
    }

}
