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

import griffon.plugins.domain.GriffonDomain;
import griffon.plugins.domain.GriffonDomainHandler;
import griffon.plugins.domain.methods.MethodSignature;
import griffon.util.ApplicationHolder;
import org.codehaus.griffon.runtime.domain.MethodMissingInterceptor;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;

import java.lang.reflect.Modifier;

import static org.codehaus.griffon.ast.AbstractASTTransformation.makeClassSafe;
import static org.codehaus.griffon.ast.GriffonASTUtils.*;

/**
 * Base contract for a domain class injector that adds persistence
 * specific properties and other boilerplate code.
 *
 * @author Andres Almiray
 */
public abstract class GriffonDomainClassInjector {
    private static final int ACC_SYNTHETIC = 4096;
    private static final String GRIFFON_DOMAIN_CLASSNAME = GriffonDomain.class.getName();
    private static final ClassNode APPLICATION_HOLDER_TYPE = makeClassSafe(ApplicationHolder.class);
    private final ClassNode GRIFFON_DOMAIN_HANDLER_CLASS = makeClassSafe(GriffonDomainHandler.class);
    private final ClassNode METHOD_MISSING_INTERCEPTOR_CLASS = makeClassSafe(MethodMissingInterceptor.class);
    protected static final String DOMAIN_HANDLER_METHOD_NAME = "domainHandler";

    public void performInjectionOn(ClassNode classNode) {
        injectDomainHandler(classNode);
        injectMethodMissing(classNode);
        injectMethods(classNode);
        performInjection(classNode);
    }

    protected abstract MethodSignature[] getProvidedMethods();

    protected abstract void performInjection(ClassNode classNode);

    protected void injectMethods(ClassNode classNode) {
        for (MethodSignature methodSignature : getProvidedMethods()) {
            injectMethod(classNode, methodSignature);
        }
    }

    protected void injectMethod(ClassNode classNode, MethodSignature methodSignature) {
        Parameter[] parameters = makeParameters(methodSignature.getParameterTypes());

        int modifiers = Modifier.PUBLIC;
        if (methodSignature.isStatic()) modifiers |= Modifier.STATIC;
        String returnTypeClassName = methodSignature.getReturnType().getName();
        ClassNode returnType = GRIFFON_DOMAIN_CLASSNAME.equals(returnTypeClassName) ? makeClassSafe(classNode) : makeClassSafe(methodSignature.getReturnType());
        classNode.addMethod(new MethodNode(
                methodSignature.getMethodName(),
                modifiers,
                returnType,
                parameters,
                ClassNode.EMPTY_ARRAY,
                makeMethodBody(classNode, methodSignature, parameters)
        ));
    }

    protected Parameter[] makeParameters(Class[] parameterTypes) {
        if (parameterTypes.length == 0) return Parameter.EMPTY_ARRAY;
        Parameter[] parameters = new Parameter[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = new Parameter(makeClassSafe(parameterTypes[i]), "arg" + i);
        }
        return parameters;
    }

    private Statement makeMethodBody(ClassNode classNode, MethodSignature methodSignature, Parameter[] parameters) {
        String invokeMethod = methodSignature.isStatic() ? "invokeStaticMethod" : "invokeInstanceMethod";
        Expression[] args = new Expression[parameters.length + 2];
        args[0] = methodSignature.isStatic() ? new ClassExpression(classNode) : VariableExpression.THIS_EXPRESSION;
        args[1] = new ConstantExpression(methodSignature.getMethodName());
        int i = 2;
        for (Parameter parameter : parameters) {
            args[i++] = new VariableExpression(parameter.getName());
        }

        return returns(call(
                domainHandlerInstance(classNode),
                invokeMethod,
                args(args)));
    }

    protected void injectDomainHandler(ClassNode classNode) {
        classNode.addMethod(new MethodNode(
                DOMAIN_HANDLER_METHOD_NAME,
                Modifier.PUBLIC | Modifier.STATIC,
                GRIFFON_DOMAIN_HANDLER_CLASS,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                returns(domainHandlerInstance(classNode))
        ));
    }

    protected void injectMethodMissing(ClassNode classNode) {
        FieldNode methodMissingInterceptor = classNode.addField(
                "this$methodMissingInterceptor",
                Modifier.FINAL | Modifier.STATIC | Modifier.PRIVATE | ACC_SYNTHETIC,
                METHOD_MISSING_INTERCEPTOR_CLASS,
                ctor(METHOD_MISSING_INTERCEPTOR_CLASS,
                        args(classx(classNode))));

        classNode.addMethod(new MethodNode(
                "$static_methodMissing",
                Modifier.PUBLIC | Modifier.STATIC,
                ClassHelper.OBJECT_TYPE,
                params(
                        param(ClassHelper.STRING_TYPE, "methodName"),
                        param(makeClassSafe(Object[].class), "arguments")
                ),
                ClassNode.EMPTY_ARRAY,
                returns(
                        call(field(methodMissingInterceptor),
                                "handleMethodMissing",
                                args(var("methodName"), var("arguments")))
                )
        ));
    }

    protected Expression domainHandlerInstance(ClassNode classNode) {
        return call(grabGriffonClass(classNode), "getDomainHandler", NO_ARGS);
    }

    protected Expression grabGriffonClass(ClassNode classNode) {
        return call(
                artifactManagerInstance(),
                "findGriffonClass",
                args(classx(classNode)));
    }

    public static Expression applicationInstance() {
        return call(APPLICATION_HOLDER_TYPE, "getApplication", NO_ARGS);
    }

    public static Expression artifactManagerInstance() {
        return call(applicationInstance(), "getArtifactManager", NO_ARGS);
    }
}
