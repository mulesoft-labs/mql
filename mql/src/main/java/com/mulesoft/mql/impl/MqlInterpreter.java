package com.mulesoft.mql.impl;

import static com.mulesoft.mql.JoinBuilder.expression;
import static com.mulesoft.mql.ObjectBuilder.newObject;
import static com.mulesoft.mql.Restriction.and;
import static com.mulesoft.mql.Restriction.or;

import com.mulesoft.mql.JoinBuilder;
import com.mulesoft.mql.ObjectBuilder;
import com.mulesoft.mql.Query;
import com.mulesoft.mql.QueryBuilder;
import com.mulesoft.mql.Restriction;
import com.mulesoft.mql.grammar.analysis.DepthFirstAdapter;
import com.mulesoft.mql.grammar.node.AAndWhereExpression;
import com.mulesoft.mql.grammar.node.AAsStatement;
import com.mulesoft.mql.grammar.node.AAsyncStatement;
import com.mulesoft.mql.grammar.node.AEqualsComparator;
import com.mulesoft.mql.grammar.node.AFullQuery;
import com.mulesoft.mql.grammar.node.AGtComparator;
import com.mulesoft.mql.grammar.node.AGteComparator;
import com.mulesoft.mql.grammar.node.AJoinJoinStatement;
import com.mulesoft.mql.grammar.node.ALikeComparator;
import com.mulesoft.mql.grammar.node.ALtComparator;
import com.mulesoft.mql.grammar.node.ALteComparator;
import com.mulesoft.mql.grammar.node.AOnStatement;
import com.mulesoft.mql.grammar.node.AOrWhereExpression;
import com.mulesoft.mql.grammar.node.ASelectNewItem;
import com.mulesoft.mql.grammar.node.ASelectNewItemProperty;
import com.mulesoft.mql.grammar.node.ASelectOnlyQuery;
import com.mulesoft.mql.grammar.node.AThreadStatement;
import com.mulesoft.mql.grammar.node.AVariableWhereSide;
import com.mulesoft.mql.grammar.node.AWhereClause;
import com.mulesoft.mql.grammar.node.PWhereSide;

import java.util.Stack;

public class MqlInterpreter extends DepthFirstAdapter {

    private QueryBuilder queryBuilder;
    private Stack<Restriction> restrictions = new Stack<Restriction>();
    private ObjectBuilder objectBuilder;
    private JoinBuilder join;

    @Override
    public void caseAFullQuery(AFullQuery node) {
        queryBuilder = new QueryBuilder();
        queryBuilder.from(parseSpaces(node.getFromvar().toString()));

        super.caseAFullQuery(node);
        
        if (restrictions.size() == 1) {
            queryBuilder.where(restrictions.pop());
        } else if (restrictions.size() > 0) {
            throw new IllegalStateException("Too many restrictions!");
        }
    }

    @Override
    public void caseAAsStatement(AAsStatement node) {
        queryBuilder.as(node.getAsvar().getText());
        super.caseAAsStatement(node);
    }

    @Override
    public void caseASelectOnlyQuery(ASelectOnlyQuery node) {
        queryBuilder = new QueryBuilder();
        super.caseASelectOnlyQuery(node);
    }


    @Override
    public void inAJoinJoinStatement(AJoinJoinStatement node) {
        join = expression(parseSpaces(node.getJoinexpression().toString()), node.getAsvar().getText());
        queryBuilder.join(join);

        super.inAJoinJoinStatement(node);
    }

    @Override
    public void caseAOnStatement(AOnStatement node) {
        join.on(parseSpaces(node.getOnExpression().toString()));
        super.caseAOnStatement(node);
    }

    @Override
    public void caseAAsyncStatement(AAsyncStatement node) {
        join.async();
        super.caseAAsyncStatement(node);
    }

    @Override
    public void caseAThreadStatement(AThreadStatement node) {
        join.threads(Integer.valueOf(node.getThreadCount().toString().trim()));
        super.caseAThreadStatement(node);
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
        } else if (node.getComparator() instanceof ALteComparator) {
            restrictions.add(Restriction.lte(leftObj, rightObj));
        } else if (node.getComparator() instanceof AGtComparator) {
            restrictions.add(Restriction.gt(leftObj, rightObj));
        } else if (node.getComparator() instanceof AGteComparator) {
            restrictions.add(Restriction.gte(leftObj, rightObj));
        } else if (node.getComparator() instanceof ALikeComparator) {
            restrictions.add(Restriction.like(leftObj, rightObj));
        } else {
            throw new IllegalStateException("unsupported comparator " + node.getComparator().getClass().getName());
        }
        
        super.caseAWhereClause(node);
    }

    private Object getRestrictedObject(PWhereSide parsed) {
        String side = parsed.toString().trim();
        if (parsed instanceof AVariableWhereSide) {
            side = side.replace(" ", ""); // hack
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
        String javaExpression = node.getEqualsExpression().toString();
        javaExpression = parseSpaces(javaExpression); //hack
        objectBuilder.set(node.getIdentifier().getText(), javaExpression);
        super.caseASelectNewItemProperty(node);
    }

    /**
     * Hack to parse out spaces from everything but string literals.
     */
    private String parseSpaces(String expr)
    {
        int start = 0;
        return parseSpaces(expr, start);
    }

    private String parseSpaces(String expr, int start)
    {
        int singleQuoteEnd = expr.indexOf('\'', start);
        int doubleQuoteEnd = expr.indexOf('"', start);
        if (singleQuoteEnd == -1) {
            singleQuoteEnd = expr.length();
        }
        if (doubleQuoteEnd == -1) {
            doubleQuoteEnd = expr.length();
        }
        int end = Math.min(singleQuoteEnd, doubleQuoteEnd);
        
        String newExpr = expr.substring(0, start) 
            + expr.substring(start, end).replace(" ", "") ;
        
        if (end == expr.length())
        {
            return newExpr;
        }
        else
        {
            newExpr = newExpr + expr.substring(end);
            int newStart;
            if (singleQuoteEnd < doubleQuoteEnd) {
                newStart = newExpr.indexOf('\'', start);
            } else {
                newStart = newExpr.indexOf('\"', start);
            }
            newStart += 3;
            return parseSpaces(newExpr, newStart);
        }
    }

    @Override
    public void caseALtComparator(ALtComparator node) {
        super.caseALtComparator(node);
    }

    public Query getQuery() {
        return queryBuilder.build();
    }
}
