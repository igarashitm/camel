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
import javax.jms.TextMessage;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

/**
 * Unit test for Camel loadbalancer failover with JMS
 */
public class JmsLoadBalanceFailoverWithForceSendOriginalJmsMessageTest extends CamelTestSupport {
    private final boolean forceSendOriginalMessage = true;

    @Test
    public void testFailover() throws Exception {
        final MockEndpoint oneMock = getMockEndpoint("mock:one");
        final MockEndpoint threeMock = getMockEndpoint("mock:three");
        final MockEndpoint twoMock = getMockEndpoint("mock:two");
        final MockEndpoint resultMock = getMockEndpoint("mock:result");

        oneMock.expectedMessageCount(1);
        oneMock.expectedHeaderReceived("foo", "bar");
        twoMock.expectedMessageCount(1);
        twoMock.expectedHeaderReceived("foo", "bar");
        threeMock.expectedMessageCount(0);
        resultMock.expectedMessageCount(1);
        resultMock.expectedHeaderReceived("foo", "bar");

        String out = template.requestBodyAndHeader("jms:queue:start", "Hello World", "foo", "bar", String.class);
        assertEquals("Hello Back", out);

        assertMockEndpointsSatisfied();

        // we should get an TextMessage with the body and custom header intact
        assertTrue(oneMock.getExchanges().get(0).getIn().getBody() instanceof TextMessage);
        assertEquals("Hello World", ((TextMessage) oneMock.getExchanges().get(0).getIn().getBody()).getText());
        assertEquals("bar", ((TextMessage) oneMock.getExchanges().get(0).getIn().getBody()).getStringProperty("foo"));

        assertTrue(twoMock.getExchanges().get(0).getIn().getBody() instanceof TextMessage);
        assertEquals("Hello World", ((TextMessage) twoMock.getExchanges().get(0).getIn().getBody()).getText());
        assertEquals("bar", ((TextMessage) twoMock.getExchanges().get(0).getIn().getBody()).getStringProperty("foo"));

        // reset mocks
        oneMock.reset();
        twoMock.reset();
        threeMock.reset();
        resultMock.reset();

        // the round robin should now be at three so one and two should be
        // skipped

        oneMock.expectedMessageCount(0);
        twoMock.expectedMessageCount(0);
        threeMock.expectedMessageCount(1);
        threeMock.expectedHeaderReceived("foo", "bar");
        resultMock.expectedMessageCount(1);
        resultMock.expectedHeaderReceived("foo", "bar");

        out = template.requestBodyAndHeader("jms:queue:start", "Hello World", "foo", "bar", String.class);
        assertEquals("Bye World", out);

        assertMockEndpointsSatisfied();

        assertTrue(threeMock.getExchanges().get(0).getIn().getBody() instanceof TextMessage);
        assertEquals("Hello World", ((TextMessage) threeMock.getExchanges().get(0).getIn().getBody()).getText());
        assertEquals("bar", ((TextMessage) threeMock.getExchanges().get(0).getIn().getBody()).getStringProperty("foo"));
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        final CamelContext camelContext = super.createCamelContext();

        final ConnectionFactory connectionFactory = CamelJmsTestHelper.createConnectionFactory();
        final JmsComponent jms = jmsComponentAutoAcknowledge(connectionFactory);
        // we want to transfer the exception
        jms.setTransferException(true);

        camelContext.addComponent("jms", jms);

        return camelContext;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:start?mapJmsMessage=false").loadBalance().failover(-1, false, true)
                    .to("jms:queue:one?forceSendOriginalMessage=" + forceSendOriginalMessage)
                    .to("jms:queue:two?forceSendOriginalMessage=" + forceSendOriginalMessage)
                    .to("jms:queue:three?forceSendOriginalMessage=" + forceSendOriginalMessage).end().to("mock:result");

                from("jms:queue:one?mapJmsMessage=false").to("mock:one")
                    .throwException(new IllegalArgumentException("Damn"));

                from("jms:queue:two?mapJmsMessage=false").to("mock:two").transform().simple("Hello Back");

                from("jms:queue:three?mapJmsMessage=false").to("mock:three").transform().simple("Bye World");
            }
        };
    }

}
