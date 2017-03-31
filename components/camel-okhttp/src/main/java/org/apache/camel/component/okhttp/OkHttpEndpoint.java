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

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;

import org.apache.camel.Consumer;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.http.common.HttpCommonEndpoint;
import org.apache.camel.http.common.HttpHelper;
import org.apache.camel.http.common.cookie.CookieHandler;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;

/**
 * For calling out to external HTTP servers using OkHttp client.
 */
@UriEndpoint(firstVersion = "2.19.0", scheme = "okhttp", title = "OkHttp", syntax = "okhttp:httpUri",
    producerOnly = true, label = "http", lenientProperties = true)
public class OkHttpEndpoint extends HttpCommonEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OkHttpEndpoint.class);
    private OkHttpClient httpClient;

    public OkHttpEndpoint() {
    }

    public OkHttpEndpoint(String endPointURI, OkHttpComponent component, URI httpURI) throws URISyntaxException {
        super(endPointURI, component, httpURI);
    }

    public Producer createProducer() throws Exception {
        return new OkHttpProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Cannot consume from http endpoint");
    }

    public PollingConsumer createPollingConsumer() throws Exception {
        OkHttpPollingConsumer answer = new OkHttpPollingConsumer(this);
        configurePollingConsumer(answer);
        return answer;
    }

    @Override
    public OkHttpComponent getComponent() {
        return (OkHttpComponent) super.getComponent();
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    // Properties
    //-------------------------------------------------------------------------


}
