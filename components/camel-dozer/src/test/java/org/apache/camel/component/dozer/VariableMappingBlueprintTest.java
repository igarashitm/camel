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
package org.apache.camel.component.dozer;

import java.util.Dictionary;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.dozer.example.abc.ABCOrder;
import org.apache.camel.component.dozer.example.abc.ABCOrder.Header;
import org.apache.camel.component.dozer.example.xyz.XYZOrder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultComponentResolver;
import org.apache.camel.spi.ComponentResolver;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

public class VariableMappingBlueprintTest extends CamelBlueprintTestSupport {
    
    @Override
    protected String getBlueprintDescriptor() {
        return "org/apache/camel/component/dozer/VariableMappingBlueprintTest-context.xml";
    }
    
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        ComponentResolver testResolver = new DefaultComponentResolver();

        services.put(ComponentResolver.class.getName(), asService(testResolver, "component", "dozer"));
    }
    
    @Test
    public void testLiteralMapping() throws Exception {
        MockEndpoint resultEndpoint = getMockEndpoint("mock:result");
        resultEndpoint.expectedMessageCount(1);
        ABCOrder abcOrder = new ABCOrder();
        abcOrder.setHeader(new Header());
        abcOrder.getHeader().setStatus("GOLD");
        template.sendBody("direct:start", abcOrder);
        // check results
        resultEndpoint.assertIsSatisfied();
        XYZOrder result = resultEndpoint.getExchanges().get(0).getIn().getBody(XYZOrder.class);
        Assert.assertEquals(result.getPriority(), "GOLD");
        Assert.assertEquals("ACME-SALES", result.getCustId());
        Assert.assertEquals("W123-EG", result.getOrderId());
    }
}
