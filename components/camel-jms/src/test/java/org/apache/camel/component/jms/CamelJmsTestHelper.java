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
import javax.jms.Destination;

import org.apache.camel.component.jms.activemq.ActiveMqJmsTestHelper;

/**
 * A helper for unit testing with embedded JMS broker.
 */
public final class CamelJmsTestHelper {

    public static final JmsTestHelper ACTIVEMQ = new ActiveMqJmsTestHelper();

    private static final CamelJmsTestHelper INSTANCE = new CamelJmsTestHelper();

    private static final ThreadLocal<JmsTestHelper> THREAD_LOCAL_DELEGATE = new ThreadLocal<>();

    JmsTestHelper delegate() {
        return THREAD_LOCAL_DELEGATE.get();
    }

    public static ConnectionFactory createConnectionFactory() {
        return INSTANCE.delegate().createConnectionFactory();
    }

    public static ConnectionFactory createConnectionFactory(final int maximumRedeliveries) {
        return INSTANCE.delegate().createConnectionFactory(maximumRedeliveries);
    }

    public static ConnectionFactory createPersistentConnectionFactory() {
        return INSTANCE.delegate().createPersistentConnectionFactory();
    }

    public static ConnectionFactory createPooledConnectionFactory() {
        return INSTANCE.delegate().createPooledConnectionFactory();
    }

    public static Destination createQueue(final String name) {
        return INSTANCE.delegate().createQueue(name);
    }

    static void clearCurrentHelper() {
        THREAD_LOCAL_DELEGATE.set(null);
    }

    static void setCurrentHelper(final JmsTestHelper helper) {
        THREAD_LOCAL_DELEGATE.set(helper);
    }

}
