/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.api.querybuilder.select;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.BindMarker;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import java.util.Arrays;

/**
 * A SELECT query that accepts additional clauses: WHERE, GROUP BY, ORDER BY, LIMIT, PER PARTITION
 * LIMIT, ALLOW FILTERING.
 */
public interface CanAddClause {

  // Implementation note - this interface is separate from CanAddSelector to make the following a
  // compile-time error:
  // selectFrom("foo").allowFiltering().build()

  /**
   * Adds a relation in the WHERE clause. All relations are logically joined with AND.
   *
   * <p>To create the argument, use one of the {@code isXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#isColumn(CqlIdentifier) isColumn}.
   *
   * <p>If you add multiple selectors as once, consider {@link #where(Iterable)} as a more efficient
   * alternative.
   */
  Select where(Relation relation);

  /**
   * Adds multiple relations at once. All relations are logically joined with AND.
   *
   * <p>This is slightly more efficient than adding the relations one by one (since the underlying
   * implementation of this object is immutable).
   *
   * <p>To create the argument, use one of the {@code isXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#isColumn(CqlIdentifier) isColumn}.
   *
   * @see #where(Relation)
   */
  Select where(Iterable<Relation> additionalRelations);

  /** Var-arg equivalent of {@link #where(Iterable)}. */
  default Select where(Relation... additionalRelations) {
    return where(Arrays.asList(additionalRelations));
  }

  /**
   * Adds a LIMIT clause to this query with a literal value.
   *
   * <p>If this method or {@link #limit(BindMarker)} is called multiple times, the last value is
   * used.
   */
  Select limit(int limit);

  /**
   * Adds a LIMIT clause to this query with a bound value.
   *
   * <p>To create the argument, use one of the factory methods in {@link QueryBuilderDsl}, for
   * example {@link QueryBuilderDsl#bindMarker() bindMarker()}.
   *
   * <p>If this method or {@link #limit(int)} is called multiple times, the last value is used.
   */
  Select limit(BindMarker bindMarker);

  /**
   * Adds an ALLOW FILTERING clause to this query.
   *
   * <p>This method is idempotent, calling it multiple times will only add a single clause.
   */
  Select allowFiltering();
}
