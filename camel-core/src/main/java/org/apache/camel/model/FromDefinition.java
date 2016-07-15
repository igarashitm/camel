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
package org.apache.camel.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.Endpoint;
import org.apache.camel.Expression;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.spi.Contract;
import org.apache.camel.spi.ContractAware;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.ObjectHelper;

/**
 * Act as a message source as input to a route
 *
 * @version 
 */
@Metadata(label = "eip,endpoint,routing")
@XmlRootElement(name = "from")
@XmlAccessorType(XmlAccessType.FIELD)
public class FromDefinition extends OptionalIdentifiedDefinition<FromDefinition> implements EndpointRequiredDefinition, ContractAwareDefinition<FromDefinition> {
    @XmlAttribute @Metadata(required = "true")
    private String uri;
    @XmlAttribute
    @Deprecated
    private String ref;
    @XmlTransient
    private Endpoint endpoint;
    @XmlTransient
    private Contract contract;
    @XmlElementRef(required = false, name = "inputType")
    private InputTypeDefinition inputTypeDefinition;
    @XmlElementRef(required = false, name = "outputType")
    private OutputTypeDefinition outputTypeDefinition;
    @XmlElementRef(required = false, name = "contract")
    private ContractDefinition contractDefinition;
    

    public FromDefinition() {
    }

    public FromDefinition(String uri) {
        setUri(uri);
    }

    public FromDefinition(Endpoint endpoint) {
        setEndpoint(endpoint);
    }

    @Override
    public String toString() {
        return "From[" + getLabel() + "]";
    }

    public String getLabel() {
        return description(getUri(), getRef(), getEndpoint());
    }

    public Endpoint resolveEndpoint(RouteContext context) {
        if (endpoint == null) {
            Endpoint endpoint = context.resolveEndpoint(getUri(), getRef());
            if (endpoint instanceof ContractAware) {
                ((ContractAware)endpoint).setContract(getContract());
            }
            return endpoint;
        } else {
            return endpoint;
        }
    }

    @Override
    public String getEndpointUri() {
        return getUri();
    }

    // Properties
    // -----------------------------------------------------------------------

    public String getUri() {
        if (uri != null) {
            return uri;
        } else if (endpoint != null) {
            return endpoint.getEndpointUri();
        } else {
            return null;
        }
    }

    /**
     * Sets the URI of the endpoint to use
     *
     * @param uri the endpoint URI to use
     */
    public void setUri(String uri) {
        clear();
        this.uri = uri;
    }

    public String getRef() {
        return ref;
    }

    /**
     * Sets the name of the endpoint within the registry (such as the Spring
     * ApplicationContext or JNDI) to use
     *
     * @param ref the reference name to use
     * @deprecated use uri with ref:uri instead
     */
    @Deprecated
    public void setRef(String ref) {
        clear();
        this.ref = ref;
    }

    /**
     * Gets tne endpoint if an {@link Endpoint} instance was set.
     * <p/>
     * This implementation may return <tt>null</tt> which means you need to use
     * {@link #getRef()} or {@link #getUri()} to get information about the endpoint.
     *
     * @return the endpoint instance, or <tt>null</tt>
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
        this.uri = null;
        if (endpoint != null) {
            this.uri = endpoint.getEndpointUri();
        }
        if (getContract() != null && endpoint instanceof ContractAware) {
            ((ContractAware)endpoint).setContract(getContract());
        }
    }

    /**
     * Returns the endpoint URI or the name of the reference to it
     */
    public Object getUriOrRef() {
        if (ObjectHelper.isNotEmpty(uri)) {
            return uri;
        } else if (endpoint != null) {
            return endpoint.getEndpointUri();
        }
        return ref;
    }

    @Override
    public void setInputTypeDefinition(InputTypeDefinition inputType) {
        this.inputTypeDefinition = inputType;
    }

    @Override
    public InputTypeDefinition getInputTypeDefinition() {
        return this.inputTypeDefinition;
    }

    @Override
    public void setOutputTypeDefinition(OutputTypeDefinition outputType) {
        this.outputTypeDefinition = outputType;
    }

    @Override
    public OutputTypeDefinition getOutputTypeDefinition() {
        return this.outputTypeDefinition;
    }

    @Override
    public  void setContractDefinition(ContractDefinition contractDef) {
        this.contractDefinition = contractDef;
    }

    @Override
    public ContractDefinition getContractDefinition() {
        return this.contractDefinition;
    }

    @Override
    public void setContract(Contract contract) {
        this.contract = contract;
    }

    @Override
    public Contract getContract() {
        if (contract != null) {
            return contract;
        }
        if (contractDefinition != null) {
            contract = new Contract();
            contract.setInputType(contractDefinition.getInput());
            contract.setOutputType(contractDefinition.getOutput());
            return contract;
        } else if (inputTypeDefinition != null || outputTypeDefinition != null) {
            contract = new Contract();
            if (inputTypeDefinition != null) {
                contract.setInputType(inputTypeDefinition.getUrn());
            }
            if (outputTypeDefinition != null) {
                contract.setOutputType(outputTypeDefinition.getUrn());
            }
            return contract;
        }
        return null;
    }

    // Implementation methods
    // -----------------------------------------------------------------------
    protected static String description(String uri, String ref, Endpoint endpoint) {
        if (ref != null) {
            return "ref:" + ref;
        } else if (endpoint != null) {
            return endpoint.getEndpointUri();
        } else if (uri != null) {
            return uri;
        } else {
            return "no uri or ref supplied!";
        }
    }

    protected void clear() {
        this.endpoint = null;
        this.ref = null;
        this.uri = null;
    }

}
