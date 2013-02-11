/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.griffon.ast;

import griffon.plugins.domain.CommandObject;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.domain.atoms.*;
import griffon.plugins.domain.atoms.StringValue;
import griffon.util.CollectionUtils;
import org.codehaus.griffon.runtime.domain.GriffonDomainConfigurationUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static griffon.util.GriffonNameUtils.*;
import static org.codehaus.griffon.ast.GriffonASTUtils.*;

/**
 * Handles generation of code for the {@code @CommandObject} annotation.
 * <p/>
 *
 * @author Andres Almiray
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class CommandObjectASTTransformation extends ValidateableASTTransformation {
    private static final Logger LOG = LoggerFactory.getLogger(CommandObjectASTTransformation.class);
    private static final ClassNode COMMAND_OBJECT_TYPE = makeClassSafe(CommandObject.class);
    private static final ClassNode COMMAND_OBJECT_ANNOTATION = makeClassSafe(griffon.transform.CommandObject.class);
    private static final String PROPERTY = "Property";
    private static final String VALUE = "Value";
    private static final String VALUE_ARG = "value";
    private static final String SET_VALUE = "setValue";

    private static Map<ClassNode, ClassNode> SUPPORTED_ATOM_TYPES = CollectionUtils.<ClassNode, ClassNode>map()
        .e(makeClassSafe(BigDecimal.class), makeClassSafe(BigDecimalValue.class))
        .e(makeClassSafe(BigInteger.class), makeClassSafe(BigIntegerValue.class))
        .e(makeClassSafe(Boolean.class), makeClassSafe(BooleanValue.class))
        .e(makeClassSafe(Byte.class), makeClassSafe(ByteValue.class))
        .e(makeClassSafe(Calendar.class), makeClassSafe(CalendarValue.class))
        .e(makeClassSafe(Date.class), makeClassSafe(DateValue.class))
        .e(makeClassSafe(Double.class), makeClassSafe(DoubleValue.class))
        .e(makeClassSafe(Float.class), makeClassSafe(FloatValue.class))
        .e(makeClassSafe(Integer.class), makeClassSafe(IntegerValue.class))
        .e(makeClassSafe(Long.class), makeClassSafe(LongValue.class))
        .e(makeClassSafe(Short.class), makeClassSafe(ShortValue.class))
        .e(makeClassSafe(String.class), makeClassSafe(StringValue.class))
        .e(makeClassSafe(Boolean.TYPE), makeClassSafe(BooleanValue.class))
        .e(makeClassSafe(Byte.TYPE), makeClassSafe(ByteValue.class))
        .e(makeClassSafe(Double.TYPE), makeClassSafe(DoubleValue.class))
        .e(makeClassSafe(Float.TYPE), makeClassSafe(FloatValue.class))
        .e(makeClassSafe(Integer.TYPE), makeClassSafe(IntegerValue.class))
        .e(makeClassSafe(Long.TYPE), makeClassSafe(LongValue.class))
        .e(makeClassSafe(Short.TYPE), makeClassSafe(ShortValue.class));


    /**
     * Convenience method to see if an annotated node is {@code @CommandObject}.
     *
     * @param node the node to check
     * @return true if the node is annotated with @CommandObject
     */
    public static boolean hasCommandObjectAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (COMMAND_OBJECT_ANNOTATION.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes  the ast nodes
     * @param source the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        checkNodesForAnnotationAndType(nodes[0], nodes[1]);

        AnnotationNode annotationNode = (AnnotationNode) nodes[0];
        Expression expr = annotationNode.getMember("value");
        Expression baseClasses = null;
        if (expr instanceof ClassExpression) {
            baseClasses = expr;
        } else if (expr instanceof ListExpression) {
            ListExpression listExpression = (ListExpression) expr;
            for (Expression ex : listExpression.getExpressions()) {
                if (!(ex instanceof ClassExpression)) {
                    throw new GroovyBugError(COMMAND_OBJECT_ANNOTATION.getName() + " only accepts a single Class or an array of classes. Invalid expression " + ex.getText());
                }
            }
            baseClasses = expr;
        }

        addCommandObjectToClass(source, (ClassNode) nodes[1], baseClasses);
    }

    public static void addCommandObjectToClass(SourceUnit source, ClassNode classNode, Expression baseClasses) {
        if (needsValidateable(classNode, source)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Injecting " + CommandObject.class.getName() + " into " + classNode.getName());
            }
            addCommandObjectSupport(classNode, baseClasses);
        }
    }

    public static void addCommandObjectSupport(ClassNode declaringClass, Expression baseClasses) {
        injectInterface(declaringClass, COMMAND_OBJECT_TYPE);

        if (baseClasses instanceof ClassExpression) {
            addBaseProperties(declaringClass, ((ClassExpression) baseClasses).getType());
        } else if (baseClasses instanceof ListExpression) {
            ListExpression listExpression = (ListExpression) baseClasses;
            for (Expression ex : listExpression.getExpressions()) {
                addBaseProperties(declaringClass, ((ClassExpression) ex).getType());
            }
        }

        transformProperties(declaringClass);
        addValidatableBehavior(declaringClass);
    }

    private static void addBaseProperties(ClassNode declaringClass, ClassNode baseClass) {
        for (PropertyNode propertyNode : baseClass.getProperties()) {
            if (propertyNode.isStatic() || propertyNode.isSynthetic() || propertyNode.isDynamicTyped()
                || !propertyNode.isPublic() || !GriffonDomainConfigurationUtil.isNotConfigurational(propertyNode.getName())
                || GriffonDomainProperty.STANDARD_DOMAIN_PROPERTIES.contains(propertyNode.getName())) {
                continue;
            }
            declaringClass.addProperty(
                propertyNode.getName(),
                propertyNode.getModifiers(),
                makeClassSafe(propertyNode.getType()),
                null,
                null,
                null
            );
        }
    }

    private static void transformProperties(ClassNode declaringClass) {
        List<PropertyNode> properties = new ArrayList<PropertyNode>(declaringClass.getProperties());
        for (PropertyNode propertyNode : properties) {
            if (propertyNode.isStatic() || propertyNode.isSynthetic() || propertyNode.isDynamicTyped()
                || !propertyNode.isPublic() || !isPropertyTypeSupported(propertyNode))
                continue;

            String propertyName = propertyNode.getField().getName();
            declaringClass.removeField(propertyName);
            declaringClass.getProperties().remove(propertyNode);
            ClassNode propertyType = SUPPORTED_ATOM_TYPES.get(propertyNode.getType());

            FieldNode fieldNode = declaringClass.addField(
                propertyName,
                ACC_FINAL | ACC_PRIVATE,
                makeClassSafe(propertyType),
                ctor(makeClassSafe(propertyType), NO_ARGS));

            injectMethod(declaringClass, new MethodNode(
                getGetterName(propertyName),
                ACC_PUBLIC,
                makeClassSafe(propertyNode.getType()),
                params(),
                ClassNode.EMPTY_ARRAY,
                returns(call(field(fieldNode), uncapitalize(propertyNode.getType().getNameWithoutPackage()) + VALUE, NO_ARGS))
            ));

            injectMethod(declaringClass, new MethodNode(
                getSetterName(propertyName),
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                params(param(propertyNode.getType(), VALUE_ARG)),
                ClassNode.EMPTY_ARRAY,
                stmnt(call(field(fieldNode), SET_VALUE, args(var(VALUE_ARG))))
            ));

            injectMethod(declaringClass, new MethodNode(
                uncapitalize(propertyName) + PROPERTY,
                ACC_PUBLIC,
                makeClassSafe(propertyType),
                params(),
                ClassNode.EMPTY_ARRAY,
                returns(field(fieldNode))
            ));
        }
    }

    private static boolean isPropertyTypeSupported(PropertyNode propertyNode) {
        return SUPPORTED_ATOM_TYPES.keySet().contains(propertyNode.getType());
    }
}
