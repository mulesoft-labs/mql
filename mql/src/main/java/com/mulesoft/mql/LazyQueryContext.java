/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.mql;

import java.util.HashMap;

/**
 * Provides a simple class which you can extends to lazily resolve objects
 * for the query context.
 */
public abstract class LazyQueryContext extends HashMap<String, Object> {


    @Override
    public boolean containsKey(Object key) {
        boolean containsKey = super.containsKey(key);
        if (!containsKey) {
            Object loaded = load((String)key);
            return loaded != null;
        }
        return containsKey;
    }

    public abstract Object load(String key);

    @Override
    public Object get(Object key) {
        Object object = super.get(key);
        
        if (object == null) {
            object = load((String) key);
            if (object != null) {
                put((String)key, object);
            }
        }
        return object;
    }
}
