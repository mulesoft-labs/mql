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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JoinBuilder {
    private boolean async = true;
    private int threads = 10;
    private Executor executor;
    private List<JoinExpression> expressions = new LinkedList<JoinExpression>();
    
    public static JoinBuilder expression(String expression, String as) {
        return new JoinBuilder()
            .expression(new JoinExpression(expression).as(as));
    }
    
    public JoinBuilder expression(String expression) {
        return expression(new JoinExpression(expression));
    }
    
    public JoinBuilder expression(JoinExpression expression) {
        this.expressions.add(expression);
        return this;
    }

    public List<JoinExpression> getExpressions() {
        return expressions;
    }

    public boolean isAsync() {
        return async;
    }

    public JoinBuilder async() {
        this.async = true;
        return this;
    }

    public JoinBuilder sync() {
        this.async = false;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    public JoinBuilder threads(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("Thread count for an async join can not be less than one.");
        }
        
        this.threads = threads;
        return this;
    }

    public JoinBuilder executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public Executor getExecutor() {
        if (executor == null && async) {
            executor = new ThreadPoolExecutor(threads, threads, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        }
        return executor;
    }
 
    public static class JoinExpression {
        private String expression;
        private String as;
        private String on;

        public JoinExpression(String expression) {
            this.expression = expression;
        }

        public JoinExpression() {
            super();
        }

        public String getExpression() {
            return expression;
        }
        
        public String getAs() {
            return as;
        }
        
        public JoinExpression as(String as) {
            this.as = as;
            return this;
        }

        public String getOn() {
            return on;
        }

        public JoinExpression on(String on) {
            this.on = on;
            return this;
        }
    }
}
