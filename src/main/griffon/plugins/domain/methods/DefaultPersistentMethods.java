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

package griffon.plugins.domain.methods;

/**
 * @author Andres Almiray
 */
public enum DefaultPersistentMethods {
    COUNT(CountMethod.METHOD_SIGNATURES),
    COUNT_BY(CountByMethod.METHOD_NAME, CountByMethod.METHOD_SIGNATURES),
    MAKE(MakeMethod.METHOD_SIGNATURES),
    SAVE(SaveMethod.METHOD_SIGNATURES),
    DELETE(DeleteMethod.METHOD_SIGNATURES),
    FETCH(FetchMethod.METHOD_SIGNATURES),
    EXISTS(FetchMethod.METHOD_SIGNATURES),
    FETCH_ALL(FetchAllMethod.METHOD_NAME, FetchMethod.METHOD_SIGNATURES),
    LIST(ListMethod.METHOD_SIGNATURES),
    FIND(FindMethod.METHOD_SIGNATURES),
    FIND_BY(FindByMethod.METHOD_NAME, FindByMethod.METHOD_SIGNATURES),
    FIND_WHERE(FindWhereMethod.METHOD_NAME, FindWhereMethod.METHOD_SIGNATURES),
    FIND_ALL(FindAllMethod.METHOD_NAME, FindAllMethod.METHOD_SIGNATURES),
    FIND_ALL_BY(FindAllByMethod.METHOD_NAME, FindAllByMethod.METHOD_SIGNATURES),
    FIND_ALL_WHERE(FindAllWhereMethod.METHOD_NAME, FindAllMethod.METHOD_SIGNATURES),
    WITH_CRITERIA(WithCriteriaMethod.METHOD_NAME, WithCriteriaMethod.METHOD_SIGNATURES);

    // COUNT_BY('countBy'),
    // FIND_BY('findBy'),
    // FIND_ALL_BY('findAllBy');

    private final String methodName;
    private final MethodSignature[] methodSignatures;

    DefaultPersistentMethods(MethodSignature[] methodSignatures) {
        this(null, methodSignatures);
    }


    DefaultPersistentMethods(String methodName, MethodSignature[] methodSignatures) {
        this.methodName = methodName != null ? methodName : name().toLowerCase();
        this.methodSignatures = methodSignatures;
    }

    public String toString() {
        return this.methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodSignature[] getMethodSignatures() {
        return methodSignatures;
    }
}