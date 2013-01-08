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
package org.codehaus.griffon.runtime.domain.methods;

import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.exceptions.UnsupportedDomainMethodException;
import griffon.plugins.domain.methods.FindOrCreateWhereMethod;
import griffon.plugins.domain.orm.BinaryExpression;
import griffon.plugins.domain.orm.CompositeCriterion;
import griffon.plugins.domain.orm.Criterion;
import groovy.lang.MissingMethodException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public abstract class AbstractFindOrCreateWherePersistentMethod extends AbstractPersistentStaticMethodInvocation implements FindOrCreateWhereMethod {
    public AbstractFindOrCreateWherePersistentMethod(GriffonDomainHandler griffonDomainHandler) {
        super(griffonDomainHandler);
    }

    @SuppressWarnings("unchedked")
    protected Object invokeInternal(GriffonDomainClass domainClass, String methodName, Object[] arguments) {
        if (arguments.length != 1) {
            throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
        }
        final Object arg = arguments[0];
        if (arg instanceof Map) {
            return findOrCreateByParams(domainClass, (Map) arg);
        }
        throw new MissingMethodException(methodName, domainClass.getClazz(), arguments);
    }

    protected GriffonDomain findOrCreateByParams(GriffonDomainClass domainClass, Map<String, Object> params) {
        throw new UnsupportedDomainMethodException();
    }

    protected Map<String, Object> criterionToMap(Criterion criterion) {
        Map<String, Object> props = new LinkedHashMap<String, Object>();
        harvestProperties(criterion, props);
        return props;
    }

    private void harvestProperties(Criterion criterion, Map<String, Object> props) {
        if (criterion instanceof CompositeCriterion) {
            CompositeCriterion cc = (CompositeCriterion) criterion;
            for (Criterion c : cc.getCriteria()) {
                harvestProperties(c, props);
            }
        } else if (criterion instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) criterion;
            props.put(be.getPropertyName(), be.getValue());
        }
    }
}