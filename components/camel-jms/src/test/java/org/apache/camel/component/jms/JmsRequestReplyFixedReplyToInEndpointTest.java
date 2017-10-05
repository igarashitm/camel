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
package org.apache.camel.component.jms;

import javax.jms.ConnectionFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

/**
 * @version 
 */
@RunWith(MultipleJmsImplementations.class)
public class JmsRequestReplyFixedReplyToInEndpointTest extends CamelTestSupport {

    @Test
    public void testJmsRequestReplyTempReplyTo() throws Exception {
        Exchange reply = template.request("jms:queue:foo", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("World");
            }
        });
        assertEquals("Hello World", reply.getOut().getBody());
        assertTrue("Should have headers", reply.getOut().hasHeaders());
        String replyTo = reply.getOut().getHeader("JMSReplyTo", String.class);
        Assert.assertNotNull(replyTo);
        Assert.assertFalse(replyTo.isEmpty());
    }

    @Test
    public void testJmsRequestReplyFixedReplyToInEndpoint() throws Exception {
        Exchange reply = template.request("jms:queue:foo?replyTo=bar", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("World");
            }
        });
        assertEquals("Hello World", reply.getOut().getBody());
        assertTrue("Should have headers", reply.getOut().hasHeaders());
        assertTrue(reply.getOut().getHeader("JMSReplyTo", String.class).contains("bar"));
    }

    @Test
    public void testJmsRequestReplyFixedReplyToInEndpointTwoMessages() throws Exception {
        Exchange reply = template.request("jms:queue:foo?replyTo=bar", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("World");
            }
        });
        assertEquals("Hello World", reply.getOut().getBody());
        assertTrue("Should have headers", reply.getOut().hasHeaders());
        assertTrue(reply.getOut().getHeader("JMSReplyTo", String.class).contains("bar"));

        reply = template.request("jms:queue:foo?replyTo=bar", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Moon");
            }
        });
        assertEquals("Hello Moon", reply.getOut().getBody());
        assertTrue("Should have headers", reply.getOut().hasHeaders());
        assertTrue(reply.getOut().getHeader("JMSReplyTo", String.class).contains("bar"));
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        ConnectionFactory connectionFactory = CamelJmsTestHelper.createConnectionFactory();
        camelContext.addComponent("jms", jmsComponentAutoAcknowledge(connectionFactory));
        return camelContext;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:foo")
                    .transform(body().prepend("Hello "));
            }
        };
    }
}
