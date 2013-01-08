/*
 * Copyright 2004-2013 the original author or authors.
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
package org.codehaus.griffon.runtime.domain.methods;

import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.domain.methods.CreateMethod;
import groovy.lang.MissingMethodException;

import java.util.Map;

/**
 * @author Andres Almiray
 */
public abstract class AbstractCreatePersistentMethod extends AbstractPersistentStaticMethodInvocation implements CreateMethod {
    public AbstractCreatePersistentMethod(GriffonDomainHandler domainHandler) {
        super(domainHandler);
    }

    @SuppressWarnings("unchecked")
    protected final Object invokeInternal(GriffonDomainClass domainClass, String methodName, Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return domainClass.newInstance();
        } else if (arguments[0] instanceof Map) {
            return create(domainClass, (Map) arguments[0]);
        }
        throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
    }

    protected GriffonDomain create(GriffonDomainClass domainClass, Map<String, Object> props) {
        GriffonDomain instance = createInstance(domainClass);
        applyProperties(domainClass, instance, props);
        return instance;
    }

    private GriffonDomain createInstance(GriffonDomainClass domainClass) {
        return (GriffonDomain) domainClass.newInstance();
    }

    private void applyProperties(GriffonDomainClass domainClass, GriffonDomain instance, Map<String, Object> props) {
        for (GriffonDomainProperty property : domainClass.getProperties()) {
            Object value = props.get(property.getName());
            if (value != null) property.setValue(instance, value);
        }
    }
}