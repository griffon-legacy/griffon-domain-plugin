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

package org.codehaus.griffon.runtime.domain;

import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.domain.methods.InstanceMethodInvocation;
import griffon.plugins.domain.methods.MethodSignature;
import griffon.plugins.domain.methods.StaticMethodInvocation;
import griffon.plugins.domain.orm.Criterion;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import griffon.plugins.validation.exceptions.ValidationException;
import griffon.util.CollectionUtils;
import org.codehaus.griffon.runtime.domain.methods.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;

/**
 * @author Andres Almiray
 */
public class MemoryGriffonDomainHandler extends AbstractGriffonDomainHandler {
    private static final String MAPPING = "memory";
    private static final ConcurrentHashMapDatastore DEFAULT_DATASTORE = new ConcurrentHashMapDatastore("default");
    private static final Map<String, ConcurrentHashMapDatastore> DATASTORES = new ConcurrentHashMap<String, ConcurrentHashMapDatastore>();
    public static final MethodSignature[] METHOD_SIGNATURES;

    private static final MemoryGriffonDomainHandler INSTANCE = new MemoryGriffonDomainHandler();

    public static MemoryGriffonDomainHandler getInstance() {
        return INSTANCE;
    }

    private static MethodSignature[] harvestMethodSignatures() {
        Collection<MethodSignature> signatures = new ArrayList<MethodSignature>();
        signatures.addAll(asList(SaveMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(DeleteMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(MakeMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(FetchMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(ExistsMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(FetchAllMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(CountMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(CountByMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(ListMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(FindMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(FindWhereMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(FindByMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(FindAllMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(FindAllByMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(FindAllWhereMethod.METHOD_SIGNATURES));
        signatures.addAll(asList(WithCriteriaMethod.METHOD_SIGNATURES));
        return signatures.toArray(new MethodSignature[signatures.size()]);
    }

    static {
        METHOD_SIGNATURES = harvestMethodSignatures();
    }

    protected MethodSignature[] getMethodSignaturesInternal() {
        return harvestMethodSignatures();
    }

    protected Map<? extends String, ? extends InstanceMethodInvocation> getInstanceMethods() {
        Map<String, InstanceMethodInvocation> instanceMethods = new LinkedHashMap<String, InstanceMethodInvocation>();
        instanceMethods.put(SaveMethod.METHOD_NAME, new SaveMethod(this));
        instanceMethods.put(DeleteMethod.METHOD_NAME, new DeleteMethod(this));
        return instanceMethods;
    }

    protected Map<? extends String, ? extends StaticMethodInvocation> getStaticMethods() {
        Map<String, StaticMethodInvocation> staticMethods = new LinkedHashMap<String, StaticMethodInvocation>();
        staticMethods.put(MakeMethod.METHOD_NAME, new MakeMethod(this));
        staticMethods.put(ListMethod.METHOD_NAME, new ListMethod(this));
        staticMethods.put(FetchMethod.METHOD_NAME, new FetchMethod(this));
        staticMethods.put(ExistsMethod.METHOD_NAME, new ExistsMethod(this));
        staticMethods.put(FetchAllMethod.METHOD_NAME, new FetchAllMethod(this));
        staticMethods.put(CountMethod.METHOD_NAME, new CountMethod(this));
        staticMethods.put(CountByMethod.METHOD_NAME, new CountByMethod(this));
        staticMethods.put(FindMethod.METHOD_NAME, new FindMethod(this));
        staticMethods.put(FindWhereMethod.METHOD_NAME, new FindWhereMethod(this));
        staticMethods.put(FindByMethod.METHOD_NAME, new FindByMethod(this));
        staticMethods.put(FindAllMethod.METHOD_NAME, new FindAllMethod(this));
        staticMethods.put(FindAllByMethod.METHOD_NAME, new FindAllByMethod(this));
        staticMethods.put(FindAllWhereMethod.METHOD_NAME, new FindAllWhereMethod(this));
        staticMethods.put(WithCriteriaMethod.METHOD_NAME, new WithCriteriaMethod(this));
        return staticMethods;
    }

    public String getMapping() {
        return MAPPING;
    }

    public ConcurrentHashMapDatastore getDatastore() {
        return DEFAULT_DATASTORE;
    }

    public ConcurrentHashMapDatastore getDatastore(String name) {
        return DATASTORES.get(name);
    }

    private <T extends GriffonDomain> ConcurrentHashMapDatastore.Dataset<T> datasetOf(GriffonDomainClass domainClass) {
        if (domainClass == null) {
            throw new IllegalArgumentException("DomainClass is null!");
        }
        return DEFAULT_DATASTORE.dataset(domainClass);
    }

    private class SaveMethod extends AbstractSavePersistentMethod {
        private final AtomicInteger identitySequence = new AtomicInteger(0);

        public SaveMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected boolean shouldInsert(GriffonDomainClass domainClass, GriffonDomain target, Object[] arguments) {
            GriffonDomainProperty identity = identityOf(target);
            Object identityValue = identity.getValue(target);
            return identityValue == null;
        }

        @Override
        protected GriffonDomain insert(GriffonDomainClass domainClass, GriffonDomain target, Object[] arguments, Map<String, Object> params) {
            final ConcurrentHashMapDatastore.Dataset<GriffonDomain> dataset = datasetOf(domainClass);
            if (null != checkUniqueConstraints(domainClass, target, params, dataset)) {
                GriffonDomainProperty identity = identityOf(target);
                identity.setValue(target, identitySequence.incrementAndGet());
                return dataset.save(target);
            }
            return null;
        }

        @Override
        protected GriffonDomain save(GriffonDomainClass domainClass, GriffonDomain target, Object[] arguments, Map<String, Object> params) {
            final ConcurrentHashMapDatastore.Dataset<GriffonDomain> dataset = datasetOf(domainClass);
            if (null != checkUniqueConstraints(domainClass, target, params, dataset)) {
                return dataset.save(target);
            }
            return null;
        }

        private GriffonDomain checkUniqueConstraints(GriffonDomainClass domainClass, GriffonDomain target, Map<String, Object> params, ConcurrentHashMapDatastore.Dataset<GriffonDomain> dataset) {
            boolean validate = (Boolean) params.get(VALIDATE);
            boolean failOnError = (Boolean) params.get(FAIL_ON_ERROR);

            if (validate) {
                final Map<String, ConstrainedProperty> constrainedProperties = domainClass.getConstrainedProperties();
                for (Map.Entry<String, ConstrainedProperty> entry : constrainedProperties.entrySet()) {
                    String propertyName = entry.getKey();
                    GriffonDomainProperty domainProperty = domainClass.getPropertyByName(propertyName);
                    ConstrainedProperty constrainedProperty = entry.getValue();
                    Object uniqueValue = constrainedProperty.getMetaConstraintValue("unique");
                    if (null != uniqueValue && (Boolean) uniqueValue) {
                        final Object value = domainProperty.getValue(target);
                        final Map<String, Object> args = CollectionUtils.<String, Object>map().e(propertyName, value);
                        GriffonDomain other = dataset.first(args);
                        if (null != other && target != other) {
                            if (failOnError) {
                                throw new ValidationException("Constraint 'unique' failed validation for property '" + propertyName + "' with value " + value);
                            } else {
                                target.getErrors().rejectField(propertyName, value, "unique", null);
                            }
                        }
                    }
                }
                if (target.getErrors().hasErrors()) return null;
            }

            return target;
        }
    }

    private class DeleteMethod extends AbstractDeletePersistentMethod {
        public DeleteMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected GriffonDomain delete(GriffonDomainClass domainClass, GriffonDomain target) {
            return datasetOf(domainClass).remove(target);
        }
    }

    private class MakeMethod extends AbstractMakePersistentMethod {
        public MakeMethod(GriffonDomainHandler domainHandler) {
            super(domainHandler);
        }
    }

    private class FetchMethod extends AbstractFetchPersistentMethod {
        public FetchMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected GriffonDomain fetch(GriffonDomainClass domainClass, Object key) {
            return datasetOf(domainClass).fetch(key);
        }
    }

    private class ExistsMethod extends AbstractExistsPersistentMethod {
        public ExistsMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected boolean exists(GriffonDomainClass domainClass, Object key) {
            return datasetOf(domainClass).fetch(key) != null;
        }
    }

    private class FetchAllMethod extends AbstractFetchAllPersistentMethod {
        public FetchAllMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected Collection<GriffonDomain> fetchAll(GriffonDomainClass domainClass) {
            return datasetOf(domainClass).list();
        }

        @Override
        protected Collection<GriffonDomain> fetchAllByIdentities(GriffonDomainClass domainClass, List<Object> identities) {
            List<GriffonDomain> entities = new ArrayList<GriffonDomain>();
            if (identities != null && identities.size() > 0) {
                ConcurrentHashMapDatastore.Dataset dataset = datasetOf(domainClass);
                for (Object identity : identities) {
                    GriffonDomain entity = dataset.fetch(identity);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }
            }
            Collections.sort(entities, IDENTITY_COMPARATOR);
            return entities;
        }

        @Override
        protected Collection<GriffonDomain> fetchAllByIdentities(GriffonDomainClass domainClass, Object[] identities) {
            List<GriffonDomain> entities = new ArrayList<GriffonDomain>();
            if (identities != null && identities.length > 0) {
                ConcurrentHashMapDatastore.Dataset dataset = datasetOf(domainClass);
                for (Object identity : identities) {
                    GriffonDomain entity = dataset.fetch(identity);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }
            }
            Collections.sort(entities, IDENTITY_COMPARATOR);
            return entities;
        }
    }

    private class ListMethod extends AbstractListPersistentMethod {
        public ListMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected Collection<GriffonDomain> list(GriffonDomainClass domainClass) {
            return list(domainClass, Collections.<String, Object>emptyMap());
        }

        @Override
        protected Collection<GriffonDomain> list(GriffonDomainClass domainClass, Map<String, Object> options) {
            ConcurrentHashMapDatastore.Dataset dataset = datasetOf(domainClass);
            if (dataset == null) {
                return Collections.emptyList();
            } else {
                return dataset.list(options);
            }
        }
    }

    private class CountMethod extends AbstractCountPersistentMethod {
        public CountMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected int count(GriffonDomainClass domainClass) {
            return datasetOf(domainClass).size();
        }
    }

    private class FindAllWhereMethod extends AbstractFindAllWherePersistentMethod {
        public FindAllWhereMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected Collection<GriffonDomain> findByParams(GriffonDomainClass domainClass, Map<String, Object> params) {
            List<GriffonDomain> entities = datasetOf(domainClass).query(params);
            Collections.sort(entities, IDENTITY_COMPARATOR);
            return entities;
        }
    }

    private class FindWhereMethod extends AbstractFindWherePersistentMethod {
        public FindWhereMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected GriffonDomain findByParams(GriffonDomainClass domainClass, Map<String, Object> params) {
            return datasetOf(domainClass).first(params);
        }
    }

    private class FindMethod extends AbstractFindPersistentMethod {
        public FindMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected GriffonDomain findByProperties(GriffonDomainClass domainClass, Map<String, Object> properties) {
            return datasetOf(domainClass).first(properties);
        }

        @Override
        protected GriffonDomain findByExample(GriffonDomainClass domainClass, Object example) {
            return datasetOf(domainClass).first(example);
        }

        @Override
        protected GriffonDomain findByCriterion(GriffonDomainClass domainClass, Criterion criterion, Map<String, Object> options) {
            return datasetOf(domainClass).first(criterion);
        }
    }

    private class FindAllMethod extends AbstractFindAllPersistentMethod {
        public FindAllMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected Collection<GriffonDomain> findAll(GriffonDomainClass domainClass) {
            List<GriffonDomain> entities = datasetOf(domainClass).list();
            Collections.sort(entities, IDENTITY_COMPARATOR);
            return entities;
        }

        @Override
        protected Collection<GriffonDomain> findByProperties(GriffonDomainClass domainClass, Map<String, Object> properties) {
            List<GriffonDomain> entities = datasetOf(domainClass).query(properties);
            Collections.sort(entities, IDENTITY_COMPARATOR);
            return entities;
        }

        @Override
        protected Collection<GriffonDomain> findByExample(GriffonDomainClass domainClass, Object example) {
            List<GriffonDomain> entities = datasetOf(domainClass).query(example);
            Collections.sort(entities, IDENTITY_COMPARATOR);
            return entities;
        }

        @Override
        protected Collection<GriffonDomain> findByCriterion(GriffonDomainClass domainClass, Criterion criterion, Map<String, Object> options) {
            return datasetOf(domainClass).query(criterion, options);
        }
    }

    private class WithCriteriaMethod extends AbstractWithCriteriaPersistentMethod {
        public WithCriteriaMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected Collection<GriffonDomain> withCriteria(GriffonDomainClass domainClass, Criterion criterion) {
            List<GriffonDomain> entities = datasetOf(domainClass).query(criterion);
            Collections.sort(entities, IDENTITY_COMPARATOR);
            return entities;
        }
    }

    private class FindByMethod extends AbstractFindByPersistentMethod {
        public FindByMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected Object findBy(GriffonDomainClass domainClass, String methodName, Criterion criterion) {
            return datasetOf(domainClass).first(criterion);
        }
    }

    private class FindAllByMethod extends AbstractFindAllByPersistentMethod {
        public FindAllByMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected Collection findAllBy(GriffonDomainClass domainClass, String methodName, Criterion criterion) {
            List<GriffonDomain> entities = datasetOf(domainClass).query(criterion);
            Collections.sort(entities, IDENTITY_COMPARATOR);
            return entities;
        }
    }

    private class CountByMethod extends AbstractCountByPersistentMethod {
        public CountByMethod(GriffonDomainHandler griffonDomainHandler) {
            super(griffonDomainHandler);
        }

        @Override
        protected int countBy(GriffonDomainClass domainClass, String methodName, Criterion criterion) {
            return datasetOf(domainClass).query(criterion).size();
        }
    }
}
