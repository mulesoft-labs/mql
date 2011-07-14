package com.mulesoft.mql;

import com.mulesoft.mql.grammar.lexer.Lexer;
import com.mulesoft.mql.grammar.lexer.LexerException;
import com.mulesoft.mql.grammar.node.Start;
import com.mulesoft.mql.grammar.parser.Parser;
import com.mulesoft.mql.grammar.parser.ParserException;
import com.mulesoft.mql.impl.JoinPredicate;
import com.mulesoft.mql.impl.MqlInterpreter;
import com.mulesoft.mql.impl.OrderByComparator;
import com.mulesoft.mql.impl.SelectEvaluator;
import com.mulesoft.mql.impl.WherePredicate;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.collections.functors.TruePredicate;
import org.mvel2.MVEL;

/**
 * The central place for interfacing with MQL. To use, it is recommended that you 
 * compile your queries, save them, and then execute them. For instance:
 * <pre>
 * 
 *
 *  // populate some data
 *  List<Person> persons = new ArrayList<Person>();
 *  persons.add(new Person("Dan", "Diephouse", "MuleSoft", "Engineering"));
 *  persons.add(new Person("Joe", "Sales", "MuleSoft", "Sales"));
 *  
 *  // create a context for the query
 *  Map<String,Object> context = new HashMap<String,Object>();
 *  context.put("persons", persons);
 *  
 *  // store this query and reuse it
 *  Query query = Query.compile("from people where division = 'Engineering'");
 *  
 *  // execute the query
 *  Collection<Person> result = 
 *      query.execute("from people where division = 'Engineering'", context);
 * </pre>
 * Of course there is a handy shortcut method too:
 * <pre>
 * Query.execute("from people where division = 'Engineering'", persons);
 * </pre>
 */
public class Query {

    private final QueryBuilder queryBuilder;
    private Predicate joinPredicate;
    private Predicate wherePredicate;
    private JoinBuilder joinBuilder;
    private String defaultFromObject = "items";
    private Serializable compiledFromExpression;
    private SelectEvaluator selectEvaluator;

    public Query(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;

        ObjectBuilder select = queryBuilder.getSelect();
        if (select != null) {
            selectEvaluator = new SelectEvaluator(queryBuilder, select);
        }

        joinBuilder = queryBuilder.getJoin();
        joinPredicate = getJoin();
        wherePredicate = getWhere();
    }

    /**
     * Create a compiled Query object which can be used to repeatedly query.
     */
    public static Query create(String queryString) {
        Lexer lexer = new Lexer(new PushbackReader(new StringReader(queryString)));
        Parser parser = new Parser(lexer);
        
        try {
            Start ast = parser.parse();

            /* Get our Interpreter going. */
            MqlInterpreter interpreter = new MqlInterpreter();
            ast.apply(interpreter);
            
            return interpreter.getQuery();
        } catch (ParserException e) {
            throw new QueryException(e);
        } catch (LexerException e) {
            throw new QueryException(e);
        } catch (IOException e) {
            throw new QueryException(e);
        }
    }
    
    /**
     * A short cut for Query.create(queryString).execute(items);
     */
    public static <T> T execute(String queryString, Collection<?> items) {
        return create(queryString).<T>execute(items);
    }
    
    /**
     * A short cut for Query.create(queryString).execute(context);
     */
    public static <T> T execute(String queryString, Map<String,Object> context) {
        return create(queryString).<T>execute(context);
    }
    
    /**
     * Execute the query against a collection of objects. These objects will get named as "items"
     * in your variable context. Generally the execute(Map<String,Object>) method should be
     * used, but this is just too handy to take out.
     * @param <T>
     * @param items
     * @return
     */
    public <T> T execute(Collection<?> items) {
        return this.<T>execute(items, getDefaultSelectObject());
    }
    
    /**
     * Execute the query against a collection of objects which will be given the specified 
     * name inside the variable context.
     * @param <T>
     * @param items
     * @return
     */
    public <T> T execute(Collection<?> items, String as) {
        Map<String,Object> context = new HashMap<String,Object>();
        context.put(as, items);
        return this.<T>execute(context);
    }
    
    public <T> T execute(final Map<String,Object> context) {
        Collection<?> items;
        Object from = getFrom(context);
        boolean selectSingleObject = false;
        
        if (from instanceof Collection) {
            // transform a collection of objects
            items = (Collection<?>) from;
        } else if (from.getClass().isArray()) {
            // transform an array of objects
            items = (Collection<?>) Arrays.asList((Object[]) from);
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
            
            Map<String, Object> vars = new LazyQueryContext() {
                @Override
                // If a variable doesn't exist, try loading it from the context
                // the user passed in, since it might be lazy loading.
                public Object load(String key) {
                    return context.get(key);
                }
            };
            
            vars.putAll(context);
            vars.put(queryBuilder.getAs(), o);
            itemsAsMaps.add(vars);
        }
        
        List resultList = new ArrayList();
        resultList = joinAndFilter(itemsAsMaps, resultList);
        order(resultList);
        resultList = doSelect(resultList);
        
        if (selectSingleObject) {
            return (T) (resultList.size() > 0 ? resultList.get(0) : null);
        }
        
        return (T) resultList;
    }

    protected Object getFrom(final Map<String, Object> context) {
        if (compiledFromExpression == null) {
            String fromObjectName = queryBuilder.getFrom();
            if (fromObjectName == null) {
                fromObjectName = getDefaultSelectObject();
            }

            compiledFromExpression = MVEL.compileExpression(fromObjectName);
        }
        return MVEL.executeExpression(compiledFromExpression, context);
    }

    protected void order(List resultList) {
        // order the items 
        if (queryBuilder.getOrderBy() != null) {
            Collections.sort(resultList, new OrderByComparator(queryBuilder));
        }
    }

    protected List joinAndFilter(List<Map<String, Object>> itemsAsMaps, List resultList) {
        Predicate predicate = AndPredicate.getInstance(joinPredicate, wherePredicate);
        
        if (joinBuilder != null && joinBuilder.isAsync() && itemsAsMaps.size() > 1) {
            return doAsyncJoinAndFilter(itemsAsMaps, resultList, predicate);
        } else {
            doSyncJoinAndFilter(itemsAsMaps, resultList, predicate);
            return resultList;
        }
    }

    protected void doSyncJoinAndFilter(List<Map<String, Object>> itemsAsMaps, List resultList, Predicate predicate) {
        for (int i = 0; i < itemsAsMaps.size() && i < queryBuilder.getMax(); i++) {
            Map<String, Object> object = itemsAsMaps.get(i);
            if (predicate.evaluate(object)) {
                resultList.add(object);
            }
        }
    }

    protected List doAsyncJoinAndFilter(final List<Map<String, Object>> itemsAsMaps, 
                                        final List resultList, 
                                        final Predicate predicate) {
        Executor executor = joinBuilder.getExecutor();

        final List syncedList = Collections.synchronizedList(resultList);
        
        CountDownLatch latch = new CountDownLatch(itemsAsMaps.size());
        for (int i = 0; i < itemsAsMaps.size(); i++) {
            Map<String, Object> object = itemsAsMaps.get(i);
            executor.execute(new JoinAndFilterRunnable(object, predicate, syncedList, latch));
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        
        for (int i = resultList.size(); i >= queryBuilder.getMax(); i--) {
            resultList.remove(queryBuilder.getMax());
        }
        return resultList;
    }

    protected List doSelect(List list) {
        // transform the items
        ObjectBuilder select = queryBuilder.getSelect();
        if (select != null) {
            // Transform individual objects
            ArrayList transformedObjects = new ArrayList();
            for (Object o : list) {
                Map<String, Object> vars = (Map<String,Object>) o;
                
                transformedObjects.add(selectEvaluator.evaluate(vars));
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

    public String getDefaultSelectObject() {
        return defaultFromObject;
    }

    /**
     * Set the object name that is used by default for a select only query. That is,
     * if there is no 'from foo as f' clause, set the from object name.
     * @param defaultFromObject
     */
    public void setDefaultSelectObject(String defaultFromObject) {
        this.defaultFromObject = defaultFromObject;
    }

    public void setExecutor(Executor executor) {
        joinBuilder.executor(executor);
    }
    
    private final class JoinAndFilterRunnable implements Runnable {
        private final Predicate predicate;
        private final Map<String, Object> object;
        private final List syncedList;
        private final CountDownLatch latch;

        private JoinAndFilterRunnable(Map<String, Object> object, 
                                      Predicate predicate, 
                                      List syncedList, 
                                      CountDownLatch latch) {
            this.object = object;
            this.predicate = predicate;
            this.syncedList = syncedList;
            this.latch = latch;
        }

        public void run() {
            try {
                if (syncedList.size() >= queryBuilder.getMax()) {
                    return;
                }
                
                if (predicate.evaluate(object)) {
                    syncedList.add(object);
                }
            } finally {
                latch.countDown();
            }
        }
    }
}

