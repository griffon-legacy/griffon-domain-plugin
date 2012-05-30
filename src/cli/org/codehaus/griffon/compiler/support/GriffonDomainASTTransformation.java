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

import griffon.plugins.domain.Event;
import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.transform.Domain;
import org.codehaus.griffon.compiler.GriffonCompilerContext;
import org.codehaus.griffon.compiler.SourceUnitCollector;
import org.codehaus.griffon.runtime.domain.AbstractGriffonDomain;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;

import static org.codehaus.griffon.ast.GriffonASTUtils.addMethod;

/**
 * Handles generation of code for Griffon domain classes.
 * <p/>
 *
 * @author Andres Almiray
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class GriffonDomainASTTransformation extends GriffonArtifactASTTransformation {
    private static final Logger LOG = LoggerFactory.getLogger(GriffonDomainASTTransformation.class);
    private static final String ARTIFACT_PATH = "domain";
    private static final ClassNode DOMAIN_CLASS = makeClassSafe(Domain.class);
    private static final ClassNode GRIFFON_DOMAIN_CLASS = makeClassSafe(GriffonDomain.class);
    private static final ClassNode ABSTRACT_GRIFFON_DOMAIN_CLASS = makeClassSafe(AbstractGriffonDomain.class);

    public static boolean isDomainArtifact(ClassNode classNode, SourceUnit source) {
        if (classNode == null || source == null) return false;
        return ARTIFACT_PATH.equals(GriffonCompilerContext.getArtifactPath(source));
    }

    protected void transform(ClassNode classNode, SourceUnit source, String artifactPath) {
        if (!isDomainArtifact(classNode, source) ||
                !classNode.getAnnotations(DOMAIN_CLASS).isEmpty()) return;

        inject(classNode);
    }

    public static void inject(ClassNode classNode) {
        injectBaseBehavior(classNode);
        injectBehavior(classNode);
    }

    public static void injectBaseBehavior(ClassNode classNode) {
        if (ClassHelper.OBJECT_TYPE.equals(classNode.getSuperClass())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting " + ABSTRACT_GRIFFON_DOMAIN_CLASS.getName() + " as the superclass of " + classNode.getName());
            }
            classNode.setSuperClass(ABSTRACT_GRIFFON_DOMAIN_CLASS);
        } else if (!classNode.implementsInterface(GRIFFON_DOMAIN_CLASS)) {
            ClassNode superClass = classNode.getSuperClass();
            SourceUnit superSource = SourceUnitCollector.getInstance().getSourceUnit(superClass);
            if (isDomainArtifact(superClass, superSource)) return;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Injecting " + GRIFFON_DOMAIN_CLASS.getName() + " behavior to " + classNode.getName());
            }
            // 1. add interface
            classNode.addInterface(GRIFFON_DOMAIN_CLASS);
            // 2. add methods
            ASTInjector injector = new GriffonArtifactASTInjector();
            injector.inject(classNode, GriffonDomainClass.TYPE);

            for (String eventName : Event.getAllEvents()) {
                addMethod(classNode, new MethodNode(
                        eventName,
                        Modifier.PUBLIC,
                        ClassHelper.VOID_TYPE,
                        Parameter.EMPTY_ARRAY,
                        ClassNode.EMPTY_ARRAY,
                        new EmptyStatement()
                ));
            }
        }
    }

    public static void injectBehavior(ClassNode classNode) {
        GriffonDomainClassInjector injector = new DefaultGriffonDomainClassInjector();
        injector.performInjectionOn(classNode);
    }

}