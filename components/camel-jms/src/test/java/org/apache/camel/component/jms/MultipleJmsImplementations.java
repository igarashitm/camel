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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.component.jms.activemq.ActiveMqJmsTestHelper;
import org.apache.camel.component.jms.artemis.ArtemisJmsTestHelper;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public final class MultipleJmsImplementations extends Suite {

    static final class JmsHelperRule extends ExternalResource {

        private final JmsTestHelper helper;

        public JmsHelperRule(final JmsTestHelper helper) {
            this.helper = helper;
        }

        @Override
        protected void after() {
            helper.shutdown();
            CamelJmsTestHelper.clearCurrentHelper();
        }

        @Override
        protected void before() throws Throwable {
            helper.startup();
            CamelJmsTestHelper.setCurrentHelper(helper);
        }
    }

    static final class WithSpecificJmsHelperRunner extends BlockJUnit4ClassRunner {

        private final JmsTestHelper helper;

        private final String name;

        private WithSpecificJmsHelperRunner(final Class<?> klass, final String name, final JmsTestHelper helper)
            throws InitializationError {
            super(klass);
            this.name = name;
            this.helper = helper;
        }

        @Override
        protected String getName() {
            return name;
        }

        @Override
        protected List<TestRule> getTestRules(final Object target) {
            final List<TestRule> rules = super.getTestRules(target);

            rules.add(new JmsHelperRule(helper));

            return rules;
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return method.getName() + " [" + name + "]";
        }
    }

    public static final Map<String, JmsTestHelper> KNOWN_JMS_HELPERS;

    static {
        KNOWN_JMS_HELPERS = new HashMap<>();
        KNOWN_JMS_HELPERS.put("ActiveMQ", new ActiveMqJmsTestHelper());
        KNOWN_JMS_HELPERS.put("Artemis", new ArtemisJmsTestHelper());
    }

    public MultipleJmsImplementations(final Class<?> klass) throws Throwable {
        super(klass, Collections.<Runner>emptyList());
    }

    Runner createRunner(final String name, final JmsTestHelper helper) {
        try {
            final Class<?> javaClass = getTestClass().getJavaClass();

            return new WithSpecificJmsHelperRunner(javaClass, name, helper);
        } catch (final InitializationError e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected List<Runner> getChildren() {
        return KNOWN_JMS_HELPERS.entrySet().stream().map(e -> createRunner(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
}
