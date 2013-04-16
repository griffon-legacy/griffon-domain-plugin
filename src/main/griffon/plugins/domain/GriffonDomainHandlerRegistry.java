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

package griffon.plugins.domain;

import org.codehaus.griffon.runtime.domain.MemoryGriffonDomainHandler;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static griffon.util.ApplicationHolder.getApplication;
import static griffon.util.ConfigUtils.getConfigValueAsString;
import static griffon.util.GriffonExceptionHandler.sanitize;
import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public final class GriffonDomainHandlerRegistry {
    private static final Map<String, GriffonDomainHandler> DOMAIN_HANDLER_CACHE = new ConcurrentHashMap<String, GriffonDomainHandler>();
    private static final String DEFAULT_DOMAIN_HANDLER_MAPPING = "memory";
    private static final String KEY_DOMAIN_DEFAULT_MAPPING = "griffon.domain.default.mapping";

    private GriffonDomainHandlerRegistry() {
    }

    public static GriffonDomainHandler domainHandlerFor(String mapping) {
        if (isBlank(mapping)) {
            mapping = getConfigValueAsString(
                getApplication().getConfig(),
                KEY_DOMAIN_DEFAULT_MAPPING,
                DEFAULT_DOMAIN_HANDLER_MAPPING);
        }

        cacheDomainHandlers();

        GriffonDomainHandler domainHandler = DOMAIN_HANDLER_CACHE.get(mapping);
        if (null == domainHandler) {
            throw new IllegalArgumentException("No GriffonDomainHandler available for mapping '" + mapping + "'");
        }

        return domainHandler;
    }

    private static void cacheDomainHandlers() {
        if (!DOMAIN_HANDLER_CACHE.isEmpty()) return;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> urls = null;

        try {
            urls = cl.getResources("META-INF/services/" + GriffonDomainHandler.class.getName());
        } catch (IOException ioe) {
            throw new RuntimeException("No implementations found for " + GriffonDomainHandler.class.getName() + "." + ioe);
        }

        if (urls != null) {
            while (urls.hasMoreElements()) {
                try {
                    URL url = urls.nextElement();
                    Properties p = new Properties();
                    p.load(url.openStream());
                    for (String key : p.stringPropertyNames()) {
                        String className = String.valueOf(p.get(key));
                        try {
                            Class clazz = Class.forName(className);
                            DOMAIN_HANDLER_CACHE.put(key, (GriffonDomainHandler) clazz.newInstance());
                        } catch (Exception e) {
                            // can't instantiate domainHandler, bail out immediately
                            throw new IllegalArgumentException("Can't instantiate GriffonDomainHandler for " + key +
                                " with class '" + className + "'. " + e);
                        }
                    }
                } catch (IOException ioe) {
                    System.err.println(sanitize(ioe));
                }
            }
        }

        DOMAIN_HANDLER_CACHE.put(DEFAULT_DOMAIN_HANDLER_MAPPING, new MemoryGriffonDomainHandler());
    }
}
