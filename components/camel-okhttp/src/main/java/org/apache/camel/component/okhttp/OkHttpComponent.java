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
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import org.apache.camel.CamelContext;
import org.apache.camel.ComponentVerifier;
import org.apache.camel.Endpoint;
import org.apache.camel.Producer;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.VerifiableComponent;
import org.apache.camel.http.common.HttpBinding;
import org.apache.camel.http.common.HttpCommonComponent;
import org.apache.camel.http.common.HttpHelper;
import org.apache.camel.http.common.HttpRestHeaderFilterStrategy;
import org.apache.camel.http.common.UrlRewrite;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RestProducerFactory;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the <a href="http://camel.apache.org/okhttp.html">OkHttp
 * Component</a>
 *
 * @version 
 */
@Metadata(label = "verifiers", enums = "parameters,connectivity")
public class OkHttpComponent extends HttpCommonComponent implements RestProducerFactory, VerifiableComponent {

    private static final Logger LOG = LoggerFactory.getLogger(OkHttpComponent.class);

    public OkHttpComponent() {
        super(OkHttpEndpoint.class);
    }

    public OkHttpComponent(Class<? extends OkHttpEndpoint> endpointClass) {
        super(endpointClass);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new OkHttpEndpoint(endpointUriString, this, httpUri);
    }

    @Override
    public Producer createProducer(CamelContext camelContext, String host,
                                   String verb, String basePath, String uriTemplate, String queryParameters,
                                   String consumes, String produces, Map<String, Object> parameters) throws Exception {
        String url = null;
        OkHttpEndpoint endpoint = camelContext.getEndpoint(url, OkHttpEndpoint.class);
        return endpoint.createProducer();
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();
    }

    @Override
    public void doStop() throws Exception {
        super.doStop();
    }

    /**
     * TODO: document
     */
    public ComponentVerifier getVerifier() {
        return new OkHttpComponentVerifier(this);
    }
}
