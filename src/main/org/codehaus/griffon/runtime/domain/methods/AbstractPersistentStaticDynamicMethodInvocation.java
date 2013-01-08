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

import griffon.core.GriffonClass;
import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.methods.PersistentMethodInvocation;

import java.util.regex.Pattern;

/**
 * @author Andres Almiray
 */
public abstract class AbstractPersistentStaticDynamicMethodInvocation
        extends AbstractStaticDynamicMethodInvocation
        implements PersistentMethodInvocation {
    private final GriffonDomainHandler griffonDomainHandler;

    public AbstractPersistentStaticDynamicMethodInvocation(GriffonDomainHandler griffonDomainHandler, Pattern pattern) {
        super(pattern);
        this.griffonDomainHandler = griffonDomainHandler;
    }

    public GriffonDomainHandler getGriffonDomainHandler() {
        return griffonDomainHandler;
    }

    public GriffonDomainClass getDomainClassFor(Class<GriffonDomain> clazz) {
        GriffonClass griffonClass = griffonDomainHandler.getApp().getArtifactManager().findGriffonClass(clazz);
        if (griffonClass instanceof GriffonDomainClass) return (GriffonDomainClass) griffonClass;
        throw new RuntimeException("Class " + clazz.getName() + " is not a domain class.");
    }

    public final Object invoke(Class<GriffonDomain> clazz, String methodName, Object[] arguments) {
        return invokeInternal(getDomainClassFor(clazz), methodName, arguments);
    }

    protected abstract Object invokeInternal(GriffonDomainClass domainClass, String methodName, Object[] arguments);
}