package com.mulesoft.mule.mql;

import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.client.DefaultLocalMuleClient;
import org.mule.transformer.AbstractMessageTransformer;

import com.mulesoft.mql.LazyQueryContext;
import com.mulesoft.mql.Query;

import java.util.Map;

public class MqlTransformer extends AbstractMessageTransformer {

    private String query;
    private MuleClientWrapper clientWrapper;
    private Query compiledQuery;
    
    @Override
    public void initialise() throws InitialisationException {
        super.initialise();
        
        clientWrapper = new MuleClientWrapper(new DefaultLocalMuleClient(muleContext));
        compiledQuery = Query.create(query);
        compiledQuery.setDefaultSelectObject("payload");
    }

    public Object transformMessage(final MuleMessage message, String outputEncoding) throws TransformerException {
        Map<String,Object> context = new MuleMessageQueryContext(message);
        context.put("payload", message.getPayload());
        context.put("message", message);
        context.put("mule", clientWrapper);

        return compiledQuery.execute(context);
    }

    public void setQuery(String query) {
        this.query = query;
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
