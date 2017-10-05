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
package org.apache.camel.component.jms.async;

import javax.jms.ConnectionFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.CamelJmsTestHelper;
import org.apache.camel.component.jms.MultipleJmsImplementations;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

/**
 *
 */
@RunWith(MultipleJmsImplementations.class)
public class AsyncConsumerFalseTest extends CamelTestSupport {

    @Test
    public void testAsyncJmsConsumer() throws Exception {
        // async is disabled (so we should receive in same order)
        getMockEndpoint("mock:result").expectedBodiesReceived("Camel", "Hello World");

        template.sendBody("jms:queue:start", "Hello Camel");
        template.sendBody("jms:queue:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        camelContext.addComponent("async", new MyAsyncComponent());

        ConnectionFactory connectionFactory = CamelJmsTestHelper.createConnectionFactory();
        camelContext.addComponent("jms", jmsComponentAutoAcknowledge(connectionFactory));

        return camelContext;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // disable async in only mode on the consumer
                from("jms:queue:start?asyncConsumer=false")
                        .choice()
                            .when(body().contains("Camel"))
                            .to("async:camel?delay=2000")
                            .to("mock:result")
                        .otherwise()
                            .to("mock:result");
            }
        };
    }
}
