/* Copyright (C) 2011 SpringSource
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
package org.codehaus.griffon.runtime.validation.constraints;

import griffon.core.GriffonClass;
import griffon.exceptions.GriffonException;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainClassProperty;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import griffon.plugins.validation.constraints.ConstraintDef;
import griffon.plugins.validation.constraints.ConstraintUtils;
import griffon.plugins.validation.constraints.ConstraintsEvaluator;
import griffon.util.ApplicationHolder;
import griffon.util.GriffonClassUtils;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.Map.Entry;

/**
 * Default implementation of the {@link griffon.plugins.validation.constraints.ConstraintsEvaluator} interface.
 * <p/>
 *
 * @author Graeme Rocher (Grails 2.0)
 */
public class DefaultConstraintsEvaluator implements ConstraintsEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConstraintsEvaluator.class);

    public DefaultConstraintsEvaluator() {
        // default
    }

    public Map<String, ConstrainedProperty> evaluate(@SuppressWarnings("rawtypes") Class cls) {
        return evaluateConstraints(cls, null);
    }

    public Map<String, ConstrainedProperty> evaluate(GriffonDomainClass cls) {
        return evaluate(cls.getClazz(), cls.getPersistentProperties());
    }

    /**
     * Evaluates the constraints closure to build the list of constraints
     *
     * @param theClass   The domain class to evaluate constraints for
     * @param properties The properties of the instance
     * @return A Map of constraints
     */
    protected Map<String, ConstrainedProperty> evaluateConstraints(
        final Class<?> theClass,
        GriffonDomainClassProperty[] properties) {

        Map<String, Object> defaultConstraints = ConstraintUtils.getDefaultConstraints(null);
        System.out.println(defaultConstraints);

        // boolean javaEntity = theClass.isAnnotationPresent(Domain.class);
        LinkedList<?> classChain = getSuperClassChain(theClass);
        Class<?> clazz;

        ConstrainedPropertyBuilder delegate = new ConstrainedPropertyBuilder(theClass);

        // Evaluate all the constraints closures in the inheritance chain
        for (Object aClassChain : classChain) {
            clazz = (Class<?>) aClassChain;
            Object constraintsProperty = GriffonClassUtils.getStaticPropertyValue(clazz, PROPERTY_NAME);

            if (constraintsProperty instanceof Closure) {
                Closure<?> c = (Closure<?>) constraintsProperty;
                c = (Closure<?>) c.clone();
                c.setResolveStrategy(Closure.DELEGATE_ONLY);
                c.setDelegate(delegate);
                c.call();
            } else if (constraintsProperty instanceof Map) {
                Map<String, List<ConstraintDef>> constraints = (Map<String, List<ConstraintDef>>) constraintsProperty;
                delegate.assemble(constraints);
            } else {
                LOG.debug("User-defined constraints not found on class [" + clazz.getName() + "]");
            }
        }

        Map<String, ConstrainedProperty> constrainedProperties = delegate.getConstrainedProperties();
        if (properties != null && !(constrainedProperties.isEmpty() /*&& javaEntity*/)) {
            for (GriffonDomainClassProperty p : properties) {
                // assume no formula issues if Hibernate isn't available to avoid CNFE
                // if (canPropertyBeConstrained(p)) {
                /*if (p.isDerived()) {
                  if (constrainedProperties.remove(p.getName()) != null) {
                      LOG.warn("Derived properties may not be constrained. Property [" + p.getName() + "] of domain class " + theClass.getName() + " will not be checked during validation.");
                  }
              } else {*/
                final String propertyName = p.getName();
                ConstrainedProperty cp = constrainedProperties.get(propertyName);
                if (cp == null) {
                    cp = new ConstrainedProperty(p.getDomainClass().getClazz(), propertyName, p.getType());
                    cp.setOrder(constrainedProperties.size() + 1);
                    cp.setMessageSource(ApplicationHolder.getApplication());
                    constrainedProperties.put(propertyName, cp);
                }
                // Make sure all fields are required by default, unless
                // specified otherwise by the constraints
                // If the field is a Java entity annotated with @Entity skip this
                applyDefaultConstraints(propertyName, p, cp, defaultConstraints);
                //}
                // }
            }
        }
        if (properties == null || properties.length == 0) {
            // harvest all properties from class, excluding those we may already have
            // marked as constrained or those that cannot be constrained
            for (PropertyDescriptor pd : GriffonClassUtils.getPropertyDescriptors(theClass)) {
                String propertyName = pd.getName();
                if (constrainedProperties.containsKey(propertyName) || !isConstrainableProperty(propertyName))
                    continue;
                ConstrainedProperty cp = new ConstrainedProperty(theClass, propertyName, pd.getPropertyType());
                cp.setOrder(constrainedProperties.size() + 1);
                cp.setMessageSource(ApplicationHolder.getApplication());
                constrainedProperties.put(propertyName, cp);
            }
        }

        Set<Entry<String, ConstrainedProperty>> entrySet = constrainedProperties.entrySet();
        for (Entry<String, ConstrainedProperty> entry : entrySet) {
            ConstrainedProperty constrainedProperty = entry.getValue();
            if (!constrainedProperty
                .hasAppliedConstraint(NullableConstraint.VALIDATION_DSL_NAME)) {
                applyDefaultNullableConstraint(constrainedProperty);
            }
        }

        applySharedConstraints(delegate, constrainedProperties, defaultConstraints);

        return constrainedProperties;
    }

    protected void applySharedConstraints(
        ConstrainedPropertyBuilder constrainedPropertyBuilder,
        Map<String, ConstrainedProperty> constrainedProperties, Map<String, Object> defaultConstraints) {
        for (Map.Entry<String, ConstrainedProperty> entry : constrainedProperties.entrySet()) {
            String propertyName = entry.getKey();
            ConstrainedProperty constrainedProperty = entry.getValue();
            String sharedConstraintReference = constrainedPropertyBuilder.getSharedConstraint(propertyName);
            if (sharedConstraintReference != null && defaultConstraints != null) {
                Object o = defaultConstraints.get(sharedConstraintReference);
                if (o instanceof Map) {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Map<String, Object> constraintsWithinSharedConstraint = (Map) o;
                    for (Map.Entry<String, Object> e : constraintsWithinSharedConstraint.entrySet()) {
                        constrainedProperty.applyConstraint(e.getKey(), e.getValue());
                    }
                } else {
                    throw new GriffonException("Property [" + constrainedProperty.getOwningClass().getName() + '.' + propertyName + "] references shared constraint [" + sharedConstraintReference + ":" + o + "], which doesn't exist!");
                }
            }
        }
    }

    /*
    protected boolean canPropertyBeConstrained(@SuppressWarnings("unused") GriffonDomainClassProperty property) {
        return true;
    }
    */

    public static LinkedList<?> getSuperClassChain(Class<?> theClass) {
        LinkedList<Class<?>> classChain = new LinkedList<Class<?>>();
        Class<?> clazz = theClass;
        while (clazz != Object.class && clazz != null) {
            classChain.addFirst(clazz);
            clazz = clazz.getSuperclass();
        }
        return classChain;
    }

    @SuppressWarnings("unchecked")
    protected void applyDefaultConstraints(String propertyName, GriffonDomainClassProperty p,
                                           ConstrainedProperty cp,/*, @SuppressWarnings("hiding") Map<String, Object> defaultConstraints*/Map<String, Object> defaultConstraints) {

        if (defaultConstraints != null && !defaultConstraints.isEmpty()) {
            if (defaultConstraints.containsKey("*")) {
                final Object o = defaultConstraints.get("*");
                if (o instanceof Map) {
                    Map<String, Object> globalConstraints = (Map<String, Object>) o;
                    applyMapOfConstraints(globalConstraints, propertyName, p, cp);
                }
            }
        }

        if (canApplyNullableConstraint(propertyName, p, cp)) {
            applyDefaultNullableConstraint(p, cp);
        }
    }

    protected void applyDefaultNullableConstraint(@SuppressWarnings("unused") GriffonDomainProperty p,
                                                  ConstrainedProperty cp) {
        applyDefaultNullableConstraint(cp);
    }

    protected void applyDefaultNullableConstraint(ConstrainedProperty cp) {
        boolean isCollection = Collection.class.isAssignableFrom(cp.getPropertyType()) || Map.class.isAssignableFrom(cp.getPropertyType());
        cp.applyConstraint(NullableConstraint.VALIDATION_DSL_NAME, isCollection);
    }


    protected boolean canApplyNullableConstraint(String propertyName, GriffonDomainClassProperty property, ConstrainedProperty constrainedProperty) {
        if (property == null || property.getType() == null) return false;

        final GriffonDomainClass domainClass = property.getDomainClass();
        // only apply default nullable to Groovy entities not legacy Java ones
        if (!GroovyObject.class.isAssignableFrom(domainClass.getClazz()))
            return false;

        final boolean isVersion = GriffonDomainProperty.VERSION.equals(property.getName());
        final boolean isIdentity = GriffonDomainProperty.IDENTITY.equals(property.getName());
        return !constrainedProperty.hasAppliedConstraint(NullableConstraint.VALIDATION_DSL_NAME) &&
            isConstrainableProperty(property, propertyName) && !isIdentity && !isVersion /*&& !property.isDerived()*/;
    }

    protected void applyMapOfConstraints(Map<String, Object> constraints, String propertyName, GriffonDomainClassProperty p, ConstrainedProperty cp) {
        for (Map.Entry<String, Object> entry : constraints.entrySet()) {
            String constraintName = entry.getKey();
            Object constrainingValue = entry.getValue();
            if (!cp.hasAppliedConstraint(constraintName) && cp.supportsContraint(constraintName)) {
                if (NullableConstraint.VALIDATION_DSL_NAME.equals(constraintName)) {
                    if (isConstrainableProperty(p, propertyName)) {
                        cp.applyConstraint(constraintName, constrainingValue);
                    }
                } else {
                    cp.applyConstraint(constraintName, constrainingValue);
                }
            }
        }
    }

    protected boolean isConstrainableProperty(String propertyName) {
        return !propertyName.equals("errors") &&
            !GriffonClass.STANDARD_PROPERTIES.contains(propertyName) &&
            !propertyName.equals(GriffonDomainProperty.DATE_CREATED) &&
            !propertyName.equals(GriffonDomainProperty.LAST_UPDATED) &&
            !GriffonDomainProperty.VERSION.equals(propertyName) &&
            !GriffonDomainProperty.IDENTITY.equals(propertyName);
    }

    protected boolean isConstrainableProperty(GriffonDomainProperty p, String propertyName) {
        return isConstrainableProperty(propertyName) /*&&
                !((p.isOneToOne() || p.isManyToOne()) && p.isCircular())*/;
    }

    public Map<String, ConstrainedProperty> evaluate(Object object, GriffonDomainClassProperty[] properties) {
        return evaluateConstraints(object.getClass(), properties);
    }

    public Map<String, ConstrainedProperty> evaluate(Class<?> cls, GriffonDomainClassProperty[] properties) {
        return evaluateConstraints(cls, properties);
    }
}
