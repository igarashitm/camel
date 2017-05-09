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
package org.apache.camel.component.jms.artemis;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singletonList;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.camel.component.jms.JmsTestHelper;

public class ArtemisJmsTestHelper implements JmsTestHelper {

    private static final AtomicInteger BROKER_COUNT = new AtomicInteger();

    private EmbeddedJMS broker;

    private ServerLocator serviceLocator;

    private ActiveMQConnectionFactory connectionFactory;

    @Override
    public ConnectionFactory createConnectionFactory() {
        return connectionFactory;
    }

    @Override
    public ConnectionFactory createConnectionFactory(final int maximumRedeliveries) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionFactory createPersistentConnectionFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionFactory createPooledConnectionFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Destination createQueue(final String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void shutdown() {
        connectionFactory.close();

        serviceLocator.close();

        try {
            broker.stop();
        } catch (final Exception ignored) {
            // ignored
        }
    }

    @Override
    public void startup() {
        final Configuration configuration;
        try {
            configuration = new ConfigurationImpl().setPersistenceEnabled(false).setSecurityEnabled(false)
                .addAcceptorConfiguration("test", "vm://?serverId=" + BROKER_COUNT.incrementAndGet())
                .addConnectorConfiguration("connector",
                    new TransportConfiguration(InVMAcceptorFactory.class.getName()));
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        final ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl().setName("cf")
            .setConnectorNames("connector").setBindings("cf");

        final JMSConfiguration jmsConfig = new JMSConfigurationImpl()
            .setConnectionFactoryConfigurations(singletonList(cfConfig));

        broker = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfig);

        try {
            broker.start();
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        serviceLocator = ActiveMQClient.createServerLocator(false,
            new TransportConfiguration(InVMConnectorFactory.class.getName()));

        connectionFactory = new ActiveMQConnectionFactory(serviceLocator);
    }

}
