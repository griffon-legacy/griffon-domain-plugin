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
import griffon.plugins.domain.methods.ListOrderByMethod;
import groovy.lang.MissingMethodException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static griffon.util.GriffonNameUtils.isBlank;
import static griffon.util.GriffonNameUtils.uncapitalize;

/**
 * @author Andres Almiray
 */
public abstract class AbstractListOrderByPersistentMethod extends AbstractPersistentStaticDynamicMethodInvocation implements ListOrderByMethod {
    private static final String METHOD_PATTERN = "(" + METHOD_NAME + ")([A-Z]\\w*)";

    protected AbstractListOrderByPersistentMethod(GriffonDomainHandler griffonDomainHandler) {
        super(griffonDomainHandler, Pattern.compile(METHOD_PATTERN));
    }

    @Override
    protected Object invokeInternal(GriffonDomainClass domainClass, String methodName, Object[] arguments) {
        String propertyName = (String) arguments[0];
        Map<String, Object> params = new LinkedHashMap<String, Object>();

        if (isBlank(propertyName)) {
            throw new IllegalArgumentException("Property name is either null or empty!");
        }

        propertyName = uncapitalize(propertyName);

        GriffonDomainProperty prop = domainClass.getPropertyByName(propertyName);
        if (prop == null) {
            throw new IllegalArgumentException("Property " + propertyName + " doesn't exist for '" + domainClass.getClazz().getName() + "'");
        }

        if (arguments.length == 2) {
            if (arguments[1] instanceof Map) {
                params = (Map<String, Object>) arguments[1];
            } else {
                throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
            }
        }
        if (!params.containsKey(ORDER)) params.put(ORDER, ASC);
        return listOrderBy(domainClass, propertyName, params);
    }

    protected Collection listOrderBy(GriffonDomainClass domainClass, String propertyName, Map<String, Object> params) {
        throw new UnsupportedDomainMethodException();
    }
}