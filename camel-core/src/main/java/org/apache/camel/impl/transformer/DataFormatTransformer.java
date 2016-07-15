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
package org.apache.camel.impl.transformer;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.converter.stream.OutputStreamBuilder;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.transformer.DataFormatTransformerDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.DataType;
import org.apache.camel.spi.Transformer;
import org.apache.camel.support.ServiceSupport;
import org.apache.camel.util.ServiceHelper;

/**
 * A <a href="http://camel.apache.org/transformer.html">Transformer</a>
 * leverages DataFormat to perform transformation.
 */
public class DataFormatTransformer extends Transformer {

    private String dataFormatRef;
    private DataFormat dataFormat;

    public DataFormatTransformer(CamelContext context) {
        setCamelContext(context);
    }

    /**
     * Perform data transformation with specified from/to type using DataFormat.
     * @param message message to apply transformation
     * @param from 'from' data type
     * @param to 'to' data type
     */
    @Override
    public void transform(Message message, DataType from, DataType to) throws Exception {
        Exchange exchange = message.getExchange();
        CamelContext context = exchange.getContext();
        
        // Unmarshalling into Java Object
        if (to.isJavaType() && (from.equals(getFrom()) || from.getModel().equals(getModel()))) {
            Object answer = getDataFormat(exchange).unmarshal(exchange, message.getBody(InputStream.class));
            Class<?> toClass = context.getClassResolver().resolveClass(to.getName());
            if (!toClass.isAssignableFrom(answer.getClass())) {
                answer = context.getTypeConverter().mandatoryConvertTo(toClass, answer);
            }
            message.setBody(answer);
            
        // Marshalling from Java Object
        } else if (from.isJavaType() && (to.equals(getTo()) || to.getModel().equals(getModel()))) {
            Object input = message.getBody();
            Class<?> fromClass = context.getClassResolver().resolveClass(from.getName());
            if (!fromClass.isAssignableFrom(input.getClass())) {
                input = context.getTypeConverter().mandatoryConvertTo(fromClass, input);
            }
            OutputStreamBuilder osb = OutputStreamBuilder.withExchange(exchange);
            getDataFormat(exchange).marshal(exchange, message.getBody(), osb);
            message.setBody(osb.build());
            
        } else {
            throw new IllegalArgumentException("Unsupported transformation: from='" + from + ", to='" + to + "'");
        }
    }

    /**
     * A bit dirty hack to create DataFormat instance, as it requires a RouteContext anyway.
     */
    private DataFormat getDataFormat(Exchange exchange) throws Exception {
        if (this.dataFormat == null) {
            this.dataFormat = DataFormatDefinition.getDataFormat(
                exchange.getUnitOfWork().getRouteContext(), null, this.dataFormatRef);
            if (this.dataFormat != null && !getCamelContext().hasService(this.dataFormat)) {
                getCamelContext().addService(this.dataFormat, false);
            }
        }
        return this.dataFormat;
    }

    /**
     * Set DataFormat ref.
     * @param ref DataFormat ref
     * @return this DataFormatTransformer instance
     */
    public DataFormatTransformer setDataFormatRef(String ref) {
        this.dataFormatRef = ref;
        return this;
    }

    @Override
    protected void doStart() throws Exception {
        // no-op
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopService(this.dataFormat);
        getCamelContext().removeService(this.dataFormat);
    }
}
