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

import griffon.persistence.BelongsTo;
import griffon.persistence.HasMany;
import griffon.plugins.domain.GriffonDomainProperty;
import griffon.plugins.domain.methods.CreateMethod;
import griffon.plugins.domain.methods.DefaultPersistentMethods;
import griffon.plugins.domain.methods.MethodSignature;
import org.codehaus.griffon.ast.GriffonASTUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.lang.reflect.Modifier;
import java.util.*;

import static griffon.util.GriffonNameUtils.*;
import static org.codehaus.griffon.ast.GriffonASTUtils.*;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;

// import javax.persistence.OneToOne;

/**
 * Default implementation of domain class injector interface that adds the 'id'
 * and 'version' properties and other previously boilerplate code.
 *
 * @author Andres Almiray
 */
public class DefaultGriffonDomainClassInjector extends GriffonDomainClassInjector {
    private List<ClassNode> classesWithInjectedToString = new ArrayList<ClassNode>();
    private static final ClassNode PROPERTY_TYPE = makeClassSafe(Long.class);
    // private static final ClassNode HAS_ONE_TYPE = makeClassSafe(HasOne.class);
    private static final ClassNode HAS_MANY_TYPE = makeClassSafe(HasMany.class);
    private static final ClassNode BELONGS_TO_TYPE = makeClassSafe(BelongsTo.class);
    private static final ClassNode COLLECTION_TYPE = makeClassSafe(Collection.class);
    private static final ClassNode LIST_TYPE = makeClassSafe(List.class);
    private static final ClassNode SET_TYPE = makeClassSafe(Set.class);
    private static final ClassNode ARRAY_LIST_TYPE = makeClassSafe(ArrayList.class);
    private static final ClassNode LINKED_HASH_SET_TYPE = makeClassSafe(LinkedHashSet.class);
    private static final Expression NIL = ConstantExpression.NULL;

    private static final String MAPPED_BY = "mappedBy";
    private static final String ADD_TO = "addTo";
    private static final String REMOVE_FROM = "removeFrom";
    private static final String ADD = "add";
    private static final String REMOVE = "remove";

    protected void performInjection(ClassNode classNode) {
        injectAssociations(classNode);
        injectIdProperty(classNode);
        injectVersionProperty(classNode);
        injectToStringMethod(classNode, GriffonDomainProperty.IDENTITY);
    }

    protected void injectToStringMethod(ClassNode classNode, String propertyName) {
        final boolean hasToString = implementsOrInheritsZeroArgMethod(
            classNode, "toString", classesWithInjectedToString);

        if (!hasToString && !GriffonASTUtils.isEnum(classNode)) {
            GStringExpression ge = new GStringExpression(classNode.getName() + " : ${" + propertyName + "}");
            ge.addString(new ConstantExpression(classNode.getName() + " : "));
            ge.addValue(new VariableExpression(propertyName));
            Statement s = new ReturnStatement(ge);
            MethodNode mn = new MethodNode("toString", Modifier.PUBLIC, new ClassNode(String.class), new Parameter[0], new ClassNode[0], s);
            classNode.addMethod(mn);
            classesWithInjectedToString.add(classNode);
        }
    }

    private void injectAssociations(ClassNode classNode) {
        for (PropertyNode propertyNode : classNode.getProperties()) {
            AnnotationNode hasManyAnnotation = getAnnotation(propertyNode, HAS_MANY_TYPE);
            if (hasManyAnnotation != null) {
                processHasMany(classNode, propertyNode, hasManyAnnotation);
            }
        }
    }

    private void processHasMany(ClassNode classNode, PropertyNode propertyNode, AnnotationNode hasManyAnnotation) {
        ClassNode propertyType = propertyNode.getType();

        if (!propertyType.implementsInterface(COLLECTION_TYPE)) {
            throw new GroovyBugError("The type of property " + classNode.getName() + "." + propertyNode.getName() + " (" + propertyType.getText() + ") is not compatible with " + Collection.class.getName());
        }
        GenericsType[] genericTypes = propertyType.getGenericsTypes();
        if (genericTypes == null || genericTypes.length != 1) {
            throw new GroovyBugError("Property " + classNode.getName() + "." + propertyNode.getName() + " does not define a type for the enclosed elements.");
        }
        ClassNode ownedSide = genericTypes[0].getType();

        String owningSide = getMemberAsString(hasManyAnnotation, MAPPED_BY);
        if (isBlank(owningSide)) {
            for (PropertyNode ownedProperty : ownedSide.getProperties()) {
                AnnotationNode belongsToAnnotation = getAnnotation(ownedProperty, BELONGS_TO_TYPE);
                if (belongsToAnnotation != null && ownedProperty.getType().getName().equals(classNode.getName())) {
                    owningSide = ownedProperty.getName();
                    break;
                }
            }
        }
        injectAddToMethod(classNode, propertyNode, ownedSide, owningSide);
        injectRemoveFromMethod(classNode, propertyNode, ownedSide, owningSide);
    }

    private void injectAddToMethod(ClassNode classNode, PropertyNode propertyNode, ClassNode ownedSide, String owningSide) {
        String methodName = ADD_TO + capitalize(propertyNode.getName());
        String argName = getPropertyName(ownedSide.getNameWithoutPackage());
        String collectionName = propertyNode.getName();
        ClassNode collectionType = propertyNode.getType();
        FieldExpression collectionField = field(classNode, collectionName);

        ClassNode initializingType = null;
        if (collectionType.isDerivedFrom(LIST_TYPE)) {
            initializingType = makeClassSafe(ARRAY_LIST_TYPE);
        } else if (collectionType.isDerivedFrom(SET_TYPE)) {
            initializingType = makeClassSafe(LINKED_HASH_SET_TYPE);
        } else {
            throw new GroovyBugError("Unsupported Collection type " + collectionType.getName() + " for property " + classNode.getName() + "." + collectionName);
        }

        BlockStatement body = block(
            ifs_no_return(eq(var(argName), NIL), returns(NIL)),
            ifs_no_return(eq(collectionField, NIL), assign(collectionField, ctor(initializingType, NO_ARGS))),
            stmnt(call(collectionField, ADD, args(var(argName))))
        );
        if (!isBlank(owningSide)) {
            body.addStatement(stmnt(
                call(var(argName), getSetterName(owningSide), args(THIS))
            ));
        }

        classNode.addMethod(new MethodNode(
            methodName,
            Modifier.PUBLIC,
            ClassHelper.VOID_TYPE,
            params(param(ownedSide, argName)),
            ClassNode.EMPTY_ARRAY,
            body
        ));

        classNode.addMethod(new MethodNode(
            methodName,
            Modifier.PUBLIC,
            ClassHelper.VOID_TYPE,
            params(param(makeClassSafe(MAP_TYPE), argName)),
            ClassNode.EMPTY_ARRAY,
            stmnt(call(THIS, methodName, args(
                call(ownedSide, CreateMethod.METHOD_NAME, args(var(argName)))
            )))
        ));
    }

    private void injectRemoveFromMethod(ClassNode classNode, PropertyNode propertyNode, ClassNode ownedSide, String owningSide) {
        String methodName = REMOVE_FROM + capitalize(propertyNode.getName());
        String argName = getPropertyName(ownedSide.getNameWithoutPackage());
        String collectionName = propertyNode.getName();
        FieldExpression collectionField = field(classNode, collectionName);

        BlockStatement body = block(
            ifs_no_return(eq(var(argName), NIL), returns(NIL)),
            ifs_no_return(ne(collectionField, NIL), call(collectionField, REMOVE, args(var(argName))))
        );
        if (!isBlank(owningSide)) {
            body.addStatement(stmnt(
                call(var(argName), getSetterName(owningSide), args(NIL))
            ));
        }

        classNode.addMethod(new MethodNode(
            methodName,
            Modifier.PUBLIC,
            ClassHelper.VOID_TYPE,
            params(param(ownedSide, argName)),
            ClassNode.EMPTY_ARRAY,
            body
        ));
    }

    private String getMemberAsString(AnnotationNode annotation, String memberName) {
        Expression member = annotation.getMember(memberName);
        if (member != null && member instanceof ConstantExpression) {
            return ((ConstantExpression) member).getText();
        }
        return null;
    }

    private AnnotationNode getAnnotation(PropertyNode propertyNode, ClassNode annotationType) {
        FieldNode fieldNode = propertyNode.getField();
        List<AnnotationNode> annotations = fieldNode.getAnnotations(annotationType);
        if (annotations != null && annotations.size() == 1)
            return annotations.get(0);
        annotations = propertyNode.getAnnotations(annotationType);
        return annotations != null && annotations.size() == 1 ? annotations.get(0) : null;
    }

    protected void injectVersionProperty(ClassNode classNode) {
        injectProperty(classNode, GriffonDomainProperty.VERSION, PROPERTY_TYPE.getTypeClass(), null);
    }

    protected void injectIdProperty(ClassNode classNode) {
        injectProperty(classNode, GriffonDomainProperty.IDENTITY, PROPERTY_TYPE.getTypeClass(), null);
    }

    @Override
    protected MethodSignature[] getProvidedMethods() {
        Set<MethodSignature> methodSignatures = new TreeSet<MethodSignature>();
        for (DefaultPersistentMethods method : DefaultPersistentMethods.values()) {
            MethodSignature[] signatures = method.getMethodSignatures();
            for (int i = 0, length = signatures.length; i < length; i++) {
                methodSignatures.add(signatures[i]);
            }
        }
        return methodSignatures.toArray(new MethodSignature[methodSignatures.size()]);
    }
}
