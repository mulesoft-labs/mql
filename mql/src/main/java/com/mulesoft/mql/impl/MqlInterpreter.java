package com.mulesoft.mql.impl;

import static com.mulesoft.mql.Restriction.and;
import static com.mulesoft.mql.Restriction.or;
import static com.mulesoft.mql.ObjectBuilder.*;

import java.util.Stack;

import com.mulesoft.mql.ObjectBuilder;
import com.mulesoft.mql.Query;
import com.mulesoft.mql.QueryBuilder;
import com.mulesoft.mql.Restriction;
import com.mulesoft.mql.grammar.analysis.DepthFirstAdapter;
import com.mulesoft.mql.grammar.node.AAndWhereExpression;
import com.mulesoft.mql.grammar.node.AEqualsComparator;
import com.mulesoft.mql.grammar.node.ALtComparator;
import com.mulesoft.mql.grammar.node.AOrWhereExpression;
import com.mulesoft.mql.grammar.node.AQuery;
import com.mulesoft.mql.grammar.node.ASelectNewItem;
import com.mulesoft.mql.grammar.node.ASelectNewItemProperty;
import com.mulesoft.mql.grammar.node.AVariableWhereSide;
import com.mulesoft.mql.grammar.node.AWhereClause;
import com.mulesoft.mql.grammar.node.PWhereSide;

public class MqlInterpreter extends DepthFirstAdapter {

    private QueryBuilder queryBuilder;
    private Stack<Restriction> restrictions = new Stack<Restriction>();
    private ObjectBuilder objectBuilder;

    @Override
    public void caseAQuery(AQuery node) {
        queryBuilder = new QueryBuilder();
        queryBuilder.from(node.getFromvar().getText());
        queryBuilder.as(node.getAsvar().getText());
                   
        super.caseAQuery(node);
        
        if (restrictions.size() == 1) {
            queryBuilder.where(restrictions.pop());
        } else if (restrictions.size() > 0) {
            throw new IllegalStateException("Too many restrictions!");
        }
    }

    @Override
    public void caseAOrWhereExpression(AOrWhereExpression node) {
        super.caseAOrWhereExpression(node);
        
        Restriction right = restrictions.pop();
        Restriction left = restrictions.pop();
        
        restrictions.add(or(left, right));
    }

    @Override
    public void caseAAndWhereExpression(AAndWhereExpression node) {
        super.caseAAndWhereExpression(node);
        
        Restriction right = restrictions.pop();
        Restriction left = restrictions.pop();
        
        restrictions.add(and(left, right));
    }

    @Override
    public void caseAWhereClause(AWhereClause node) {
        Object leftObj = getRestrictedObject(node.getLeft());
        Object rightObj = getRestrictedObject(node.getRight());
        
        if (node.getComparator() instanceof AEqualsComparator) {
            restrictions.add(Restriction.eq(leftObj, rightObj));
        } else if (node.getComparator() instanceof ALtComparator) {
            restrictions.add(Restriction.lt(leftObj, rightObj));
        } else {
            throw new IllegalStateException("unsupported comparator " + node.getComparator().getClass().getName());
        }
        
        super.caseAWhereClause(node);
    }

    private Object getRestrictedObject(PWhereSide parsed) {
        String side = parsed.toString().trim();
        if (parsed instanceof AVariableWhereSide) {
            return Restriction.property(side);
        }
        return side.substring(1, side.length()-1);
    }

    @Override
    public void caseASelectNewItem(ASelectNewItem node) {
        objectBuilder = newObject();
        super.caseASelectNewItem(node);
        queryBuilder.select(objectBuilder);
    }

    @Override
    public void caseASelectNewItemProperty(ASelectNewItemProperty node) {
        objectBuilder.set(node.getBasicVar().getText(), node.getObjectVar().getText());
        super.caseASelectNewItemProperty(node);
    }

    @Override
    public void caseALtComparator(ALtComparator node) {
        super.caseALtComparator(node);
    }

    public Query getQuery() {
        return queryBuilder.build();
    }
    

}
