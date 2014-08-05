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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

public final class CamelMQTTExample {

    private static SimpleDateFormat _format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss,SSS");
    private static final String TOPIC_IN = "testTopicIn";
    private static final String TOPIC_OUT = "testTopicOut";
    private static final String PAYLOAD = "test mqtt";

    public static void main(String args[]) throws Exception {
        // Start a ActiveMQ broker with MQTT connector enabled
        BrokerService broker = new BrokerService();
        broker.setBrokerName("default");
        broker.setUseJmx(false);
        broker.setPersistent(false);
        broker.addConnector("mqtt://localhost:1883");
        broker.setUseShutdownHook(false);
        broker.start();

        // Start a camel route which receives a message from testTopicIn and forwards it to testTopicOut
        CamelContext context = new DefaultCamelContext();
        RouteBuilder routeBuilder = new RouteBuilder() {
            public void configure() {
                from("mqtt:input?subscribeTopicName=" + TOPIC_IN)
                    .setProperty("MQTTTopicPropertyName", constant(TOPIC_OUT))
                    .to("mqtt:output?publishTopicName=" + TOPIC_OUT);
            }
        };
        context.addRoutes(routeBuilder);
        context.start();

        // Start a thread to receive a output message from testTopicOut
        MQTT mqtt = new MQTT();
        final BlockingConnection subscribeConnection = mqtt.blockingConnection();
        subscribeConnection.connect();
        Topic topic = new Topic(TOPIC_OUT, QoS.AT_LEAST_ONCE);
        subscribeConnection.subscribe(new Topic[]{topic});
        final AtomicBoolean received = new AtomicBoolean(false);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (subscribeConnection.isConnected()) {
                        Message msg = subscribeConnection.receive(1000, TimeUnit.MILLISECONDS);
                        if (msg == null) {
                            System.out.println("No message from " + TOPIC_OUT + " - continue...");
                            continue;
                        } else {
                            System.out.println(_format.format(new Date(System.currentTimeMillis())) + " >>>>> Received from " + TOPIC_OUT + ": " + msg.getPayload());
                            msg.ack();
                            received.set(true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        // Send a test message to testTopicIn
        BlockingConnection publishConnection = mqtt.blockingConnection();
        publishConnection.connect();
        publishConnection.publish(TOPIC_IN, PAYLOAD.getBytes(), QoS.AT_LEAST_ONCE, false);
        publishConnection.disconnect();

        Thread.sleep(20000);
        subscribeConnection.unsubscribe(new String[]{TOPIC_OUT});
        subscribeConnection.disconnect();
        if (!received.get()) {
            throw new AssertionError("No message was received from " + TOPIC_OUT);
        }

        context.stop();
        broker.stop();
    }
}
