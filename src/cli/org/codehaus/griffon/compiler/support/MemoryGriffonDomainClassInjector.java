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
package org.codehaus.griffon.compiler.support;

import griffon.plugins.domain.methods.MethodSignature;
import org.codehaus.griffon.ast.AbstractASTTransformation;
import org.codehaus.griffon.runtime.domain.MemoryGriffonDomainHandler;
import org.codehaus.groovy.ast.ClassNode;

/**
 * @author Andres Almiray
 */
public class MemoryGriffonDomainClassInjector extends DefaultGriffonDomainClassInjector {
    private final ClassNode DOMAIN_HANDLER_CLASS = AbstractASTTransformation.
            makeClassSafe(MemoryGriffonDomainHandler.class);

    @Override
    protected MethodSignature[] getProvidedMethods() {
        return MemoryGriffonDomainHandler.METHOD_SIGNATURES;
    }

    @Override
    protected ClassNode getDomainHandlerClass() {
        return DOMAIN_HANDLER_CLASS;
    }
}
