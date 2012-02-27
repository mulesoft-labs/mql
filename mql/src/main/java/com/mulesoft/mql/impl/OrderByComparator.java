package com.mulesoft.mql.impl;

import com.mulesoft.mql.QueryBuilder;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import org.mvel2.MVEL;

public class OrderByComparator implements Comparator<Object> {

    private final QueryBuilder queryBuilder;
    private Serializable expression;

    public OrderByComparator(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        expression = MVEL.compileExpression(queryBuilder.getOrderBy());
    }

     public int compare(Object o1, Object o2) {
        
    	Object r1 = null, r2 = null;
    	if(o1 instanceof Map && o2 instanceof Map) {
    		Map<String,Object> o1Map = (Map<String,Object>) o1;
    		Map<String,Object> o2Map = (Map<String,Object>)  o2;
    		 r1 = MVEL.executeExpression(expression,o1Map.get(queryBuilder.getAs()), o1Map);
             r2 = MVEL.executeExpression(expression, o2Map.get(queryBuilder.getAs()), o2Map);
           
    	}else {
    		 r1 = MVEL.executeExpression(expression,  o1);
             r2 = MVEL.executeExpression(expression,  o2);
           
    	}
    		
         
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
