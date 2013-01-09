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
package griffon.plugins.validation.constraints;

import groovy.lang.Closure;
import groovy.lang.IntRange;

import java.util.List;

/**
 * @author Andres Almiray
 */
public final class Constraints {
    private Constraints() {

    }

    public static ConstraintDef blank(boolean value) {
        return new ConstraintDef("blank", value);
    }

    public static ConstraintDef creditCard(boolean value) {
        return new ConstraintDef("creditCard", value);
    }

    public static ConstraintDef email(boolean value) {
        return new ConstraintDef("email", value);
    }

    public static ConstraintDef inList(List<?> elements) {
        return new ConstraintDef("inList", elements);
    }

    public static ConstraintDef matches(String pattern) {
        return new ConstraintDef("matches", pattern);
    }

    public static ConstraintDef max(Object value) {
        return new ConstraintDef("max", value);
    }

    public static ConstraintDef maxSize(int value) {
        return new ConstraintDef("maxSize", value);
    }

    public static ConstraintDef min(Object value) {
        return new ConstraintDef("min", value);
    }

    public static ConstraintDef minSize(int value) {
        return new ConstraintDef("minSize", value);
    }

    public static ConstraintDef notEqual(Object value) {
        return new ConstraintDef("notEqual", value);
    }

    public static ConstraintDef nullable(boolean value) {
        return new ConstraintDef("nullable", value);
    }

    public static ConstraintDef range(int from, int to) {
        return new ConstraintDef("range", new IntRange(from, to));
    }

    public static ConstraintDef scale(int scale) {
        return new ConstraintDef("scale", scale);
    }

    public static ConstraintDef size(int from, int to) {
        return new ConstraintDef("size", new IntRange(from, to));
    }

    public static ConstraintDef url(boolean value) {
        return new ConstraintDef("url", value);
    }

    public static ConstraintDef url(String pattern) {
        return new ConstraintDef("url", pattern);
    }

    public static ConstraintDef url(List<?> pattern) {
        return new ConstraintDef("url", pattern);
    }

    public static ConstraintDef validator(Closure validator) {
        return new ConstraintDef("validator", validator);
    }

    public static ConstraintDef unique(boolean value) {
        return new ConstraintDef("unique", value);
    }
}
