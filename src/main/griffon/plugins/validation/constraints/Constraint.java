/*
 * Copyright 2004-2013 the original author or authors.
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
package griffon.plugins.validation.constraints;

import griffon.core.i18n.MessageSource;
import griffon.plugins.validation.Errors;

/**
 * Defines a validatable constraint.
 *
 * @author Graeme Rocher (Grails)
 */
public interface Constraint {
    String INVALID_SUFFIX = ".invalid";
    String EXCEEDED_SUFFIX = ".exceeded";
    String NOTMET_SUFFIX = ".notmet";
    String NOT_PREFIX = "not.";
    String TOOBIG_SUFFIX = ".toobig";
    String TOOLONG_SUFFIX = ".toolong";
    String TOOSMALL_SUFFIX = ".toosmall";
    String TOOSHORT_SUFFIX = ".tooshort";

    /**
     * Returns whether the constraint supports being applied against the specified type;
     *
     * @param type The type to support
     * @return True if the constraint can be applied against the specified type
     */
    @SuppressWarnings("rawtypes")
    boolean supports(Class type);

    /**
     * Return whether the constraint is valid for the owning class
     *
     * @return True if it is
     */
    boolean isValid();

    /**
     * Validate this constraint against a property value. If implementation is vetoing (isVetoing() method
     * returns true), then it could return 'true' to stop further validation.
     *
     * @param target
     * @param propertyValue The property value to validate
     * @param errors        The errors instance to record errors against
     */
    void validate(Object target, Object propertyValue, Errors errors);

    /**
     * The parameter which the constraint is validated against.
     *
     * @param parameter
     */
    void setParameter(Object parameter);

    Object getParameter();

    /**
     * The class the constraint applies to
     *
     * @param owningClass
     */
    @SuppressWarnings("rawtypes")
    void setOwningClass(Class owningClass);

    /**
     * The name of the property the constraint applies to
     *
     * @param propertyName
     */
    void setPropertyName(String propertyName);

    /**
     * @return The name of the constraint
     */
    String getName();

    /**
     * @return The property name of the constraint
     */
    String getPropertyName();

    /**
     * The message source to evaluate the default messages from
     *
     * @param source
     */
    void setMessageSource(MessageSource source);
}
