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
class IPAddressConstraint extends AbstractConstraint {
    public static final String VALIDATION_DSL_NAME = 'ipAddress'
    public static final String DEFAULT_INVALID_IP_ADDRESS_MESSAGE_CODE = 'default.not.ipAddress.message'
    public static final String DEFAULT_INVALID_IP_ADDRESS_MESSAGE = "Property [{0}] of class [{1}] with value [{2}] is not a valid IP address"

    @Override
    protected void processValidate(Object target, Object propertyValue, Errors errors) {
        if (!validInternetProtocolAddress(constraintParameter, propertyValue)) {
            Object[] args = [constraintPropertyName, constraintOwningClass, propertyValue]
            rejectValue(target, errors, DEFAULT_INVALID_IP_ADDRESS_MESSAGE_CODE,
                NOT_PREFIX + VALIDATION_DSL_NAME, args)
        }
    }

    private boolean validInternetProtocolAddress(allowInternal, propertyValue) {
        try {
            Integer[] octets = splitOctets(propertyValue as String)
            boolean validatesNumerically = (octets.findAll { it > 255 }.isEmpty() && octets[0] > 0)
            boolean inAllowedRange = allowInternal ? true : !isInternal(octets)
            return validatesNumerically && inAllowedRange
        } catch (NumberFormatException nfe) {
            return false
        }
    }

    private boolean isInternal(Integer[] octets) {
        switch (octets[0]) {
            case 10:
                return true
            case 192:
                return octets[1] == 168
            case 172:
                return (16..32).containsWithinBounds(octets[2])
            default:
                return false
        }
    }

    private Integer[] splitOctets(String address) {
        return address.split("\\.").collect { Integer.parseInt(it) }
    }

    boolean supports(Class type) {
        return type == String.class
    }

    String getName() {
        return VALIDATION_DSL_NAME
    }
}
