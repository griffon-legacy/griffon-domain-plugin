/* Copyright 2004-2005 Graeme Rocher
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


import griffon.plugins.scaffolding.atoms.NumericAtomicValue;
import griffon.plugins.validation.Errors;
import griffon.util.GriffonClassUtils;

/**
 * Implements a maximum value constraint.
 *
 * @author Graeme Rocher (Grails 0.4)
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MaxConstraint extends AbstractConstraint {
    public static final String VALIDATION_DSL_NAME = "max";
    public static final String DEFAULT_INVALID_MAX_MESSAGE_CODE = "default.invalid.max.message";
    public static final String DEFAULT_INVALID_MAX_MESSAGE = "Property [{0}] of class [{1}] with value [{2}] exceeds maximum value [{3}]";

    private Comparable maxValue;

    /**
     * @return Returns the maxValue.
     */
    public Comparable getMaxValue() {
        return maxValue;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
     */
    public boolean supports(Class type) {
        return type != null && (
            Comparable.class.isAssignableFrom(type) ||
                GriffonClassUtils.isAssignableOrConvertibleFrom(Number.class, type) ||
                NumericAtomicValue.class.isAssignableFrom(type));
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
     */
    @Override
    public void setParameter(Object constraintParameter) {
        if (constraintParameter == null) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                VALIDATION_DSL_NAME + "] of property [" +
                constraintPropertyName + "] of class [" + constraintOwningClass + "] cannot be null");
        }

        if (!(constraintParameter instanceof Comparable<?>) && (!constraintParameter.getClass().isPrimitive())) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                VALIDATION_DSL_NAME + "] of property [" + constraintPropertyName +
                "] of class [" + constraintOwningClass + "] must implement the interface [java.lang.Comparable]");
        }

        Class<?> propertyClass = GriffonClassUtils.getPropertyType(constraintOwningClass, constraintPropertyName);
        if (!GriffonClassUtils.isAssignableOrConvertibleFrom(constraintParameter.getClass(), propertyClass)) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                VALIDATION_DSL_NAME + "] of property [" +
                constraintPropertyName + "] of class [" + constraintOwningClass +
                "] must be the same type as property: [" + propertyClass.getName() + "]");
        }

        maxValue = (Comparable) constraintParameter;
        super.setParameter(constraintParameter);
    }

    public String getName() {
        return VALIDATION_DSL_NAME;
    }

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        if (propertyValue instanceof NumericAtomicValue) {
            propertyValue = ((NumericAtomicValue) propertyValue).getValue();
        }

        if (null == propertyValue || maxValue.compareTo(propertyValue) < 0) {
            Object[] args = new Object[]{constraintPropertyName, constraintOwningClass, propertyValue, maxValue};
            rejectValue(target, errors, DEFAULT_INVALID_MAX_MESSAGE_CODE,
                VALIDATION_DSL_NAME + EXCEEDED_SUFFIX, args);
        }
    }
}
