package com.mulesoft.mule.mql;

import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.mulesoft.mql.Query;

public class MqlTransformer extends AbstractMessageTransformer {
    private String query;

    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        Map<String,Object> context = new HashMap<String,Object>();
        context.put("payload", message.getPayload());
        context.put("message", message);
        
        // TODO: make properties available as a hashmap
        
        return Query.execute(query, context);
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
