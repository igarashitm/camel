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

import java.util.Collections;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class JmsMock implements TestRule {

    public final Connection connection = mock(Connection.class);

    public final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

    public final MessageConsumer messageConsumer = mock(MessageConsumer.class);

    public final MessageProducer messageProducer = mock(MessageProducer.class);

    public final Session session = mock(Session.class);

    public final TextMessage textMessage = mock(TextMessage.class);

    @Override
    public Statement apply(final Statement base, final Description description) {
        resetToDefault();

        return base;
    }

    public void receiveMessageFromQueue() {
        try {
            final Message message = message();

            when(messageConsumer.receive()).thenReturn(message);
            when(messageConsumer.receive(anyLong())).thenReturn(message);
        } catch (final JMSException ignored) {
            // ignored
        }
    }

    private void resetToDefault() {
        try {
            reset(connectionFactory, connection, session);

            when(connectionFactory.createConnection()).thenReturn(connection);

            when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);

            when(session.createTemporaryQueue()).thenReturn(mock(TemporaryQueue.class));
            when(session.createTemporaryTopic()).thenReturn(mock(TemporaryTopic.class));
            when(session.createQueue(anyString())).thenReturn(mock(Queue.class));
            when(session.createTextMessage()).thenReturn(textMessage);

            when(session.createProducer(any(Destination.class))).thenReturn(messageProducer);
            when(session.createConsumer(any(Destination.class))).thenReturn(messageConsumer);
            when(session.createConsumer(any(Destination.class), anyString())).thenReturn(messageConsumer);
        } catch (final JMSException ignored) {
            // ignored
        }
    }

    public static Message message() {
        final Message message = mock(Message.class);
        try {
            when(message.getPropertyNames()).thenReturn(Collections.emptyEnumeration());
        } catch (final JMSException ignored) {
            // ignored
        }

        return message;
    }
}
