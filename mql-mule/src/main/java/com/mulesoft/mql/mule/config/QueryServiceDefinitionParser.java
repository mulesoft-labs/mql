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

import org.mule.config.spring.parsers.processors.CheckExclusiveAttributes;
import org.mule.config.spring.parsers.specific.AbstractFlowConstructDefinitionParser;

import org.w3c.dom.Element;

public class QueryServiceDefinitionParser extends AbstractFlowConstructDefinitionParser {

    public QueryServiceDefinitionParser() {
        super.addAlias("endpoint", "endpointBuilder");

        super.registerPreProcessor(new CheckExclusiveAttributes(new String[][] { new String[] { ADDRESS_ATTRIBUTE },
                new String[] { ENDPOINT_REF_ATTRIBUTE } }));
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return QueryServiceFactoryBean.class;
    }
}
