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

import griffon.plugins.validation.Errors;
import griffon.util.GriffonClassUtils;
import groovy.lang.Range;

/**
 * Validates a range.
 *
 * @author Graeme Rocher
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class RangeConstraint extends AbstractConstraint {
    public static final String VALIDATION_DSL_NAME = "range";
    public static final String DEFAULT_INVALID_RANGE_MESSAGE_CODE = "default.invalid.range.message";
    public static final String DEFAULT_INVALID_RANGE_MESSAGE = "Property [{0}] of class [{1}] with value [{2}] does not fall within the valid range from [{3}] to [{4}]";

    Range range;

    /**
     * @return Returns the range.
     */
    public Range getRange() {
        return range;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
     */
    public boolean supports(Class type) {
        return type != null && (Comparable.class.isAssignableFrom(type) ||
            GriffonClassUtils.isAssignableOrConvertibleFrom(Number.class, type)/* ||
                NumericAtomicValue.class.isAssignableFrom(type)*/);
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
     */
    @Override
    public void setParameter(Object constraintParameter) {
        if (!(constraintParameter instanceof Range)) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                VALIDATION_DSL_NAME + "] of property [" +
                constraintPropertyName + "] of class [" +
                constraintOwningClass + "] must be a of type [groovy.lang.Range]");
        }

        range = (Range) constraintParameter;
        super.setParameter(constraintParameter);
    }

    public String getName() {
        return VALIDATION_DSL_NAME;
    }

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        if (range.contains(propertyValue)) {
            return;
        }

        Object[] args = new Object[]{constraintPropertyName, constraintOwningClass,
            propertyValue, range.getFrom(), range.getTo()};

        Comparable from = range.getFrom();
        Comparable to = range.getTo();

        // Upgrade the numbers to Long, so all integer types can be compared.
        if (from instanceof Number) {
            from = ((Number) from).longValue();
        }
        if (to instanceof Number) {
            to = ((Number) to).longValue();
        }
        if (propertyValue instanceof Number) {
            propertyValue = ((Number) propertyValue).longValue();
        }

        if (null == propertyValue || from.compareTo(propertyValue) > 0) {
            rejectValue(target, errors, DEFAULT_INVALID_RANGE_MESSAGE_CODE,
                VALIDATION_DSL_NAME + TOOSMALL_SUFFIX, args);
        } else if (to.compareTo(propertyValue) < 0) {
            rejectValue(target, errors, DEFAULT_INVALID_RANGE_MESSAGE_CODE,
                VALIDATION_DSL_NAME + TOOBIG_SUFFIX, args);
        }
    }
}
