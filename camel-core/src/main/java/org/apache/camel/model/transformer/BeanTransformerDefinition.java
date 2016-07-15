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
import org.apache.camel.component.bean.BeanHolder;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.bean.ConstantStaticTypeBeanHolder;
import org.apache.camel.component.bean.ConstantTypeBeanHolder;
import org.apache.camel.component.bean.RegistryBean;
import org.apache.camel.impl.transformer.ProcessorTransformer;
import org.apache.camel.model.BeanDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.DataType;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.Transformer;
import org.apache.camel.util.ObjectHelper;

/**
 * Represents a BeanTransformer.
 */
@Metadata(label = "transformation")
@XmlType(name = "beanTransformer")
@XmlAccessorType(XmlAccessType.FIELD)
public class BeanTransformerDefinition extends TransformerDefinition {

    @XmlAttribute
    private String ref;
    @XmlAttribute
    private String beanType;
    @XmlAttribute
    private String method;
    @XmlAttribute @Metadata(defaultValue = "true")
    private Boolean cache = true;

    @Override
    protected Transformer doCreateTransformer() throws Exception {
        return new ProcessorTransformer(getCamelContext())
            .setProcessor(createBeanProcessor())
            .setModel(getScheme())
            .setFrom(getFrom())
            .setTo(getTo());
    }

    protected BeanProcessor createBeanProcessor() throws Exception {
        BeanProcessor answer;
        BeanHolder beanHolder;
        if (ObjectHelper.isNotEmpty(ref)) {
            if (cache) {
                beanHolder = new RegistryBean(getCamelContext(), ref).createCacheHolder();
            } else {
                beanHolder = new RegistryBean(getCamelContext(), ref);
            }
        } else if (ObjectHelper.isNotEmpty(beanType)) {
            Class<?> clazz;
            try {
                clazz = getCamelContext().getClassResolver().resolveMandatoryClass(beanType);
            } catch (ClassNotFoundException e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
            if (cache && ObjectHelper.hasDefaultPublicNoArgConstructor(clazz)) {
                // we can only cache if we can create an instance of the bean, and for that we need a public constructor
                try {
                    beanHolder = new ConstantTypeBeanHolder(clazz, getCamelContext()).createCacheHolder();
                } catch (Exception e) {
                    throw ObjectHelper.wrapRuntimeCamelException(e);
                }
            } else {
                if (ObjectHelper.hasDefaultPublicNoArgConstructor(clazz)) {
                    beanHolder = new ConstantTypeBeanHolder(clazz, getCamelContext());
                } else {
                    // this is only for invoking static methods on the bean
                    beanHolder = new ConstantStaticTypeBeanHolder(clazz, getCamelContext());
                }
            }
        } else {
            throw new IllegalArgumentException("One of 'ref' or 'beanType' must be specified for bean transformer");
        }
        
        answer = new BeanProcessor(beanHolder);
        answer.setMethod(method);
        return answer;
    }

    public String getRef() {
        return ref;
    }

    /**
     * Set a reference of the Bean.
     * @param ref the reference of the Bean
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getBeanType() {
        return beanType;
    }

    /**
     * Set a Bean clas name.
     * @param type Bean class name
     */
    public void setBeanType(String type) {
        this.beanType = type;
    }

    public String getMethod() {
        return method;
    }

    /**
     * Set a method name to be invoked.
     * @param method method name
     */
    public void setMethod(String method) {
        this.method = method;
    }

    public Boolean getCache() {
        return cache;
    }

    /**
     * Set if Bean instance should be cached or not.
     * @param cache true if cache
     */
    public void setCache(Boolean cache) {
        this.cache = cache;
    }
    
}

