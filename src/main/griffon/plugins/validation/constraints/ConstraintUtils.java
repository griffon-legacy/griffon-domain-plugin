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

package griffon.plugins.validation.constraints;

import griffon.core.GriffonApplication;
import griffon.plugins.validation.factories.ConstraintsEvaluatorFactory;
import griffon.util.ApplicationHolder;
import griffon.util.ClosureToMapPopulator;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static griffon.util.ConfigUtils.getConfigValue;
import static griffon.util.ConfigUtils.getConfigValueAsString;
import static org.codehaus.griffon.runtime.util.GriffonApplicationHelper.safeNewInstance;

/**
 * @author Andres Almiray
 */
public class ConstraintUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ConstraintUtils.class);
    private static final String KEY_CONSTRAINTS_EVALUATOR_FACTORY = "app.constraintsEvaluator.factory";
    private static final String DEFAULT_CONSTRAINTS_EVALUATOR_FACTORY = "org.codehaus.griffon.runtime.validation.constraints.DefaultConstraintsEvaluatorFactory";
    private static final String KEY_DEFAULT_CONSTRAINTS = "griffon.grorm.default.constraints";

    private static ConstraintsEvaluator constraintsEvaluator;
    private static volatile boolean constraintsEvaluatorInitialized;
    private static final ReentrantReadWriteLock CONSTRAINTS_EVALUATOR_LOCK = new ReentrantReadWriteLock();

    public static ConstraintsEvaluator getConstraintsEvaluator(GriffonApplication app) {
        if (app == null) {
            app = ApplicationHolder.getApplication();
        }
        CONSTRAINTS_EVALUATOR_LOCK.readLock().lock();
        if (!constraintsEvaluatorInitialized) {
            // Must release read lock before acquiring write lock
            CONSTRAINTS_EVALUATOR_LOCK.readLock().unlock();
            CONSTRAINTS_EVALUATOR_LOCK.writeLock().lock();
            try {
                // Recheck state because another thread might have
                // acquired write lock and changed state before we did.
                if (!constraintsEvaluatorInitialized) {
                    String className = getConfigValueAsString(app.getConfig(), KEY_CONSTRAINTS_EVALUATOR_FACTORY, DEFAULT_CONSTRAINTS_EVALUATOR_FACTORY);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Using " + className + " as ConstraintsEvaluatorFactory");
                    }
                    ConstraintsEvaluatorFactory factory = (ConstraintsEvaluatorFactory) safeNewInstance(className);
                    constraintsEvaluator = factory.create(app);
                    constraintsEvaluatorInitialized = true;
                }
                // Downgrade by acquiring read lock before releasing write lock
                CONSTRAINTS_EVALUATOR_LOCK.readLock().lock();
            } finally {
                CONSTRAINTS_EVALUATOR_LOCK.writeLock().unlock(); // Unlock write, still hold read
            }
        }

        try {
            return constraintsEvaluator;
        } finally {
            CONSTRAINTS_EVALUATOR_LOCK.readLock().unlock();
        }
    }

    private static Map<String, Object> defaultConstraintsMap;
    private static volatile boolean defaultConstraintsInitialized;
    private static final ReentrantReadWriteLock DEFAULT_CONSTRAINTS_LOCK = new ReentrantReadWriteLock();

    public static Map<String, Object> getDefaultConstraints(GriffonApplication app) {
        if (app == null) {
            app = ApplicationHolder.getApplication();
        }
        DEFAULT_CONSTRAINTS_LOCK.readLock().lock();
        if (!defaultConstraintsInitialized) {
            // Must release read lock before acquiring write lock
            DEFAULT_CONSTRAINTS_LOCK.readLock().unlock();
            DEFAULT_CONSTRAINTS_LOCK.writeLock().lock();
            try {
                // Recheck state because another thread might have
                // acquired write lock and changed state before we did.
                if (!defaultConstraintsInitialized) {
                    Object constraints = getConfigValue(app.getConfig(), KEY_DEFAULT_CONSTRAINTS, null);
                    if (constraints instanceof Closure) {
                        defaultConstraintsMap = new ClosureToMapPopulator().populate((Closure<?>) constraints);
                    } else {
                        defaultConstraintsMap = Collections.emptyMap();
                    }
                    defaultConstraintsInitialized = true;
                }
                // Downgrade by acquiring read lock before releasing write lock
                DEFAULT_CONSTRAINTS_LOCK.readLock().lock();
            } finally {
                DEFAULT_CONSTRAINTS_LOCK.writeLock().unlock(); // Unlock write, still hold read
            }
        }

        try {
            return defaultConstraintsMap;
        } finally {
            DEFAULT_CONSTRAINTS_LOCK.readLock().unlock();
        }
    }
}
