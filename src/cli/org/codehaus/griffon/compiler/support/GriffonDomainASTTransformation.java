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

package org.codehaus.griffon.compiler.support;

import griffon.plugins.domain.Event;
import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainClass;
import griffon.transform.Domain;
import org.codehaus.griffon.compiler.GriffonCompilerContext;
import org.codehaus.griffon.runtime.domain.AbstractGriffonDomain;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.reflect.Modifier;

import static org.codehaus.griffon.ast.GriffonASTUtils.*;

/**
 * Handles generation of code for Griffon domain classes.
 * <p/>
 *
 * @author Andres Almiray
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class GriffonDomainASTTransformation extends GriffonArtifactASTTransformation {
    private static final String ARTIFACT_PATH = "domain";
    private static final ClassNode DOMAIN_CLASS = makeClassSafe(Domain.class);
    private static final ClassNode GRIFFON_DOMAIN_CLASS = makeClassSafe(GriffonDomain.class);
    private static final ClassNode ABSTRACT_GRIFFON_DOMAIN_CLASS = makeClassSafe(AbstractGriffonDomain.class);

    public static boolean isDomainArtifact(ClassNode classNode, SourceUnit source) {
        System.out.println(classNode + " " + source);
        if (classNode == null || source == null) return false;
        System.out.println(ARTIFACT_PATH.equals(GriffonCompilerContext.getArtifactPath(source)) &&
            !classNode.getAnnotations(DOMAIN_CLASS).isEmpty());
        return ARTIFACT_PATH.equals(GriffonCompilerContext.getArtifactPath(source)) &&
            !classNode.getAnnotations(DOMAIN_CLASS).isEmpty();
    }

    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (!(astNodes[0] instanceof AnnotationNode) || !(astNodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: wrong types: $node.class / $parent.class");
        }

        AnnotatedNode parent = (AnnotatedNode) astNodes[1];
        AnnotationNode node = (AnnotationNode) astNodes[0];
        if (!DOMAIN_CLASS.equals(node.getClassNode()) || !(parent instanceof ClassNode)) {
            return;
        }

        ClassNode classNode = (ClassNode) parent;
        String cName = classNode.getName();
        if (classNode.isInterface()) {
            throw new RuntimeException("Error processing interface '" + cName + "'. " +
                DOMAIN_CLASS + " not allowed for interfaces.");
        }

        transform(classNode);
        injectDomainBehavior(classNode);
    }

    private void injectDomainBehavior(ClassNode classNode) {
        ASTInjector injector = new DefaultGriffonDomainClassInjector();
        injector.inject(classNode, getArtifactType());
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
        addMethod(classNode, new MethodNode(
            "beforeValidate",
            Modifier.PUBLIC,
            ClassHelper.VOID_TYPE,
            params(param(makeClassSafe(ClassHelper.LIST_TYPE), "propertyNames")),
            ClassNode.EMPTY_ARRAY,
            new EmptyStatement()
        ));
    }

    protected String getArtifactType() {
        return GriffonDomainClass.TYPE;
    }

    protected ClassNode getSuperClassNode(ClassNode classNode) {
        return ABSTRACT_GRIFFON_DOMAIN_CLASS;
    }

    protected ClassNode getInterfaceNode() {
        return GRIFFON_DOMAIN_CLASS;
    }

    protected boolean matches(ClassNode classNode, SourceUnit source) {
        return isDomainArtifact(classNode, source);
    }

    protected ASTInjector[] getASTInjectors() {
        return new ASTInjector[]{
            new GriffonArtifactASTInjector(),
            new DefaultGriffonDomainClassInjector()
        };
    }
}