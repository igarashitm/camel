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
package org.apache.camel.component.okhttp;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.ServicePoolAware;
import org.apache.camel.http.common.HttpHelper;
import org.apache.camel.impl.PollingConsumerSupport;
import org.apache.camel.spi.HeaderFilterStrategy;

/**
 * A polling HTTP consumer which by default performs a GET
 */
public class OkHttpPollingConsumer extends PollingConsumerSupport implements ServicePoolAware {
    private final OkHttpEndpoint endpoint;

    public OkHttpPollingConsumer(OkHttpEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public Exchange receive() {
        return doReceive(-1);
    }

    public Exchange receive(long timeout) {
        return doReceive((int) timeout);
    }

    public Exchange receiveNoWait() {
        return doReceive(-1);
    }

    protected Exchange doReceive(int timeout) {
        Exchange exchange = endpoint.createExchange();
        return exchange;
    }

    // Properties
    //-------------------------------------------------------------------------

    // Implementation methods
    //-------------------------------------------------------------------------

    protected void doStart() throws Exception {
    }

    protected void doStop() throws Exception {
    }
}