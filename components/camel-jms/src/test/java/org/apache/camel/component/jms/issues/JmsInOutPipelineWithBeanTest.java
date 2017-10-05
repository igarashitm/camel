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
package org.apache.camel.component.jms.issues;

import javax.jms.ConnectionFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.CamelJmsTestHelper;
import org.apache.camel.component.jms.MultipleJmsImplementations;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

/**
 * Unit test from an user request on the forum.
 */
@RunWith(MultipleJmsImplementations.class)
public class JmsInOutPipelineWithBeanTest extends CamelTestSupport {

    @Test
    public void testA() throws Exception {
        Object response = template.requestBody("jms:testA", "Hello World");
        assertEquals("Reply", "Hello World,From Bean,From A,From B", response);
    }

    @Test
    public void testB() throws Exception {
        Object response = template.requestBody("jms:testB", "Hello World");
        assertEquals("Reply", "Hello World,From A,From Bean,From B", response);
    }

    @Test
    public void testC() throws Exception {
        Object response = template.requestBody("jms:testC", "Hello World");
        assertEquals("Reply", "Hello World,From A,From B,From Bean", response);
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        ConnectionFactory connectionFactory = CamelJmsTestHelper.createConnectionFactory();
        camelContext.addComponent("jms", jmsComponentAutoAcknowledge(connectionFactory));
        return camelContext;
    }

    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry reg = super.createRegistry();
        reg.bind("dummyBean", new MyDummyBean());
        return reg;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("jms:testA").to("bean:dummyBean").to("jms:a").to("jms:b");
                from("jms:testB").to("jms:a").to("bean:dummyBean").to("jms:b");
                from("jms:testC").to("jms:a").to("jms:b").to("bean:dummyBean");

                from("jms:a").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String body = exchange.getIn().getBody(String.class);
                        exchange.getOut().setBody(body + ",From A");
                    }
                });

                from("jms:b").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String body = exchange.getIn().getBody(String.class);
                        exchange.getOut().setBody(body + ",From B");
                    }
                });
            }
        };
    }

    public static class MyDummyBean {
        public void doSomething(Exchange exchange) {
            String body = exchange.getIn().getBody(String.class);
            exchange.getOut().setBody(body + ",From Bean");
        }
    }

}
