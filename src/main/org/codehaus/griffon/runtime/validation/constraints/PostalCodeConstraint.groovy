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

/**
 * Validates a postal code based on a country format.
 * Thanks to http://www.zorched.net/2008/01/25/build-a-custom-validator-in-grails-with-a-plugin/
 *
 * @author Antony Jones (Grails)
 * @author Andres Almiray
 */
class PostalCodeConstraint extends AbstractConstraint {
    public static final String VALIDATION_DSL_NAME = "postalCode"
    public static final String DEFAULT_INVALID_POSTAL_CODE_MESSAGE_CODE = "default.not.postalCode.message"
    public static final String DEFAULT_INVALID_POSTAL_CODE_MESSAGE = "Property [{0}] of class [{1}] with value [{2}] is not a valid [{3}] postal code";

    private US = { postalCode ->
        postalCode ==~ /\d{5}/
    }

    private BR = { postalCode ->
        postalCode ==~ /^[0-9]{5}-[0-9]{3}$/
    }

    private CA = { postalCode ->
        postalCode ==~ /[A-Z]\d[A-Z] \d[A-Z]\d/
    }

    private UK = { postalCode ->
        postalCode ==~ /(GIR 0AA)|(((A[BL]|B[ABDHLNRSTX]?|C[ABFHMORTVW]|D[ADEGHLNTY]|E[HNX]?|F[KY]|G[LUY]?|H[ADGPRSUX]|I[GMPV]|JE|K[ATWY]|L[ADELNSU]?|M[EKL]?|N[EGNPRW]?|O[LX]|P[AEHLOR]|R[GHM]|S[AEGKLMNOPRSTY]?|T[ADFNQRSW]|UB|W[ADFNRSV]|YO|ZE)[1-9]?[0-9]|((E|N|NW|SE|SW|W)1|EC[1-4]|WC[12])[A-HJKMNPR-Y]|(SW|W)([2-9]|[1-9][0-9])|EC[1-9][0-9]) [0-9][ABD-HJLNP-UW-Z]{2})/
    }

    @Override
    public void setParameter(Object constraintParameter) {
        if (constraintParameter instanceof PostalCountry) {
            super.setParameter(constraintParameter)
        } else {
            throw new IllegalArgumentException("""Parameter for constraint [${VALIDATION_DSL_NAME}] of property
                [${constraintPropertyName}] of class [${constraintOwningClass}] must be a value of the PostalCountry
                 class, one of [${PostalCountry.values()}]""")
        }
    }

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        if (!validPostalCode(constraintParameter, propertyValue)) {
            Object[] args = [constraintPropertyName, constraintOwningClass, propertyValue, constraintParameter]
            rejectValue(target, errors, DEFAULT_INVALID_POSTAL_CODE_MESSAGE_CODE,
                NOT_PREFIX + VALIDATION_DSL_NAME, args)
        }
    }

    @Override
    boolean supports(Class type) {
        return type != null && String.class.isAssignableFrom(type)
    }

    @Override
    String getName() {
        return VALIDATION_DSL_NAME
    }

    boolean validPostalCode(country, propertyValue) {
        return this."${country}"(propertyValue)
    }
}