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
package org.apache.camel.model.transformer;

import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Message;
import org.apache.camel.impl.transformer.DataFormatTransformer;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.Transformer;

/**
 * Represents a DataFormatTransformer.
 */
@Metadata(label = "transformation")
@XmlType(name = "dataFormatTransformer")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataFormatTransformerDefinition extends TransformerDefinition {

    @XmlAttribute
    private String ref;

    @Override
    protected Transformer doCreateTransformer() {
        return new DataFormatTransformer(getCamelContext())
                .setDataFormatRef(ref)
                .setModel(getScheme())
                .setFrom(getFrom())
                .setTo(getTo());
    }

    public String getRef() {
        return ref;
    }

    /**
     * Set the reference of the DataFormat.
     * @param ref reference of the DataFormat
     */
    public void setRef(String ref) {
        this.ref = ref;
    }
}

