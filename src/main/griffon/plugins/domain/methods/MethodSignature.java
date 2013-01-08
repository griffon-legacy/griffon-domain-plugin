/*
 * Copyright 2004-2013 the original author or authors.
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
package griffon.plugins.domain.methods;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.join;

/**
 * @author Andres Almiray
 */
public class MethodSignature implements Comparable<MethodSignature> {
    private final Class returnType;
    private final String methodName;
    private final Class[] parameterTypes;
    private final String[] parameterClassnames;
    private final boolean isStatic;

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    public MethodSignature(String methodName) {
        this(false, Void.TYPE, methodName, EMPTY_CLASS_ARRAY);
    }

    public MethodSignature(Class returnType, String methodName) {
        this(false, returnType, methodName, EMPTY_CLASS_ARRAY);
    }

    public MethodSignature(String methodName, Class... parameterTypes) {
        this(false, Void.TYPE, methodName, parameterTypes);
    }

    public MethodSignature(Class returnType, String methodName, Class... parameterTypes) {
        this(false, returnType, methodName, parameterTypes);
    }

    public MethodSignature(boolean isStatic, String methodName) {
        this(isStatic, Void.TYPE, methodName, EMPTY_CLASS_ARRAY);
    }

    public MethodSignature(boolean isStatic, Class returnType, String methodName) {
        this(isStatic, returnType, methodName, EMPTY_CLASS_ARRAY);
    }

    public MethodSignature(boolean isStatic, String methodName, Class... parameterTypes) {
        this(isStatic, Void.TYPE, methodName, parameterTypes);
    }

    public MethodSignature(boolean isStatic, Class returnType, String methodName, Class... parameterTypes) {
        this.isStatic = isStatic;
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes == EMPTY_CLASS_ARRAY ? EMPTY_CLASS_ARRAY : copyClassArray(parameterTypes);
        this.parameterClassnames = new String[this.parameterTypes.length];
        for (int i = 0; i < this.parameterTypes.length; i++) {
            this.parameterClassnames[i] = this.parameterTypes[i].getName();
        }
    }

    public Class getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParameterTypes() {
        return copyClassArray(parameterTypes);
    }

    public boolean isStatic() {
        return isStatic;
    }

    private Class[] copyClassArray(Class[] array) {
        Class[] copy = new Class[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public String toString() {
        return (isStatic ? "static " : "") +
                returnType.getName() + " " +
                methodName + "(" +
                join(parameterTypes, ",") +
                ")";
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        MethodSignature rhs = (MethodSignature) obj;
        return new EqualsBuilder()
                .append(methodName, rhs.methodName)
                .append(parameterClassnames, rhs.parameterClassnames)
                .append(returnType.getName(), rhs.returnType.getName())
                .append(isStatic, rhs.isStatic)
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(methodName)
                .append(parameterClassnames)
                .append(returnType.getName())
                .append(isStatic)
                .toHashCode();
    }

    public int compareTo(MethodSignature other) {
        return new CompareToBuilder()
                .append(methodName, other.methodName)
                .append(parameterClassnames, other.parameterClassnames)
                .append(returnType, other.returnType)
                .append(isStatic, other.isStatic)
                .toComparison();

    }
}