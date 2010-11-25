package com.mulesoft.mule.mql;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.MessageProcessorDefinitionParser;

public class MqlNamespaceHandler extends AbstractMuleNamespaceHandler {

    public void init() {
        registerBeanDefinitionParser("transform", new MessageProcessorDefinitionParser(MqlTransformer.class)); 
    }

}
