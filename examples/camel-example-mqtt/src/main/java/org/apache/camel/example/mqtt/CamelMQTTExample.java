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
package org.apache.camel.example.mqtt;

import java.util.concurrent.TimeUnit;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CamelMQTTExample {

    private Logger _logger = LoggerFactory.getLogger(CamelMQTTExample.class);
    private static final String DIRECT = "direct:foo";
    private static final String TOPIC_IN = "testTopicIn";
    private static final String TOPIC_OUT = "testTopicOut";
    private static final String PAYLOAD = "test mqtt";

    private BrokerService brokerService;
    private CamelContext camelContext;

    public void testProduce() throws Exception {

        _logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        testProduceFromDirect();
        testProduceFromMQTTConsumer();
        testProduceFromDirect();
        testProduceFromMQTTConsumer();
        _logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

    }

    public void testProduceFromDirect() throws Exception {
        MQTT mqtt = new MQTT();
        BlockingConnection subscribeConnection = subscribeMQTTTopic(mqtt, TOPIC_OUT, QoS.AT_LEAST_ONCE);
        long start = System.currentTimeMillis();
        sendDirect(camelContext, DIRECT, PAYLOAD);
        Message msg = subscribeConnection.receive(30000, TimeUnit.MILLISECONDS);
        long end = System.currentTimeMillis();
        if (msg != null) {
            _logger.info(">>>>> Direct input:\t" + (end-start) + "[ms]");
        } else {
            _logger.info(">>>>> Direct input:\tNo message was received.");
        }
        subscribeConnection.disconnect();
    }

    public void testProduceFromMQTTConsumer() throws Exception {
        MQTT mqtt = new MQTT();
        BlockingConnection subscribeConnection = subscribeMQTTTopic(mqtt, TOPIC_OUT, QoS.AT_LEAST_ONCE);
        long start = System.currentTimeMillis();
        publishMQTTMessage(mqtt, TOPIC_IN, PAYLOAD, QoS.AT_LEAST_ONCE, false);
        Message msg = subscribeConnection.receive(30000, TimeUnit.MILLISECONDS);
        long end = System.currentTimeMillis();
        if (msg != null) {
            _logger.info(">>>>> MQTT consumer:\t" + (end-start) + "[ms]");
        } else {
            _logger.info(">>>>> MQTT consumer:\tNo message was received.");
        }
    }

    public void before() throws Exception {
        // Start a ActiveMQ broker with MQTT connector enabled
        brokerService = createActiveMQBroker();
        brokerService.start();

        // Start a camel route which receives a message from testTopicIn and forwards it to testTopicOut
        camelContext = new DefaultCamelContext();
        camelContext.addRoutes(createCamelRoute());
        camelContext.start();
    }

    public void after() throws Exception {
        camelContext.stop();
        brokerService.stop();
    }

    private BrokerService createActiveMQBroker() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName("default");
        broker.setUseJmx(false);
        broker.setPersistent(false);
        broker.addConnector("mqtt://localhost:1883?maximumConnections=1000&wireFormat.maxFrameSize=104857600");
        broker.setUseShutdownHook(false);
        return broker;
    }

    private RouteBuilder createCamelRoute() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("mqtt:input?subscribeTopicName=" + TOPIC_IN)
                    .from(DIRECT)
                    .removeProperty("MQTTTopicPropertyName")
                    .to("mqtt:output?publishTopicName=" + TOPIC_OUT);
            }
        };
    }

    private void publishMQTTMessage(MQTT mqtt, String topic, String payload, QoS qos, boolean retain) throws Exception {
        BlockingConnection conn = mqtt.blockingConnection();
        conn.connect();
        conn.publish(topic, payload.getBytes(), qos, retain);
        conn.disconnect();
    }

    private void sendDirect(CamelContext context, String target, String payload) throws Exception {
        Producer producer = context.getEndpoint(target).createProducer();
        Exchange exchange = producer.createExchange();
        exchange.getIn().setBody(payload);
        producer.process(exchange);
    }

    private BlockingConnection subscribeMQTTTopic(MQTT mqtt, String topicName, QoS qos) throws Exception {
        BlockingConnection conn = mqtt.blockingConnection();
        conn.connect();
        Topic topic = new Topic(topicName, qos);
        conn.subscribe(new Topic[]{topic});
        return conn;
    }

    public static void main(String args[]) throws Exception {
        CamelMQTTExample instance = new CamelMQTTExample();
        instance.before();
        instance.testProduce();
        instance.after();
    }
}
