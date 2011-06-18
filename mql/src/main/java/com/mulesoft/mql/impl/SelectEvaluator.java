/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.mql.impl;

import com.mulesoft.mql.ObjectBuilder;
import com.mulesoft.mql.QueryBuilder;
import com.mulesoft.mql.QueryException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.mvel2.MVEL;

/**
 * Wraps up the logic for the select statement. Builds new objects using info from ObjectBuilders.
 */
public class SelectEvaluator {
    private Map<String,Serializable> compiledExpressions = new HashMap<String,Serializable>();
    private Map<String,SelectEvaluator> objectProperties = new HashMap<String,SelectEvaluator>();
    private final QueryBuilder queryBuilder;
    private final ObjectBuilder objectBuilder;

    public SelectEvaluator(QueryBuilder queryBuilder, ObjectBuilder objectBuilder) {
        super();
        this.queryBuilder = queryBuilder;
        this.objectBuilder = objectBuilder;
        
        // Compile MVEL expressions and select evaluators
        for (Map.Entry<String,Object> e : objectBuilder.getPropertyToValues().entrySet()) {
            Object value = e.getValue();
            String propertyName = e.getKey();
            if (value instanceof String) {
                compiledExpressions.put(propertyName, MVEL.compileExpression(value.toString()));
            } else if (value instanceof ObjectBuilder) {
                objectProperties.put(propertyName, new SelectEvaluator(queryBuilder, (ObjectBuilder) value));
            } else {
                throw new IllegalStateException("Unrecognized value type for property " + propertyName);
            }
        }
    }

    private Object transformToPojo(String clsName, Map<String, Object> vars) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> cls = cl.loadClass(clsName);
            Constructor<?> constructor = cls.getConstructor();
            
            Object t = constructor.newInstance();
            for (Map.Entry<String,Serializable> e : compiledExpressions.entrySet()) {
                PropertyUtils.setProperty(t, e.getKey(), MVEL.executeExpression(e.getValue(), vars));
            }
            return t;
        } catch (ClassNotFoundException e1) {
            throw new QueryException(MessageFormat.format("Select class {0} was not found.", clsName), e1);
        } catch (NoSuchMethodException e) {
            throw new QueryException(MessageFormat.format("Class {0} did not have an empty constructor.", clsName), e);
        } catch (Exception e) {
            throw new QueryException(e);
        }
    }

    private Map<String, Object> transformToMap(Map<String, Object> vars) {
        Map<String, Object> t = new HashMap<String,Object>();
        
        for (Map.Entry<String,Serializable> e : compiledExpressions.entrySet()) {
            t.put(e.getKey(), MVEL.executeExpression(e.getValue(), vars.get(queryBuilder.getAs()), vars));
        }
        
        for (Map.Entry<String,SelectEvaluator> e : objectProperties.entrySet()) {
            t.put(e.getKey(), e.getValue().evaluate(vars));
        }
        
        return t;
    }

    public Object evaluate(Map<String, Object> vars) {
        if (objectBuilder.getTransformClass() == null) {
            return transformToMap(vars);
        } else {
            return transformToPojo(objectBuilder.getTransformClass(), vars);
        }
    }

    

}
