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
import griffon.plugins.domain.*;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import griffon.transform.Domain;
import org.codehaus.griffon.runtime.core.AbstractGriffonClass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of the {@code GriffonDomainClass} interface
 *
 * @author Andres Almiray
 */
public abstract class AbstractGriffonDomainClass extends AbstractGriffonClass implements GriffonDomainClass {
    protected Map<String, GriffonDomainClassProperty> domainProperties = new LinkedHashMap<String, GriffonDomainClassProperty>();
    protected Map<String, ConstrainedProperty> constrainedProperties = new LinkedHashMap<String, ConstrainedProperty>();
    protected GriffonDomainHandler domainHandler;

    public AbstractGriffonDomainClass(GriffonApplication app, Class<?> clazz) {
        super(app, clazz, TYPE, TRAILING);
        domainHandler = resolveDomainHandler(clazz);
        initialize();
    }

    private static GriffonDomainHandler resolveDomainHandler(Class<?> clazz) {
        String implementation = null;
        final Domain domain = clazz.getAnnotation(Domain.class);
        if (domain != null) {
            implementation = domain.value();
        }
        return GriffonDomainHandlerRegistry.domainHandlerFor(implementation);
    }

    protected abstract void initialize();

    public GriffonDomainClassProperty[] getProperties() {
        return domainProperties.values().toArray(new GriffonDomainClassProperty[domainProperties.size()]);
    }

    public GriffonDomainClassProperty[] getPersistentProperties() {
        List<GriffonDomainProperty> persistent = new ArrayList<GriffonDomainProperty>();
        for (GriffonDomainClassProperty property : domainProperties.values()) {
            if (property.isPersistent()) {
                persistent.add(property);
            }
        }
        return persistent.toArray(new GriffonDomainClassProperty[persistent.size()]);
    }

    public GriffonDomainClassProperty getPropertyByName(String name) {
        return domainProperties.get(name);
    }

    public GriffonDomainProperty getIdentity() {
        return getPropertyByName(GriffonDomainProperty.IDENTITY);
    }

    public GriffonDomainHandler getDomainHandler() {
        return domainHandler;
    }

    public Map<String, ConstrainedProperty> getConstrainedProperties() {
        return constrainedProperties;
    }
}
