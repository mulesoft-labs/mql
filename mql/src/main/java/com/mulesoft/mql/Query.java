package com.mulesoft.mql;

import com.mulesoft.mql.grammar.lexer.Lexer;
import com.mulesoft.mql.grammar.lexer.LexerException;
import com.mulesoft.mql.grammar.node.Start;
import com.mulesoft.mql.grammar.parser.Parser;
import com.mulesoft.mql.grammar.parser.ParserException;
import com.mulesoft.mql.impl.JoinPredicate;
import com.mulesoft.mql.impl.MqlInterpreter;
import com.mulesoft.mql.impl.OrderByComparator;
import com.mulesoft.mql.impl.WherePredicate;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.collections.functors.TruePredicate;
import org.mvel.MVEL;

public class Query {

    private final QueryBuilder queryBuilder;
    private Map<String,Serializable> compiledExpressions = new HashMap<String,Serializable>();
    private Predicate joinPredicate;
    private Predicate wherePredicate;

    public Query(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;

        ObjectBuilder select = queryBuilder.getSelect();
        if (select != null) {
            // Compile MVEL expressions
            for (Map.Entry<String,String> e : select.getPropertyToExpression().entrySet()) {
                compiledExpressions.put(e.getKey(), MVEL.compileExpression(e.getValue()));
            }
        }


        joinPredicate = getJoin();
        wherePredicate = getWhere();
    }

    public static <T> T execute(String queryString, Map<String,Object> context) {
        Lexer lexer = new Lexer(new PushbackReader(new StringReader(queryString)));
        Parser parser = new Parser(lexer);
        
        try {
            Start ast = parser.parse();

            /* Get our Interpreter going. */
            MqlInterpreter interpreter = new MqlInterpreter();
            ast.apply(interpreter);
            
            Query query = interpreter.getQuery();
            
            return query.execute(context);
        } catch (ParserException e) {
            throw new QueryException(e);
        } catch (LexerException e) {
            throw new QueryException(e);
        } catch (IOException e) {
            throw new QueryException(e);
        }
    }
    
    public <T> T execute(Collection<?> items) {
        Map<String,Object> context = new HashMap<String,Object>();
        context.put("items", items);
        return execute(context);
    }
    
    public <T> T execute(Map<String,Object> context) {
        Collection<?> items;
        Object from = context.get(queryBuilder.getFrom());
        boolean selectSingleObject = false;
        
        if (from instanceof Collection) {
            // transform a collection of objects
            items = (Collection<?>) from;
        } else {
            // support transformation on a single object
            items = Arrays.asList(from);
            selectSingleObject = true;
        }
        
        List<Map<String,Object>> itemsAsMaps = new ArrayList<Map<String,Object>>();
        for (Object o : items) {
            if (o == null) {
                throw new IllegalStateException("null items are not allowed in the list of queryable objects.");
            }
            Map<String, Object> vars = new HashMap<String,Object>();
            vars.putAll(context);
            vars.put(queryBuilder.getAs(), o);
            itemsAsMaps.add(vars);
        }
        
        ArrayList resultList = new ArrayList();

        joinAndFilter(itemsAsMaps, resultList);
        order(resultList);
        resultList = doSelect(resultList);
        
        if (selectSingleObject) {
            return (T) (resultList.size() > 0 ? resultList.get(0) : null);
        }
        
        return (T) resultList;
    }

    protected void order(ArrayList resultList) {
        // order the items 
        if (queryBuilder.getOrderBy() != null) {
            Collections.sort(resultList, new OrderByComparator(queryBuilder));
        }
    }

    protected void joinAndFilter(List<Map<String, Object>> itemsAsMaps, ArrayList resultList) {
        Predicate predicate = AndPredicate.getInstance(joinPredicate, wherePredicate);
        for (int i = 0; i < itemsAsMaps.size() && i < queryBuilder.getMax(); i++) {
            Map<String, Object> object = itemsAsMaps.get(i);
            if (predicate.evaluate(object)) {
                resultList.add(object);
            }
        }
    }

    protected ArrayList doSelect(ArrayList list) {
        // transform the items
        ObjectBuilder select = queryBuilder.getSelect();
        if (select != null) {
            // Transform individual objects
            ArrayList transformedObjects = new ArrayList();
            for (Object o : list) {
                Map<String, Object> vars = (Map<String,Object>) o;
                
                Object transform;
                if (select.getTransformClass() == null) {
                    transform = transformToMap(vars);
                } else {
                    transform = transformToPojo(select.getTransformClass(), vars);
                }
                transformedObjects.add(transform);
            }
            
            list = transformedObjects;
        }
        return list;
    }

    protected Predicate getJoin() {
        final Predicate joinPredicate;
        if (queryBuilder.getJoin() != null) {
            joinPredicate = new JoinPredicate(queryBuilder);
        } else {
            joinPredicate = TruePredicate.INSTANCE;
        }
        return joinPredicate;
    }

    protected Predicate getWhere() {
        final Predicate wherePredicate;
        if (queryBuilder.getRestriction() != null) {
            wherePredicate = new WherePredicate(queryBuilder);
        } else {
            wherePredicate = TruePredicate.INSTANCE;
        }
        return wherePredicate;
    }
    
    private Object transformToPojo(String clsName, Map<String, Object> vars) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> cls = cl.loadClass(clsName);
            Constructor<?> constructor = cls.getConstructor();
            
            Object t = constructor.newInstance();
            for (Map.Entry<String,Serializable> e : compiledExpressions.entrySet()) {
                PropertyUtils.setProperty(t, e.getKey(), MVEL.executeExpression(e.getValue(), vars));
            }
            return t;
        } catch (ClassNotFoundException e1) {
            throw new QueryException(MessageFormat.format("Select class {0} was not found.", clsName), e1);
        } catch (NoSuchMethodException e) {
            throw new QueryException(MessageFormat.format("Class {0} did not have an empty constructor.", clsName), e);
        } catch (Exception e) {
            throw new QueryException(e);
        }
    }

    private Map<String, Object> transformToMap(Map<String, Object> vars) {
        Map<String, Object> t = new HashMap<String,Object>();
        
        for (Map.Entry<String,Serializable> e : compiledExpressions.entrySet()) {
            t.put(e.getKey(), MVEL.executeExpression(e.getValue(), vars));
        }
        return t;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("from xxxx");
        
        if (queryBuilder.as != null) {
            builder.append(" as ").append(queryBuilder.as);
        }
        if (queryBuilder.getRestriction() != null) {
            builder.append(" where ").append(queryBuilder.getRestriction());
        }
        return builder.toString();
    }
}
