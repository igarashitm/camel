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

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp producer.
 */
public class OkHttpProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(OkHttpProducer.class);
    private OkHttpClient httpClient;

    public OkHttpProducer(OkHttpEndpoint endpoint) {
        super(endpoint);
        httpClient = endpoint.getHttpClient();
    }

    public void process(Exchange exchange) throws Exception {
        OkHttpClient client = prepareHttpClient(exchange);
        Request request = prepareRequest(exchange);
        Response response = client.newCall(request).execute();
        populateResponse(exchange, response);
    }

    private OkHttpClient prepareHttpClient(Exchange exchange) {
        OkHttpClient.Builder builder = httpClient.newBuilder();
        return builder.build();
    }

    private Request prepareRequest(Exchange exchange) {
        Request.Builder builder = new Request.Builder();

        return builder.build();
    }

    private void populateResponse(Exchange exchange, Response response) {
        exchange.getOut().setBody(response.body().byteStream());
    }

    @Override
    public OkHttpEndpoint getEndpoint() {
        return (OkHttpEndpoint) super.getEndpoint();
    }

}