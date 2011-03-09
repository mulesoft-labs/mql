package com.mulesoft.mql.impl;

import com.mulesoft.mql.QueryBuilder;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import org.mvel2.MVEL;

public class OrderByComparator implements Comparator<Map<String,Object>> {

    private final QueryBuilder queryBuilder;
    private Serializable expression;

    public OrderByComparator(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        expression = MVEL.compileExpression(queryBuilder.getOrderBy());
    }

    public int compare(Map<String,Object> o1, Map<String,Object> o2) {
        Object r1 = MVEL.executeExpression(expression, o1.get(queryBuilder.getAs()), o1);
        Object r2 = MVEL.executeExpression(expression, o1.get(queryBuilder.getAs()), o1);
        
        if (r1 instanceof Comparable && r2 instanceof Comparable) {
            return ((Comparable)r1).compareTo(r2);
        }
        
        if (r1 == null && r2 == null) {
            return 0;
        }
        
        if (r1 == null) {
            return -1;
        }

        if (r2 == null) {
            return 1;
        }
        
        return r1.toString().compareTo(r2.toString());
    }

}
