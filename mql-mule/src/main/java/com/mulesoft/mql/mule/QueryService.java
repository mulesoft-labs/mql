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

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.processor.MessageProcessorChainBuilder;
import org.mule.api.routing.filter.Filter;
import org.mule.api.source.MessageSource;
import org.mule.construct.AbstractFlowConstruct;
import org.mule.module.json.transformers.JsonToObject;
import org.mule.module.json.transformers.ObjectToJson;
import org.mule.processor.ResponseMessageProcessorAdapter;
import org.mule.routing.ChoiceRouter;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.http.transformers.FormTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Map;

public class QueryService extends AbstractFlowConstruct {

    private final String query;

    public QueryService(String name, String query, 
                        MessageSource messageSource, MuleContext muleContext) {
        super(name, muleContext);
        setMessageSource(messageSource);
        this.query = query;
    }

    @Override
    protected void configureMessageProcessors(MessageProcessorChainBuilder builder) throws MuleException {
        final JsonToObject jsonArrayToObject = new JsonToObject();
        jsonArrayToObject.setReturnDataType(DataTypeFactory.create(Map[].class));
        jsonArrayToObject.setMuleContext(muleContext);
        jsonArrayToObject.initialise();
        
        final JsonToObject jsonToObject = new JsonToObject();
        jsonToObject.setReturnDataType(DataTypeFactory.create(Map.class));
        jsonToObject.setMuleContext(muleContext);
        jsonToObject.initialise();
        
        builder.chain(new MessageProcessor() {

            public MuleEvent process(MuleEvent event) throws MuleException {
                PushbackInputStream stream = new PushbackInputStream(event.getMessage().getPayload(InputStream.class));
                try {
                    int firstChar = stream.read();
                    if (firstChar == '[') {
                        stream.unread(firstChar);
                        MuleEvent event2 = jsonArrayToObject.process(event);
                        System.out.println(event2.getMessage().getPayload());
                                                return event2;
                    } else {
                        return jsonToObject.process(event);
                    }
                } catch (IOException e) {
                    throw new DefaultMuleException(e);
                }
            }
            
        });
        builder.chain(new ResponseMessageProcessorAdapter(new ObjectToJson()));
        
        Filter formFilter = getFormFilter();
        FormTransformer formTransformer = new FormTransformer();
        ChoiceRouter choiceRouter = new ChoiceRouter();
        choiceRouter.addRoute(formTransformer, formFilter);
        
        MqlTransformer mqlTransformer = new MqlTransformer();
        mqlTransformer.setQuery(query);
        builder.chain(mqlTransformer);
    }

    protected Filter getFormFilter() {
        Filter formFilter = new Filter() {
            
            public boolean accept(MuleMessage msg) {
                Object ct = msg.getInboundProperty("Content-Type");
                if (ct != null && ct.toString().contains("multipart/form-data")) {
                    return true;
                }
                return false;
            }
        };
        return formFilter;
    }

    @Override
    public String getConstructType() {
        return "Query-Service";
    }

}
