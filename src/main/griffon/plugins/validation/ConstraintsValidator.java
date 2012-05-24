/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.plugins.validation;

import griffon.plugins.domain.*;
import griffon.plugins.validation.constraints.ConstrainedProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public final class ConstraintsValidator {
    private ConstraintsValidator() {

    }

    public static boolean evaluate(Validateable validateable) {
        Map<String, ConstrainedProperty> constrainedProperties = validateable.constrainedProperties();

        Map<String, GriffonDomainProperty> domainProperties = fetchDomainProperties(validateable);
        for (Map.Entry<String, ConstrainedProperty> entry : constrainedProperties.entrySet()) {
            GriffonDomainProperty domainProperty = domainProperties.get(entry.getKey());
            ConstrainedProperty constrainedProperty = entry.getValue();
            constrainedProperty.validate(validateable, domainProperty.getValue(validateable), validateable.getErrors());
        }

        return !validateable.getErrors().hasErrors();
    }

    private static Map<String, GriffonDomainProperty> fetchDomainProperties(Validateable validateable) {
        Map<String, GriffonDomainProperty> domainProperties = new LinkedHashMap<String, GriffonDomainProperty>();

        if (validateable instanceof CommandObject) {
            domainProperties.putAll(((CommandObject) validateable).domainProperties());
        } else if (validateable instanceof GriffonDomain) {
            GriffonDomainClass griffonDomainClass = (GriffonDomainClass) ((GriffonDomain) validateable).getGriffonClass();
            for (GriffonDomainClassProperty property : griffonDomainClass.getPersistentProperties()) {
                domainProperties.put(property.getName(), property);
            }
        }

        return domainProperties;
    }
}
