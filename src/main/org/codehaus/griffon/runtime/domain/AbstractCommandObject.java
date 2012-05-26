/*
 * Copyright 2012 the original author or authors.
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

import griffon.plugins.domain.CommandObject;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.domain.Value;
import griffon.plugins.validation.ConstraintsValidator;
import griffon.plugins.validation.Errors;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import griffon.util.GriffonClassUtils;
import org.codehaus.griffon.runtime.core.AbstractObservable;
import org.codehaus.griffon.runtime.validation.DefaultErrors;
import org.codehaus.griffon.runtime.validation.constraints.ConstraintsEvaluator;
import org.codehaus.griffon.runtime.validation.constraints.DefaultConstraintsEvaluator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public class AbstractCommandObject extends AbstractObservable implements CommandObject {
    private PropertyChangeSupport pcs;
    private final Errors errors;
    private final Map<String, ConstrainedProperty> constrainedProperties = new LinkedHashMap<String, ConstrainedProperty>();
    private final Map<String, GriffonDomainProperty> domainProperties = new LinkedHashMap<String, GriffonDomainProperty>();

    public AbstractCommandObject() {
        this.errors = new DefaultErrors(getClass());
        this.pcs = new PropertyChangeSupport(this);
        ConstraintsEvaluator constraintsEvaluator = new DefaultConstraintsEvaluator();
        constrainedProperties.putAll(constraintsEvaluator.evaluate(getClass()));

        for (PropertyDescriptor propertyDescriptor : GriffonClassUtils.getPropertyDescriptors(getClass())) {
            if (Value.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                domainProperties.put(propertyDescriptor.getName(), new DefaultGriffonDomainProperty(propertyDescriptor));
            }
        }
    }

    public boolean validate() {
        return ConstraintsValidator.evaluate(this);
    }

    public Errors getErrors() {
        return errors;
    }

    public Map<String, ConstrainedProperty> constrainedProperties() {
        return constrainedProperties;
    }

    public Map<String, GriffonDomainProperty> domainProperties() {
        return domainProperties;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    protected void firePropertyChange(PropertyChangeEvent event) {
        pcs.firePropertyChange(event);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }
}
