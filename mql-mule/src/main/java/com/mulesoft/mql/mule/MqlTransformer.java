package com.mulesoft.mql.mule;

import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.OutputHandler;
import org.mule.api.transport.PropertyScope;
import org.mule.client.DefaultLocalMuleClient;
import org.mule.module.json.transformers.JsonToObject;
import org.mule.module.json.transformers.ObjectToJson;
import org.mule.transformer.AbstractMessageTransformer;

import com.mulesoft.mql.LazyQueryContext;
import com.mulesoft.mql.Query;

import java.util.Map;
import java.util.concurrent.Executor;

public class MqlTransformer extends AbstractMessageTransformer {
    
    private JsonToObject JSON_TO_OBJECT = new JsonToObject();
    private ObjectToJson OBJECT_TO_JSON = new ObjectToJson();
    
    private String query;
    private MuleClientWrapper clientWrapper;
    private Query compiledQuery;
    private Executor executor;
    private Type type = Type.POJO;
    
    @Override
    public void initialise() throws InitialisationException {
        super.initialise();
        
        JSON_TO_OBJECT.setReturnClass(java.lang.Object.class);
        JSON_TO_OBJECT.setMuleContext(muleContext);
        JSON_TO_OBJECT.initialise();
        
        OBJECT_TO_JSON.setMuleContext(muleContext);
        OBJECT_TO_JSON.initialise();
        
        clientWrapper = new MuleClientWrapper(new DefaultLocalMuleClient(muleContext));
        compiledQuery = Query.create(query);
        compiledQuery.setDefaultSelectObject("payload");
        
        if (executor != null) {
            compiledQuery.setExecutor(executor);
        }
    }

    public Object transformMessage(final MuleMessage message, String outputEncoding) throws TransformerException {
        // Auto transform from JSON if need be
        Object payload = message.getPayload();
        boolean isJson = false;
        
        if (Type.JSON.equals(type)) {
            isJson = true;
            // hack
            if (payload instanceof OutputHandler) {
                try {
                    message.getPayloadAsString();
                } catch (Exception e) {
                    throw new TransformerException(this, e);
                }
            }
            payload = JSON_TO_OBJECT.transformMessage(message, outputEncoding);
        }
        
        // execute query
        Map<String,Object> context = new MuleMessageQueryContext(message);
        context.put("payload", payload);
        context.put("message", message);
        context.put("mule", clientWrapper);

        Object result = compiledQuery.execute(context);
        
        // Auto transform back to JSON if need be
        if (isJson) {
            message.setPayload(result);
            result = OBJECT_TO_JSON.transformMessage(message, outputEncoding);
        }
        
        return result;
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

    public class MuleMessageQueryContext extends LazyQueryContext {
        private final MuleMessage message;

        public MuleMessageQueryContext(MuleMessage message) {
            this.message = message;
        }

        @Override
        public Object load(String key) {
            Object object = message.getProperty(key, PropertyScope.OUTBOUND);

            // ugly but it works
            if (object == null) {
                object = message.getProperty(key, PropertyScope.INVOCATION);

                if (object == null) {
                    object = message.getProperty(key, PropertyScope.INBOUND);

                    if (object == null) {
                        object = muleContext.getRegistry().lookupObject(key);
                    }
                }
            }

            return object;
        }
    }
}
