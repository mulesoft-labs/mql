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
import org.mule.api.transport.PropertyScope;

import com.mulesoft.mql.LazyQueryContext;

public class MuleMessageQueryContext extends LazyQueryContext {
    private final MuleMessage message;

    public MuleMessageQueryContext(MuleMessage message) {
        this.message = message;
    }

    @Override
    public Object load(String key) {
        Object object = message.getProperty(key, PropertyScope.OUTBOUND);

        // ugly but it works
        if (object == null) {
            object = message.getProperty(key, PropertyScope.INVOCATION);

            if (object == null) {
                object = message.getProperty(key, PropertyScope.INBOUND);

                if (object == null) {
                    object = message.getMuleContext().getRegistry().lookupObject(key);
                }
            }
        }

        return object;
    }
}