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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.component.jms.JmsTestHelper;
import org.apache.camel.util.FileUtil;

public final class ActiveMqJmsTestHelper implements JmsTestHelper {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private static final String JVM_NAME = ManagementFactory.getRuntimeMXBean().getName();

    private final Set<File> persistedDirectories = new HashSet<>();

    @Override
    public ConnectionFactory createConnectionFactory() {
        // using a unique broker name improves testing when running the entire
        // test suite in the same JVM
        final String id = brokerIdentifier();
        final String url = "vm://test-broker-" + id + "?broker.persistent=false&broker.useJmx=false";

        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        // optimize AMQ to be as fast as possible so unit testing is quicker
        connectionFactory.setCopyMessageOnSend(false);
        connectionFactory.setOptimizeAcknowledge(true);
        connectionFactory.setOptimizedMessageDispatch(true);
        // When using asyncSend, producers will not be guaranteed to send in the
        // order we have in the tests (which may be confusing for queues) so we
        // need this set to false.
        // Another way of guaranteeing order is to use persistent messages or
        // transactions.
        connectionFactory.setUseAsyncSend(false);
        connectionFactory.setAlwaysSessionAsync(false);
        connectionFactory.setTrustAllPackages(true);
        return connectionFactory;
    }

    @Override
    public ConnectionFactory createConnectionFactory(final int maximumRedeliveries) {
        final ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) createConnectionFactory();
        connectionFactory.getRedeliveryPolicy().setMaximumRedeliveries(maximumRedeliveries);
        return connectionFactory;
    }

    @Override
    public ConnectionFactory createPersistentConnectionFactory() {
        return createPersistentConnectionFactory(null);
    }

    public ConnectionFactory createPersistentConnectionFactory(final String options) {
        // using a unique broker name improves testing when running the entire
        // test suite in the same JVM
        final String id = brokerIdentifier();

        // use an unique data directory in target
        final String dir = "target/activemq-data-" + id;

        // remove dir so its empty on startup
        final File persistentDirectory = new File(dir);
        FileUtil.removeDir(persistentDirectory);
        persistentDirectory.deleteOnExit();

        persistedDirectories.add(persistentDirectory);

        String url = "vm://test-broker-" + id + "?broker.persistent=true&broker.useJmx=false&broker.dataDirectory="
            + dir;
        if (options != null) {
            url = url + "&" + options;
        }
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        // optimize AMQ to be as fast as possible so unit testing is quicker
        connectionFactory.setCopyMessageOnSend(false);
        connectionFactory.setOptimizeAcknowledge(true);
        connectionFactory.setOptimizedMessageDispatch(true);
        connectionFactory.setAlwaysSessionAsync(false);
        connectionFactory.setTrustAllPackages(true);
        return connectionFactory;
    }

    @Override
    public ConnectionFactory createPooledConnectionFactory() {
        final ConnectionFactory cf = createConnectionFactory();
        final PooledConnectionFactory pooled = new PooledConnectionFactory();
        pooled.setConnectionFactory(cf);
        pooled.setMaxConnections(8);
        return pooled;
    }

    @Override
    public Destination createQueue(final String name) {
        return new ActiveMQQueue(name);
    }

    @Override
    public void shutdown() {
        persistedDirectories.forEach(File::delete);
    }

    static String brokerIdentifier() {
        return JVM_NAME + "-" + COUNTER.incrementAndGet();
    }

}
