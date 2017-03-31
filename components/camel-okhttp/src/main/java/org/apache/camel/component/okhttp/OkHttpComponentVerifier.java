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

import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;

import org.apache.camel.ComponentVerifier;
import org.apache.camel.http.common.HttpHelper;
import org.apache.camel.impl.verifier.DefaultComponentVerifier;
import org.apache.camel.impl.verifier.ResultBuilder;
import org.apache.camel.impl.verifier.ResultErrorBuilder;
import org.apache.camel.impl.verifier.ResultErrorHelper;

final class OkHttpComponentVerifier extends DefaultComponentVerifier {
    private final OkHttpComponent component;

    OkHttpComponentVerifier(OkHttpComponent component) {
        super("okhttp", component.getCamelContext());

        this.component = component;
    }

    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        // The default is success
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS);

        // Validate using the catalog
        super.verifyParametersAgainstCatalog(builder, parameters);

        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************

    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        // Default is success
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY);

        Optional<String> uri = getOption(parameters, "httpUri", String.class);
        if (!uri.isPresent()) {
            // lack of httpUri is a blocking issue
            builder.error(ResultErrorHelper.requiresOption("httpUri", parameters));
        } else {
            builder.error(parameters, this::verifyHttpConnectivity);
        }

        return builder.build();
    }

    private void verifyHttpConnectivity(ResultBuilder builder, Map<String, Object> parameters) throws Exception {
        Optional<String> uri = getOption(parameters, "httpUri", String.class);

    }

}
