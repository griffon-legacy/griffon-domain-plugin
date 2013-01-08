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

import griffon.core.GriffonApplication;
import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.domain.exceptions.UnsupportedDomainMethodException;
import griffon.plugins.domain.methods.InstanceMethodInvocation;
import griffon.plugins.domain.methods.StaticMethodInvocation;
import griffon.util.ApplicationHolder;

import java.util.LinkedHashMap;
import java.util.Map;

import static griffon.util.GriffonExceptionHandler.sanitize;

/**
 * @author Andres Almiray
 */
public abstract class AbstractGriffonDomainHandler implements GriffonDomainHandler {
    private final Map<String, InstanceMethodInvocation> instanceMethods = new LinkedHashMap<String, InstanceMethodInvocation>();
    private final Map<String, StaticMethodInvocation> staticMethods = new LinkedHashMap<String, StaticMethodInvocation>();

    public AbstractGriffonDomainHandler() {
        instanceMethods.putAll(getInstanceMethods());
        staticMethods.putAll(getStaticMethods());
    }

    protected abstract Map<String, InstanceMethodInvocation> getInstanceMethods();

    protected abstract Map<String, StaticMethodInvocation> getStaticMethods();

    public final GriffonApplication getApp() {
        return ApplicationHolder.getApplication();
    }

    public final Object invokeInstanceMethod(Object target, String methodName, Object... args) {
        try {
            InstanceMethodInvocation method = instanceMethods.get(methodName);
            if (method == null) {
                throw new IllegalArgumentException("Method " + methodName + " is undefined for domain classes mapped with '" + getMapping() + "'");
            }
            if (target == null) {
                throw new IllegalArgumentException("Cannot call " + methodName + "() on a null instance");
            }
            if (!GriffonDomain.class.isAssignableFrom(target.getClass())) {
                throw new IllegalArgumentException("Cannot call " + methodName + "() on non-domain class [" + target.getClass().getName() + "]");
            }
            return doInvokeInstanceMethod(method, (GriffonDomain) target, methodName, args);
        } catch (UnsupportedDomainMethodException udme) {
            throw (RuntimeException) sanitize(new UnsupportedOperationException("Domain method " + methodName + " is not supported by mapping '" + getMapping() + "'"));
        } catch (RuntimeException e) {
            Throwable t = sanitize(e);
            throw (RuntimeException) t;
        }
    }

    public final Object invokeStaticMethod(Class<GriffonDomain> clazz, String methodName, Object... args) {
        try {
            StaticMethodInvocation method = staticMethods.get(methodName);
            if (method == null) {
                throw new IllegalArgumentException("Method " + methodName + " is undefined for domain classes mapped with '" + getMapping() + "'");
            }
            if (clazz == null) {
                throw new IllegalArgumentException("Cannot call " + methodName + "() on a null class");
            }
            if (!GriffonDomain.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Cannot call " + methodName + "() on non-domain class [" + clazz.getName() + "]");
            }
            return doInvokeStaticMethod(method, clazz, methodName, args);
        } catch (UnsupportedDomainMethodException udme) {
            throw (RuntimeException) sanitize(new UnsupportedOperationException("Domain method " + methodName + " is not supported by mapping '" + getMapping() + "'"));
        } catch (RuntimeException e) {
            Throwable t = sanitize(e);
            throw (RuntimeException) t;
        }
    }

    protected Object doInvokeInstanceMethod(InstanceMethodInvocation method, GriffonDomain target, String methodName, Object... args) {
        return method.invoke(target, methodName, args);
    }

    protected Object doInvokeStaticMethod(StaticMethodInvocation method, Class<GriffonDomain> clazz, String methodName, Object... args) {
        return method.invoke(clazz, methodName, args);
    }

    protected final GriffonDomain.Comparator IDENTITY_COMPARATOR = new GriffonDomain.Comparator(GriffonDomainProperty.IDENTITY);

    protected GriffonDomainProperty identityOf(GriffonDomain target) {
        GriffonDomainClass domainClass = (GriffonDomainClass) target.getGriffonClass();
        return domainClass.getPropertyByName(GriffonDomainProperty.IDENTITY);
    }
}
