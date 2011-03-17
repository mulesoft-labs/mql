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

import org.mule.api.MuleException;
import org.mule.api.client.MuleClient;

/**
 * Provides a simple API for the query context around the MuleClient.
 */
public class MuleClientWrapper {
    private final MuleClient muleClient;

    public MuleClientWrapper(MuleClient muleClient) {
        super();
        this.muleClient = muleClient;
    }
    
    public Object send(String url, Object payload) throws MuleException {
        return muleClient.send(url, payload, null).getPayload();
    }

    public MuleClient getMuleClient() {
        return muleClient;
    }

}
