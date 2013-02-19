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

import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainClassProperty;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import griffon.plugins.validation.constraints.Constraint;
import griffon.plugins.validation.constraints.ConstraintDef;
import griffon.util.ApplicationHolder;
import griffon.util.GriffonClassUtils;
import groovy.lang.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

/**
 * Default implementation of the {@link ConstraintsEvaluator} interface.
 * <p/>
 *
 * @author Graeme Rocher (Grails 2.0)
 */
public class DefaultConstraintsEvaluator implements ConstraintsEvaluator {
    private static final Log LOG = LogFactory.getLog(DefaultConstraintsEvaluator.class);
    // private Map<String, Object> defaultConstraints;

    /*
    public DefaultConstraintsEvaluator(Map<String, Object> defaultConstraints) {
        this.defaultConstraints = defaultConstraints;
    }
    */

    public DefaultConstraintsEvaluator() {
        // default
    }

    /*
    public Map<String, Object> getDefaultConstraints() {
        return defaultConstraints;
    }
    */

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
                    // cp.setOrder(constrainedProperties.size() + 1);
                    cp.setMessageSource(ApplicationHolder.getApplication());
                    constrainedProperties.put(propertyName, cp);
                }
                // Make sure all fields are required by default, unless
                // specified otherwise by the constraints
                // If the field is a Java entity annotated with @Entity skip this
                applyDefaultConstraints(propertyName, p, cp);
                //}
                // }
            }
        }
        if (properties == null || properties.length == 0) {
            final Set<Entry<String, ConstrainedProperty>> entrySet = constrainedProperties
                .entrySet();
            for (Entry<String, ConstrainedProperty> entry : entrySet) {
                final ConstrainedProperty constrainedProperty = entry
                    .getValue();
                if (!constrainedProperty
                    .hasAppliedConstraint(NullableConstraint.VALIDATION_DSL_NAME)) {
                    applyDefaultNullableConstraint(constrainedProperty);
                }
            }
        }

        // applySharedConstraints(delegate, constrainedProperties);

        return constrainedProperties;
    }

    /*
    protected void applySharedConstraints(
            ConstrainedPropertyBuilder constrainedPropertyBuilder,
            Map<String, ConstrainedProperty> constrainedProperties) {
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
                    throw new GriffonConfigurationException("Property [" + constrainedProperty.owningClass.getName() + '.' + propertyName + "] references shared constraint [" + sharedConstraintReference + ":" + o + "], which doesn't exist!");
                }
            }
        }
    }
    */

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

    protected Closure<?> getConstraintsFromScript(Class<?> theClass) {
        // Fallback to xxxxConstraints.groovy script for Java domain classes
        String className = theClass.getName();
        String constraintsScript = className.replaceAll("\\.", "/") + CONSTRAINTS_GROOVY_SCRIPT;
        InputStream stream = getClass().getClassLoader().getResourceAsStream(constraintsScript);

        if (stream != null) {
            GroovyClassLoader gcl = new GroovyClassLoader();
            try {
                Class<?> scriptClass = gcl.parseClass(DefaultGroovyMethods.getText(stream));
                Script script = (Script) scriptClass.newInstance();
                script.run();
                Binding binding = script.getBinding();
                if (binding.getVariables().containsKey(PROPERTY_NAME)) {
                    return (Closure<?>) binding.getVariable(PROPERTY_NAME);
                }
                LOG.warn("Unable to evaluate constraints from [" + constraintsScript + "], constraints closure not found!");
                return null;
            } catch (CompilationFailedException e) {
                LOG.error("Compilation error evaluating constraints for class [" + className + "]: " + e.getMessage(), e);
                return null;
            } catch (InstantiationException e) {
                LOG.error("Instantiation error evaluating constraints for class [" + className + "]: " + e.getMessage(), e);
                return null;
            } catch (IllegalAccessException e) {
                LOG.error("Illegal access error evaluating constraints for class [" + className + "]: " + e.getMessage(), e);
                return null;
            } catch (IOException e) {
                LOG.error("IO error evaluating constraints for class [" + className + "]: " + e.getMessage(), e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void applyDefaultConstraints(String propertyName, GriffonDomainClassProperty p,
                                           ConstrainedProperty cp/*, @SuppressWarnings("hiding") Map<String, Object> defaultConstraints*/) {
        /*
        if (defaultConstraints != null && !defaultConstraints.isEmpty()) {
            if (defaultConstraints.containsKey("*")) {
                final Object o = defaultConstraints.get("*");
                if (o instanceof Map) {
                    Map<String, Object> globalConstraints = (Map<String, Object>) o;
                    applyMapOfConstraints(globalConstraints, propertyName, p, cp);
                }
            }
        }
        */

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

    /*
    protected void applyMapOfConstraints(Map<String, Object> constraints, String propertyName, GriffonDomainClassProperty p, ConstrainedProperty cp) {
        for (Map.Entry<String, Object> entry : constraints.entrySet()) {
            String constraintName = entry.getKey();
            Object constrainingValue = entry.getValue();
            if (!cp.hasAppliedConstraint(constraintName) && cp.supportsContraint(constraintName)) {
                if (ConstrainedProperty.NULLABLE_CONSTRAINT.equals(constraintName)) {
                    if (isConstrainableProperty(p, propertyName)) {
                        cp.applyConstraint(constraintName, constrainingValue);
                    }
                } else {
                    cp.applyConstraint(constraintName, constrainingValue);
                }
            }
        }
    }
    */

    protected boolean isConstrainableProperty(GriffonDomainProperty p, String propertyName) {
        return !propertyName.equals(GriffonDomainProperty.DATE_CREATED) &&
            !propertyName.equals(GriffonDomainProperty.LAST_UPDATED) /*&&
                !((p.isOneToOne() || p.isManyToOne()) && p.isCircular())*/;
    }

    public Map<String, ConstrainedProperty> evaluate(Object object, GriffonDomainClassProperty[] properties) {
        return evaluateConstraints(object.getClass(), properties);
    }

    public Map<String, ConstrainedProperty> evaluate(Class<?> cls, GriffonDomainClassProperty[] properties) {
        return evaluateConstraints(cls, properties);
    }
}
