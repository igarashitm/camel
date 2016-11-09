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

import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.camel.spi.Contract;

/**
 * Models can support being configured with contract which defines inputType and outputType.
 */
public interface ContractAwareDefinition<Type> {

    /**
     * Set InputTypeDefinition.
     * @param inputType InputTypeDefinition
     */
    void setInputTypeDefinition(InputTypeDefinition inputType);

    /**
     * Get InputTypeDefinition.
     * 
     * @return InputTypeDefinition
     */
    InputTypeDefinition getInputTypeDefinition();

    /**
     * Set OutputTypeDefinition.
     * @param outputType OutputTypeDefinition
     */
    void setOutputTypeDefinition(OutputTypeDefinition outputType);

    /**
     * Get OutputTypeDefinition.
     * 
     * @return OutputTypeDefinition
     */
    OutputTypeDefinition getOutputTypeDefinition();

    /**
     * Set ContractDefinition.
     * @param contract ContractDefinition
     */
    void setContractDefinition(ContractDefinition contract);

    /**
     * Get ContractDefinition.
     * 
     * @return ContractTypeDefinition
     */
    ContractDefinition getContractDefinition();

    /**
     * Set Contract.
     * @param contract Contract
     */
    void setContract(Contract contract);

    /**
     * Get Contract associated with this instance.
     * @return Contract
     */
    Contract getContract();
}
