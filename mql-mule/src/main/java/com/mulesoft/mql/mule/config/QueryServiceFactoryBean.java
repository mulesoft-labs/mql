/*
 * $Id: QueryServiceFactoryBean.java 20320 2010-11-24 15:03:31Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.mql.mule.config;

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.config.spring.factories.AbstractFlowConstructFactoryBean;
import org.mule.config.spring.util.SpringBeanLookup;
import org.mule.construct.builder.AbstractFlowConstructBuilder;

import com.mulesoft.mql.mule.QueryService;
import com.mulesoft.mql.mule.Type;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Builds QueryService instances by using the QueryServiceBuilder.
 */
public class QueryServiceFactoryBean extends AbstractFlowConstructFactoryBean {
    final QueryServiceBuilder simpleServiceBuilder = new QueryServiceBuilder();

    private SpringBeanLookup springBeanLookup;

    public Class<?> getObjectType() {
        return QueryService.class;
    }

    @Override
    protected AbstractFlowConstructBuilder<QueryServiceBuilder, QueryService> getFlowConstructBuilder() {
        return simpleServiceBuilder;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);

        if (springBeanLookup != null) {
            springBeanLookup.setApplicationContext(applicationContext);
        }
    }

    public void setQuery(String query) {
        simpleServiceBuilder.query(query);
    }

    public void setEndpointBuilder(EndpointBuilder endpointBuilder) {
        simpleServiceBuilder.inboundEndpoint(endpointBuilder);
    }

    public void setAddress(String address) {
        simpleServiceBuilder.inboundAddress(address);
    }
    public void setType(Type type) {
        simpleServiceBuilder.type(type);
    }

}
