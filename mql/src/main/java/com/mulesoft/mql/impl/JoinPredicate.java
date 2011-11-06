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

import com.mulesoft.mql.JoinBuilder;
import com.mulesoft.mql.JoinBuilder.JoinExpression;
import com.mulesoft.mql.QueryBuilder;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.mvel2.MVEL;

public class JoinPredicate implements Predicate {

    private Map<JoinExpression,Serializable> compiledExpressions = new LinkedHashMap<JoinExpression,Serializable>();
    private Map<JoinExpression,Serializable> onExpressions = new LinkedHashMap<JoinExpression,Serializable>();
    private JoinBuilder join;

    public JoinPredicate(QueryBuilder queryBuilder) {
        join = queryBuilder.getJoin();
        
        for (JoinExpression e : join.getExpressions()) {
            if (e.getOn() != null) {
                onExpressions.put(e, MVEL.compileExpression(e.getOn()));
            }
            compiledExpressions.put(e, MVEL.compileExpression(e.getExpression()));
        }
    }

    public boolean evaluate(Object object) {
        Map<String,Object> vars = (Map<String, Object>) object;
        
        for (Map.Entry<JoinExpression,Serializable> entry : compiledExpressions.entrySet()) {
            try {
                Serializable onExpression = onExpressions.get(entry.getKey());
                if (onExpression != null) {
                    // Execute the on expression. Continue as long as it doesn't return null or false.
                    Object result = MVEL.executeExpression(onExpression, vars);
                    
                    if (result == null || Boolean.FALSE.equals(result)) {
                        return false;
                    }
                }
            } catch (RuntimeException e) {
                throw new RuntimeException("Could not execute expression " + entry.getKey().getExpression() + " on context " + object, e);
            }
            
            try {
                // Execute the expression and shove the result back in as the context variable
                Object result = MVEL.executeExpression(entry.getValue(), vars);
                
                vars.put(entry.getKey().getAs(), result);
            } catch (RuntimeException e) {
                throw new RuntimeException("Could not execute expression " + entry.getKey().getExpression() + " on context " + object, e);
            }
        
        }
        return true;
    }

}
