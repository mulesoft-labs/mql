package com.mulesoft.mql;

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
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.mvel.MVEL;

import com.mulesoft.mql.grammar.lexer.Lexer;
import com.mulesoft.mql.grammar.lexer.LexerException;
import com.mulesoft.mql.grammar.node.Start;
import com.mulesoft.mql.grammar.parser.Parser;
import com.mulesoft.mql.grammar.parser.ParserException;
import com.mulesoft.mql.impl.MqlInterpreter;
import com.mulesoft.mql.impl.OrderByComparator;
import com.mulesoft.mql.impl.WherePredicate;

public class Query {

    private final QueryBuilder queryBuilder;

    public Query(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
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
        ArrayList list = new ArrayList();
        
        // select the items based on the where clause
        if (queryBuilder.getRestriction() != null) {
            CollectionUtils.select(items, new WherePredicate(queryBuilder), list);
        } else {
            list.addAll(items);
        }
        
        // order the items 
        if (queryBuilder.getOrderBy() != null) {
            Collections.sort(list, new OrderByComparator(queryBuilder));
        }
        
        // Filter out any items over the max size
        if (queryBuilder.getMax() > -1) {
            for (int i = queryBuilder.getMax(); i < list.size(); i++) {
                list.remove(i);
            }
        }
        
        // transform the items
        ObjectBuilder select = queryBuilder.getSelect();
        if (select != null) {
            // Compile MVEL expressions
            Map<String,Serializable> compiledExpressions = new HashMap<String,Serializable>();
            for (Map.Entry<String,String> e : select.getPropertyToExpression().entrySet()) {
                compiledExpressions.put(e.getKey(), MVEL.compileExpression(e.getValue()));
            }
            
            // Transform individual objects
            ArrayList transformedObjects = new ArrayList();
            for (Object o : list) {
                Map<String, Object> vars = new HashMap<String,Object>();
                vars.putAll(context);
                vars.put(queryBuilder.getAs(), o);
                
                Object transform;
                if (select.getTransformClass() == null) {
                    transform = transformToMap(compiledExpressions, vars);
                } else {
                    transform = transformToPojo(select.getTransformClass(), compiledExpressions, vars);
                }
                transformedObjects.add(transform);
            }
            
            list = transformedObjects;
        }
        
        if (selectSingleObject) {
            return (T) (list.size() > 0 ? list.get(0) : null);
        }
        
        return (T) list;
    }
    
    private Object transformToPojo(String clsName, Map<String, Serializable> compiledExpressions, Map<String, Object> vars) {
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

    private Map<String, Object> transformToMap(Map<String, Serializable> compiledExpressions, Map<String, Object> vars) {
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
