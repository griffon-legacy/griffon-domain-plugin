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

package org.codehaus.griffon.runtime.domain;

import griffon.plugins.domain.GriffonDomainProperty;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.PropertyDescriptor;

/**
 * @author Andres Almiray
 */
public class DefaultGriffonDomainProperty implements GriffonDomainProperty {
    private final PropertyDescriptor propertyDescriptor;

    public DefaultGriffonDomainProperty(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public String getName() {
        return propertyDescriptor.getName();
    }

    public Class getType() {
        return propertyDescriptor.getPropertyType();
    }

    public Object getValue(Object owner) {
        return InvokerHelper.getProperty(owner, getName());
    }

    public void setValue(Object owner, Object value) {
        InvokerHelper.setProperty(owner, propertyDescriptor.getName(), value);
    }
}
