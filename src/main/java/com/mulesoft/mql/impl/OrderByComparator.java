package com.mulesoft.mql.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import com.mulesoft.mql.QueryBuilder;

public class OrderByComparator implements Comparator<Object> {

    private final QueryBuilder queryBuilder;

    public OrderByComparator(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public int compare(Object o1, Object o2) {
        if (o1 instanceof Map) {
            Map m1 = (Map) o1;
            Map m2 = (Map) o2;
            
            // what if these properties return Maps? This code won't work. 
            return compare(m1.get(queryBuilder.getOrderBy()), m2.get(queryBuilder.getOrderBy()));
        } else {
            try {
                Object p1 = PropertyUtils.getProperty(o1, queryBuilder.getOrderBy());
                Object p2 = PropertyUtils.getProperty(o2, queryBuilder.getOrderBy());
                
                if (p1 == null && p2 == null) {
                    return 0;
                } else if (p1 == null && p2 != null) {
                    return -1;
                } else if (p1 != null && p2 == null) {
                    return 1;
                } else if (p1 instanceof Comparable && p2 instanceof Comparable) {
                    return ((Comparable) p1).compareTo(p2);
                } else {
                    return p1.toString().compareTo(p2.toString());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
