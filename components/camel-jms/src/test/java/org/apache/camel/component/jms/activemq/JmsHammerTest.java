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
package org.apache.camel.component.jms.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.CamelJmsTestHelper;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See MR-170
 */
@Ignore
public class JmsHammerTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(JmsHammerTest.class);

    private String message;
    // TODO: AMQ reaches a limit when sending #169.
    // private int size = 200;
    private int size = 10;

    public void prepareMessage() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            sb.append("hellothere");
        }
        message = sb.toString();
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
            "vm://localhost?broker.persistent=false&broker.useJmx=false&jms.redeliveryPolicy.maximumRedeliveries=0" 
                + "&jms.redeliveryPolicy.initialRedeliveryDelay=500&jms.useAsyncSend=false&jms.sendTimeout=10000"
                + "&jms.maxReconnectAttempts=1&jms.timeout=3000");
        JmsComponent jms = new JmsComponent();
        jms.setConnectionFactory(cf);
        camelContext.addComponent("jms", jms);
        return camelContext;
    }

    @Test
    public void testHammerJms() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(size);

        for (int i = 0; i < size; i++) {
            template.sendBody("direct:start", message);
            LOG.info("Send #" + i);
        }

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        prepareMessage();

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("jms:queue:dropOff", "mock:result");
            }
        };
    }
}
