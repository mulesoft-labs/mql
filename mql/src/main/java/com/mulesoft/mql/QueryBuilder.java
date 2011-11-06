package com.mulesoft.mql;

import com.mulesoft.mql.JoinBuilder.JoinExpression;

import java.util.concurrent.Executor;


public class QueryBuilder {
    
    protected String from;
    protected String as = "item";
    protected String orderBy;
    protected int max = Integer.MAX_VALUE;
    protected ObjectBuilder select;
    private Restriction restriction;
    private JoinBuilder join;
    private boolean singleResult;
    
    public QueryBuilder where(Restriction restriction) {
        this.restriction = restriction;
        return this;
    }

    public QueryBuilder from(String from) {
        this.from = from;
        return this;
    }
    
    public QueryBuilder as(String property) {
        this.as = property;
        return this;
    }

    public QueryBuilder orderby(String property) {
        this.orderBy = property;
        return this;
    }
    

    public QueryBuilder max(int max) {
        this.max = max;
        return this;
    }

    public QueryBuilder select(ObjectBuilder objectBuilder) {
        this.select = objectBuilder;
        return this;
    }
    
    public Query build() {
        return new Query(this);
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getAs() {
        return as;
    }

    public int getMax() {
        return max;
    }

    public ObjectBuilder getSelect() {
        return select;
    }

    public Restriction getRestriction() {
        return restriction;
    }

    public String getFrom() {
        return from;
    }

    public QueryBuilder join(JoinBuilder join) {
        this.join = join;
        return this;
    }

    public QueryBuilder join(String expression, String as) {
        join = new JoinBuilder();
        join.expression(new JoinExpression(expression).as(as));
        return this;
    }
    
    /**
     * Asynchronously join with a default of 10 threads.
     */
    public QueryBuilder joinAsync(String expression, String as) {
        return joinAsync(expression, as, 10);
    }
    
    /**
     * Asynchronously join with the specified number of threads.
     */
    public QueryBuilder joinAsync(String expression, String as, int threads) {
        this.join = JoinBuilder.expression(expression, as).async().threads(threads);
        return this;
    }
    
    /**
     * Asynchronously join with the specified number of threads.
     */
    public QueryBuilder joinAsync(String expression, String as, Executor executor) {
        join = new JoinBuilder()
            .expression(new JoinExpression(expression).as(as))
            .async()
            .executor(executor);
        return this;
    }

    public JoinBuilder getJoin() {
        return join;
    }

    public QueryBuilder singleResult() {
        singleResult = true;
        return this;
    }

    public boolean isSingleResult() {
        return singleResult;
    }
    
}
