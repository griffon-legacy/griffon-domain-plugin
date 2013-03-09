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

package griffon.plugins.domain.orm;

import java.util.Arrays;

/**
 * @author Andres Almiray
 */
public final class CompositeCriterion implements Criterion {
    private final Criterion[] criteria;
    private final Operator operator;

    public CompositeCriterion(Criterion... criteria) {
        this(Operator.AND, criteria);
    }

    public CompositeCriterion(Operator operator, Criterion... criteria) {
        if (criteria == null) {
            this.criteria = new Criterion[0];
        } else {
            this.criteria = new Criterion[criteria.length];
            System.arraycopy(criteria, 0, this.criteria, 0, criteria.length);
        }

        operator = operator == null ? Operator.AND : operator;
        if (operator != Operator.AND && operator != Operator.OR) {
            throw new IllegalArgumentException("Invalid operator '" + operator + "'. Allowed operators are AND, OR.");
        }
        this.operator = operator;
    }

    public Criterion[] getCriteria() {
        Criterion[] tmp = new Criterion[criteria.length];
        System.arraycopy(criteria, 0, tmp, 0, criteria.length);
        return tmp;
    }

    public Operator getOperator() {
        return this.operator;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CompositeCriterion");
        sb.append("{criteria=").append(criteria == null ? "null" : Arrays.asList(criteria).toString());
        sb.append(", operator=").append(operator);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeCriterion that = (CompositeCriterion) o;

        if (!Arrays.equals(criteria, that.criteria)) return false;
        if (operator != that.operator) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(criteria);
        result = 31 * result + operator.hashCode();
        return result;
    }
}
