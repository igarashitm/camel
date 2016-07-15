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
 * Set data type of the input message.
 */
@Metadata(label = "configuration")
@XmlRootElement(name = "inputType")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputTypeDefinition extends OptionalIdentifiedDefinition<InputTypeDefinition> {
    @XmlAttribute(required = true)
    private String urn;

    public InputTypeDefinition() {
    }

    @Override
    public String toString() {
        return "inputType[" + urn + "]";
    }

    @Override
    public String getLabel() {
        return "inputType[" + urn + "]";
    }

    /**
     * Get input type URN.
     * @return input type URN
     */
    public String getUrn() {
        return urn;
    }

    /**
     * Set input type URN.
     * @param urn input type URN
     */
    public void setUrn(String urn) {
        this.urn = urn;
    }
}
