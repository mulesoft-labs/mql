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

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JoinBuilder {
    private String expression;
    private String as;
    private String on;
    private boolean async;
    private int threads;
    private Executor executor;
    
    public static JoinBuilder expression(String expression, String as) {
        return new JoinBuilder()
            .expression(expression)
            .as(as);
    }
    
    public String getExpression() {
        return expression;
    }
    
    public JoinBuilder expression(String expression) {
        this.expression = expression;
        return this;
    }
    
    public String getAs() {
        return as;
    }
    
    public JoinBuilder as(String as) {
        this.as = as;
        return this;
    }

    public String getOn() {
        return on;
    }

    public JoinBuilder on(String on) {
        this.on = on;
        return this;
    }

    public boolean isAsync() {
        return async;
    }

    public JoinBuilder async() {
        this.async = true;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    public JoinBuilder threads(int threads) {
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
    
}
