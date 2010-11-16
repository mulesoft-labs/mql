package com.mulesoft.mql.impl;

import java.text.MessageFormat;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.Predicate;

import com.mulesoft.mql.QueryBuilder;
import com.mulesoft.mql.QueryException;
import com.mulesoft.mql.Restriction;
import com.mulesoft.mql.Restriction.Property;

public class WherePredicate implements Predicate {
    private final QueryBuilder queryBuilder;

    public WherePredicate(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public boolean evaluate(Object object) {
        Restriction r = queryBuilder.getRestriction();
        
        return evaluate(object, r);
    }

    private boolean evaluate(Object object, Restriction r) {
        switch (r.getOperator()) {
        case EQUALS:
            return equals(object, r);
        case OR:
            return or(object, r);
        case AND:
            return and(object, r);
        case GT:
            return evaluateComparison(object, r) > 0;
        case GTE:
            return evaluateComparison(object, r) >= 0;
        case LT:
            return evaluateComparison(object, r) < 0;
        case LTE:
            return evaluateComparison(object, r) <= 0;
        }
        
        return false;
    }

    private boolean and(Object object, Restriction r) {
        Object left = r.getLeft();
        Object right = r.getRight();
        
        if (!(left instanceof Restriction)) {
            throw new QueryException(MessageFormat.format("Left statement of an 'and' restriction must be another restriction. Found {0} instead", left));
        }
        
        if (!(right instanceof Restriction)) {
            throw new QueryException(MessageFormat.format("Right statement of an 'and' restriction must be another restriction. Found {0} instead", right));
        }
        
        return evaluate(object, (Restriction)left) && evaluate(object, (Restriction) right);
    }

    private boolean or(Object object, Restriction r) {
        Object left = r.getLeft();
        Object right = r.getRight();
        
        if (!(left instanceof Restriction)) {
            throw new QueryException(MessageFormat.format("Left statement of an 'or' restriction must be another restriction. Found {0} instead", left));
        }
        
        if (!(right instanceof Restriction)) {
            throw new QueryException(MessageFormat.format("Right statement of an 'or' restriction must be another restriction. Found {0} instead", right));
        }
        
        return evaluate(object, (Restriction)left) || evaluate(object, (Restriction) right);
    }

    private boolean equals(Object object, Restriction r) {
        Object left = evaluate(object, r.getLeft());
        Object right = evaluate(object, r.getRight());
        
        if (left != null && right != null) {
            return left.equals(right);
        } else if (left == null && right == null) {
            return true;
        } else {
            return false;
        }
    }

    // TODO: this won't work unless the left and right are the same Class. Damn you Java.
    private int evaluateComparison(Object object, Restriction r) {
        Object left = evaluate(object, r.getLeft());
        Object right = evaluate(object, r.getRight());
        
        if (left != null && right != null) {
            return ((Comparable)left).compareTo(right);
        } else if (left == null && right == null) {
            return 0;
        } else {
            // TODO: not sure what to do here. Throwing an exception for now so a real user can complain about it
            throw new QueryException(MessageFormat.format("Left could not be compared to write. Left: {0}, Right: {1}.", left, right));
        }
    }

    private Object evaluate(Object object, Object expression) {
        if (expression instanceof Property) {
            try {
                return PropertyUtils.getProperty(object, ((Property) expression).getName());
            } catch (NoSuchMethodException e) {
                throw new QueryException(MessageFormat.format("Property {0} does note exist on class {1}", expression, object.getClass()));
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
        } else {
            return expression;
        }
    }
}