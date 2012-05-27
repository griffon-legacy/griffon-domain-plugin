/*
 * Copyright 2010-2012 the original author or authors.
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
import griffon.plugins.domain.methods.GetAllMethod;
import groovy.lang.MissingMethodException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Andres Almiray
 */
public abstract class AbstractGetAllPersistentMethod extends AbstractPersistentStaticMethodInvocation implements GetAllMethod {
    public AbstractGetAllPersistentMethod(GriffonDomainHandler griffonDomainHandler) {
        super(griffonDomainHandler);
    }

    @SuppressWarnings("unchecked")
    protected final Object invokeInternal(GriffonDomainClass domainClass, String methodName, Object[] arguments) {
        if (arguments.length == 0) {
            return getAll(domainClass);
        }

        final Object arg = arguments[0];
        if (arg instanceof List) {
            return getAllByIdentities(domainClass, (List) arg);
        } else if (arg instanceof Object[]) {
            return getAllByIdentities(domainClass, (Object[]) arg);
        }

        throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
    }

    protected Collection<GriffonDomain> getAll(GriffonDomainClass domainClass) {
        return Collections.<GriffonDomain>emptyList();
    }

    protected Collection<GriffonDomain> getAllByIdentities(GriffonDomainClass domainClass, List<Object> identities) {
        return Collections.<GriffonDomain>emptyList();
    }

    protected Collection<GriffonDomain> getAllByIdentities(GriffonDomainClass domainClass, Object[] identities) {
        return Collections.<GriffonDomain>emptyList();
    }
}