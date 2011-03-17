package com.mulesoft.mql.mule.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.MessageProcessorDefinitionParser;

import com.mulesoft.mql.mule.MqlTransformer;

public class MqlNamespaceHandler extends AbstractMuleNamespaceHandler {

    public void init() {
        registerBeanDefinitionParser("transform", new MessageProcessorDefinitionParser(MqlTransformer.class));
        registerBeanDefinitionParser("query-service", new QueryServiceDefinitionParser()); 
    }

}
