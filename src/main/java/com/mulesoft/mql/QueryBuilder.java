package com.mulesoft.mql;


public class QueryBuilder {
    
    protected String as;
    protected String orderBy;
    protected int max = -1;
    protected ObjectBuilder select;
    private Restriction restriction;

    public QueryBuilder where(Restriction restriction) {
        this.restriction = restriction;
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
    
}
