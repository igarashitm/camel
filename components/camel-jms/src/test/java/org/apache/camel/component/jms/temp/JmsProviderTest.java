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
package org.apache.camel.component.jms.temp;

import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.naming.Context;

import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.jms.JmsMock;
import org.apache.camel.component.jms.JmsProviderMetadata;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Rule;
import org.junit.Test;

public class JmsProviderTest extends CamelTestSupport {

    @Rule
    public JmsMock mock = new JmsMock();

    @Test
    public void testTemporaryDestinationTypes() throws Exception {
        JmsEndpoint endpoint = getMandatoryEndpoint("jms:test.queue", JmsEndpoint.class);
        JmsConfiguration configuration = endpoint.getConfiguration();

        JmsProviderMetadata providerMetadata = configuration.getProviderMetadata();
        assertNotNull("provider", providerMetadata);

        Class<? extends TemporaryQueue> queueType = endpoint.getTemporaryQueueType();
        Class<? extends TemporaryTopic> topicType = endpoint.getTemporaryTopicType();

        log.info("Found queue type: " + queueType);
        log.info("Found topic type: " + topicType);

        assertNotNull("queueType", queueType);
        assertNotNull("topicType", topicType);

        assertTrue("queueType", TemporaryQueue.class.isAssignableFrom(queueType));
        assertTrue("topicType", TemporaryTopic.class.isAssignableFrom(topicType));
    }

    @Override
    protected Context createJndiContext() throws Exception {
        Context context = super.createJndiContext();
        JmsComponent jms = new JmsComponent();

        jms.setConnectionFactory(mock.connectionFactory);

        context.bind("jms", jms);
        return context;
    }
}
