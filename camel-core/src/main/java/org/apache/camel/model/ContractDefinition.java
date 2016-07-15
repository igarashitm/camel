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
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.processor.SetBodyProcessor;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RouteContext;

/**
 * Sets the contract of the message body which defines input/output type.
 */
@Metadata(label = "configuration")
@XmlRootElement(name = "contract")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContractDefinition extends OptionalIdentifiedDefinition<ContractDefinition> {
    @XmlAttribute
    private String input;
    @XmlAttribute
    private String output;


    public ContractDefinition() {
    }

    @Override
    public String toString() {
        return "contract[input=" + input + ", output=" + output + "]";
    }

    @Override
    public String getLabel() {
        return "contract[input=" + input + ", output=" + output + "]";
    }

    /**
     * Get input type URN.
     * @return input type URN
     */
    public String getInput() {
        return input;
    }

    /**
     * Set input type URN.
     * @param input input type URN
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * Get output type URN.
     * @return output type URN
     */
    public String getOutput() {
        return output;
    }

    /**
     * Set output type URN.
     * @param output output type URN
     */
    public void setOutput(String output) {
        this.output = output;
    }
}
