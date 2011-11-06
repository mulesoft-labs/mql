package com.mulesoft.mql.mule;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transport.OutputHandler;
import org.mule.client.DefaultLocalMuleClient;
import org.mule.module.json.transformers.JsonToObject;
import org.mule.module.json.transformers.ObjectToJson;
import org.mule.processor.AbstractInterceptingMessageProcessor;

import com.mulesoft.mql.Query;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executor;

public class MqlTransformer extends AbstractInterceptingMessageProcessor implements Initialisable {
    
    private JsonToObject JSON_TO_OBJECT = new JsonToObject();
    private ObjectToJson OBJECT_TO_JSON = new ObjectToJson();
    
    private String query;
    private Query compiledQuery;
    private Executor executor;
    private Type type = Type.POJO;
    private DefaultLocalMuleClient muleClient;
    
    public void initialise() throws InitialisationException {
        JSON_TO_OBJECT.setReturnClass(java.lang.Object.class);
        JSON_TO_OBJECT.setMuleContext(muleContext);
        JSON_TO_OBJECT.initialise();
        
        OBJECT_TO_JSON.setMuleContext(muleContext);
        OBJECT_TO_JSON.initialise();
        
        muleClient = new DefaultLocalMuleClient(muleContext);
        
        compiledQuery = Query.create(query);
        compiledQuery.setDefaultSelectObject("payload");
        
        if (executor != null) {
            compiledQuery.setExecutor(executor);
        }
    }

    public MuleEvent process(MuleEvent event) throws MuleException {
        MuleMessage message = event.getMessage();
        
        // Auto transform from JSON if need be
        Object payload = message.getPayload();
        boolean isJson = false;
        
        if (Type.JSON.equals(type)) {
            isJson = true;
        } else if (Type.AUTO.equals(type)) {
            String ct = message.getOutboundProperty("Content-Type");
            if (ct == null) {
                ct = message.getInvocationProperty("Content-Type");
                
                if (ct == null) {
                    ct = message.getInboundProperty("Content-Type");
                }
            } 
            
            if (ct != null && ct.startsWith("application/json") && isData(message.getPayload())) {
                isJson = true;
            }
        }
        
        if (isJson) {
            // hack
            if (payload instanceof OutputHandler) {
                try {
                    message.getPayloadAsString();
                } catch (Exception e) {
                    throw new DefaultMuleException("Could not convert message to a string.", e);
                }
            }
            payload = JSON_TO_OBJECT.transformMessage(message, event.getEncoding());
        }
        
        // execute query
        Map<String,Object> context = new MuleMessageQueryContext(message);
        context.put("payload", payload);
        context.put("message", message);

        MuleClientWrapper clientWrapper = new MuleClientWrapper(event, muleClient, muleContext);
        context.put("mule", clientWrapper);

        Object result = compiledQuery.execute(context);
        
        // Auto transform back to JSON if need be
        if (isJson) {
            message.setPayload(result);
            result = OBJECT_TO_JSON.transformMessage(message, event.getEncoding());
        }

        message.setPayload(result);
        
        return this.processNext(event);
    }

    private boolean isData(Object payload) {
        return payload instanceof InputStream || payload instanceof OutputHandler || payload instanceof String
                || payload instanceof byte[];
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
