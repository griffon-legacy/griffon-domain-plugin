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

import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.exceptions.UnsupportedDomainMethodException;
import griffon.plugins.domain.methods.FindOrCreateByMethod;
import griffon.plugins.domain.orm.BinaryExpression;
import griffon.plugins.domain.orm.CompositeCriterion;
import griffon.plugins.domain.orm.Criterion;
import griffon.plugins.domain.orm.Restrictions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Andres Almiray
 */
public abstract class AbstractFindOrCreateByPersistentMethod extends AbstractClausedStaticPersistentMethod implements FindOrCreateByMethod {
    private static final String METHOD_PATTERN = "(" + METHOD_NAME + ")([A-Z]\\w*)";

    public AbstractFindOrCreateByPersistentMethod(GriffonDomainHandler griffonDomainHandler) {
        super(griffonDomainHandler, Pattern.compile(METHOD_PATTERN), OPERATORS);
    }

    @Override
    protected boolean isStrict() {
        return true;
    }

    @Override
    protected Object doInvokeInternalWithExpressions(GriffonDomainClass domainClass, String methodName, Object[] arguments, List<GriffonMethodExpression> expressions, String operatorInUse) {
        if (expressions.size() == 1) {
            return findOrCreateBy(domainClass, methodName, expressions.get(0).getCriterion());
        } else {
            List<Criterion> criteria = new ArrayList<Criterion>();
            for (GriffonMethodExpression expr : expressions) {
                criteria.add(expr.getCriterion());
            }
            Criterion[] array = criteria.toArray(new Criterion[criteria.size()]);
            Criterion criterion = Restrictions.and(array);
            return findOrCreateBy(domainClass, methodName, criterion);
        }
    }

    protected Object findOrCreateBy(GriffonDomainClass domainClass, String methodName, Criterion criterion) {
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