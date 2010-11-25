package com.mulesoft.mql;

import java.util.HashMap;
import java.util.Map;


public class ObjectBuilder {

    private Map<String,String> propertyToExpression = new HashMap<String,String>();
    private String cls;
    
    protected ObjectBuilder(String cls) {
        this.cls = cls;
    }

    protected ObjectBuilder() {
    }
    
    /**
     * Create a new object which is based on a Map.
     * @return
     */
    public static ObjectBuilder newObject() {
        return new ObjectBuilder();
    }
    
    /**
     * Create a new object of a particular class.
     */
    public static ObjectBuilder newObject(String cls) {
        return new ObjectBuilder(cls);
    }
    
    public ObjectBuilder set(String property, String expression) {
        propertyToExpression.put(property, expression);
        return this;
    }

    public Map<String, String> getPropertyToExpression() {
        return propertyToExpression;
    }

    public String getTransformClass() {
        return cls;
    }
}
