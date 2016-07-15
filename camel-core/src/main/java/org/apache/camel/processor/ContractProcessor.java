/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.DelegateProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.spi.Contract;
import org.apache.camel.spi.DataType;
import org.apache.camel.spi.Transformer;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.camel.util.ExchangeHelper;

/**
 * A camel internal processor which manipulates contract information on a exchange.
 */
public class ContractProcessor extends DelegateAsyncProcessor {

    private Contract contract;
    private List<CamelInternalProcessorAdvice> advices = new ArrayList<CamelInternalProcessorAdvice>();

    public ContractProcessor(Processor target, Contract contract) {
        super(target);
        this.contract = contract;
        advices.add(new TransformerAdvice());
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        AsyncProcessorHelper.process(this, exchange);
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        final List<Object> states = new ArrayList<Object>(advices.size());
        for (int i = 0; i < advices.size(); i++) {
            try {
                CamelInternalProcessorAdvice task = advices.get(i);
                Object state = task.before(exchange);
                states.add(state);
            } catch (Throwable t) {
                exchange.setException(t);
                callback.done(true);
                return true;
            }
        }
        
        return processor.process(exchange, new AsyncCallback() {
            @Override
            public void done(boolean doneSync) {
                try {
                    // TODO Can we add FAULT_TYPE and allow type based FAULT handling?
                    if (exchange.getException() != null) {
                        return;
                    }
                    
                    for (int i = advices.size() - 1; i >= 0; i--) {
                        CamelInternalProcessorAdvice task = advices.get(i);
                        Object state = states.get(i);
                        try {
                            task.after(exchange, state);
                        } catch (Throwable t) {
                            exchange.setException(t);
                        }
                    }
                } finally {
                    callback.done(doneSync);
                }
            }});
    }

    public Contract getContract() {
        return contract;
    }

    /**
     * Adds an {@link CamelInternalProcessorAdvice} advice to the list of advices to execute by this internal processor.
     *
     * @param advice  the advice to add
     */
    public void addAdvice(CamelInternalProcessorAdvice advice) {
        advices.add(advice);
    }

    // Advices
    // -------------------------------------------------------------------------
    
    /**
     * Advice to perform transformation along the contract.
     */
    class TransformerAdvice implements CamelInternalProcessorAdvice {

        @Override
        public Object before(Exchange exchange) throws Exception {
            DataType from = exchange.getProperty(Exchange.INPUT_TYPE, DataType.class);
            DataType to = contract.getInputType();
            convertBody(exchange.getIn(), from, to);
            exchange.setProperty(Exchange.INPUT_TYPE, to);
            return null;
        }
        
        @Override
        public void after(Exchange exchange, Object data) throws Exception {
            DataType from = exchange.getProperty(Exchange.OUTPUT_TYPE, DataType.class);
            DataType to = contract.getOutputType();
            convertBody(exchange.hasOut() ? exchange.getOut() : exchange.getIn(), from, to);
            exchange.setProperty(Exchange.OUTPUT_TYPE, to);
        }
        
        private void convertBody(Message message, DataType from, DataType to) throws Exception {
            if (to == null || (from != null && from.equals(to))) {
                return;
            }
            
            CamelContext context = message.getExchange().getContext();
            // transform into 'from' type before performing declared transformation
            if (from != null && from.isJavaType()) {
                Class<?> fromJava = getClazz(from.getName(), context);
                if (!fromJava.isAssignableFrom(message.getBody().getClass())) {
                    Object fromBody = message.getMandatoryBody(fromJava);
                    message.setBody(fromBody);
                }
            }
            
            Transformer transformer = context.resolveTransformer(from, to);
            if (transformer != null) {
                // Applying exactly matched transformer. Java-Java transformer is also allowed.
                transformer.transform(message, from, to);
                return;
            } else if (from == null || from.isJavaType()) {
                if (to.isJavaType()) {
                    // Java->Java transformation just relies on TypeConverter if no declared transformer
                    // TODO for better performance it may be better to add TypeConveterTransformer
                    // into transformer registry to avoid unnecessary scan in transformer registry
                    Object answer = message.getMandatoryBody(getClazz(to.getName(), context));
                    message.setBody(answer);
                    return;
                } else {
                    // Java->Other transformation
                    transformer = context.resolveTransformer(to.getModel());
                    if (transformer != null) {
                        transformer.transform(message, from, to);
                        return;
                    }
                }
            } else if (from != null) {
                if (to.isJavaType()) {
                    // Other->Java transformation
                    transformer = context.resolveTransformer(from.getModel());
                    if (transformer != null) {
                        transformer.transform(message, from, to);
                        return;
                    }
                } else {
                    // Other->Other transformation - look for a transformer chain
                    Transformer fromTransformer = context.resolveTransformer(from.getModel());
                    Transformer toTransformer = context.resolveTransformer(to.getModel());
                    if (fromTransformer != null && toTransformer != null) {
                        fromTransformer.transform(message, from, new DataType(Object.class));
                        toTransformer.transform(message, new DataType(Object.class), to);
                        return;
                    }
                }
            }
            
            throw new IllegalArgumentException("No Transformer found for [from='" + from + "', to='" + to + "']");
        }
        
        private Class<?> getClazz(String type, CamelContext context) throws Exception {
            return context.getClassResolver().resolveMandatoryClass(type);
        }
    }
}
