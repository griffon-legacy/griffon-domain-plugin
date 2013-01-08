/*
 * Copyright 2009-2013 the original author or authors.
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

import griffon.plugins.domain.AtomicValue;
import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainClassProperty;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public final class ConstraintsValidator {
    private ConstraintsValidator() {

    }

    public static boolean evaluate(Validateable validateable, List<String> properties) {
        List<String> list = properties != null ? properties : Collections.<String>emptyList();
        return evaluate(validateable, list.toArray(new String[list.size()]));
    }

    public static boolean evaluate(Validateable validateable, String... properties) {
        Map<String, ConstrainedProperty> constrainedProperties = new LinkedHashMap<String, ConstrainedProperty>();

        if (properties == null || properties.length == 0) {
            constrainedProperties.putAll(validateable.constrainedProperties());
        } else {
            for (String property : properties) {
                constrainedProperties.put(property, validateable.constrainedProperties().get(property));
            }
        }

        for (Map.Entry<String, ConstrainedProperty> entry : constrainedProperties.entrySet()) {
            ConstrainedProperty constrainedProperty = entry.getValue();
            constrainedProperty.validate(validateable, getPropertyValue(validateable, entry.getKey()), validateable.getErrors());
        }

        return !validateable.getErrors().hasErrors();
    }

    private static Object getPropertyValue(Validateable validateable, String propertyName) {
        if (validateable instanceof GriffonDomain) {
            GriffonDomainClass griffonDomainClass = (GriffonDomainClass) ((GriffonDomain) validateable).getGriffonClass();
            for (GriffonDomainClassProperty property : griffonDomainClass.getPersistentProperties()) {
                if (property.getName().equals(propertyName)) {
                    return property.getValue(validateable);
                }
            }
        } else {
            Object value = InvokerHelper.getProperty(validateable, propertyName);
            if (value instanceof AtomicValue) {
                return ((AtomicValue) value).getValue();
            }
            return value;
        }
        return null;
    }
}
