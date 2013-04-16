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
package org.codehaus.griffon.runtime.validation;

import griffon.plugins.validation.FieldObjectError;

import static griffon.util.GriffonNameUtils.isBlank;
import static java.util.Arrays.asList;

/**
 * @author Andres Almiray
 */
public class DefaultFieldObjectError extends DefaultObjectError implements FieldObjectError {
    private final String fieldName;
    private Object rejectedValue;

    public DefaultFieldObjectError(String fieldName, String code) {
        this(fieldName, null, code, NO_ARGS, code);
    }

    public DefaultFieldObjectError(String fieldName, String code, Object[] arguments) {
        this(fieldName, null, code, arguments, code);
    }

    public DefaultFieldObjectError(String fieldName, String code, Object[] arguments, String defaultMessage) {
        this(fieldName, null, code, arguments, defaultMessage);
    }

    public DefaultFieldObjectError(String fieldName, String code, String defaultMessage) {
        this(fieldName, null, code, NO_ARGS, defaultMessage);
    }

    public DefaultFieldObjectError(String fieldName, Object rejectedValue, String code) {
        this(fieldName, rejectedValue, code, NO_ARGS, code);
    }

    public DefaultFieldObjectError(String fieldName, Object rejectedValue, String code, Object[] arguments) {
        this(fieldName, rejectedValue, code, arguments, code);
    }

    public DefaultFieldObjectError(String fieldName, Object rejectedValue, String code, String defaultMessage) {
        this(fieldName, rejectedValue, code, NO_ARGS, defaultMessage);
    }

    public DefaultFieldObjectError(String fieldName, Object rejectedValue, String code, Object[] arguments, String defaultMessage) {
        super(code, arguments, defaultMessage);
        if (isBlank(fieldName)) {
            throw new IllegalArgumentException("Invalid value for fieldName: empty or null");
        }
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    public DefaultFieldObjectError(String fieldName, String[] codes) {
        this(fieldName, null, codes, NO_ARGS, null);
    }

    public DefaultFieldObjectError(String fieldName, String[] codes, Object[] arguments) {
        this(fieldName, null, codes, arguments, null);
    }

    public DefaultFieldObjectError(String fieldName, String[] codes, String defaultMessage) {
        this(fieldName, null, codes, NO_ARGS, defaultMessage);
    }

    public DefaultFieldObjectError(String fieldName, String[] codes, Object[] arguments, String defaultMessage) {
        this(fieldName, null, codes, arguments, defaultMessage);
    }

    public DefaultFieldObjectError(String fieldName, Object rejectedValue, String[] codes) {
        this(fieldName, rejectedValue, codes, NO_ARGS, null);
    }

    public DefaultFieldObjectError(String fieldName, Object rejectedValue, String[] codes, Object[] arguments) {
        this(fieldName, rejectedValue, codes, arguments, null);
    }

    public DefaultFieldObjectError(String fieldName, Object rejectedValue, String[] codes, String defaultMessage) {
        this(fieldName, rejectedValue, codes, NO_ARGS, defaultMessage);
    }

    public DefaultFieldObjectError(String fieldName, Object rejectedValue, String[] codes, Object[] arguments, String defaultMessage) {
        super(codes, arguments, defaultMessage);
        if (isBlank(fieldName)) {
            throw new IllegalArgumentException("Invalid value for fieldName: empty or null");
        }
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }


    public String getFieldName() {
        return fieldName;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldObjectError)) return false;
        if (!super.equals(o)) return false;

        DefaultFieldObjectError that = (DefaultFieldObjectError) o;

        if (!fieldName.equals(that.fieldName)) return false;
        if (rejectedValue != null ? !rejectedValue.equals(that.rejectedValue) : that.rejectedValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + fieldName.hashCode();
        result = 31 * result + (rejectedValue != null ? rejectedValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FieldObjectError{" +
            "fieldName='" + fieldName + '\'' +
            ", rejectedValue=" + rejectedValue +
            ", codes=" + asList(getCodes()) +
            ", arguments=" + asList(getArguments()) +
            ", defaultMessage='" + getDefaultMessage() + '\'' +
            '}';
    }
}
