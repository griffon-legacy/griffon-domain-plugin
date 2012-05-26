/*
 * Copyright 2012 the original author or authors.
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

import griffon.exceptions.GriffonException;
import griffon.plugins.validation.Errors;
import griffon.plugins.validation.FieldObjectError;
import griffon.plugins.validation.MessageCodesResolver;
import griffon.plugins.validation.ObjectError;
import griffon.util.GriffonClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static griffon.util.GriffonExceptionHandler.sanitize;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableList;

/**
 * @author Andres Almiray
 */
public class DefaultErrors implements Errors {
    private final Map<String, List<FieldObjectError>> fieldErrors = synchronizedMap(new LinkedHashMap<String, java.util.List<FieldObjectError>>());
    private final List<ObjectError> objectErrors = new CopyOnWriteArrayList<ObjectError>();
    private MessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();
    private final String objectName;
    private final Class objectClass;

    public DefaultErrors(Class objectClass) {
        this.objectClass = objectClass;
        this.objectName = objectClass.getName();
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean hasErrors() {
        return hasGlobalErrors() || hasFieldErrors();
    }

    public boolean hasGlobalErrors() {
        return !objectErrors.isEmpty();
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    public FieldObjectError getFieldError(String field) {
        List<FieldObjectError> errors = fieldErrors.get(field);
        return null != errors && errors.size() > 0 ? errors.get(0) : null;
    }

    public List<FieldObjectError> getFieldErrors(String field) {
        List<FieldObjectError> errors = fieldErrors.get(field);
        return null != errors ? unmodifiableList(errors) : Collections.<FieldObjectError>emptyList();
    }

    public int getFieldErrorCount(String field) {
        return getFieldErrors(field).size();
    }

    public List<ObjectError> getGlobalErrors() {
        return unmodifiableList(objectErrors);
    }

    public int getGlobalErrorCount() {
        return objectErrors.size();
    }

    public ObjectError getGlobalError() {
        return objectErrors.size() > 0 ? objectErrors.get(0) : null;
    }

    public int getErrorCount() {
        int count = 0;
        synchronized (fieldErrors) {
            for (List<FieldObjectError> errors : fieldErrors.values()) {
                count += errors.size();
            }
        }
        return count + objectErrors.size();
    }

    public List<ObjectError> getAllErrors() {
        List<ObjectError> tmp = new ArrayList<ObjectError>();
        tmp.addAll(objectErrors);
        synchronized (fieldErrors) {
            for (List<FieldObjectError> errors : fieldErrors.values()) {
                tmp.addAll(errors);
            }
        }
        return tmp;
    }

    public void addError(ObjectError objectError) {
        if (objectError instanceof FieldObjectError) {
            FieldObjectError fieldError = (FieldObjectError) objectError;
            List<FieldObjectError> errors = fieldErrors.get(fieldError.getFieldName());
            if (null == errors) {
                errors = new ArrayList<FieldObjectError>();
                fieldErrors.put(fieldError.getFieldName(), errors);
            }
            if (!errors.contains(fieldError)) {
                errors.add(fieldError);
            }
        }
    }

    public void clearAllErrors() {
        clearGlobalErrors();
        clearFieldErrors();
    }

    public void clearGlobalErrors() {
        objectErrors.clear();
    }

    public void clearFieldErrors() {
        fieldErrors.clear();
    }

    public void clearFieldErrors(String field) {
        fieldErrors.remove(field);
    }

    public MessageCodesResolver getMessageCodesResolver() {
        return messageCodesResolver;
    }

    public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
        this.messageCodesResolver = messageCodesResolver;
    }

    public String[] resolveMessageCodes(String code) {
        return messageCodesResolver.resolveMessageCodes(code, objectName);
    }

    public String[] resolveMessageCodes(String code, String field, Class fieldType) {
        return messageCodesResolver.resolveMessageCodes(code, objectName, field, fieldType);
    }

    public void reject(String code, Object[] args, String defaultMessage) {
        ObjectError objectError = new DefaultObjectError(resolveMessageCodes(code), args, defaultMessage);
        if (!objectErrors.contains(objectError)) objectErrors.add(objectError);
    }

    public void reject(String code, String defaultMessage) {
        reject(code, ObjectError.NO_ARGS, defaultMessage);
    }

    public void rejectField(String field, String code, Object[] args, String defaultMessage) {
        rejectField(field, null, code, ObjectError.NO_ARGS, defaultMessage);
    }

    public void rejectField(String field, String code, String defaultMessage) {
        rejectField(field, null, code, ObjectError.NO_ARGS, defaultMessage);
    }

    public void rejectField(String field, Object rejectedValue, String code, Object[] args, String defaultMessage) {
        Class fieldType = getFieldType(field);
        FieldObjectError fieldError = new DefaultFieldObjectError(field, rejectedValue, resolveMessageCodes(code, field, fieldType), args, defaultMessage);
        List<FieldObjectError> errors = fieldErrors.get(field);
        if (null == errors) {
            errors = new ArrayList<FieldObjectError>();
            fieldErrors.put(field, errors);
        }
        if (!errors.contains(fieldError)) errors.add(fieldError);
    }

    public void rejectField(String field, Object rejectedValue, String code, String defaultMessage) {
        rejectField(field, rejectedValue, code, null, defaultMessage);
    }

    private Class getFieldType(String field) {
        try {
            return GriffonClassUtils.getPropertyDescriptor(objectClass, field).getPropertyType();
        } catch (IllegalAccessException e) {
            sanitize(e);
            throw new GriffonException(e);
        } catch (InvocationTargetException e) {
            sanitize(e);
            throw new GriffonException(e);
        } catch (NoSuchMethodException e) {
            sanitize(e);
            throw new GriffonException(e);
        }
    }
}
