package com.mulesoft.mql.impl;

import com.mulesoft.mql.QueryBuilder;
import com.mulesoft.mql.QueryException;
import com.mulesoft.mql.Restriction;
import com.mulesoft.mql.Restriction.Property;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.mvel2.MVEL;

public class WherePredicate implements Predicate {
    private final QueryBuilder queryBuilder;
    // Property -> MVEL expressions
    private Map<Property,Serializable> compiledExpressions = new HashMap<Property,Serializable>();

    public WherePredicate(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        
        Restriction restriction = queryBuilder.getRestriction();
        initializeExpressions(restriction);
    }

    private void initializeExpressions(Restriction restriction) {
        initializeExpression(restriction.getLeft());
        initializeExpression(restriction.getRight());
    }

    private void initializeExpression(Object o) {
        if (o instanceof Restriction) {
            initializeExpressions((Restriction) o);
            return;
        }
        if (!(o instanceof Property)) {
            return;
        }
        
        String expression = ((Property) o).getName();
        
        compiledExpressions.put((Property)o, MVEL.compileExpression(expression));
    }

    public boolean evaluate(Object object) {
        Restriction r = queryBuilder.getRestriction();
        
        return evaluate(object, r);
    }

    private boolean evaluate(Object object, Restriction r) {
        switch (r.getOperator()) {
        case EQUALS:
            return equals(object, r);
        case NOT:
            return !evaluate(object, (Restriction)r.getRight());
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
        case LIKE:
            return evaluateLike(object, r);
        }
        
        return false;
    }

    private boolean evaluateLike(Object object, Restriction r) {
        if (r.getLeft() instanceof Restriction) {
            throw new QueryException("The left side of a like clause cannot be a restriction.");
        } else if (r.getRight() instanceof Restriction) {
            throw new QueryException("The right side of a like clause cannot be a restriction.");
        }
        String left = evaluate(object, r.getLeft()).toString();
        String right = evaluate(object, r.getRight()).toString();
        
        return left.toLowerCase().contains(right.toLowerCase());
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
            Serializable compiled = compiledExpressions.get((Property)expression);

            Map<String,Object> vars = (Map<String,Object>) object;
            Object ctx = vars.get(queryBuilder.getAs()); 
            
            return MVEL.executeExpression(compiled, ctx, vars);
        } else {
            return expression;
        }
    }
}