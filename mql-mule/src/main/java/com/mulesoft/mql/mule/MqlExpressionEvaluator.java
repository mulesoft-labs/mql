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

import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionEvaluator;

import com.mulesoft.mql.Query;

import java.util.Map;

public class MqlExpressionEvaluator implements ExpressionEvaluator {

    public void setName(String name) {
        throw new UnsupportedOperationException("setName");
    }

    public String getName() {
        return "mql";
    }

    public Object evaluate(String expression, MuleMessage message) {
        Map<String,Object> context = new MuleMessageQueryContext(message);
        context.put("payload", message.getPayload());
        context.put("message", message);

        // TODO
//        MuleClientWrapper clientWrapper = new MuleClientWrapper(event, muleClient, muleContext);
//        context.put("mule", clientWrapper);
//        context.put("payload", message.getPayload());
        
        Query query = Query.create(expression);
        query.setDefaultSelectObject("payload");
        return query.execute(context);
    }

}
