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

package org.codehaus.griffon.runtime.validation.constraints

import griffon.plugins.validation.Errors
// import groovy.transform.CompileStatic

/**
 * Validates an IP Address.
 *
 * @author Antony Jones (Grails)
 * @author Andres Almiray
 */
// @CompileStatic
class ConfirmedPasswordConstraint extends AbstractConstraint {
    public static final String VALIDATION_DSL_NAME = 'confirmedPassword'
    public static final String DEFAULT_INVALID_PASSWORD_CONFIRMATION_MESSAGE_CODE = 'default.not.confirmedPassword.message'
    public static final String DEFAULT_INVALID_PASSWORD_CONFIRMATION_MESSAGE = "Property [{0}] of class [{1}] does not match value of property [{2}]"

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        String confirmationValue = target."${constraintParameter}"

        if (!validatePasswordConfirmation(target, confirmationValue, propertyValue)) {
            Object[] args = [constraintPropertyName, constraintOwningClass, constraintParameter]
            rejectValue(target, errors, DEFAULT_INVALID_PASSWORD_CONFIRMATION_MESSAGE_CODE,
                NOT_PREFIX + VALIDATION_DSL_NAME, args)
        }
    }

    @Override
    public void setParameter(Object constraintParameter) {
        if (constraintParameter instanceof String && constraintOwningClass.hasProperty((String) constraintParameter)) {
            super.setParameter(constraintParameter)
        } else {
            throw new IllegalArgumentException("""Parameter for constraint [${VALIDATION_DSL_NAME}] of property
                [${constraintParameter}] must be the name of a field on class [${constraintOwningClass}]""")
        }
    }

    boolean validatePasswordConfirmation(target, confirmationValue, propertyValue) {
        return confirmationValue == propertyValue
    }

    @Override
    boolean supports(Class type) {
        return type != null && String.class.isAssignableFrom(type)
    }

    @Override
    String getName() {
        return VALIDATION_DSL_NAME
    }
}
