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

package griffon.plugins.domain.methods;

/**
 * @author Andres Almiray
 */
public enum DefaultPersistentMethods {
    COUNT(CountMethod.METHOD_SIGNATURES),
    COUNT_BY(CountByMethod.METHOD_NAME, CountByMethod.METHOD_SIGNATURES),
    CREATE(CreateMethod.METHOD_SIGNATURES),
    SAVE(SaveMethod.METHOD_SIGNATURES),
    DELETE(DeleteMethod.METHOD_SIGNATURES),
    GET(GetMethod.METHOD_SIGNATURES),
    EXISTS(ExistsMethod.METHOD_SIGNATURES),
    GET_ALL(GetAllMethod.METHOD_NAME, GetAllMethod.METHOD_SIGNATURES),
    LIST(ListMethod.METHOD_SIGNATURES),
    LIST_ORDER_BY(ListOrderByMethod.METHOD_SIGNATURES),
    FIRST(FirstMethod.METHOD_SIGNATURES),
    LAST(LastMethod.METHOD_SIGNATURES),
    FIND(FindMethod.METHOD_SIGNATURES),
    FIND_BY(FindByMethod.METHOD_NAME, FindByMethod.METHOD_SIGNATURES),
    FIND_WHERE(FindWhereMethod.METHOD_NAME, FindWhereMethod.METHOD_SIGNATURES),
    FIND_ALL(FindAllMethod.METHOD_NAME, FindAllMethod.METHOD_SIGNATURES),
    FIND_ALL_BY(FindAllByMethod.METHOD_NAME, FindAllByMethod.METHOD_SIGNATURES),
    FIND_ALL_WHERE(FindAllWhereMethod.METHOD_NAME, FindAllWhereMethod.METHOD_SIGNATURES),
    FIND_OR_CREATE_BY(FindOrCreateByMethod.METHOD_NAME, FindOrCreateByMethod.METHOD_SIGNATURES),
    FIND_OR_CREATE_WHERE(FindOrCreateWhereMethod.METHOD_NAME, FindOrCreateWhereMethod.METHOD_SIGNATURES),
    FIND_OR_SAVE_BY(FindOrSaveByMethod.METHOD_NAME, FindOrSaveByMethod.METHOD_SIGNATURES),
    FIND_OR_SAVE_WHERE(FindOrSaveWhereMethod.METHOD_NAME, FindOrSaveWhereMethod.METHOD_SIGNATURES),
    // WHERE(WhereMethod.METHOD_NAME, WhereMethod.METHOD_SIGNATURES),
    WITH_CRITERIA(WithCriteriaMethod.METHOD_NAME, WithCriteriaMethod.METHOD_SIGNATURES);

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