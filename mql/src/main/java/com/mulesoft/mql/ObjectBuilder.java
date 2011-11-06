package com.mulesoft.mql;

import java.util.HashMap;
import java.util.Map;


public class ObjectBuilder {

    private Map<String,Object> propertyToValue = new HashMap<String,Object>();
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
        propertyToValue.put(property, expression);
        return this;
    }

    public Map<String, Object> getPropertyToValues() {
        return propertyToValue;
    }

    public void setTransformClass(String name) {
        this.cls = name;
    }
    
    public String getTransformClass() {
        return cls;
    }

    public void set(String property, ObjectBuilder object) {
        propertyToValue.put(property, object);
    }

    public void set(String property, QueryBuilder query) {
        propertyToValue.put(property, query);
    }
}
