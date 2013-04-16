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

import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainClassUtils;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.exceptions.UnsupportedDomainMethodException;
import griffon.plugins.domain.methods.WhereMethod;
import griffon.plugins.domain.orm.Criterion;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public abstract class AbstractWherePersistentMethod extends AbstractPersistentStaticMethodInvocation implements WhereMethod {
    public AbstractWherePersistentMethod(GriffonDomainHandler griffonDomainHandler) {
        super(griffonDomainHandler);
    }

    @SuppressWarnings("unchecked")
    protected Object invokeInternal(GriffonDomainClass domainClass, String methodName, Object[] arguments) {
        if (arguments.length == 1) {
            final Object arg1 = arguments[0];
            if (arg1 instanceof Closure) {
                return withCriteria(domainClass, GriffonDomainClassUtils.getInstance().buildCriterion((Closure) arg1), Collections.<String, Object>emptyMap());
            }
        } else if (arguments.length == 2) {
            final Object arg1 = arguments[0];
            final Object arg2 = arguments[1];

            if (arg1 instanceof Map && arg2 instanceof Closure) {
                return withCriteria(domainClass, GriffonDomainClassUtils.getInstance().buildCriterion((Closure) arg2), (Map) arg1);
            }
        }
        throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
    }

    protected Collection<GriffonDomain> withCriteria(GriffonDomainClass domainClass, Criterion criterion, Map<String, Object> options) {
        throw new UnsupportedDomainMethodException();
    }
}