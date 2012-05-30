/*
 * Copyright 2010-2012 the original author or authors.
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
package org.codehaus.griffon.runtime.domain.methods;

import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.exceptions.UnsupportedDomainMethodException;
import griffon.plugins.domain.methods.SaveMethod;
import griffon.plugins.validation.exceptions.ValidationException;
import groovy.lang.MissingMethodException;

import java.util.LinkedHashMap;
import java.util.Map;

import static griffon.util.ApplicationHolder.getApplication;
import static griffon.util.ConfigUtils.getConfigValueAsBoolean;

/**
 * @author Andres Almiray
 */
public abstract class AbstractSavePersistentMethod extends AbstractPersistentInstanceMethodInvocation implements SaveMethod {
    public static final String FAIL_ON_ERROR = "failOnError";
    public static final String VALIDATE = "validate";
    private static final String FAIL_ON_ERROR_CONFIG_KEY = "griffon.domain.failOnError";

    public AbstractSavePersistentMethod(GriffonDomainHandler griffonDomainHandler) {
        super(griffonDomainHandler);
    }

    protected final Object invokeInternal(GriffonDomainClass domainClass, GriffonDomain target, String methodName, Object[] arguments) {
        if (target == null) {
            throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
        }

        Map<String, Object> params = validate(domainClass, methodName, target, arguments);
        if (params == null) return null;

        GriffonDomain entity = null;
        if (shouldInsert(domainClass, target, arguments)) {
            target.beforeInsert();
            entity = insert(domainClass, target, arguments, params);
            target.onSave();
            target.afterInsert();
        } else {
            target.beforeUpdate();
            entity = save(domainClass, target, arguments, params);
            target.onSave();
            target.afterUpdate();
        }

        return entity;
    }

    protected Map<String, Object> validate(GriffonDomainClass domainClass, String methodName, GriffonDomain target, Object[] arguments) {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        if (arguments.length == 1) {
            if (arguments[0] instanceof Map) {
                params.putAll((Map) arguments[0]);
            } else {
                throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
            }
        } else if (arguments.length > 0) {
            throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
        }


        boolean validate = getConfigValueAsBoolean(params, VALIDATE, true);
        boolean failOnError = getConfigValueAsBoolean(getApplication().getConfig(), FAIL_ON_ERROR_CONFIG_KEY, false);
        if (params.containsKey(FAIL_ON_ERROR)) failOnError = getConfigValueAsBoolean(params, FAIL_ON_ERROR);
        params.put(VALIDATE, validate);
        params.put(FAIL_ON_ERROR, failOnError);

        if (validate) {
            target.getErrors().clearAllErrors();
            if (!target.validate()) {
                if (failOnError) {
                    throw new ValidationException("An instance of " + target.getClass() + " failed validation");
                } else {
                    return null;
                }
            }
        }
        return params;
    }

    protected abstract boolean shouldInsert(GriffonDomainClass domainClass, GriffonDomain target, Object[] arguments);

    protected GriffonDomain insert(GriffonDomainClass domainClass, GriffonDomain target, Object[] arguments, Map<String, Object> params) {
        throw new UnsupportedDomainMethodException();
    }

    protected GriffonDomain save(GriffonDomainClass domainClass, GriffonDomain target, Object[] arguments, Map<String, Object> params) {
        throw new UnsupportedDomainMethodException();
    }
}