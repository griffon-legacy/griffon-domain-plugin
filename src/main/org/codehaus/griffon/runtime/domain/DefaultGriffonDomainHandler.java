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

package org.codehaus.griffon.runtime.domain;

import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.methods.InstanceMethodInvocation;
import griffon.plugins.domain.methods.StaticMethodInvocation;

import java.util.Map;

import static griffon.util.GriffonExceptionHandler.sanitize;
import static java.util.Collections.emptyMap;

/**
 * @author Andres Almiray
 */
public class DefaultGriffonDomainHandler extends AbstractGriffonDomainHandler {
    @Override
    protected Map<String, InstanceMethodInvocation> getInstanceMethods() {
        return emptyMap();
    }

    @Override
    protected Map<String, StaticMethodInvocation> getStaticMethods() {
        return emptyMap();
    }

    public String getMapping() {
        return "default";
    }

    @Override
    protected Object doInvokeInstanceMethod(InstanceMethodInvocation method, GriffonDomain target, String methodName, Object... args) {
        throw (RuntimeException) sanitize(new UnsupportedOperationException("Domain method " + methodName + " is not supported by mapping '" + getMapping() + "'"));
    }

    @Override
    protected Object doInvokeStaticMethod(StaticMethodInvocation method, Class<GriffonDomain> clazz, String methodName, Object... args) {
        throw (RuntimeException) sanitize(new UnsupportedOperationException("Domain method " + methodName + " is not supported by mapping '" + getMapping() + "'"));
    }
}
