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

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singletonList;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreAddressConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.camel.component.jms.JmsTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtemisJmsTestHelper implements JmsTestHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJmsTestHelper.class);
    private static final String JVM_NAME = ManagementFactory.getRuntimeMXBean().getName();
    private static final String DATA_DIR = "target" + File.separator + "artemis-data-" + JVM_NAME;
    private static final AtomicInteger BROKER_COUNT = new AtomicInteger();
    private static final Map<Integer, EmbeddedJMS> brokers = new ConcurrentHashMap<>();

    @Override
    public ConnectionFactory createConnectionFactory() {
        return createConnectionFactory(AddressSettings.DEFAULT_MAX_DELIVERY_ATTEMPTS, false);
    }

    @Override
    public ConnectionFactory createConnectionFactory(final int maximumRedeliveries) {
        return createConnectionFactory(maximumRedeliveries, false);
    }

    @Override
    public ConnectionFactory createPersistentConnectionFactory() {
        return createConnectionFactory(AddressSettings.DEFAULT_MAX_DELIVERY_ATTEMPTS, true);
    }

    @Override
    public ConnectionFactory createPooledConnectionFactory() {
        throw new UnsupportedOperationException("Artemis doesn't support pooled connection factory");
    }

    @Override
    public Destination createQueue(final String name) {
        return new ActiveMQQueue(name);
    }

    public ConnectionFactory createConnectionFactory(final int maximumRedeliveries, final boolean persistent) {
        int id = BROKER_COUNT.incrementAndGet();
        String baseDir = DATA_DIR + File.separator + id;
        final Configuration configuration;
        try {
            configuration = new ConfigurationImpl()
                .setPersistenceEnabled(persistent)
                .setSecurityEnabled(false)
                .setBindingsDirectory(baseDir + File.separator + "bindings")
                .setJournalDirectory(baseDir + File.separator + "journal")
                .setPagingDirectory(baseDir + File.separator + "paging")
                .setLargeMessagesDirectory(baseDir + File.separator + "largemessages")
                .addAcceptorConfiguration("invm", "vm://" + id)
                .addConnectorConfiguration("invm", "vm://" + id);
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        final ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl().setName("cf")
            .setConnectorNames("invm").setBindings("cf");

        final JMSConfiguration jmsConfig = new JMSConfigurationImpl()
            .setConnectionFactoryConfigurations(singletonList(cfConfig));

        EmbeddedJMS broker = new EmbeddedJMS().setConfiguration(configuration).setJmsConfiguration(jmsConfig);

        try {
            broker.start();
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        brokers.put(id, broker);

        final AddressSettings addressSettings = new AddressSettings()
                                                        .setMaxDeliveryAttempts(maximumRedeliveries)
                                                        .setDeadLetterAddress(new SimpleString("jms.queue.deadletter"))
                                                        .setExpiryAddress(new SimpleString("jms.queue.expired"));
        broker.getActiveMQServer().getAddressSettingsRepository().addMatch("#", addressSettings);

        TransportConfiguration transportConfigs = new TransportConfiguration(InVMConnectorFactory.class.getName());
        transportConfigs.getParams().put(TransportConstants.SERVER_ID_PROP_NAME, id);
        ServerLocator serviceLocator = ActiveMQClient.createServerLocator(false, transportConfigs);

        return new ActiveMQConnectionFactory(serviceLocator);
    }

    @Override
    public void shutdown() {
        for (Entry<Integer,EmbeddedJMS> set : brokers.entrySet()) {
            try {
                set.getValue().stop();
                Path dir = Paths.get(DATA_DIR + File.separator + set.getKey());
                Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            } catch (Exception e) {
                LOG.warn("Failed to remove artemis data file/dir: " + e.getMessage());
            }
        }
        new File(DATA_DIR).delete();
        brokers.clear();
    }

}
