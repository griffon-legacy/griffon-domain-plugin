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
package org.codehaus.griffon.runtime.domain.methods;

import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.domain.exceptions.UnsupportedDomainMethodException;
import griffon.plugins.domain.methods.FirstMethod;
import groovy.lang.MissingMethodException;

import java.util.Map;

/**
 * @author Andres Almiray
 */
public abstract class AbstractFirstPersistentMethod extends AbstractPersistentStaticMethodInvocation implements FirstMethod {
    public AbstractFirstPersistentMethod(GriffonDomainHandler griffonDomainHandler) {
        super(griffonDomainHandler);
    }

    @SuppressWarnings("unchecked")
    protected Object invokeInternal(GriffonDomainClass domainClass, String methodName, Object[] arguments) {
        if (arguments.length == 0) {
            return firstByPropertyName(domainClass, GriffonDomainProperty.IDENTITY);
        }
        if (arguments.length == 1) {
            final Object arg = arguments[0];
            if (arg instanceof CharSequence) {
                return firstByPropertyName(domainClass, (String.valueOf(arg)));
            } else if (arg instanceof Map) {
                Map map = (Map) arg;
                Object propertyName = map.get("sort");
                if (propertyName == null) propertyName = GriffonDomainProperty.IDENTITY;
                return firstByPropertyName(domainClass, (String.valueOf(propertyName)));
            }
        }
        throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
    }

    protected Object firstByPropertyName(GriffonDomainClass domainClass, String propertyName) {
        throw new UnsupportedDomainMethodException();
    }
}