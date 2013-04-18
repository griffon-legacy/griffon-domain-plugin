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

import java.util.List;

/**
 * A constraint that validates the property is contained within the supplied list.
 *
 * @author Graeme Rocher (Grails 0.4)
 */
public class InListConstraint extends AbstractConstraint {
    public static final String VALIDATION_DSL_NAME = "inList";
    public static final String DEFAULT_NOT_INLIST_MESSAGE_CODE = "default.not.inlist.message";
    public static final String DEFAULT_NOT_IN_LIST_MESSAGE = "Property [{0}] of class [{1}] with value [{2}] is not contained within the list [{3}]";

    List<?> list;

    /**
     * @return Returns the list.
     */
    public List<?> getList() {
        return list;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.Constraint#supports(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    public boolean supports(Class type) {
        return type != null;
    }

    /* (non-Javadoc)
     * @see org.codehaus.groovy.grails.validation.ConstrainedProperty.AbstractConstraint#setParameter(java.lang.Object)
     */
    @Override
    public void setParameter(Object constraintParameter) {
        if (!(constraintParameter instanceof List<?>)) {
            throw new IllegalArgumentException("Parameter for constraint [" +
                VALIDATION_DSL_NAME + "] of property [" +
                constraintPropertyName + "] of class [" + constraintOwningClass +
                "] must implement the interface [java.util.List]");
        }

        list = (List<?>) constraintParameter;
        super.setParameter(constraintParameter);
    }

    public String getName() {
        return VALIDATION_DSL_NAME;
    }

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        // Check that the list contains the given value. If not, add an error.
        if (!list.contains(propertyValue)) {
            Object[] args = new Object[]{constraintPropertyName, constraintOwningClass, propertyValue, list};
            rejectValue(target, errors, DEFAULT_NOT_INLIST_MESSAGE_CODE,
                NOT_PREFIX + VALIDATION_DSL_NAME, args);
        }
    }
}