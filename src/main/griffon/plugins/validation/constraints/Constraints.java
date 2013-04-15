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

import griffon.util.CollectionUtils;
import groovy.lang.Closure;
import groovy.lang.IntRange;
import org.codehaus.griffon.runtime.validation.constraints.*;

import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public final class Constraints {
    private Constraints() {

    }

    public static CollectionUtils.MapBuilder<String, List<ConstraintDef>> map() {
        return CollectionUtils.<String, List<ConstraintDef>>map();
    }

    public static CollectionUtils.ListBuilder<ConstraintDef> list() {
        return CollectionUtils.<ConstraintDef>list();
    }

    public static CollectionUtils.ListBuilder<ConstraintDef> list(ConstraintDef... defs) {
        CollectionUtils.ListBuilder<ConstraintDef> list = CollectionUtils.<ConstraintDef>list();
        for (ConstraintDef d : defs) {
            list.add(d);
        }
        return list;
    }

    public static ConstraintDef blank(boolean value) {
        return new ConstraintDef(BlankConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef creditCard(boolean value) {
        return new ConstraintDef(CreditCardConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef email(boolean value) {
        return new ConstraintDef(EmailConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef inList(List<?> elements) {
        return new ConstraintDef(InListConstraint.VALIDATION_DSL_NAME, elements);
    }

    public static ConstraintDef matches(String pattern) {
        return new ConstraintDef(MatchesConstraint.VALIDATION_DSL_NAME, pattern);
    }

    public static ConstraintDef max(Object value) {
        return new ConstraintDef(MaxConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef maxSize(int value) {
        return new ConstraintDef(MaxSizeConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef min(Object value) {
        return new ConstraintDef(MinConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef minSize(int value) {
        return new ConstraintDef(MinSizeConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef notEqual(Object value) {
        return new ConstraintDef(NotEqualConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef nullable(boolean value) {
        return new ConstraintDef(NullableConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef range(int from, int to) {
        return new ConstraintDef(RangeConstraint.VALIDATION_DSL_NAME, new IntRange(from, to));
    }

    public static ConstraintDef scale(int scale) {
        return new ConstraintDef(ScaleConstraint.VALIDATION_DSL_NAME, scale);
    }

    public static ConstraintDef size(int from, int to) {
        return new ConstraintDef(SizeConstraint.VALIDATION_DSL_NAME, new IntRange(from, to));
    }

    public static ConstraintDef url(boolean value) {
        return new ConstraintDef(UrlConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef url(String pattern) {
        return new ConstraintDef(UrlConstraint.VALIDATION_DSL_NAME, pattern);
    }

    public static ConstraintDef url(List<?> pattern) {
        return new ConstraintDef(UrlConstraint.VALIDATION_DSL_NAME, pattern);
    }

    public static ConstraintDef validator(Closure validator) {
        return new ConstraintDef(ValidatorConstraint.VALIDATION_DSL_NAME, validator);
    }

    public static ConstraintDef unique(boolean value) {
        return new ConstraintDef("unique", value);
    }

    public static ConstraintDef date(String value) {
        return new ConstraintDef(DateConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef confirmedPassword(String value) {
        return new ConstraintDef(ConfirmedPasswordConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef ipAddress(String value) {
        return new ConstraintDef(IPAddressConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef postalCode(PostalCountry value) {
        return new ConstraintDef(PostalCodeConstraint.VALIDATION_DSL_NAME, value);
    }

    public static ConstraintDef shared(String value) {
        return new ConstraintDef("shared", value);
    }

    public static ConstraintDef importFrom(Class value) {
        return new ConstraintDef("importFrom", value);
    }

    public static ConstraintDef importFrom(Map<String, Object> value) {
        return new ConstraintDef("importFrom", value);
    }
}
