package com.mulesoft.mql.impl;

import static com.mulesoft.mql.ObjectBuilder.newObject;
import static com.mulesoft.mql.Restriction.and;
import static com.mulesoft.mql.Restriction.or;

import com.mulesoft.mql.JoinBuilder;
import com.mulesoft.mql.JoinBuilder.JoinExpression;
import com.mulesoft.mql.ObjectBuilder;
import com.mulesoft.mql.Query;
import com.mulesoft.mql.QueryBuilder;
import com.mulesoft.mql.Restriction;
import com.mulesoft.mql.grammar.analysis.DepthFirstAdapter;
import com.mulesoft.mql.grammar.node.AAndWhereExpression;
import com.mulesoft.mql.grammar.node.AAsStatement;
import com.mulesoft.mql.grammar.node.AAsyncAsyncStatement;
import com.mulesoft.mql.grammar.node.AClassParens;
import com.mulesoft.mql.grammar.node.AEqualsComparator;
import com.mulesoft.mql.grammar.node.AFullQuery;
import com.mulesoft.mql.grammar.node.AGtComparator;
import com.mulesoft.mql.grammar.node.AGteComparator;
import com.mulesoft.mql.grammar.node.AJoinExpression;
import com.mulesoft.mql.grammar.node.AJoinJoinStatement;
import com.mulesoft.mql.grammar.node.ALikeComparator;
import com.mulesoft.mql.grammar.node.ALtComparator;
import com.mulesoft.mql.grammar.node.ALteComparator;
import com.mulesoft.mql.grammar.node.ANotEqualsComparator;
import com.mulesoft.mql.grammar.node.AOnStatement;
import com.mulesoft.mql.grammar.node.AOrWhereExpression;
import com.mulesoft.mql.grammar.node.ASelectMvelPropertySelectNewItemProperty;
import com.mulesoft.mql.grammar.node.ASelectNewListSelectNewItemProperty;
import com.mulesoft.mql.grammar.node.ASelectNewObjectSelectNewItemProperty;
import com.mulesoft.mql.grammar.node.ASelectNewSelectStatement;
import com.mulesoft.mql.grammar.node.ASelectOnlyQuery;
import com.mulesoft.mql.grammar.node.ASyncAsyncStatement;
import com.mulesoft.mql.grammar.node.AThreadStatement;
import com.mulesoft.mql.grammar.node.AVariableWhereSide;
import com.mulesoft.mql.grammar.node.AWhereClause;
import com.mulesoft.mql.grammar.node.PWhereSide;

import java.util.Stack;

public class MqlInterpreter extends DepthFirstAdapter {

    private Stack<QueryBuilder> queryBuilders = new Stack<QueryBuilder>();
    private Stack<Restriction> restrictions = new Stack<Restriction>();
    private Stack<ObjectBuilder> objectBuilder = new Stack<ObjectBuilder>();
    private JoinBuilder join;
    private JoinExpression joinExpression;

    @Override
    public void caseAFullQuery(AFullQuery node) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.from(parseSpaces(node.getFromvar().toString()));
        queryBuilders.push(queryBuilder);
        
        super.caseAFullQuery(node);
        
        if (restrictions.size() == 1) {
            queryBuilder.where(restrictions.pop());
        } else if (restrictions.size() > 0) {
            throw new IllegalStateException("Too many restrictions!");
        }
    }

    @Override
    public void caseAAsStatement(AAsStatement node) {
        queryBuilders.peek().as(node.getAsvar().getText());
        super.caseAAsStatement(node);
    }

    @Override
    public void caseASelectOnlyQuery(ASelectOnlyQuery node) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilders.push(queryBuilder);
        super.caseASelectOnlyQuery(node);
    }


    @Override
    public void inAJoinJoinStatement(AJoinJoinStatement node) {
        join = new JoinBuilder();
        queryBuilders.peek().join(join);

        super.inAJoinJoinStatement(node);
    }

    @Override
    public void caseAJoinExpression(AJoinExpression node) {
         joinExpression = new JoinExpression(parseSpaces(node.getJoinexpression().toString())).as(node.getAsvar().getText());
        join.expression(joinExpression);
        
        super.caseAJoinExpression(node);
    }

    @Override
    public void caseAOnStatement(AOnStatement node) {
        joinExpression.on(parseSpaces(node.getOnExpression().toString()));
        super.caseAOnStatement(node);
    }

    @Override
    public void caseAAsyncAsyncStatement(AAsyncAsyncStatement node) {
        join.async();
        super.caseAAsyncAsyncStatement(node);
    }

    @Override
    public void outASyncAsyncStatement(ASyncAsyncStatement node) {
        join.sync();
        super.outASyncAsyncStatement(node);
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
        }  else if (node.getComparator() instanceof ANotEqualsComparator) {
            restrictions.add(Restriction.not(Restriction.eq(leftObj, rightObj)));
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
    public void caseASelectNewSelectStatement(ASelectNewSelectStatement node) {
        QueryBuilder qb = queryBuilders.peek();
        if (qb.getSelect() == null) {
            objectBuilder.push(newObject());
            qb.select(objectBuilder.peek());
            if (node.getSingle() != null) {
                qb.singleResult();
            }
        }
        super.caseASelectNewSelectStatement(node);
    }
    
    @Override
    public void caseAClassParens(AClassParens node) {
        if (node.getClassName() != null) {
            objectBuilder.peek().setTransformClass(parseSpaces(node.getClassName().toString()));
        }
        super.caseAClassParens(node);
    }
    
    @Override
    public void inASelectMvelPropertySelectNewItemProperty(ASelectMvelPropertySelectNewItemProperty node) {
        String javaExpression = node.getEqualsExpression().toString();
        javaExpression = parseSpaces(javaExpression); //hack
        objectBuilder.peek().set(node.getIdentifier().getText(), javaExpression);
        super.inASelectMvelPropertySelectNewItemProperty(node);
    }

    @Override
    public void inASelectNewObjectSelectNewItemProperty(ASelectNewObjectSelectNewItemProperty node) {
        ObjectBuilder current = objectBuilder.peek();
        ObjectBuilder next = objectBuilder.push(newObject());
        current.set(node.getIdentifier().getText(), next);
        super.inASelectNewObjectSelectNewItemProperty(node);
    }

    @Override
    public void inASelectNewListSelectNewItemProperty(ASelectNewListSelectNewItemProperty node) {
        QueryBuilder childQB = new QueryBuilder();
        childQB.from(parseSpaces(node.getFromvar().toString()));
        queryBuilders.push(childQB);
        ObjectBuilder current = objectBuilder.peek();
        current.set(node.getIdentifier().getText(), childQB);
        super.inASelectNewListSelectNewItemProperty(node);
    }

    @Override
    public void outASelectNewListSelectNewItemProperty(ASelectNewListSelectNewItemProperty node) {
        super.outASelectNewListSelectNewItemProperty(node);
        
        queryBuilders.pop();
    }

    @Override
    public void outASelectNewObjectSelectNewItemProperty(ASelectNewObjectSelectNewItemProperty node) {
        super.outASelectNewObjectSelectNewItemProperty(node);
        objectBuilder.pop();
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
        int quoteStart = getNextQuoteIndex(expr, start);
        int quoteEnd = getNextQuoteIndex(expr, quoteStart+1);
        
        if (quoteStart == -1) { 
            return expr.substring(0, start) + expr.substring(start).replace(" ", "");
        } else if (quoteEnd == -1) {
            return expr;
        } else {
            String part1 = expr.substring(0, start);
            String part2 = expr.substring(start, quoteStart).replace(" ", "");
            String part3 = expr.substring(quoteStart);
            
            String newExpr = part1 + part2 + part3;
            int newStart = getNextQuoteIndex(part3, 1) + part1.length() + part2.length() + 1;
            if (newStart == expr.length()) {
                return newExpr;
            }
            return parseSpaces(newExpr, newStart);
        }
    }

    protected int getNextQuoteIndex(String expr, int start) {
        int quoteStart = expr.indexOf('\'', start);
        if (quoteStart != -1 && (quoteStart == 0 || expr.charAt(quoteStart-1) == '\\')) {
            return getNextQuoteIndex(expr, quoteStart + 1);
        }
        return quoteStart;
    }

    @Override
    public void caseALtComparator(ALtComparator node) {
        super.caseALtComparator(node);
    }

    public Query getQuery() {
        return queryBuilders.peek().build();
    }
}
